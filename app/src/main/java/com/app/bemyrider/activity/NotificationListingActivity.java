package com.app.bemyrider.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.NotificationListAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.activity.user.NotificationActivity;
import com.app.bemyrider.databinding.ActivityNotificationListingBinding;
import com.app.bemyrider.model.NotificationData;
import com.app.bemyrider.model.NotificationListItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.viewmodel.NotificationViewModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationListingActivity extends AppCompatActivity {

    private ActivityNotificationListingBinding binding;
    private int page = 1, totalPages = 1;
    private LinearLayoutManager layoutManager;
    private NotificationListAdapter adapter;
    private ArrayList<NotificationListItem> notifications = new ArrayList<>();
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private boolean isLoading = false;
    private boolean pendingClear = false;
    private Context context;
    private Activity activity;
    private ConnectionManager connectionManager;
    private NotificationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = NotificationListingActivity.this;
        activity = NotificationListingActivity.this;

        binding = DataBindingUtil.setContentView(activity, R.layout.activity_notification_listing, null);

        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        observeViewModel();
        initToolBar();
        loadNotifications(true);

        binding.rvNotification.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && page <= totalPages) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            page++;
                            loadNotifications(false);
                        }
                    }
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getNotifications().observe(this, pojo -> {
            if (binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.isStatus()) {
                NotificationData notiData = pojo.getNotificationData();
                List<NotificationListItem> list = notiData.getNotificationList();
                if (pendingClear) {
                    notifications.clear();
                    pendingClear = false;
                }
                notifications.addAll(list);
                boolean hasItems = !notifications.isEmpty();
                binding.txtNoRecordDis.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.rvNotification.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                adapter.notifyDataSetChanged();
                totalPages = notiData.getPagination().getTotalPages();
                page = notiData.getPagination().getCurrentPage();
            }
            isLoading = false;
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                isLoading = false;
            }
        });
    }

    private void initToolBar() {
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getResources().getString(R.string.notifications), HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
            loadNotifications(true);
        });
    }

    private void loadNotifications(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.txtNoRecordDis.setVisibility(View.GONE);
            binding.rvNotification.scrollToPosition(0);
        }
        isLoading = true;
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }
        String userId = PrefsUtil.with(activity).readString("UserId");
        String userType = PrefsUtil.with(activity).readString("UserType");
        viewModel.loadNotifications(userId, userType, page);
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
            startActivity(new Intent(activity, NotificationActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
