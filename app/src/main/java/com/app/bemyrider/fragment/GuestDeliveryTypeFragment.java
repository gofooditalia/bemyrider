package com.app.bemyrider.fragment;

import android.app.Activity;
import android.content.Context;
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
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.FragmentDeliveryTypeListingBinding;
import com.app.bemyrider.model.user.ProviderData;
import com.app.bemyrider.model.user.ProviderItem;
import com.app.bemyrider.model.user.ProviderMainPojo;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.DeliveryTypeViewModel;

import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class GuestDeliveryTypeFragment extends Fragment {

    private static final String TAG = "GuestDeliveryType";
    private FragmentDeliveryTypeListingBinding binding;
    private DeliveryTypeAdapter deliveryTypeAdapter;
    private ArrayList<ProviderItem> arrayList;
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private int pastVisibleItems, visibleItemCount, totalItemCount, page = 1, total_page = 1;
    private DeliveryTypeViewModel viewModel;
    private Context context;
    private Activity activity;
    private int currentIndex = 0;
    private String mAddress = "", mLatitude = "", mLongitude = "", mStrAsc = "", mStrDesc = "", mStrSearch = "", mStrRating = "";

    public static GuestDeliveryTypeFragment newInstance(int index) {
        GuestDeliveryTypeFragment f = new GuestDeliveryTypeFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) currentIndex = args.getInt("index", 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_delivery_type_listing, container, false);
        context = getContext();
        activity = getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView();
        viewModel = new ViewModelProvider(this).get(DeliveryTypeViewModel.class);
        observeViewModel();
        binding.getRoot().post(() -> getAllProviders(true));
        
        binding.rvDeliveryList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if (!isLoading && page < total_page) {
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

    private void observeViewModel() {
        viewModel.getProviders().observe(getViewLifecycleOwner(), pojo -> {
            if (!isAdded()) return;
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                arrayList.clear();
                arrayList.addAll(pojo.getData().getProviderList());
                boolean hasItems = !arrayList.isEmpty();
                binding.rvDeliveryList.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                binding.txtNoRecord.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                deliveryTypeAdapter.submitList(new ArrayList<>(arrayList));
                total_page = pojo.getData().getPagination().getTotalPages();
                page = pojo.getData().getPagination().getCurrentPage();
            }
            isLoading = false;
        });
        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && isAdded()) {
                if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                isLoading = false;
            }
        });
    }

    private void getAllProviders(boolean isClear) {
        if (!isAdded()) return;
        if (isClear) {
            page = 1;
            arrayList.clear();
            binding.txtNoRecord.setVisibility(View.GONE);
            binding.rvDeliveryList.scrollToPosition(0);
        }
        isLoading = true;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);

        String deliveryType = currentIndex == 2 ? "large" : currentIndex == 1 ? "medium" : "small";
        if (deliveryTypeAdapter != null) deliveryTypeAdapter.setDeliveryType(deliveryType);

        String sort = "y".equalsIgnoreCase(mStrAsc) ? "asc" : ("y".equalsIgnoreCase(mStrDesc) ? "desc" : "");
        viewModel.loadProviders(currentIndex, sort, mStrRating, mAddress, mLatitude, mLongitude,
                Utils.encodeEmoji(mStrSearch), page);
    }

    public void searchDataUpdate(String strSearch, int position){
        currentIndex = position;
        mStrSearch = strSearch;
        binding.rvDeliveryList.setVisibility(View.GONE);
        binding.getRoot().post(() -> getAllProviders(true));
    }

    public void filterDataUpdate(String address, String latitude, String longitude, String strAsc, String strDesc, String strSearch, String strRating, int position){
        currentIndex = position;
        mAddress = address;
        mLatitude = latitude;
        mLongitude = longitude;
        mStrAsc = strAsc;
        mStrDesc = strDesc;
        mStrSearch = strSearch;
        mStrRating = strRating;
        binding.rvDeliveryList.setVisibility(View.GONE);
        binding.getRoot().post(() -> getAllProviders(true));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
