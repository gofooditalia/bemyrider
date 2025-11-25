package com.app.bemyrider.fragment.user;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.FragmentServiceHistoryBinding;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.utils.ConnectionManager;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ServiceHistoryFragment extends Fragment implements TabLayout.OnTabSelectedListener {

    FragmentServiceHistoryBinding binding;
    private Context context;
    private AppCompatActivity activity;
    private int[] tabString =
            {
                    R.string.upcoming,
                    R.string.ongoing,
                    R.string.past
            };
    private ConnectionManager connectionManager;
    private UpcomingServiceFragment upcomingServiceFragment;
    private OngoingServiceFragment ongoingServiceFragment;
    private PreviousServiceFragment previousServiceFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_history, container, false);
        context = getContext();
        activity = (AppCompatActivity) getActivity();

        activity.setSupportActionBar(binding.toolbar);

        initViews();

        setupViewPager(binding.pagerHistory);
        binding.tabLayoutServiceHistory.setupWithViewPager(binding.pagerHistory);
        setupTabString();

        return binding.getRoot();

    }

    private void initViews() {
        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        upcomingServiceFragment = new UpcomingServiceFragment();
        ongoingServiceFragment = new OngoingServiceFragment();
        previousServiceFragment = new PreviousServiceFragment();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFrag(upcomingServiceFragment, "Upcoming");
        adapter.addFrag(ongoingServiceFragment, "Ongoing");
        adapter.addFrag(previousServiceFragment, "Previous");
        binding.pagerHistory.setAdapter(adapter);
    }

    private void setupTabString() {
        /*binding.tabLayoutServiceHistory.getTabAt(0).setIcon(tabIcons[0]);
        binding.tabLayoutServiceHistory.getTabAt(1).setIcon(tabIcons[1]);
        binding.tabLayoutServiceHistory.getTabAt(2).setIcon(tabIcons[2]);*/
        binding.tabLayoutServiceHistory.getTabAt(0).setText(tabString[0]);
        binding.tabLayoutServiceHistory.getTabAt(1).setText(tabString[1]);
        binding.tabLayoutServiceHistory.getTabAt(2).setText(tabString[2]);
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            if (event.getType().equalsIgnoreCase("s")) {
                setupViewPager(binding.pagerHistory);
                binding.tabLayoutServiceHistory.setupWithViewPager(binding.pagerHistory);
                setupTabString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

}
