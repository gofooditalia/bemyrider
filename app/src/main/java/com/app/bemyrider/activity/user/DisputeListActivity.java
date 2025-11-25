package com.app.bemyrider.activity.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;

import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
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
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityDisputelistBinding;
import com.app.bemyrider.model.DisputeListPojo;
import com.app.bemyrider.model.DisputeListPojoItem;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.utils.ConnectionManager;
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
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Modified by Hardik Talaviya on 11/12/19.
 */

public class DisputeListActivity extends AppCompatActivity {

    //Dispute Listing for User
    private ActivityDisputelistBinding binding;
    private List<DisputeListPojoItem> disputeList;
    private DisputeListAdapter adapter;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int page = 1;
    private int total_records = 0;
    private RecyclerView.OnScrollListener listner;
    private AsyncTask disputeListAsync;
    private LinearLayoutManager layoutManager;
    private boolean loading = true;
    private SharedPreferences preferences;
    private Context context;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(DisputeListActivity.this, R.layout.activity_disputelist, null);

        preferences = getSharedPreferences("Unique", MODE_PRIVATE);

        initViews();

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

        /*Init Internet Connection Class For No Internet Banner*/
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

    /*----------------- Get Dispute List Api Call ------------------*/
    private void getDisputeList(final boolean isClear) {
        if (isClear) {
            page = 1;
            binding.txtNoRecordDis.setVisibility(View.GONE);
            binding.rvDisputeList.scrollToPosition(0);
        }
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        String url = WebServiceUrl.URL_GETDISPUTELIST;
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(DisputeListActivity.this).readString("UserId"));

        textParams.put("page", String.valueOf(page));

        textParams.put("lId", preferences.getString("lanId", "1"));

        new WebServiceCall(this, url, textParams, DisputeListPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (binding.swipeRefresh.isRefreshing()) {
                            binding.swipeRefresh.setRefreshing(false);
                        }
                        if (status) {
                            DisputeListPojo disputeListPojo = (DisputeListPojo) obj;
                            List<DisputeListPojoItem> list = ((DisputeListPojo) obj).getData().getDisputeList();
                            if (isClear) {
                                disputeList.clear();
                            }
                            binding.progress.setVisibility(View.GONE);
                            binding.rvDisputeList.setVisibility(View.VISIBLE);

                            disputeList.addAll(list);
                            adapter.notifyDataSetChanged();
                            loading = true;
                            if (disputeList.size() > 0) {
                                binding.txtNoRecordDis.setVisibility(View.GONE);
                                binding.rvDisputeList.setVisibility(View.VISIBLE);
                            } else {
                                binding.rvDisputeList.setVisibility(View.GONE);
                                binding.txtNoRecordDis.setVisibility(View.VISIBLE);
                            }
                            try {
                                total_records = disputeListPojo.getData().getPagination().getTotalRecords();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            binding.rvDisputeList.addOnScrollListener(listner);
                        } else {
                            binding.progress.setVisibility(View.GONE);
                            Toast.makeText(DisputeListActivity.this, (String) obj,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        disputeListAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        disputeListAsync = null;
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
                    disputeList.clear();
                    getOfflineDetails();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getOfflineDetails() {
        try {
            binding.progress.setVisibility(View.GONE);
            binding.txtNoRecordDis.setVisibility(View.GONE);
            binding.rvDisputeList.setVisibility(View.VISIBLE);
            Log.e("Offline", "onMessageEvent: My Dispute");
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
            JSONArray serviceList = dataObj.getJSONArray("disputeList");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
            Gson gson = gsonBuilder.create();
            Type listType = new TypeToken<List<DisputeListPojoItem>>() {
            }.getType();
            ArrayList<DisputeListPojoItem> arrayList = gson.fromJson(serviceList.toString(), listType);

            //MyServiceListPojo response_myservice = new MyServiceListPojo();
            // item.setData();
            disputeList.addAll(arrayList);
            if (!(arrayList.size() > 0)) {
                binding.txtNoRecordDis.setVisibility(View.VISIBLE);

            }
           /* if (response_myservice.getData().size() > 0) {
                loading = true;
            }*/
//                    next_avail_record = arrayList.size()

            adapter.notifyDataSetChanged();
            binding.rvDisputeList.removeOnScrollListener(listner);
//            new ConnectionCheck().showDialogWithMessage(DisputeListActivity.this, getString(R.string.sync_data_message)).show();


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
        Utils.cancelAsyncTask(disputeListAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}

