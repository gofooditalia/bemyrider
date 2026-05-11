package com.app.bemyrider.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.NotificationListAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.activity.user.NotificationActivity;
import com.app.bemyrider.databinding.FragmentNotificationBinding;
import com.app.bemyrider.model.NotificationData;
import com.app.bemyrider.model.NotificationListItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.viewmodel.NotificationViewModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;
    private int page = 1, totalPages = 1;
    private LinearLayoutManager layoutManager;
    private NotificationListAdapter adapter;
    private ArrayList<NotificationListItem> notifications = new ArrayList<>();
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private boolean isLoading = false;
    private boolean pendingClear = false;
    private Context context;
    private AppCompatActivity activity;
    private ConnectionManager connectionManager;
    private NotificationViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false);

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

        binding.imgSettings.setOnClickListener(v ->
                startActivity(new Intent(activity, NotificationActivity.class)));

        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getNotifications().observe(getViewLifecycleOwner(), pojo -> {
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

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
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
        context = getContext();
        activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar);
        }

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        layoutManager = new LinearLayoutManager(context);
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
        String userId = PrefsUtil.with(context).readString("UserId");
        String userType = PrefsUtil.with(context).readString("UserType");
        viewModel.loadNotifications(userId, userType, page);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
