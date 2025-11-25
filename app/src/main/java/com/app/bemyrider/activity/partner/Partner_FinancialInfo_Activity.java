package com.app.bemyrider.activity.partner;

import android.content.Context;
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

import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerActivityFinancialInfoBinding;
import com.app.bemyrider.model.FinancialInfoPojo;
import com.app.bemyrider.model.FinancialInfoPojoItem;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 3/12/19.
 */

public class Partner_FinancialInfo_Activity extends AppCompatActivity {

    private PartnerActivityFinancialInfoBinding binding;
    private AsyncTask finincialDetailAsync;
    private Context context;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(Partner_FinancialInfo_Activity.this, R.layout.partner_activity_financial_info, null);

        initViews();

        if (new ConnectionCheck().isNetworkConnected(this)) {
            serviceCallGetFinancialDetails();
        } else {
            getOfflineDetails();
        }

    }

    private void initViews() {
        context = Partner_FinancialInfo_Activity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.financial_info),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallGetFinancialDetails();
        });

    }

    /*------------- Financial Details Api Call ---------------*/
    private void serviceCallGetFinancialDetails() {

        if (!binding.swipeRefresh.isRefreshing()) {
            binding.llMain.setVisibility(View.GONE);
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(Partner_FinancialInfo_Activity.this).readString("UserId"));

        new WebServiceCall(Partner_FinancialInfo_Activity.this,
                WebServiceUrl.URL_GET_FINANCIAL_INFO, textParams, FinancialInfoPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (binding.swipeRefresh.isRefreshing()) {
                            binding.swipeRefresh.setRefreshing(false);
                        }
                        binding.progress.setVisibility(View.GONE);
                        binding.llMain.setVisibility(View.VISIBLE);
                        if (status) {
                            FinancialInfoPojo infoPojo = (FinancialInfoPojo) obj;
                            binding.txtCompleteServiceA.setText(String.valueOf(infoPojo.getData().getTotalCompletedService()));
                            binding.txtTotalCommision.setText(PrefsUtil.with(Partner_FinancialInfo_Activity.this).readString("CurrencySign") + infoPojo.getData().getTotalCommission());
                            binding.txtTotalEarned.setText(PrefsUtil.with(Partner_FinancialInfo_Activity.this).readString("CurrencySign") + infoPojo.getData().getTotalNetEarned());
                            binding.txtTotal.setText(PrefsUtil.with(Partner_FinancialInfo_Activity.this).readString("CurrencySign") + infoPojo.getData().getTotalEarned());
                        } else {
                            Toast.makeText(Partner_FinancialInfo_Activity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        finincialDetailAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        finincialDetailAsync = null;
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
            binding.llMain.setVisibility(View.VISIBLE);
            Log.e("Offline", "onMessageEvent: Partner Financial Info");
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
            JSONObject financialInfo = dataObj.getJSONObject("financialInfo");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
            Gson gson = gsonBuilder.create();
            FinancialInfoPojoItem item = new FinancialInfoPojoItem();
            item = gson.fromJson(financialInfo.toString(), FinancialInfoPojoItem.class);
            FinancialInfoPojo infoPojo = new FinancialInfoPojo();
            infoPojo.setData(item);
            binding.txtCompleteServiceA.setText(PrefsUtil.with(Partner_FinancialInfo_Activity.this).readString("CurrencySign") + infoPojo.getData().getTotalCompletedService());
            binding.txtTotalCommision.setText(String.format("%s%s", PrefsUtil.with(Partner_FinancialInfo_Activity.this).readString("CurrencySign"), infoPojo.getData().getTotalCommission()));
            binding.txtTotalEarned.setText(String.format("%s%s", PrefsUtil.with(Partner_FinancialInfo_Activity.this).readString("CurrencySign"), infoPojo.getData().getTotalNetEarned()));
            binding.txtTotal.setText(String.format("%s%s", PrefsUtil.with(Partner_FinancialInfo_Activity.this).readString("CurrencySign"), infoPojo.getData().getTotalEarned()));
//            new ConnectionCheck().showDialogWithMessage(Partner_FinancialInfo_Activity.this, getString(R.string.sync_data_message)).show();
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
        Utils.cancelAsyncTask(finincialDetailAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
