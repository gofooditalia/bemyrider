package com.app.bemyrider.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.bemyrider.R;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.utils.ConnectionManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

public class PaymentConfirmationActivity extends AppCompatActivity {

    WebView ivLoading;
    private Context context;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_confirmation);

        context = PaymentConfirmationActivity.this;

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        ivLoading = findViewById(R.id.ivLoading);
        ivLoading.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        ivLoading.getSettings().setLoadWithOverviewMode(true);
        ivLoading.getSettings().setUseWideViewPort(true);
        ivLoading.loadDataWithBaseURL("file:///android_res/raw/",
                "<img src='loading2.gif' />", "text/html", "utf-8",
                null);
    }

    @Override
    public void onBackPressed() {
       super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            if (event.getType().equalsIgnoreCase("notifyPayment")) {
                Map<String, String> remoteMessage = event.getData();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("MESSAGE", remoteMessage.get("message"));
                if (remoteMessage.get("status").equalsIgnoreCase("true")) {
                    Log.e("WAITING PAYMENT", "SUCCESS");
                    setResult(Activity.RESULT_OK, resultIntent);
                } else {
                    Log.e("WAITING PAYMENT", "FAIL");
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                }
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
