package com.app.bemyrider.fragment.partner;

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

import com.app.bemyrider.Adapter.Partner.RvPreviousServiceRequestAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerServicehistoryPreviousListBinding;
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

public class Fragment_Previous_ServiceRequest extends Fragment {

    private PartnerServicehistoryPreviousListBinding binding;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int page = 1, total_page = 1;
    private ArrayList<ProviderHistoryPojoItem> previousArrayList = new ArrayList<>();
    private RvPreviousServiceRequestAdapter adapter;
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private String keyWord = "";
    private AsyncTask previousServiceAsync;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.partner_servicehistory_previous_list, container, false);
        page = 1;
        initView();

        serviceCallGetPreviousServices(true);

        binding.rvPreviousServiceRequest.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ((!isLoading) && page < total_page) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            page++;
                            serviceCallGetPreviousServices(false);
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
            binding.rvPreviousServiceRequest.setVisibility(View.GONE);
            serviceCallGetPreviousServices(true);
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

    /*----------------- Previous Service Api call ------------------*/
    private void serviceCallGetPreviousServices(boolean isClear) {

        if (isClear) {
            page = 1;
            binding.layoutNoRecord.setVisibility(View.GONE);
            binding.rvPreviousServiceRequest.scrollToPosition(0);
        }
        isLoading = true;
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(getActivity()).readString("UserId"));
        textParams.put("tab", "past");
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
                        previousArrayList.clear();
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.rvPreviousServiceRequest.setVisibility(View.VISIBLE);
                    previousArrayList.addAll(pojo.getData().getServiceList());
                    if (!(previousArrayList.size() > 0)) {
                        binding.layoutNoRecord.setVisibility(View.VISIBLE);
                        binding.rvPreviousServiceRequest.setVisibility(View.GONE);
                    } else {
                        binding.layoutNoRecord.setVisibility(View.GONE);
                        binding.rvPreviousServiceRequest.setVisibility(View.VISIBLE);
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
                previousServiceAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                previousServiceAsync = null;
            }
        });
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(getActivity());
        binding.rvPreviousServiceRequest.setLayoutManager(layoutManager);
        binding.rvPreviousServiceRequest.setHasFixedSize(false);
        adapter = new RvPreviousServiceRequestAdapter(getActivity(), previousArrayList);
        binding.rvPreviousServiceRequest.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallGetPreviousServices(true);
        });
    }

    @Override
    public void onDestroy() {
        try {
            if (previousServiceAsync != null) {
                previousServiceAsync.cancel(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}