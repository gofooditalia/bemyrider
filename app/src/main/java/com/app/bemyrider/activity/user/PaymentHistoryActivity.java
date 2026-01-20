package com.app.bemyrider.activity.user;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;

import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
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

import com.app.bemyrider.Adapter.User.PaymentHistoryAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityPaymentHistoryBinding;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.PaymentHistoryPojo;
import com.app.bemyrider.model.PaymentHistoryPojoItem;
import com.app.bemyrider.model.TransectionListItem;
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

public class PaymentHistoryActivity extends AppCompatActivity {

    private ActivityPaymentHistoryBinding binding;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private ArrayList<TransectionListItem> paymentHistoryPojoItems;
    private WebServiceCall paymentHistoryAsync;
    private Context context;
    private ConnectionManager connectionManager;
    private LinearLayoutManager linearLayoutManager;

    /*pagination vars start*/
    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisiblesItems = 0, visibleItemCount, totalItemCount;
    /*pagination vars end*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(PaymentHistoryActivity.this, R.layout.activity_payment_history, null);

        initViews();

        binding.recyclerPaymentHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                if (dy > 0) {
                visibleItemCount = linearLayoutManager.getChildCount();
                totalItemCount = linearLayoutManager.getItemCount();
                pastVisiblesItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;
                        if (paymentHistoryPojoItems.size() < total_records) {
                            page++;
                            serviceCallGetPaymentHistory(false);
                        }
                        //Do pagination.. i.e. fetch new data
                    }
                }
            }
//            }
        });

        if (new ConnectionCheck().isNetworkConnected(this)) {
            serviceCallGetPaymentHistory(true);
        } else {
            getOfflineDetails();
        }

    }

    private void initViews() {
        context = PaymentHistoryActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.payment_history),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        paymentHistoryPojoItems = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(PaymentHistoryActivity.this);
        binding.recyclerPaymentHistory.setLayoutManager(linearLayoutManager);
        paymentHistoryAdapter = new PaymentHistoryAdapter(paymentHistoryPojoItems, PaymentHistoryActivity.this);
        binding.recyclerPaymentHistory.setAdapter(paymentHistoryAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallGetPaymentHistory(true);
        });
    }

    /*----------------- Get Payment History Api Call ------------------*/
    private void serviceCallGetPaymentHistory(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.txtNoRecordFav.setVisibility(View.GONE);
            binding.recyclerPaymentHistory.scrollToPosition(0);
        }
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(PaymentHistoryActivity.this).readString("UserId"));
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(PaymentHistoryActivity.this, WebServiceUrl.URL_PAYMENT_HISTORY,
                textParams, PaymentHistoryPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                if (status) {
                    PaymentHistoryPojo paymentHistoryPojo = (PaymentHistoryPojo) obj;
                    if (isClear) {
                        paymentHistoryPojoItems.clear();
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.recyclerPaymentHistory.setVisibility(View.VISIBLE);

                    if(paymentHistoryPojo.getData().getTransectionList() != null) {
                        paymentHistoryPojoItems.addAll(paymentHistoryPojo.getData().getTransectionList());
                        paymentHistoryAdapter.notifyDataSetChanged();
                        loading = true;
                        if (paymentHistoryPojoItems.size() > 0) {
                            binding.txtNoRecordFav.setVisibility(View.GONE);
                            binding.recyclerPaymentHistory.setVisibility(View.VISIBLE);
                        } else {
                            binding.recyclerPaymentHistory.setVisibility(View.GONE);
                            binding.txtNoRecordFav.setVisibility(View.VISIBLE);
                        }
                    }else{
                        binding.recyclerPaymentHistory.setVisibility(View.GONE);
                        binding.txtNoRecordFav.setVisibility(View.VISIBLE);
                    }

                    try {
                        total_records = paymentHistoryPojo.getData().getPagination().getTotalRecords();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(Object asyncTask) {
                paymentHistoryAsync = (WebServiceCall) asyncTask;
            }

            @Override
            public void onCancelled() {
                paymentHistoryAsync = null;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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
                    paymentHistoryPojoItems.clear();
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
            binding.txtNoRecordFav.setVisibility(View.GONE);
            binding.recyclerPaymentHistory.setVisibility(View.VISIBLE);
            Log.e("Offline", "onMessageEvent: My Resolution");
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
            JSONArray serviceList = dataObj.getJSONArray("transection_list");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
            Gson gson = gsonBuilder.create();
            Type listType = new TypeToken<List<PaymentHistoryPojoItem>>() {
            }.getType();
            ArrayList<TransectionListItem> arrayList = gson.fromJson(serviceList.toString(), listType);

            //MyServiceListPojo response_myservice = new MyServiceListPojo();
            // item.setData();
            paymentHistoryPojoItems.clear();
            paymentHistoryPojoItems.addAll(arrayList);
            if (!(arrayList.size() > 0)) {
                binding.txtNoRecordFav.setVisibility(View.VISIBLE);

            }
           /* if (response_myservice.getData().size() > 0) {
                loading = true;
            }*/
//                    next_avail_record = arrayList.size()

            paymentHistoryAdapter.notifyDataSetChanged();
//            new ConnectionCheck().showDialogWithMessage(PaymentHistoryActivity.this, getString(R.string.sync_data_message)).show();


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
        Utils.cancelAsyncTask(paymentHistoryAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
