package com.app.bemyrider.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.app.bemyrider.R;
import com.app.bemyrider.utils.PrefsUtil;

public class MyStripeConnectActivity extends AppCompatActivity {

    private static final String TAG = "MyStripeConnectActivity";

    private WebView mWebView;
    private String connectUrl = "";

    private ProgressDialog mSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypalwebview);

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.stripe_conncent),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mWebView = findViewById(R.id.webview_paypal);

        connectUrl = getIntent().getStringExtra("StripeUrl");

        mSpinner = new ProgressDialog(this);
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");

        Log.e(TAG, "setUpWebView: url" + connectUrl);

        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e(TAG, "OAuthWebViewClient.shouldOverrideUrlLoading" + "Redirecting URL" + url);
                // https://gotasker.ncryptedprojects.com/ws/profile-nct/stripe_success-nct.php?status=done
                Log.e("ON CLICK", url);
                if (url.contains("stripe_success-nct.php")) {
                    //Toast.makeText(MyStripeConnectActivity.this, getString(R.string.you_have_successfully_connected_your_stripe_account), Toast.LENGTH_SHORT).show();
                    view.loadUrl(url);
                    PrefsUtil.with(MyStripeConnectActivity.this).write("stripe_connect", "y");
                    //finish();
                    return true;
                } else {
                    view.loadUrl(url);
                    return true;
                }
            }


            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                Log.e(TAG, "OAuthWebViewClient.onReceivedError " + "Page error[errorCode=" + errorCode + "]: " + description);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.e(TAG, "OAuthWebViewClient.onPageStarted " + "url: " + url);
                mSpinner.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.e(TAG, "OAuthWebViewClient.onPageFinished " + "url: " + url);
                mSpinner.dismiss();
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.loadUrl(connectUrl);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
