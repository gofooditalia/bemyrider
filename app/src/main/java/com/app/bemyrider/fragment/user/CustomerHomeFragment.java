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

import java.util.ArrayList;

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
    private int[] tabString =
            {
                    R.string.small,
                    R.string.medium,
                    R.string.large
            };
    private String keyWord = "";
    private int currentIndex = 0;
    private ArrayList<Fragment> mNewFragmentList = new ArrayList<>();

    ViewPagerAdapter adapter;
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

        binding.pagerHome.setAdapter(createCardAdapter());
        binding.pagerHome.setOffscreenPageLimit(tabString.length);
        new TabLayoutMediator(binding.tabLayoutHome, binding.pagerHome,
                (tab, position) -> tab.setText(tabString[position])).attach();
        //binding.pagerHome.setUserInputEnabled(false);

        ((CustomerHomeActivity) getActivity()).setOnHomeFilterData((address, latitude, longitude, strAsc, strDesc, strSearch, strRating) -> {
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.strAsc = strAsc;
            this.strDesc = strDesc;
            this.strSearch = strSearch;
            this.strRating = strRating;
            Log.e(TAG, binding.pagerHome.getCurrentItem() + "");
            Fragment fragment = mNewFragmentList.get(binding.pagerHome.getCurrentItem());
            // Check fragment type to make sure it is one we know has an updateView Method
            if (fragment instanceof DeliveryTypeFragment) {
                DeliveryTypeFragment deliveryTypeFragment = (DeliveryTypeFragment) fragment;
                deliveryTypeFragment.filterDataUpdate(address, latitude, longitude, strAsc, strDesc, strSearch, strRating, binding.pagerHome.getCurrentItem());
                //isPageRefreshFilter = !"".equals(address) || !"".equals(latitude) || !"".equals(longitude) || !"".equals(strAsc) || !"".equals(strDesc) || !"".equals(strSearch) || !"".equals(strRating);
                isPageRefreshFilter = true;
                isPageRefreshSearch = false;
            }
        });

        binding.pagerHome.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                customerHomeFragmentListener.setCurrentPosition(position);
                if (isPageRefreshSearch) {
                    Utils.hideSoftKeyboard(activity);
                    Log.e(TAG, binding.pagerHome.getCurrentItem() + "");
                    Fragment fragment = mNewFragmentList.get(position);
                    // Check fragment type to make sure it is one we know has an updateView Method
                    if (fragment instanceof DeliveryTypeFragment) {
                        DeliveryTypeFragment deliveryTypeFragment = (DeliveryTypeFragment) fragment;
                        deliveryTypeFragment.searchDataUpdate(keyWord, binding.pagerHome.getCurrentItem());
                    }
                } else if (isPageRefreshFilter) {
                    Log.e(TAG, binding.pagerHome.getCurrentItem() + "");
                    Fragment fragment = mNewFragmentList.get(position);
                    // Check fragment type to make sure it is one we know has an updateView Method
                    if (fragment instanceof DeliveryTypeFragment) {
                        DeliveryTypeFragment deliveryTypeFragment = (DeliveryTypeFragment) fragment;
                        deliveryTypeFragment.filterDataUpdate(address, latitude, longitude, strAsc, strDesc, strSearch, strRating, binding.pagerHome.getCurrentItem());
                    }
                }
                Log.e("onPageSelected", String.valueOf(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });



        binding.imgFilter.setOnClickListener(v -> {
            customerHomeFragmentListener.onFilterClick();
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
            Utils.hideSoftKeyboard(activity);
            Log.e(TAG, binding.pagerHome.getCurrentItem() + "");
            Fragment fragment = mNewFragmentList.get(binding.pagerHome.getCurrentItem());
            // Check fragment type to make sure it is one we know has an updateView Method
            if (fragment instanceof DeliveryTypeFragment) {
                DeliveryTypeFragment deliveryTypeFragment = (DeliveryTypeFragment) fragment;
                deliveryTypeFragment.searchDataUpdate(keyWord, binding.pagerHome.getCurrentItem());
                isPageRefreshSearch = true;
                isPageRefreshFilter = false;
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CustomerHomeFragmentListener) {
            customerHomeFragmentListener = (CustomerHomeFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement customerHomeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        customerHomeFragmentListener = null;
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
            Fragment fragment = DeliveryTypeFragment.newInstance(position);
            mNewFragmentList.add(fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return CARD_ITEM_SIZE;
        }

    }

    /*public class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return mFragmentList[position];
        }

        private void refreshFragment(int index, Fragment fragment) {
            binding.pagerHome.post(() -> {
                mFragmentList[index] = fragment;
                notifyItemChanged(index);
            });
            //binding.pagerHome.post(() -> adapter.notifyItemChanged(binding.pagerHome.getCurrentItem()));
        }

        public void updateFragment(int position){
            Fragment fragment = mFragmentList[position];
            // Check fragment type to make sure it is one we know has an updateView Method
            if (fragment instanceof DeliveryTypeFragment){
                DeliveryTypeFragment textFragment = (DeliveryTypeFragment) fragment;
                textFragment.updateView();
            }
        }

        @Override
        public long getItemId(int position) {
            return mFragmentList[position].hashCode();
        }

        @Override
        public int getItemCount() {
            return mFragmentList.length;
        }
    }*/

}
