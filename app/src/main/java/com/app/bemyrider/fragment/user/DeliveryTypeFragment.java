package com.app.bemyrider.fragment.user;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import com.app.bemyrider.databinding.FragmentDeliveryTypeListingBinding;
import com.app.bemyrider.model.user.ProviderData;
import com.app.bemyrider.model.user.ProviderItem;
import com.app.bemyrider.model.user.ProviderMainPojo;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Optimized for Lazy Loading and Request Cancellation by Gemini - 2024.
 */
public class DeliveryTypeFragment extends Fragment {

    private static final String TAG = "DeliveryTypeFragment";

    private FragmentDeliveryTypeListingBinding binding;
    private DeliveryTypeAdapter deliveryTypeAdapter;
    private ArrayList<ProviderItem> arrayList;
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private boolean isDataLoaded = false;
    private int pastVisibleItems, visibleItemCount, totalItemCount, page = 1, total_page = 1;
    private WebServiceCall searchListAsync;
    private Context context;
    private Activity activity;

    private int currentIndex = 0;
    private String mAddress = "", mLatitude = "", mLongitude = "", mStrAsc = "", mStrDesc = "", mStrSearch = "", mStrRating = "";

    public static DeliveryTypeFragment newInstance(int index) {
        DeliveryTypeFragment f = new DeliveryTypeFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        return f;
    }

    public DeliveryTypeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) currentIndex = args.getInt("index", 0);
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
        
        // Lazy Loading: Carica solo se è la prima tab e posticipa per l'ottimizzazione UI
        if (currentIndex == 0) {
            binding.getRoot().post(this::lazyLoad);
        }

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
            getAllProviders(true);
        });
    }

    public void lazyLoad() {
        if (!isDataLoaded && binding != null) {
            getAllProviders(true);
        }
    }

    private void getAllProviders(boolean isClear) {
        // --- OPTIMIZATION: Cancel pending request before starting a new one ---
        if (searchListAsync != null) {
            searchListAsync.cancel();
        }

        if (isClear) {
            page = 1;
            binding.txtNoRecord.setVisibility(View.GONE);
            binding.rvDeliveryList.scrollToPosition(0);
        }
        
        isLoading = true;
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        String action = "small";
        String url = WebServiceUrl.URL_SMALL;
        if (currentIndex == 2) {
            url = WebServiceUrl.URL_LARGE;
            action = "large";
        } else if (currentIndex == 1) {
            url = WebServiceUrl.URL_MEDIUM;
            action = "medium";
        }

        if (deliveryTypeAdapter != null) {
            deliveryTypeAdapter.setDeliveryType(action);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("action", action);
        textParams.put("sort", mStrAsc.equalsIgnoreCase("y") ? "asc" : (mStrDesc.equalsIgnoreCase("y") ? "desc" : ""));
        textParams.put("search_rating", mStrRating);
        textParams.put("search_location", mAddress);
        textParams.put("search_lat", mLatitude);
        textParams.put("search_long", mLongitude);
        textParams.put("search_keyword", Utils.encodeEmoji(mStrSearch));
        textParams.put("page", String.valueOf(page));

        searchListAsync = new WebServiceCall(context, url, textParams, ProviderMainPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (!isAdded()) return;
                
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                
                if (status) {
                    isDataLoaded = true;
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
                    // Non mostrare errore se è stato cancellato dall'utente
                    if (searchListAsync != null && !searchListAsync.isCancelled()) {
                        Toast.makeText(context, (String) obj, Toast.LENGTH_SHORT).show();
                    }
                }
                isLoading = false;
            }

            @Override
            public void onAsync(Object task) {
                // Già salvato sopra, ma manteniamo per logica
            }

            @Override
            public void onCancelled() {
                isLoading = false;
            }
        });
    }

    public void searchDataUpdate(String strSearch, int position) {
        currentIndex = position;
        mStrSearch = strSearch;
        if (binding != null) {
            binding.rvDeliveryList.setVisibility(View.GONE);
            binding.getRoot().post(() -> getAllProviders(true));
        }
    }

    public void filterDataUpdate(String address, String latitude, String longitude, String strAsc, String strDesc, String strSearch, String strRating, int position) {
        currentIndex = position;
        mAddress = address;
        mLatitude = latitude;
        mLongitude = longitude;
        mStrAsc = strAsc;
        mStrDesc = strDesc;
        mStrSearch = strSearch;
        if (!"0.0".equals(strRating)) mStrRating = strRating;
        if (binding != null) {
            binding.rvDeliveryList.setVisibility(View.GONE);
            binding.getRoot().post(() -> getAllProviders(true));
        }
    }

    @Override
    public void onDestroy() {
        if (searchListAsync != null) {
            searchListAsync.cancel();
        }
        super.onDestroy();
    }
}
