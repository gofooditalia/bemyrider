package com.app.bemyrider.fragment.user;

import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.FragmentServiceHistoryBinding;
import com.app.bemyrider.model.BulkInvoicePojo;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

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

        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar);
        }

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

        binding.fabBulkInvoice.setOnClickListener(v -> showBulkDownloadDialog());
    }

    private void showBulkDownloadDialog() {
        String[] periods = {
                getString(R.string.period_last_week),
                getString(R.string.period_last_month),
                getString(R.string.period_custom)
        };
        new AlertDialog.Builder(context)
                .setTitle(R.string.download_period_title)
                .setItems(periods, (dialog, which) -> {
                    if (which == 2) {
                        showCustomDatePicker();
                    } else {
                        String period = (which == 0) ? "last_week" : "last_month";
                        callBulkInvoiceApi(period, null, null);
                    }
                })
                .show();
    }

    private void showCustomDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog fromDatePicker = new DatePickerDialog(context, (view, year1, month1, dayOfMonth) -> {
            Calendar fromCal = Calendar.getInstance();
            fromCal.set(year1, month1, dayOfMonth);
            String dateFrom = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fromCal.getTime());

            DatePickerDialog toDatePicker = new DatePickerDialog(context, (view1, year2, month2, dayOfMonth1) -> {
                Calendar toCal = Calendar.getInstance();
                toCal.set(year2, month2, dayOfMonth1);
                String dateTo = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(toCal.getTime());

                callBulkInvoiceApi("custom", dateFrom, dateTo);
            }, year, month, day);
            toDatePicker.setTitle(R.string.select_date_to);
            toDatePicker.show();

        }, year, month, day);
        fromDatePicker.setTitle(R.string.select_date_from);
        fromDatePicker.show();
    }

    private void callBulkInvoiceApi(String period, String dateFrom, String dateTo) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("user_id", PrefsUtil.with(context).readString("UserId"));
        params.put("user_type", PrefsUtil.with(context).readString("UserType"));
        params.put("period", period);
        if (dateFrom != null) params.put("date_from", dateFrom);
        if (dateTo != null) params.put("date_to", dateTo);

        new WebServiceCall(activity, WebServiceUrl.URL_BULK_INVOICES, params,
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
        DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) {
            dm.enqueue(request);
            Toast.makeText(context, getString(R.string.download_started), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFrag(upcomingServiceFragment, "Upcoming");
        adapter.addFrag(ongoingServiceFragment, "Ongoing");
        adapter.addFrag(previousServiceFragment, "Previous");
        binding.pagerHistory.setAdapter(adapter);
    }

    private void setupTabString() {
        if (binding.tabLayoutServiceHistory.getTabAt(0) != null)
            binding.tabLayoutServiceHistory.getTabAt(0).setText(tabString[0]);
        if (binding.tabLayoutServiceHistory.getTabAt(1) != null)
            binding.tabLayoutServiceHistory.getTabAt(1).setText(tabString[1]);
        if (binding.tabLayoutServiceHistory.getTabAt(2) != null)
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
