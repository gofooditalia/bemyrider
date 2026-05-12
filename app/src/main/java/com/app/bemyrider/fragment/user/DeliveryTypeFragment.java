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
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.FragmentDeliveryTypeListingBinding;
import com.app.bemyrider.model.user.ProviderData;
import com.app.bemyrider.model.user.ProviderItem;
import com.app.bemyrider.model.user.ProviderMainPojo;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.DeliveryTypeViewModel;

import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class DeliveryTypeFragment extends Fragment {

    private FragmentDeliveryTypeListingBinding binding;
    private DeliveryTypeAdapter deliveryTypeAdapter;
    private ArrayList<ProviderItem> arrayList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private boolean isDataLoaded = false;
    private int page = 1, total_page = 1;
    private DeliveryTypeViewModel viewModel;
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
        viewModel = new ViewModelProvider(this).get(DeliveryTypeViewModel.class);
        observeViewModel();

        if (currentIndex == 0) {
            binding.getRoot().post(this::lazyLoad);
        }

        binding.rvDeliveryList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && page < total_page) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 2) {
                            getAllProviders(false);
                        }
                    }
                }
            }
        });
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(context);
        binding.rvDeliveryList.setLayoutManager(layoutManager);
        binding.rvDeliveryList.setItemAnimator(new DefaultItemAnimator());

        deliveryTypeAdapter = new DeliveryTypeAdapter(activity);
        binding.rvDeliveryList.setAdapter(deliveryTypeAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            getAllProviders(true);
        });
    }

    public void lazyLoad() {
        if (!isDataLoaded && binding != null) {
            getAllProviders(true);
        }
    }

    private void observeViewModel() {
        viewModel.getProviders().observe(getViewLifecycleOwner(), pojo -> {
            if (!isAdded()) return;
            binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            isLoading = false;

            if (pojo != null && pojo.getData() != null) {
                isDataLoaded = true;
                List<ProviderItem> newItems = pojo.getData().getProviderList();
                int serverPage = pojo.getData().getPagination().getCurrentPage();
                total_page = pojo.getData().getPagination().getTotalPages();
                page = serverPage;

                if (serverPage == 1) {
                    arrayList.clear();
                    if (newItems != null) arrayList.addAll(newItems);
                } else {
                    if (newItems != null) arrayList.addAll(newItems);
                }

                boolean hasItems = !arrayList.isEmpty();
                binding.txtNoRecord.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.rvDeliveryList.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                
                deliveryTypeAdapter.submitList(new ArrayList<>(arrayList));
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && isAdded()) {
                binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                isLoading = false;
            }
        });
    }

    private void getAllProviders(boolean isClear) {
        if (isLoading) return;
        isLoading = true;

        if (isClear) {
            page = 1;
            binding.txtNoRecord.setVisibility(View.GONE);
        } else {
            page++;
        }

        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);

        String action = currentIndex == 2 ? "large" : currentIndex == 1 ? "medium" : "small";
        if (deliveryTypeAdapter != null) deliveryTypeAdapter.setDeliveryType(action);

        String sort = mStrAsc.equalsIgnoreCase("y") ? "asc" : (mStrDesc.equalsIgnoreCase("y") ? "desc" : "");
        viewModel.loadProviders(currentIndex, sort, mStrRating, mAddress, mLatitude, mLongitude,
                Utils.encodeEmoji(mStrSearch), page);
    }

    public void searchDataUpdate(String strSearch, int position) {
        currentIndex = position;
        mStrSearch = strSearch;
        if (binding != null) {
            binding.rvDeliveryList.setVisibility(View.GONE);
            getAllProviders(true);
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
            getAllProviders(true);
        }
    }
}
