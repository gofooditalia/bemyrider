package com.app.bemyrider.fragment.user;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.ServiceListUpcomingAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.FragmentServiceListingBinding;
import com.app.bemyrider.model.CustomerHistoryPojo;
import com.app.bemyrider.model.CustomerHistoryPojoItem;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 10/12/19.
 */

public class UpcomingServiceFragment extends Fragment {

    //upcoming service history tab --> ServiceHistoryActivity

    private FragmentServiceListingBinding binding;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int page = 1, total_page = 1;
    private ServiceListUpcomingAdapter adapter;
    private ArrayList<CustomerHistoryPojoItem> historyPojoItems;
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private WebServiceCall upcomingServiceAsync;
    private Context context;
    ActivityResultLauncher<Intent> myActivityResultLauncher;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_listing, container, false);

        initView();

        serviceCallGetUpcomingService(true);

        binding.rvServiceList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    Log.e("Item As Per", "visibleItemCount " + layoutManager.getChildCount() + "&& totalItemCount " + layoutManager.getItemCount() + "&& pastVisibleItems " + layoutManager.findFirstVisibleItemPosition());

                    if ((!isLoading) && page < total_page) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            page++;
                            serviceCallGetUpcomingService(false);
                        }
                    }
                }
            }
        });

        return binding.getRoot();
    }

    private void myActivityResult() {
        myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    binding.rvServiceList.setVisibility(View.GONE);
                    serviceCallGetUpcomingService(true);
                }
            }
        });
    }

    /*-------------- Get Upcoming Service Api Call ----------------*/
    private void serviceCallGetUpcomingService(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.txtNoRecord.setVisibility(View.GONE);
            binding.rvServiceList.scrollToPosition(0);
        }
        isLoading = true;
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(context).readString("UserId"));
        textParams.put("tab", "history");
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(getActivity(), WebServiceUrl.URL_GETSERVICEHISTORY, textParams,
                CustomerHistoryPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                if (status) {
                    CustomerHistoryPojo historyPojo = (CustomerHistoryPojo) obj;

                    if (isClear) {
                        historyPojoItems.clear();
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.rvServiceList.setVisibility(View.VISIBLE);

                    historyPojoItems.addAll(historyPojo.getData().getServiceList());
                    if (historyPojoItems.size() > 0) {
                        binding.txtNoRecord.setVisibility(View.GONE);
                        binding.rvServiceList.setVisibility(View.VISIBLE);
                    } else {
                        binding.rvServiceList.setVisibility(View.GONE);
                        binding.txtNoRecord.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();

                    total_page = historyPojo.getData().getPagination().getTotalPages();
                    page = historyPojo.getData().getPagination().getCurrentPage();
                } else {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }
                isLoading = false;
            }

            @Override
            public void onAsync(Object asyncTask) {
                upcomingServiceAsync = null;
            }

            @Override
            public void onCancelled() {
                upcomingServiceAsync = null;
            }
        });
    }

    private void initView() {
        context = getActivity();

        myActivityResult();

        historyPojoItems = new ArrayList<>();
        layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        binding.rvServiceList.setLayoutManager(layoutManager);
        adapter = new ServiceListUpcomingAdapter(getActivity(), historyPojoItems,myActivityResultLauncher);
        binding.rvServiceList.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallGetUpcomingService(true);
        });

    }

    @Override
    public void onDestroy() {
        Utils.cancelAsyncTask(upcomingServiceAsync);
        super.onDestroy();
    }
}
