package com.app.bemyrider.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.user.NotificationActivity;
import com.app.bemyrider.Adapter.NotificationListAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityNotificationListingBinding;
import com.app.bemyrider.model.NotificationData;
import com.app.bemyrider.model.NotificationDataPOJO;
import com.app.bemyrider.model.NotificationListItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Modified by Hardik Talaviya on 2/12/19.
 */

public class NotificationListingActivity extends AppCompatActivity {

    private ActivityNotificationListingBinding binding;
    private int page = 1, totalPages = 1;
    private LinearLayoutManager layoutManager;
    private NotificationListAdapter adapter;
    private ArrayList<NotificationListItem> notifications = new ArrayList<>();
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private boolean isLoading = false;
    private AsyncTask notificationListAsync;
    private Context context;
    private Activity activity;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = NotificationListingActivity.this;
        activity = NotificationListingActivity.this;

        binding = DataBindingUtil.setContentView(activity, R.layout.activity_notification_listing, null);

        initToolBar();

        getNotificationList(true);

        binding.rvNotification.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ((!isLoading) && page <= totalPages) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            page++;
                            getNotificationList(false);
                        }
                    }
                }
            }
        });


    }

    private void initToolBar() {
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getResources().getString(R.string.notifications),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.rvNotification.setLayoutManager(layoutManager);
        binding.rvNotification.setItemAnimator(new DefaultItemAnimator());

        notifications = new ArrayList<>();
        adapter = new NotificationListAdapter(activity, notifications);
        binding.rvNotification.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            getNotificationList(true);
        });

    }


    /*-------------- Notification List Api Call -----------------*/
    private void getNotificationList(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.txtNoRecordDis.setVisibility(View.GONE);
            binding.rvNotification.scrollToPosition(0);
        }
        isLoading = true;
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        String url = WebServiceUrl.URL_GETNOTIFICATIONS;
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(activity).readString("UserId"));

        textParams.put("user_type", PrefsUtil.with(activity).readString("UserType"));

        textParams.put("page", String.valueOf(page));

        new WebServiceCall(this, url, textParams, NotificationDataPOJO.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (binding.swipeRefresh.isRefreshing()) {
                            binding.swipeRefresh.setRefreshing(false);
                        }
                        if (status) {
                            NotificationDataPOJO disputeListPojo = (NotificationDataPOJO) obj;
                            NotificationData notiData = disputeListPojo.getNotificationData();
                            List<NotificationListItem> list = notiData.getNotificationList();
                            if (isClear) {
                                notifications.clear();
                            }
                            binding.progress.setVisibility(View.GONE);
                            binding.rvNotification.setVisibility(View.VISIBLE);
                            notifications.addAll(list);
                            if (!(notifications.size() > 0)) {
                                binding.txtNoRecordDis.setVisibility(View.VISIBLE);
                                binding.rvNotification.setVisibility(View.GONE);
                            } else {
                                binding.txtNoRecordDis.setVisibility(View.GONE);
                                binding.rvNotification.setVisibility(View.VISIBLE);
                            }
                            adapter.notifyDataSetChanged();

                            totalPages = notiData.getPagination().getTotalPages();
                            page = notiData.getPagination().getCurrentPage();
                        } else {
                            binding.progress.setVisibility(View.GONE);
                            Toast.makeText(context, (String) obj,
                                    Toast.LENGTH_SHORT).show();
                        }
                        isLoading = false;
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        notificationListAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        notificationListAsync = null;
                    }
                });
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(notificationListAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_settings) {
            Intent resultIntent = new Intent(activity, NotificationActivity.class);
            startActivity(resultIntent);
            // finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
