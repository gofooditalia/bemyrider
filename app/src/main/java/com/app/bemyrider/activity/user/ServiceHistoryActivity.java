package com.app.bemyrider.activity.user;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.fragment.user.OngoingServiceFragment;
import com.app.bemyrider.fragment.user.PreviousServiceFragment;
import com.app.bemyrider.fragment.user.UpcomingServiceFragment;
import com.app.bemyrider.model.BulkInvoicePojo;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.LinkedHashMap;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


public class ServiceHistoryActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

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
    private ConnectionManager connectionManager;
    private UpcomingServiceFragment upcomingServiceFragment;
    private OngoingServiceFragment ongoingServiceFragment;
    private PreviousServiceFragment previousServiceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_history);

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.service_request),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabString();
    }

    private void initViews() {
        context = ServiceHistoryActivity.this;

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        viewPager = findViewById(R.id.pager_history);
        tabLayout = findViewById(R.id.tabLayout_serviceHistory);

        upcomingServiceFragment = new UpcomingServiceFragment();
        ongoingServiceFragment = new OngoingServiceFragment();
        previousServiceFragment = new PreviousServiceFragment();

        FloatingActionButton fab = findViewById(R.id.fab_bulk_invoice);
        fab.setOnClickListener(v -> showBulkDownloadDialog());
    }

    private void showBulkDownloadDialog() {
        String[] periods = {
            getString(R.string.period_last_week),
            getString(R.string.period_last_month)
        };
        new AlertDialog.Builder(this)
            .setTitle(R.string.download_period_title)
            .setItems(periods, (dialog, which) -> {
                String period = (which == 0) ? "last_week" : "last_month";
                callBulkInvoiceApi(period);
            })
            .show();
    }

    private void callBulkInvoiceApi(String period) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("user_id", PrefsUtil.with(this).readString("UserId"));
        params.put("user_type", PrefsUtil.with(this).readString("UserType"));
        params.put("period", period);

        new WebServiceCall(this, WebServiceUrl.URL_BULK_INVOICES, params,
            BulkInvoicePojo.class, true, new WebServiceCall.OnResultListener() {
                @Override
                public void onResult(boolean status, Object obj) {
                    if (status) {
                        BulkInvoicePojo pojo = (BulkInvoicePojo) obj;
                        startZipDownload(pojo.getData().getFileName(), pojo.getData().getCount());
                    } else {
                        Toast.makeText(context, obj.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                @Override public void onAsync(Object obj) {}
                @Override public void onCancelled() {}
            });
    }

    private void startZipDownload(String url, int count) {
        String fileName = "ricevute-bemyrider-" + System.currentTimeMillis() + ".zip";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(getString(R.string.download_invoices_bulk));
        request.setDescription(count + " ricevute");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.allowScanningByMediaScanner();
        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) {
            dm.enqueue(request);
            Toast.makeText(context, getString(R.string.download_started), Toast.LENGTH_SHORT).show();
        }
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
        adapter.addFrag(upcomingServiceFragment, "Upcoming");
        adapter.addFrag(ongoingServiceFragment, "Ongoing");
        adapter.addFrag(previousServiceFragment, "Previous");
        viewPager.setAdapter(adapter);
    }

    private void setupTabString() {
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
        // La logica di coloring non ha senso se non ci sono icone, la lascio commentata come nell'originale.
        /*int tabIconColor = ContextCompat.getColor(this, R.color.button);
        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);*/
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
        EventBus.getDefault().register(this);
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
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            // Se l'Activity è l'ascoltatore attivo per l'evento di servizio,
            // aggiorna i dati dei fragment esistenti invece di ricreare tutto.
            if (event.getType().equalsIgnoreCase("s")) {
                // Notifica a ciascun fragment di ricaricare i propri dati
                upcomingServiceFragment.refreshData();
                ongoingServiceFragment.refreshData();
                previousServiceFragment.refreshData();
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
