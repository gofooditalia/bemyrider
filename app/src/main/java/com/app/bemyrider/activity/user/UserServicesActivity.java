package com.app.bemyrider.activity.user;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
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
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerActivityMyServicesBinding;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.partner.MyServiceListItem;
import com.app.bemyrider.model.partner.MyServiceListPojo;
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
import java.util.LinkedHashMap;
import java.util.List;

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
    private AsyncTask serviceListAsync;
    private ConnectionManager connectionManager;

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
            }
        } else {
            Toast.makeText(mContext, getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        initView();

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

    private void serviceCall(boolean isClear) {

        if (isClear) {
            page = 1;
            binding.layoutNoservice.setVisibility(View.GONE);
            binding.rvMyservice.scrollToPosition(0);
        }
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", providerId);
        textParams.put("keyword", Utils.encodeEmoji(keyWord));
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(mContext, WebServiceUrl.URL_MYSERVICE, textParams,
                MyServiceListPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                if (status) {
                    MyServiceListPojo response_myservice = (MyServiceListPojo) obj;
                    if (isClear) {
                        arrayList.clear();
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.rvMyservice.setVisibility(View.VISIBLE);

                    arrayList.addAll(response_myservice.getData().getServiceList());
                    adapter.notifyDataSetChanged();

                    if (!(arrayList.size() > 0)) {
                        binding.layoutNoservice.setVisibility(View.VISIBLE);
                        binding.rvMyservice.setVisibility(View.GONE);
                    } else {
                        binding.layoutNoservice.setVisibility(View.GONE);
                        binding.rvMyservice.setVisibility(View.VISIBLE);
                    }
                    binding.rvMyservice.addOnScrollListener(listner);
                    loading = true;
                    try {
                        total_records = response_myservice.getData().getPagination().getTotalRecords();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(mContext, obj.toString(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                serviceListAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                serviceListAsync = null;
            }
        });

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
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
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
        try {
            binding.layoutNoservice.setVisibility(View.GONE);
            binding.progress.setVisibility(View.GONE);
            binding.rvMyservice.setVisibility(View.VISIBLE);
            Log.e("Offline", "onMessageEvent: My Service");
            File f = new File(getFilesDir().getPath() + "/" + "offline.json");
            //check whether file exists
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
            gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
            Gson gson = gsonBuilder.create();
            Type listType = new TypeToken<List<MyServiceListItem>>() {
            }.getType();
            ArrayList<MyServiceListItem> list = gson.fromJson(serviceList.toString(), listType);

            arrayList.clear();
            arrayList.addAll(list);
            if (!(arrayList.size() > 0)) {
                binding.layoutNoservice.setVisibility(View.VISIBLE);
            }

            adapter.notifyDataSetChanged();
            binding.rvMyservice.removeOnScrollListener(listner);
//            new ConnectionCheck().showDialogWithMessage(UserServicesActivity.this, getString(R.string.sync_data_message)).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(serviceListAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
