package com.app.bemyrider.activity.user;

import static com.app.bemyrider.utils.Utils.PROVIDER_ID;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.bemyrider.R;
import com.app.bemyrider.fragment.user.ServiceListFragment;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


public class PopularCategoryActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener {

    private TabLayout tlPopular;
    private ViewPager vpPopular;
    private Context context;
    private ConnectionManager connectionManager;
    //private TaskersListFragment taskersListFragment;
    private ServiceListFragment serviceListFragment;
    private String providerId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_popular_category);

        if (getIntent().hasExtra(PROVIDER_ID)) {
            if (getIntent().getStringExtra(PROVIDER_ID) != null
                    && getIntent().getStringExtra(PROVIDER_ID).length() > 0) {
                providerId = getIntent().getStringExtra(PROVIDER_ID);
            }
        }

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>"
                + getIntent().getStringExtra("serviceName"),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        setupViewPager(vpPopular);
        tlPopular.setupWithViewPager(vpPopular);
        setupTabIcons();
    }

    private void initViews() {
        context = PopularCategoryActivity.this;

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        //taskersListFragment = new TaskersListFragment();
        serviceListFragment = new ServiceListFragment();

        vpPopular = findViewById(R.id.pager_popular);
        tlPopular = findViewById(R.id.tabLayout_popular);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        Bundle b = new Bundle();
        b.putString(PROVIDER_ID, providerId);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        serviceListFragment.setArguments(b);
        adapter.addFrag(serviceListFragment, getResources().getString(R.string.services));
        /*taskersListFragment.setArguments(b);
        adapter.addFrag(taskersListFragment, getResources().getString(R.string.taskers));*/
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        tlPopular.getTabAt(0).setText(getResources().getString(R.string.services));
        // tlPopular.getTabAt(1).setText(getResources().getString(R.string.taskers));
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        vpPopular.setCurrentItem(tab.getPosition());
        int tabIconColor = ContextCompat.getColor(this, R.color.button);
        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            // taskersListFragment.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            if (event.getType().equalsIgnoreCase("s")) {
                setupViewPager(vpPopular);
                tlPopular.setupWithViewPager(vpPopular);
                setupTabIcons();
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
            //Display Title with Icon
            // return mFragmentTitleList.get(position);
            return null;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
