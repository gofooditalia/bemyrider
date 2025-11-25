package com.app.bemyrider.fragment.user;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.ServiceListOnGoingAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.FragmentServiceListingBinding;
import com.app.bemyrider.model.CustomerHistoryPojo;
import com.app.bemyrider.model.CustomerHistoryPojoItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 10/12/19.
 */

public class OngoingServiceFragment extends Fragment {

    private FragmentServiceListingBinding binding;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int page = 1, total_page = 1;
    private ServiceListOnGoingAdapter adapter;
    private ArrayList<CustomerHistoryPojoItem> historyPojoItems;
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private AsyncTask ongoingServiceAsync;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_listing, container, false);

        initView();

        serviceCallGetOnGoingService(true);

        binding.rvServiceList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ((!isLoading) && page < total_page) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            page++;
                            serviceCallGetOnGoingService(false);
                        }
                    }
                }
            }
        });

        return binding.getRoot();
    }

    /*------------- Get On Going Service Api Call ---------------*/
    private void serviceCallGetOnGoingService(boolean isClear) {
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
        textParams.put("tab", "ongoing");
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
            public void onAsync(AsyncTask asyncTask) {
                ongoingServiceAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                ongoingServiceAsync = null;
            }
        });
    }

    private void initView() {
        context = getActivity();

        historyPojoItems = new ArrayList<>();
        layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        binding.rvServiceList.setLayoutManager(layoutManager);
        adapter = new ServiceListOnGoingAdapter(getActivity(), historyPojoItems);
        binding.rvServiceList.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallGetOnGoingService(true);
        });
    }

    @Override
    public void onDestroy() {
        Utils.cancelAsyncTask(ongoingServiceAsync);
        super.onDestroy();
    }
}
