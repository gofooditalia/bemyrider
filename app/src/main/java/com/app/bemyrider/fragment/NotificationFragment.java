package com.app.bemyrider.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.NotificationListAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.user.NotificationActivity;
import com.app.bemyrider.databinding.FragmentNotificationBinding;
import com.app.bemyrider.model.NotificationData;
import com.app.bemyrider.model.NotificationDataPOJO;
import com.app.bemyrider.model.NotificationListItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class NotificationFragment extends Fragment {
    FragmentNotificationBinding binding;

    private int page = 1, totalPages = 1;
    private LinearLayoutManager layoutManager;
    private NotificationListAdapter adapter;
    private ArrayList<NotificationListItem> notifications = new ArrayList<>();
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private boolean isLoading = false;
    private AsyncTask notificationListAsync;
    private Context context;
    private AppCompatActivity activity;
    private ConnectionManager connectionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false);

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

        binding.imgSettings.setOnClickListener(v -> {
            Intent resultIntent = new Intent(activity, NotificationActivity.class);
            startActivity(resultIntent);
        });
        return binding.getRoot();
    }

    private void initToolBar() {
        context = getContext();
        activity = (AppCompatActivity) getActivity();

        activity.setSupportActionBar(binding.toolbar);
        /*Init Internet Connection Class For No Internet Banner*/
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

        textParams.put("user_id", PrefsUtil.with(context).readString("UserId"));

        textParams.put("user_type", PrefsUtil.with(context).readString("UserType"));

        textParams.put("page", String.valueOf(page));

        new WebServiceCall(context, url, textParams, NotificationDataPOJO.class, false,
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
    public void onDestroy() {
        super.onDestroy();
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(notificationListAsync);
    }


}
