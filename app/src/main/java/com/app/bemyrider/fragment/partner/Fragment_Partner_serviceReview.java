package com.app.bemyrider.fragment.partner;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.Partner.RvMyServiceDetailsReviewAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.PartnerServicedetailReviewFragmentBinding;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.model.ServiceReviewItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.viewmodel.PartnerReviewsViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Fragment_Partner_serviceReview extends Fragment {

    private PartnerServicedetailReviewFragmentBinding binding;
    private RvMyServiceDetailsReviewAdapter adapter;
    private ArrayList<ServiceReviewItem> arrayList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private ProviderServiceDetailsItem serviceDetailData;
    private PartnerReviewsViewModel viewModel;
    private Context context;

    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisibleItems = 0, visibleItemCount, totalItemCount;
    private boolean pendingClear = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.partner_servicedetail_review_fragment, container, false);
        serviceDetailData = (ProviderServiceDetailsItem) getArguments().getSerializable("data");
        initView();

        viewModel = new ViewModelProvider(this).get(PartnerReviewsViewModel.class);
        observeViewModel();
        serviceCallGetReview(true);

        binding.rvMyservicedetailsReviews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if (loading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        loading = false;
                        if (arrayList.size() < total_records) {
                            page++;
                            serviceCallGetReview(false);
                        }
                    }
                }
            }
        });

        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getReviews().observe(getViewLifecycleOwner(), pojo -> {
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                if (pendingClear) { arrayList.clear(); pendingClear = false; }
                arrayList.addAll(pojo.getData().getReviewList());
                adapter.notifyDataSetChanged();
                loading = true;
                boolean hasItems = !arrayList.isEmpty();
                binding.rvMyservicedetailsReviews.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                binding.txtNoDataPartnerReview.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                try { total_records = pojo.getData().getPagination().getTotalRecords(); } catch (Exception ignored) {}
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void serviceCallGetReview(boolean isClear) {
        if (isClear) { page = 1; binding.txtNoDataPartnerReview.setVisibility(View.GONE); binding.rvMyservicedetailsReviews.scrollToPosition(0); }
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("user_id", PrefsUtil.with(context).readString("UserId"));
        params.put("user_type", "p");
        params.put("service_id", serviceDetailData.getServiceId());
        params.put("page", String.valueOf(page));
        viewModel.loadReviews(params);
    }

    private void initView() {
        context = getActivity();
        binding.rvMyservicedetailsReviews.setHasFixedSize(false);
        adapter = new RvMyServiceDetailsReviewAdapter(getActivity(), arrayList);
        layoutManager = new LinearLayoutManager(getActivity());
        binding.rvMyservicedetailsReviews.setLayoutManager(layoutManager);
        binding.rvMyservicedetailsReviews.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(() -> { binding.swipeRefresh.setRefreshing(true); serviceCallGetReview(true); });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
