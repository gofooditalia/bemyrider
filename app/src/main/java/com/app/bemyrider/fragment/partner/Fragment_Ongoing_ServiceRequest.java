package com.app.bemyrider.fragment.partner;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.app.bemyrider.Adapter.Partner.RvOngoingServiceRequestAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.PartnerServicehistoryOngoingListBinding;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.model.ProviderHistoryPojoItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.PartnerServiceRequestViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class Fragment_Ongoing_ServiceRequest extends Fragment {

    private static final String TAG = "FragmentOngoingService";
    private PartnerServicehistoryOngoingListBinding binding;
    private RvOngoingServiceRequestAdapter adapter;
    private ArrayList<ProviderHistoryPojoItem> ongoingarrayList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private boolean loading = false;
    private boolean pendingClear = false;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int page = 1, total_page = 1;
    private String keyWord = "";
    private PartnerServiceRequestViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.partner_servicehistory_ongoing_list, container, false);
        page = 1;
        initView();
        viewModel = new ViewModelProvider(this).get(PartnerServiceRequestViewModel.class);
        observeViewModel();
        loadRequests(true);

        binding.rvOnGoingServiceRequest.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
            binding.rvOnGoingServiceRequest.setVisibility(View.GONE);
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
                if (pendingClear) { ongoingarrayList.clear(); pendingClear = false; }
                ongoingarrayList.addAll(pojo.getData().getServiceList());
                binding.rvOnGoingServiceRequest.setVisibility(View.VISIBLE);
                boolean hasItems = !ongoingarrayList.isEmpty();
                binding.layoutNoRecord.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.rvOnGoingServiceRequest.setVisibility(hasItems ? View.VISIBLE : View.GONE);
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
        if (isClear) { page = 1; binding.layoutNoRecord.setVisibility(View.GONE); binding.rvOnGoingServiceRequest.scrollToPosition(0); }
        loading = true;
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);
        viewModel.loadRequests(PrefsUtil.with(getActivity()).readString("UserId"), "ongoing", Utils.encodeEmoji(keyWord), page);
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(getActivity());
        binding.rvOnGoingServiceRequest.setLayoutManager(layoutManager);
        binding.rvOnGoingServiceRequest.setHasFixedSize(false);
        adapter = new RvOngoingServiceRequestAdapter(getActivity(), ongoingarrayList);
        binding.rvOnGoingServiceRequest.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(() -> { binding.swipeRefresh.setRefreshing(true); loadRequests(true); });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!loading && page == 1 && viewModel != null) loadRequests(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            if (event.getType() != null && event.getType().equalsIgnoreCase("s")) {
                Log.d(TAG, "Service notification received, refreshing list");
                loadRequests(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling EventBus message", e);
        }
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
