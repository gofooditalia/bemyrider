package com.app.bemyrider.fragment.partner;

import android.content.Context;
import android.os.AsyncTask;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.Partner.RvUpcomingServiceRequestAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerServicehistoryUpcomingListBinding;
import com.app.bemyrider.model.ProviderHistoryPojo;
import com.app.bemyrider.model.ProviderHistoryPojoItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Created by nct121 on 8/12/16.
 * Modified by Hardik Talaviya on 2/12/19.
 */

public class Fragment_Upcoming_ServiceRequest extends Fragment {

    private static final String TAG = "FragmentUpcomingService";
    private PartnerServicehistoryUpcomingListBinding binding;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int page = 1, total_page = 1;
    private RvUpcomingServiceRequestAdapter adapter;
    private ArrayList<ProviderHistoryPojoItem> upcomingarrayList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private String keyWord = "";
    private AsyncTask upcomingServiceAsync;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.partner_servicehistory_upcoming_list, container, false);

        initView();

        serviceCallGetUpcomingServices(true);

        binding.rvUpcomingserviceRequest.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ((!isLoading) && page < total_page) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            page++;
                            serviceCallGetUpcomingServices(false);
                        }
                    }
                }
            }
        });

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                keyWord = s.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.imgSearch.setOnClickListener(v -> {
            Utils.hideSoftKeyboard(getActivity());
            binding.rvUpcomingserviceRequest.setVisibility(View.GONE);
            serviceCallGetUpcomingServices(true);
        });

        binding.edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.imgSearch.performClick();
                return true;
            }
            return false;
        });

        return binding.getRoot();

    }

    /*----------------- Up Coming Service Api Call --------------------*/
    private void serviceCallGetUpcomingServices(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.layoutNoRecord.setVisibility(View.GONE);
            binding.rvUpcomingserviceRequest.scrollToPosition(0);
        }
        isLoading = true;
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(context).readString("UserId"));
        textParams.put("tab", "history");
        textParams.put("keyword", Utils.encodeEmoji(keyWord));
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(getActivity(), WebServiceUrl.URL_SERVICE_REQUEST_LIST, textParams,
                ProviderHistoryPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                if (status) {
                    ProviderHistoryPojo pojo = (ProviderHistoryPojo) obj;
                    if (isClear) {
                        upcomingarrayList.clear();
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.rvUpcomingserviceRequest.setVisibility(View.VISIBLE);
                    upcomingarrayList.addAll(pojo.getData().getServiceList());
                    if (!(upcomingarrayList.size() > 0)) {
                        binding.rvUpcomingserviceRequest.setVisibility(View.GONE);
                        binding.layoutNoRecord.setVisibility(View.VISIBLE);
                    } else {
                        binding.rvUpcomingserviceRequest.setVisibility(View.VISIBLE);
                        binding.layoutNoRecord.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();

                    total_page = pojo.getData().getPagination().getTotalPages();
                    page = pojo.getData().getPagination().getCurrentPage();
                } else {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }
                isLoading = false;
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                upcomingServiceAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                upcomingServiceAsync = null;
            }
        });
    }

    private void initView() {
        context = getActivity();

        layoutManager = new LinearLayoutManager(getActivity());
        binding.rvUpcomingserviceRequest.setLayoutManager(layoutManager);
        binding.rvUpcomingserviceRequest.setHasFixedSize(false);
        adapter = new RvUpcomingServiceRequestAdapter(getActivity(), upcomingarrayList);
        binding.rvUpcomingserviceRequest.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallGetUpcomingServices(true);
        });
    }

    @Override
    public void onDestroy() {
        Utils.cancelAsyncTask(upcomingServiceAsync);
        super.onDestroy();
    }
}
