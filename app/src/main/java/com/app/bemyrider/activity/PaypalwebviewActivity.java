package com.app.bemyrider.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;

import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;

import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import com.app.bemyrider.R;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;


public class PaypalwebviewActivity extends AppCompatActivity {

    private WebView webview_paypal;
    private ProgressDialog progressBar;
    private Toolbar toolbar;
    private TextView txt_toolbar_title;
    private String AMOUNT = "", SERVICE_ID = "";
    private String PAYPAL_LINK = "";
    private String STRIPE_LINK = "";
    String bookingAmount = "", customerCommission = "", providerCommission = "", totalAmountToCharge = "", totalAmountToChargeFull = "",  serviceId = "", serviceMasterType = "";

    private Context mContext = PaypalwebviewActivity.this;
    private ConnectionManager connectionManager;
    ActivityResultLauncher<Intent> myActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_paypalwebview);

        initView();

        progressBar = ProgressDialog.show(PaypalwebviewActivity.this, getString(R.string.stripe_payment),
                "Loading...");

        /*if (getIntent().getStringExtra("amount") != null) {
            AMOUNT = getIntent().getStringExtra("amount");
            if (getIntent().hasExtra("serviceId")) {
                if (getIntent().getStringExtra("serviceId") != null) {
                    SERVICE_ID = getIntent().getStringExtra("serviceId");
                    PAYPAL_LINK = WebServiceUrl.URL_PAYPALBUTTON
                            + PrefsUtil.with(this).readString("UserId")
                            + "&amount=" + AMOUNT
                            + "&deposit_commission="
                            + getIntent().getFloatExtra("deposit_commission",
                            0.0f) + "&action=hire_service"
                            + "&service_id=" + SERVICE_ID;
                } else {
                    PAYPAL_LINK = WebServiceUrl.URL_PAYPALBUTTON
                            + PrefsUtil.with(this).readString("UserId") + "&amount=" + AMOUNT;
                }
            } else {
                PAYPAL_LINK = WebServiceUrl.URL_PAYPALBUTTON
                        + PrefsUtil.with(this).readString("UserId") + "&amount=" + AMOUNT;
            }
            Log.e("LINK", PAYPAL_LINK);

            WebSettings settings = webview_paypal.getSettings();
            settings.setJavaScriptEnabled(true);
            webview_paypal.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            webview_paypal.setWebViewClient(new MyWebViewClient());
            webview_paypal.loadUrl(PAYPAL_LINK);
        }*/

        getIntentData();

        WebSettings settings = webview_paypal.getSettings();
        settings.setJavaScriptEnabled(true);
        webview_paypal.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webview_paypal.setWebViewClient(new MyWebViewClient());
        webview_paypal.loadUrl(STRIPE_LINK);

       /* if (getIntent().getStringExtra("amount") != null) {
            AMOUNT = getIntent().getStringExtra("amount");
            SERVICE_ID = getIntent().getStringExtra("serviceId");
            if (getIntent().hasExtra("deposit_commission")) {
                if (getIntent().getFloatExtra("deposit_commission",
                        0.0f) == 0.0f) {
                    PAYPAL_LINK = WebServiceUrl.URL_PAYPALBUTTON
                            + PrefsUtil.with(this).readString("UserId")
                            + "&amount=" + AMOUNT
                            + "&deposit_commission="
                            + getIntent().getFloatExtra("deposit_commission",
                            0.0f) + "&action=hire_service"
                            + "&service_id=" + SERVICE_ID;
                } else {
                    PAYPAL_LINK = WebServiceUrl.URL_PAYPALBUTTON
                            + PrefsUtil.with(this).readString("UserId") + "&amount=" + AMOUNT;
                }
            } else {
                PAYPAL_LINK = WebServiceUrl.URL_PAYPALBUTTON
                        + PrefsUtil.with(this).readString("UserId") + "&amount=" + AMOUNT;
            }
            Log.e("LINK", PAYPAL_LINK);

            WebSettings settings = webview_paypal.getSettings();
            settings.setJavaScriptEnabled(true);
            webview_paypal.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            webview_paypal.setWebViewClient(new MyWebViewClient());
            webview_paypal.loadUrl(PAYPAL_LINK);
        }*/

    }

    private void getIntentData() {
        if (getIntent() != null) {
            if (getIntent().hasExtra("booking_amount")) {
                bookingAmount = getIntent().getStringExtra("booking_amount");
            }
            if (getIntent().hasExtra("customer_commission_wallet")) {
                customerCommission = getIntent().getStringExtra("customer_commission_wallet");
            }
            if (getIntent().hasExtra("provider_commission")) {
                providerCommission = getIntent().getStringExtra("provider_commission");
            }
            if (getIntent().hasExtra("total_amount_to_charge")) {
                totalAmountToCharge = getIntent().getStringExtra("total_amount_to_charge");
            }
            if (getIntent().hasExtra("total_amount_to_charge_full")) {
                totalAmountToChargeFull = getIntent().getStringExtra("total_amount_to_charge_full");
            }
            if (getIntent().hasExtra("payment_url")) {
                STRIPE_LINK = getIntent().getStringExtra("payment_url");
            }
            if (getIntent().hasExtra("serviceId")) {
                serviceId = getIntent().getStringExtra("serviceId");
            }
            if (getIntent().hasExtra("serviceMasterType")) {
                serviceMasterType = getIntent().getStringExtra("serviceMasterType");
            }
        }
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    public void onPause() {
        super.onPause();
        webview_paypal.onPause();
        webview_paypal.pauseTimers();
    }

    @Override
    public void onResume() {
        super.onResume();
        webview_paypal.onResume();
        webview_paypal.resumeTimers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        webview_paypal.stopLoading();
        webview_paypal.setWebChromeClient(null);
        webview_paypal.setWebViewClient(null);
        webview_paypal.destroy();
        webview_paypal = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private void initView() {
        setupToolbar();
        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        webview_paypal = (WebView) findViewById(R.id.webview_paypal);
        myActivityResult();
    }

    private void myActivityResult() {
        myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            try {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("MESSAGE", result.getData().getStringExtra("MESSAGE"));
                if (result.getResultCode() == RESULT_OK) {
                    setResult(Activity.RESULT_OK, resultIntent);
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                }
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupToolbar() {
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + "Payment",HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    public class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("ON CLICK", url);
            /*if (url.contains("success_stripe.php")) {
                Log.e("PAYMENT", "SUCCESS");
                Intent resultIntent = new Intent();
                resultIntent.putExtra("MESSAGE", "Payment success");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                /*Intent i = new Intent(PaypalwebviewActivity.this, PaymentConfirmationActivity.class);
                myActivityResultLauncher.launch(i);*//*
                return true;
            } else if (url.contains("cancel_stripe.php")) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);
                finish();
                return true;
            } else {

            }*/
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (progressBar.isShowing()) {
                progressBar.dismiss();
                Log.e("FINISH URL", url);
            }
            if (url.contains("success_stripe.php")) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("MESSAGE", "Payment success");
                setResult(Activity.RESULT_OK, resultIntent);
                onBackPressed();
                finish();
                /*Intent i = new Intent(PaypalwebviewActivity.this, PaymentConfirmationActivity.class);
                myActivityResultLauncher.launch(i);*/
            } else if (url.contains("cancel_stripe.php")) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);
                onBackPressed();
                finish();
            }
        }

        //https://gotasker.ncryptedprojects.com/ws/payment-nct/success.php?PayerID=8JBFD9MP68B7E
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.e("onPageStarted_url", url);
           /* Uri uri = Uri.parse(url);
            Set<String> args = uri.getQueryParameterNames();
            Log.e("onPageStarted_args",args.toString());
            String strPayerID = uri.getQueryParameter("PayerID");
            String strAction = uri.getQueryParameter("action");
            Log.e("onPageStarted_payerId",strPayerID);
            Log.e("onPageStarted_action",strAction);
            Log.e("PAYMENT", "SUCCESS");*/

        }



        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Toast.makeText(PaypalwebviewActivity.this, "Receive Error..", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
