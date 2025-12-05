package com.app.bemyrider.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.DeliveryTypeAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.GuestHomeActivity;
import com.app.bemyrider.databinding.FragmentDeliveryTypeListingBinding;
import com.app.bemyrider.model.user.ProviderData;
import com.app.bemyrider.model.user.ProviderItem;
import com.app.bemyrider.model.user.ProviderMainPojo;
import com.app.bemyrider.utils.Utils;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 10/12/19.
 */

public class GuestDeliveryTypeFragment extends Fragment {

    private static final String TAG = "GuestDeliveryType";
    // upcoming service history tab --> ServiceHistoryActivity

    private FragmentDeliveryTypeListingBinding binding;
    private DeliveryTypeAdapter deliveryTypeAdapter;
    private GoogleMap map;
    private ArrayList<ProviderItem> arrayList;
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private int pastVisibleItems, visibleItemCount, totalItemCount, page = 1, total_page = 1;
    private AsyncTask searchListAsync;
    private Context context;
    private Activity activity;

    private int currentIndex = 0;

    private String mAddress = "", mLatitude = "", mLongitude = "", mStrAsc = "", mStrDesc = "", mStrSearch = "", mStrRating = "";

    public static GuestDeliveryTypeFragment newInstance(int index) {
        GuestDeliveryTypeFragment f = new GuestDeliveryTypeFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        return f;
    }

    public GuestDeliveryTypeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) currentIndex = args.getInt("index", 0);
        Log.e("TAG", "onCreateView: " + currentIndex);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_delivery_type_listing, container, false);
        context = getContext();
        activity = getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView();

        ((GuestHomeActivity) getActivity()).setOnHomePositionData(position -> {
            currentIndex = position;
            Log.e(TAG, "onPageChanged: " + currentIndex + " " + position);
        });

        getAllProviders(true);

        binding.rvDeliveryList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if ((!isLoading) && page < total_page) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            page++;
                            getAllProviders(false);
                        }
                    }
                }
            }
        });
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(context);
        binding.rvDeliveryList.setHasFixedSize(true);
        binding.rvDeliveryList.setLayoutManager(layoutManager);
        binding.rvDeliveryList.setItemAnimator(new DefaultItemAnimator());

        arrayList = new ArrayList<>();
        deliveryTypeAdapter = new DeliveryTypeAdapter(activity);
        binding.rvDeliveryList.setAdapter(deliveryTypeAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            /*mAddress = "";
            mLatitude = "";
            mLongitude = "";
            mStrAsc = "";
            mStrDesc = "";
            mStrSearch = "";
            mStrRating = "";*/
            getAllProviders(true);
        });
    }

    /*-------------- Search Provider Api Call ------------------*/
    private void getAllProviders(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.txtNoRecord.setVisibility(View.GONE);
            binding.rvDeliveryList.scrollToPosition(0);
        }
        isLoading = true;
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }
        Log.e(TAG, "getAllProviders: " + currentIndex + " ");

        String action = "small";
        String url = WebServiceUrl.URL_SMALL;
        if (currentIndex == 2) {
            url = WebServiceUrl.URL_LARGE;
            action = "large";
        } else if (currentIndex == 1) {
            url = WebServiceUrl.URL_MEDIUM;
            action = "medium";
        } else {
            url = WebServiceUrl.URL_SMALL;
            action = "small";
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("action", action);
        if (mStrAsc.equalsIgnoreCase("y")) {
            textParams.put("sort", "asc");
        } else if (mStrDesc.equalsIgnoreCase("y")) {
            textParams.put("sort", "desc");
        } else {
            textParams.put("sort", "");
        }
        textParams.put("search_rating", mStrRating);
        textParams.put("search_location", mAddress);
        textParams.put("search_lat", mLatitude);
        textParams.put("search_long", mLongitude);
        textParams.put("search_keyword", Utils.encodeEmoji(mStrSearch));
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(context, url, textParams, ProviderMainPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                if (status) {
                    ProviderMainPojo providerData = (ProviderMainPojo) obj;
                    ProviderData providerListData = providerData.getData();
                    if (isClear) {
                        arrayList.clear();
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.rvDeliveryList.setVisibility(View.VISIBLE);

                    arrayList.addAll(providerListData.getProviderList());

                    if (arrayList.size() > 0) {
                        binding.rvDeliveryList.setVisibility(View.VISIBLE);
                        binding.txtNoRecord.setVisibility(View.GONE);
                    } else {
                        binding.rvDeliveryList.setVisibility(View.GONE);
                        binding.txtNoRecord.setVisibility(View.VISIBLE);
                    }

                    deliveryTypeAdapter.submitList(new ArrayList<>(arrayList));

                    total_page = providerListData.getPagination().getTotalPages();
                    page = providerListData.getPagination().getCurrentPage();
                } else {
                    Toast.makeText(context, (String) obj,
                            Toast.LENGTH_SHORT).show();
                }
                isLoading = false;
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                searchListAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                searchListAsync = null;
            }
        });
    }


    public void searchDataUpdate(String strSearch,int position){
        currentIndex = position;
        mStrSearch = strSearch;
        Log.e(TAG, "onViewCreated: "+mStrSearch);
        binding.rvDeliveryList.setVisibility(View.GONE);
        getAllProviders(true);
    }

    public void filterDataUpdate(String address, String latitude, String longitude, String strAsc, String strDesc, String strSearch, String strRating,int position){
        currentIndex = position;
        mAddress = address;
        mLatitude = latitude;
        mLongitude = longitude;
        mStrAsc = strAsc;
        mStrDesc = strDesc;
        mStrSearch = strSearch;
        mStrRating = strRating;
        Log.e(TAG, "onViewCreated: "+mStrSearch);
        binding.rvDeliveryList.setVisibility(View.GONE);
        getAllProviders(true);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.cancelAsyncTask(searchListAsync);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
