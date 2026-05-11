package com.app.bemyrider.fragment.partner;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.Partner.RvUpcomingServiceRequestAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.PartnerServicehistoryUpcomingListBinding;
import com.app.bemyrider.model.ProviderHistoryPojoItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.PartnerServiceRequestViewModel;

import java.util.ArrayList;

public class Fragment_Upcoming_ServiceRequest extends Fragment {

    private PartnerServicehistoryUpcomingListBinding binding;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int page = 1, total_page = 1;
    private RvUpcomingServiceRequestAdapter adapter;
    private ArrayList<ProviderHistoryPojoItem> upcomingarrayList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private boolean loading = false;
    private boolean pendingClear = false;
    private String keyWord = "";
    private PartnerServiceRequestViewModel viewModel;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.partner_servicehistory_upcoming_list, container, false);
        initView();
        viewModel = new ViewModelProvider(this).get(PartnerServiceRequestViewModel.class);
        observeViewModel();
        loadRequests(true);

        binding.rvUpcomingserviceRequest.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if (!loading && page < total_page && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        page++;
                        loadRequests(false);
                    }
                }
            }
        });

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { keyWord = s.toString().trim(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.imgSearch.setOnClickListener(v -> {
            Utils.hideSoftKeyboard(getActivity());
            binding.rvUpcomingserviceRequest.setVisibility(View.GONE);
            loadRequests(true);
        });

        binding.edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { binding.imgSearch.performClick(); return true; }
            return false;
        });

        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getRequests().observe(getViewLifecycleOwner(), pojo -> {
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                if (pendingClear) { upcomingarrayList.clear(); pendingClear = false; }
                upcomingarrayList.addAll(pojo.getData().getServiceList());
                binding.rvUpcomingserviceRequest.setVisibility(View.VISIBLE);
                boolean hasItems = !upcomingarrayList.isEmpty();
                binding.layoutNoRecord.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.rvUpcomingserviceRequest.setVisibility(hasItems ? View.VISIBLE : View.GONE);
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

    private void loadRequests(boolean isClear) {
        if (isClear) { page = 1; binding.layoutNoRecord.setVisibility(View.GONE); binding.rvUpcomingserviceRequest.scrollToPosition(0); }
        loading = true;
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);
        viewModel.loadRequests(PrefsUtil.with(context).readString("UserId"), "history", Utils.encodeEmoji(keyWord), page);
    }

    private void initView() {
        context = getActivity();
        layoutManager = new LinearLayoutManager(getActivity());
        binding.rvUpcomingserviceRequest.setLayoutManager(layoutManager);
        binding.rvUpcomingserviceRequest.setHasFixedSize(false);
        adapter = new RvUpcomingServiceRequestAdapter(getActivity(), upcomingarrayList);
        binding.rvUpcomingserviceRequest.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(() -> { binding.swipeRefresh.setRefreshing(true); loadRequests(true); });
    }
}
