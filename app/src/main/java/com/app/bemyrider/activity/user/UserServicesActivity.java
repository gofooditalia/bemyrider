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
import com.app.bemyrider.utils.Log;
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

/**
 * Modified by Hardik Talaviya on 2/12/19.
 */


public class UserServicesActivity extends AppCompatActivity {

    private PartnerActivityMyServicesBinding binding;
    private RecyclerView.OnScrollListener listner;
    private ProviderServicesAdapter adapter;
    private ArrayList<MyServiceListItem> arrayList = new ArrayList<>();
    private Context mContext = UserServicesActivity.this;
    private LinearLayoutManager layoutManager;
    private String providerId = "";
    private String keyWord = "";
    private PartnerMyServicesViewModel viewModel;
    private boolean pendingClear = false;
    private ConnectionManager connectionManager;
    
    // Flag per evitare loop di navigazione sugli auto-redirects
    private boolean isFromDeepLink = false;
    
    private ExecutorService diskExecutor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    /*pagination vars start*/
    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisibleItems = 0, visibleItemCount, totalItemCount;
    /*pagination vars end*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(UserServicesActivity.this, R.layout.partner_activity_my_services, null);

        if (getIntent().hasExtra(Utils.PROVIDER_ID)) {
            if (getIntent().getStringExtra(Utils.PROVIDER_ID) != null
                    && getIntent().getStringExtra(Utils.PROVIDER_ID).length() > 0) {
                providerId = getIntent().getStringExtra(Utils.PROVIDER_ID);
            } else {
                Toast.makeText(mContext, getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(mContext, getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Lettura del flag dal deep link
        isFromDeepLink = getIntent().getBooleanExtra("isFromDeepLink", false);

        initView();

        viewModel = new ViewModelProvider(this).get(PartnerMyServicesViewModel.class);
        observeViewModel();
        serviceCall(true);

        binding.imgSearch.setOnClickListener(v -> {
            binding.rvMyservice.setVisibility(View.GONE);
            serviceCall(true);
        });

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                keyWord = s.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.fabAddService.setOnClickListener(v -> {
            startActivity(new Intent(mContext, AddNewService_Activity.class));
        });
    }

    private void observeViewModel() {
        viewModel.getServices().observe(this, pojo -> {
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                if (pendingClear) { arrayList.clear(); pendingClear = false; }
                arrayList.addAll(pojo.getData().getServiceList());
                adapter.notifyDataSetChanged();

                if (isFromDeepLink && arrayList.size() == 1) {
                    isFromDeepLink = false;
                    MyServiceListItem item = arrayList.get(0);
                    Intent i = new Intent(mContext, ServiceDetailActivity.class);
                    i.putExtra(Utils.PROVIDER_SERVICE_ID, item.getProviderServiceId());
                    i.putExtra(Utils.PROVIDER_ID, item.getUserId());
                    i.putExtra("providerImage", getIntent().getStringExtra("providerImage"));
                    startActivity(i);
                    finish();
                    return;
                }

                boolean hasItems = !arrayList.isEmpty();
                binding.layoutNoservice.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.rvMyservice.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                binding.rvMyservice.addOnScrollListener(listner);
                loading = true;
                try { total_records = pojo.getData().getPagination().getTotalRecords(); } catch (Exception ignored) {}
            }
        });
        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void serviceCall(boolean isClear) {
        if (isClear) { page = 1; binding.layoutNoservice.setVisibility(View.GONE); binding.rvMyservice.scrollToPosition(0); }
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);

        java.util.LinkedHashMap<String, String> params = new java.util.LinkedHashMap<>();
        params.put("user_id", providerId);
        params.put("keyword", Utils.encodeEmoji(keyWord));
        params.put("page", String.valueOf(page));
        viewModel.loadMyServices(params);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.services),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        binding.fabAddService.setVisibility(View.GONE);

        layoutManager = new LinearLayoutManager(mContext);
        binding.rvMyservice.setLayoutManager(layoutManager);
        adapter = new ProviderServicesAdapter(mContext, arrayList, getIntent().getStringExtra("providerImage"));
        binding.rvMyservice.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCall(true);
        });

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
                            if (arrayList.size() < total_records) {
                                page++;
                                serviceCall(false);
                            }
                        }
                    }
                }
            }
        };

    }

    @Override
    protected void onResume() {
        if (!(new ConnectionCheck().isNetworkConnected(this))) {
            getOfflineDetails();
        }
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    binding.fabAddService.setVisibility(View.GONE);
                    getOfflineDetails();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getOfflineDetails() {
        binding.layoutNoservice.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);
        binding.rvMyservice.setVisibility(View.GONE);
        Log.e("Offline", "onMessageEvent: My Service - Background task");

        diskExecutor.execute(() -> {
            try {
                File f = new File(getFilesDir().getPath() + "/" + "offline.json");
                if (!f.exists()) {
                    mainHandler.post(() -> {
                        binding.progress.setVisibility(View.GONE);
                        binding.layoutNoservice.setVisibility(View.VISIBLE);
                    });
                    return;
                }
                
                FileInputStream is = new FileInputStream(f);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String s = new String(buffer);
                
                JSONObject object = new JSONObject(s);
                JSONObject dataObj = object.getJSONObject("data");
                JSONArray serviceList = dataObj.getJSONArray("serviceList");
                
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat("M/d/yy hh:mm a"); 
                Gson gson = gsonBuilder.create();
                Type listType = new TypeToken<List<MyServiceListItem>>() {}.getType();
                ArrayList<MyServiceListItem> list = gson.fromJson(serviceList.toString(), listType);

                mainHandler.post(() -> {
                    try {
                        binding.progress.setVisibility(View.GONE);
                        binding.rvMyservice.setVisibility(View.VISIBLE);
                        
                        arrayList.clear();
                        arrayList.addAll(list);
                        
                        if (!(arrayList.size() > 0)) {
                            binding.layoutNoservice.setVisibility(View.VISIBLE);
                        }
            
                        adapter.notifyDataSetChanged();
                        binding.rvMyservice.removeOnScrollListener(listner);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    binding.progress.setVisibility(View.GONE);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
            if (diskExecutor != null && !diskExecutor.isShutdown()) diskExecutor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}