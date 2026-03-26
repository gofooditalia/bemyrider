package com.app.bemyrider.fragment;

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
import com.app.bemyrider.activity.GuestHomeActivity;
import com.app.bemyrider.databinding.FragmentGuestHomeBinding;
import com.app.bemyrider.utils.Utils;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class GuestHomeFragment extends Fragment {

    private GuestHomeFragmentListener guestHomeFragmentListener;

    public interface GuestHomeFragmentListener {
        void onFilterClick();
        void setCurrentPosition(int position);
    }

    private FragmentGuestHomeBinding binding;
    private Context context;
    private AppCompatActivity activity;
    private int[] tabString =
            {
                    R.string.small,
                    R.string.medium,
                    R.string.large
            };
    private String keyWord = "";

    private ArrayList<Fragment> mNewFragmentList = new ArrayList<>();

    private ViewPagerAdapter adapter;
    private boolean isPageRefreshSearch = false;
    private boolean isPageRefreshFilter = false;

    private String address = "", latitude = "", longitude = "", strAsc = "", strDesc = "", strSearch = "", strRating = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guest_home, container, false);

        activity = (AppCompatActivity) getActivity();
        context = getContext();

        activity.setSupportActionBar(binding.toolbar);

        binding.pagerHome.setAdapter(createCardAdapter());
        
        // Riduce animazioni pesanti pre-caricando meno pagine se ci sono problemi di memoria
        // ma per il tablayout 3 pagine vanno bene
        binding.pagerHome.setOffscreenPageLimit(1); 
        
        new TabLayoutMediator(binding.tabLayoutHome, binding.pagerHome,
                (tab, position) -> tab.setText(tabString[position])).attach();

        ((GuestHomeActivity) getActivity()).setOnHomeFilterData((address, latitude, longitude, strAsc, strDesc, strSearch, strRating) -> {
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.strAsc = strAsc;
            this.strDesc = strDesc;
            this.strSearch = strSearch;
            this.strRating = strRating;

            Fragment fragment = mNewFragmentList.get(binding.pagerHome.getCurrentItem());
            if (fragment instanceof GuestDeliveryTypeFragment) {
                GuestDeliveryTypeFragment deliveryTypeFragment = (GuestDeliveryTypeFragment) fragment;
                deliveryTypeFragment.filterDataUpdate(address, latitude, longitude, strAsc, strDesc, strSearch, strRating,binding.pagerHome.getCurrentItem());
                isPageRefreshFilter = true;
                isPageRefreshSearch = false;
            }
        });

        binding.pagerHome.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                
                if (guestHomeFragmentListener != null) {
                    guestHomeFragmentListener.setCurrentPosition(position);
                }

                // FIX PERFORMANCE TABLAYOUT: Avvolgi l'aggiornamento dati nel post
                binding.pagerHome.post(() -> {
                    if (!isAdded()) return;

                    if (isPageRefreshSearch) {
                        Utils.hideSoftKeyboard(activity);
                        if (mNewFragmentList.size() > position) {
                            Fragment fragment = mNewFragmentList.get(position);
                            if (fragment instanceof GuestDeliveryTypeFragment) {
                                ((GuestDeliveryTypeFragment) fragment).searchDataUpdate(keyWord, position);
                            }
                        }
                    } else if (isPageRefreshFilter) {
                        if (mNewFragmentList.size() > position) {
                            Fragment fragment = mNewFragmentList.get(position);
                            if (fragment instanceof GuestDeliveryTypeFragment) {
                                ((GuestDeliveryTypeFragment) fragment).filterDataUpdate(address, latitude, longitude, strAsc, strDesc, strSearch, strRating, position);
                            }
                        }
                    }
                });
            }
        });

        binding.imgFilter.setOnClickListener(v -> {
            if (guestHomeFragmentListener != null) {
                guestHomeFragmentListener.onFilterClick();
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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                keyWord = s.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.imgSearch.setOnClickListener(v -> {
            Utils.hideSoftKeyboard(activity);
            Fragment fragment = mNewFragmentList.get(binding.pagerHome.getCurrentItem());
            if (fragment instanceof GuestDeliveryTypeFragment) {
                GuestDeliveryTypeFragment deliveryTypeFragment = (GuestDeliveryTypeFragment) fragment;
                deliveryTypeFragment.searchDataUpdate(keyWord,binding.pagerHome.getCurrentItem());
                isPageRefreshSearch = true;
                isPageRefreshFilter = false;
            }
        });
        
        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GuestHomeFragmentListener) {
            guestHomeFragmentListener = (GuestHomeFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement GuestHomeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        guestHomeFragmentListener = null;
    }

    private ViewPagerAdapter createCardAdapter() {
        adapter = new ViewPagerAdapter(this);
        return adapter;
    }

    public class ViewPagerAdapter extends FragmentStateAdapter {
        private static final int CARD_ITEM_SIZE = 3;

        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = GuestDeliveryTypeFragment.newInstance(position);
            // Sicurezza: Evitiamo che mNewFragmentList sfasi con il reciclo del ViewPager
            if (position >= mNewFragmentList.size()) {
                mNewFragmentList.add(fragment);
            } else {
                mNewFragmentList.set(position, fragment);
            }
            return fragment;
        }

        @Override
        public int getItemCount() {
            return CARD_ITEM_SIZE;
        }
    }
}
