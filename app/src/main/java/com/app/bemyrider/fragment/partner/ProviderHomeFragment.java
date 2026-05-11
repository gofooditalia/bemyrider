package com.app.bemyrider.fragment.partner;

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
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.app.bemyrider.R;
import com.app.bemyrider.viewmodel.BulkInvoiceViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.databinding.FragmentProviderHomeBinding;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.google.android.material.tabs.TabLayoutMediator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProviderHomeFragment extends Fragment {

    FragmentProviderHomeBinding binding;
    private Context context;
    private AppCompatActivity activity;
    private int[] tabString =
            {
                    R.string.upcoming,
                    R.string.ongoing,
                    R.string.past
            };
    private ConnectionManager connectionManager;
    private BulkInvoiceViewModel bulkViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_provider_home, container, false);

        activity = (AppCompatActivity) getActivity();
        context = getContext();

        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar);
        }

        init();

        bulkViewModel = new ViewModelProvider(this).get(BulkInvoiceViewModel.class);
        bulkViewModel.getResult().observe(getViewLifecycleOwner(), pojo -> {
            if (pojo != null && pojo.getData() != null)
                startZipDownload(pojo.getData().getFileName(), pojo.getData().getCount());
        });
        bulkViewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
        });

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        return binding.getRoot();
    }

    private void init() {
        binding.pagerHome.setAdapter(createCardAdapter());
        new TabLayoutMediator(binding.tabLayoutHome, binding.pagerHome,
                (tab, position) -> tab.setText(tabString[position])).attach();

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
        bulkViewModel.downloadBulk(
            PrefsUtil.with(context).readString("UserId"),
            PrefsUtil.with(context).readString("UserType"),
            period, dateFrom, dateTo);
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

    private ViewPagerAdapter createCardAdapter() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(activity);
        return adapter;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            if (event.getType().equalsIgnoreCase("s")) {
                init();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public class ViewPagerAdapter extends FragmentStateAdapter {
        private static final int CARD_ITEM_SIZE = 3;

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 2) {
                return new Fragment_Previous_ServiceRequest();
            } else if (position == 1) {
                return new Fragment_Ongoing_ServiceRequest();
            } else {
                return new Fragment_Upcoming_ServiceRequest();
            }
        }

        @Override
        public int getItemCount() {
            return CARD_ITEM_SIZE;
        }
    }

}
