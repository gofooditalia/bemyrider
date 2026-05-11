package com.app.bemyrider.activity.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.app.bemyrider.Adapter.User.DisputeListAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityDisputelistBinding;
import com.app.bemyrider.model.DisputeListPojoItem;
import com.app.bemyrider.viewmodel.DisputeListViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Modified by Hardik Talaviya on 11/12/19.
 */

public class DisputeListActivity extends AppCompatActivity {

    private ActivityDisputelistBinding binding;
    private List<DisputeListPojoItem> disputeList;
    private DisputeListAdapter adapter;
    private int visibleItemCount, totalItemCount, pastVisibleItems;
    private int page = 1;
    private int total_records = 0;
    private RecyclerView.OnScrollListener listner;
    private DisputeListViewModel viewModel;
    private LinearLayoutManager layoutManager;
    private boolean loading = true;
    private boolean pendingClear = false;
    private Context context;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(DisputeListActivity.this, R.layout.activity_disputelist, null);

        initViews();

        viewModel = new ViewModelProvider(this).get(DisputeListViewModel.class);
        observeViewModel();

        if (new ConnectionCheck().isNetworkConnected(this)) {
            getDisputeList(true);
        } else {
            getOfflineDetails();
        }

        listner = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            if (disputeList.size() < total_records) {
                                page++;
                                getDisputeList(false);
                            }
                        }
                    }
                }
            }
        };
    }

    private void initViews() {
        context = DisputeListActivity.this;
        initToolBar();

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            getDisputeList(true);
        });
    }

    private void initToolBar() {
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.resolution_center),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.rvDisputeList.setLayoutManager(layoutManager);
        binding.rvDisputeList.setItemAnimator(new DefaultItemAnimator());

        disputeList = new ArrayList<>();
        adapter = new DisputeListAdapter(DisputeListActivity.this, disputeList);
        binding.rvDisputeList.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getDisputes().observe(this, pojo -> {
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                if (pendingClear) { disputeList.clear(); pendingClear = false; }
                disputeList.addAll(pojo.getData().getDisputeList());
                adapter.notifyDataSetChanged();
                loading = true;
                boolean hasItems = !disputeList.isEmpty();
                binding.txtNoRecordDis.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.rvDisputeList.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                try { total_records = pojo.getData().getPagination().getTotalRecords(); } catch (Exception ignored) {}
                binding.rvDisputeList.addOnScrollListener(listner);
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDisputeList(final boolean isClear) {
        if (isClear) { page = 1; binding.txtNoRecordDis.setVisibility(View.GONE); binding.rvDisputeList.scrollToPosition(0); }
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);
        viewModel.loadDisputes(PrefsUtil.with(this).readString("UserId"), page);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onStart() { super.onStart(); if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this); }
    @Override public void onStop() { super.onStop(); EventBus.getDefault().unregister(this); }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    disputeList.clear();
                    getOfflineDetails();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

    }

    private void getOfflineDetails() {
        try {
            binding.progress.setVisibility(View.GONE);
            binding.txtNoRecordDis.setVisibility(View.GONE);
            binding.rvDisputeList.setVisibility(View.VISIBLE);
            Log.e("Offline", "onMessageEvent: My Dispute");
            File f = new File(getFilesDir().getPath() + "/" + "offline.json");
            if (!f.exists()) return;
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String s = new String(buffer);
            JSONObject object = new JSONObject(s);
            JSONObject dataObj = object.getJSONObject("data");
            JSONArray serviceList = dataObj.getJSONArray("disputeList");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a");
            Gson gson = gsonBuilder.create();
            Type listType = new TypeToken<List<DisputeListPojoItem>>() {}.getType();
            ArrayList<DisputeListPojoItem> arrayList = gson.fromJson(serviceList.toString(), listType);
            disputeList.addAll(arrayList);
            if (disputeList.isEmpty()) {
                binding.txtNoRecordDis.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
            binding.rvDisputeList.removeOnScrollListener(listner);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        super.onDestroy();
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(LocaleManager.onAttach(newBase)); }
}
