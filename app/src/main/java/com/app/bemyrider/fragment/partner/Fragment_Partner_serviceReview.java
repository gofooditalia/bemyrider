package com.app.bemyrider.fragment.partner;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.Partner.RvMyServiceDetailsReviewAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerServicedetailReviewFragmentBinding;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.model.ServiceReviewItem;
import com.app.bemyrider.model.ServiceReviewPojo;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Created by nct121 on 12/12/16.
 * Modified by Hardik Talaviya on 6/12/19.
 */

public class Fragment_Partner_serviceReview extends Fragment {

    private PartnerServicedetailReviewFragmentBinding binding;
    private RvMyServiceDetailsReviewAdapter adapter;
    private ArrayList<ServiceReviewItem> arrayList = new ArrayList<>();

    private LinearLayoutManager layoutManager;
    private ProviderServiceDetailsItem serviceDetailData;
    private WebServiceCall reviewListAsync;
    private Context context;

    /*pagination vars start*/
    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisibleItems = 0, visibleItemCount, totalItemCount;
    /*pagination vars end*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.partner_servicedetail_review_fragment, container, false);

        serviceDetailData = (ProviderServiceDetailsItem) getArguments().getSerializable("data");

        initView();

        serviceCallGetReview(true);

        binding.rvMyservicedetailsReviews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            if (arrayList.size() < total_records) {
                                page++;
                                serviceCallGetReview(false);
                            }
                        }
                    }
                }
            }
        });

        return binding.getRoot();
    }

    /*------------------ Get Review Api Call --------------------*/
    private void serviceCallGetReview(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.txtNoDataPartnerReview.setVisibility(View.GONE);
            binding.rvMyservicedetailsReviews.scrollToPosition(0);
        }
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(context).readString("UserId"));
        textParams.put("user_type", "p");
        textParams.put("service_id", serviceDetailData.getServiceId());
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(getActivity(), WebServiceUrl.URL_PROVIDER_REVIEWS, textParams,
                ServiceReviewPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                if (status) {
                    ServiceReviewPojo reviewPojo = (ServiceReviewPojo) obj;
                    if (isClear) {
                        arrayList.clear();
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.rvMyservicedetailsReviews.setVisibility(View.VISIBLE);

                    arrayList.addAll(reviewPojo.getData().getReviewList());
                    adapter.notifyDataSetChanged();
                    loading = true;

                    if (arrayList.size() > 0) {
                        binding.rvMyservicedetailsReviews.setVisibility(View.VISIBLE);
                        binding.txtNoDataPartnerReview.setVisibility(View.GONE);
                    } else {
                        binding.rvMyservicedetailsReviews.setVisibility(View.GONE);
                        binding.txtNoDataPartnerReview.setVisibility(View.VISIBLE);
                    }
                    try {
                        total_records = reviewPojo.getData().getPagination().getTotalRecords();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onAsync(Object asyncTask) {
                reviewListAsync = null;
            }

            @Override
            public void onCancelled() {
                reviewListAsync = null;
            }
        });
    }


    private void initView() {
        context = getActivity();

        binding.rvMyservicedetailsReviews.setHasFixedSize(false);
        adapter = new RvMyServiceDetailsReviewAdapter(getActivity(), arrayList);
        layoutManager = new LinearLayoutManager(getActivity());
        binding.rvMyservicedetailsReviews.setLayoutManager(layoutManager);
        binding.rvMyservicedetailsReviews.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallGetReview(true);
        });
    }

    @Override
    public void onDestroy() {
        Utils.cancelAsyncTask(reviewListAsync);
        super.onDestroy();
    }
}
