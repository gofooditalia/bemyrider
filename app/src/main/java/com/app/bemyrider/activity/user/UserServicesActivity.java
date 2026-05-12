package com.app.bemyrider.activity.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.partner.AddNewService_Activity;
import com.app.bemyrider.Adapter.User.ProviderServicesAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.R;
import com.app.bemyrider.viewmodel.PartnerMyServicesViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.databinding.PartnerActivityMyServicesBinding;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.partner.MyServiceListItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserServicesActivity extends AppCompatActivity {

    private PartnerActivityMyServicesBinding binding;
    private ProviderServicesAdapter adapter;
    private ArrayList<MyServiceListItem> arrayList = new ArrayList<>();
    private Context mContext = UserServicesActivity.this;
    private LinearLayoutManager layoutManager;
    private String providerId = "";
    private String keyWord = "";
    private PartnerMyServicesViewModel viewModel;
    private boolean isRefreshing = false;
    private ConnectionManager connectionManager;
    
    private boolean isFromDeepLink = false;
    private ExecutorService diskExecutor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean isLoading = false;
    private int page = 1;
    private int total_records = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.partner_activity_my_services);

        if (getIntent().hasExtra(Utils.PROVIDER_ID)) {
            providerId = getIntent().getStringExtra(Utils.PROVIDER_ID);
        }
        
        if (providerId == null || providerId.isEmpty()) {
            finish();
            return;
        }

        isFromDeepLink = getIntent().getBooleanExtra("isFromDeepLink", false);

        initView();

        viewModel = new ViewModelProvider(this).get(PartnerMyServicesViewModel.class);
        observeViewModel();
        serviceCall(true);

        binding.rvMyservice.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && arrayList.size() < total_records) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 2) {
                            serviceCall(false);
                        }
                    }
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getServices().observe(this, pojo -> {
            binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            isLoading = false;

            if (pojo != null && pojo.getData() != null) {
                List<MyServiceListItem> newItems = pojo.getData().getServiceList();
                try { total_records = pojo.getData().getPagination().getTotalRecords(); } catch (Exception ignored) {}

                if (isRefreshing) {
                    arrayList.clear();
                    if (newItems != null) arrayList.addAll(newItems);
                    adapter.notifyDataSetChanged();
                    isRefreshing = false;
                } else {
                    if (newItems != null && !newItems.isEmpty()) {
                        int oldSize = arrayList.size();
                        arrayList.addAll(newItems);
                        adapter.notifyItemRangeInserted(oldSize, newItems.size());
                    }
                }

                boolean hasItems = !arrayList.isEmpty();
                binding.layoutNoservice.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.rvMyservice.setVisibility(hasItems ? View.VISIBLE : View.GONE);
            }
        });
        
        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                isLoading = false;
                isRefreshing = false;
                Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void serviceCall(boolean isClear) {
        if (isLoading) return;
        isLoading = true;

        if (isClear) {
            page = 1;
            isRefreshing = true;
            binding.layoutNoservice.setVisibility(View.GONE);
        } else {
            page++;
            isRefreshing = false;
        }

        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);

        java.util.LinkedHashMap<String, String> params = new java.util.LinkedHashMap<>();
        params.put("user_id", providerId);
        params.put("keyword", Utils.encodeEmoji(keyWord));
        params.put("page", String.valueOf(page));
        viewModel.loadMyServices(params);
    }

    private void initView() {
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.services),HtmlCompat.FROM_HTML_MODE_LEGACY));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        binding.fabAddService.setVisibility(View.GONE);

        layoutManager = new LinearLayoutManager(mContext);
        binding.rvMyservice.setLayoutManager(layoutManager);
        adapter = new ProviderServicesAdapter(mContext, arrayList, getIntent().getStringExtra("providerImage"));
        binding.rvMyservice.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> serviceCall(true));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception ignored) {}
        if (diskExecutor != null && !diskExecutor.isShutdown()) diskExecutor.shutdown();
        super.onDestroy();
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(LocaleManager.onAttach(newBase)); }
}
