package com.app.bemyrider.activity.partner;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.viewpager.widget.ViewPager;

import com.app.bemyrider.fragment.partner.Fragment_Ongoing_ServiceRequest;
import com.app.bemyrider.fragment.partner.Fragment_Previous_ServiceRequest;
import com.app.bemyrider.fragment.partner.Fragment_Upcoming_ServiceRequest;
import com.app.bemyrider.Adapter.Partner.ViewPagerAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class Partner_ServiceRequest_TabLayout_Activity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    /*private int[] tabIcons =
            {
                    R.drawable.tabicon_upcoming_style,
                    R.drawable.tabicon_ongoing_style,
                    R.drawable.tabicon_previous_style
            };*/
    private int[] tabString =
            {
                    R.string.upcoming,
                    R.string.ongoing,
                    R.string.past
            };
    private Context context;
    private Activity activity;
    private Fragment_Upcoming_ServiceRequest fragmentUpcomingServiceRequest;
    private Fragment_Ongoing_ServiceRequest fragmentOngoingServiceRequest;
    private Fragment_Previous_ServiceRequest fragmentPreviousServiceRequest;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.partner_activity_service_history__tab_layout_);

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.service_request),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

    }

    private void initViews() {
        context = Partner_ServiceRequest_TabLayout_Activity.this;
        activity = Partner_ServiceRequest_TabLayout_Activity.this;

        fragmentUpcomingServiceRequest = new Fragment_Upcoming_ServiceRequest();
        fragmentOngoingServiceRequest = new Fragment_Ongoing_ServiceRequest();
        fragmentPreviousServiceRequest = new Fragment_Previous_ServiceRequest();

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        viewPager = findViewById(R.id.pager_history);
        tabLayout = findViewById(R.id.tabLayout_serviceHistory);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(fragmentUpcomingServiceRequest, "Upcoming");
        adapter.addFrag(fragmentOngoingServiceRequest, "Ongoing");
        adapter.addFrag(fragmentPreviousServiceRequest, "Previous");
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        /*tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);*/
        tabLayout.getTabAt(0).setText(tabString[0]);
        tabLayout.getTabAt(1).setText(tabString[1]);
        tabLayout.getTabAt(2).setText(tabString[2]);
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            if (event.getType().equalsIgnoreCase("s")) {
                setupViewPager(viewPager);
                tabLayout.setupWithViewPager(viewPager);
                setupTabIcons();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
