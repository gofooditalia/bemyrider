package com.app.bemyrider.fragment.user;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.ServiceListPreviosAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.FragmentServiceListingBinding;
import com.app.bemyrider.model.CustomerHistoryPojoItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.viewmodel.CustomerServiceHistoryViewModel;

import java.util.ArrayList;

public class PreviousServiceFragment extends Fragment {

    private FragmentServiceListingBinding binding;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int page = 1, total_page = 1;
    private ServiceListPreviosAdapter adapter;
    private ArrayList<CustomerHistoryPojoItem> historyPojoItems;
    private LinearLayoutManager layoutManager;
    private boolean loading = false;
    private boolean pendingClear = false;
    private CustomerServiceHistoryViewModel viewModel;
    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_listing, container, false);
        initView();
        viewModel = new ViewModelProvider(this).get(CustomerServiceHistoryViewModel.class);
        observeViewModel();
        refreshData();
        binding.rvServiceList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if (!loading && page < total_page && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        page++;
                        loadServices(false);
                    }
                }
            }
        });
        return binding.getRoot();
    }

    public void refreshData() {
        if (context != null && viewModel != null) loadServices(true);
    }

    private void observeViewModel() {
        viewModel.getServices().observe(getViewLifecycleOwner(), pojo -> {
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                if (pendingClear) { historyPojoItems.clear(); pendingClear = false; }
                historyPojoItems.addAll(pojo.getData().getServiceList());
                boolean hasItems = !historyPojoItems.isEmpty();
                binding.txtNoRecord.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.rvServiceList.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                adapter.notifyDataSetChanged();
                total_page = pojo.getData().getPagination().getTotalPages();
                page = pojo.getData().getPagination().getCurrentPage();
            }
            loading = false;
        });
        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                loading = false;
            }
        });
    }

    private void loadServices(boolean isClear) {
        if (isClear) { page = 1; binding.txtNoRecord.setVisibility(View.GONE); binding.rvServiceList.scrollToPosition(0); }
        loading = true;
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);
        viewModel.loadServices(PrefsUtil.with(context).readString("UserId"), "past", page);
    }

    private void initView() {
        context = getActivity();
        historyPojoItems = new ArrayList<>();
        layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        binding.rvServiceList.setLayoutManager(layoutManager);
        adapter = new ServiceListPreviosAdapter(getActivity(), historyPojoItems);
        binding.rvServiceList.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(() -> { binding.swipeRefresh.setRefreshing(true); loadServices(true); });
    }
}
