package com.app.bemyrider.fragment.user;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.app.bemyrider.R;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.databinding.FragmentCustomerHomeBinding;
import com.app.bemyrider.utils.Utils;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.HashMap;
import java.util.Map;

public class CustomerHomeFragment extends Fragment {

    private static final String TAG = "CustomerHome";

    private CustomerHomeFragmentListener customerHomeFragmentListener;

    public interface CustomerHomeFragmentListener {
        void onFilterClick();
        void setCurrentPosition(int position);
    }

    private FragmentCustomerHomeBinding binding;
    private Context context;
    private AppCompatActivity activity;
    private int[] tabString = { R.string.small, R.string.medium, R.string.large };
    private String keyWord = "";
    
    // Usiamo una Map per tenere traccia dei frammenti creati in modo sicuro
    private Map<Integer, DeliveryTypeFragment> fragmentMap = new HashMap<>();

    private ViewPagerAdapter adapter;
    private boolean isPageRefreshSearch = false;
    private boolean isPageRefreshFilter = false;

    private String address = "", latitude = "", longitude = "", strAsc = "", strDesc = "", strSearch = "", strRating = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_home, container, false);
        activity = (AppCompatActivity) getActivity();
        context = getContext();
        activity.setSupportActionBar(binding.toolbar);

        adapter = new ViewPagerAdapter(this);
        binding.pagerHome.setAdapter(adapter);
        
        // Ridurre il limite offscreen o gestirlo con Lazy Loading
        binding.pagerHome.setOffscreenPageLimit(1); 

        new TabLayoutMediator(binding.tabLayoutHome, binding.pagerHome,
                (tab, position) -> tab.setText(tabString[position])).attach();

        ((CustomerHomeActivity) getActivity()).setOnHomeFilterData((address, latitude, longitude, strAsc, strDesc, strSearch, strRating) -> {
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.strAsc = strAsc;
            this.strDesc = strDesc;
            this.strSearch = strSearch;
            this.strRating = strRating;
            
            DeliveryTypeFragment currentFragment = fragmentMap.get(binding.pagerHome.getCurrentItem());
            if (currentFragment != null) {
                currentFragment.filterDataUpdate(address, latitude, longitude, strAsc, strDesc, strSearch, strRating, binding.pagerHome.getCurrentItem());
                isPageRefreshFilter = true;
                isPageRefreshSearch = false;
            }
        });

        binding.pagerHome.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (customerHomeFragmentListener != null) {
                    customerHomeFragmentListener.setCurrentPosition(position);
                }

                // FIX PERFORMANCE TABLAYOUT: Avvolgi l'aggiornamento dati nel post
                binding.pagerHome.post(() -> {
                    if (!isAdded()) return;

                    // Lazy Loading Trigger
                    DeliveryTypeFragment fragment = fragmentMap.get(position);
                    if (fragment != null) {
                        fragment.lazyLoad();
                        
                        if (isPageRefreshSearch) {
                            Utils.hideSoftKeyboard(activity);
                            fragment.searchDataUpdate(keyWord, position);
                        } else if (isPageRefreshFilter) {
                            fragment.filterDataUpdate(address, latitude, longitude, strAsc, strDesc, strSearch, strRating, position);
                        }
                    }
                });
            }
        });

        binding.imgFilter.setOnClickListener(v -> {
            if (customerHomeFragmentListener != null) {
                customerHomeFragmentListener.onFilterClick();
            }
            binding.edtSearch.setText("");
        });

        binding.edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.imgSearch.performClick();
                return true;
            }
            return false;
        });

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                keyWord = s.toString().trim();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.imgSearch.setOnClickListener(v -> {
            Utils.hideSoftKeyboard(activity);
            DeliveryTypeFragment currentFragment = fragmentMap.get(binding.pagerHome.getCurrentItem());
            if (currentFragment != null) {
                currentFragment.searchDataUpdate(keyWord, binding.pagerHome.getCurrentItem());
                isPageRefreshSearch = true;
                isPageRefreshFilter = false;
            }
        });
        
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CustomerHomeFragmentListener) {
            customerHomeFragmentListener = (CustomerHomeFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement customerHomeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        customerHomeFragmentListener = null;
    }

    public class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            DeliveryTypeFragment fragment = DeliveryTypeFragment.newInstance(position);
            fragmentMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return tabString.length;
        }
    }
}
