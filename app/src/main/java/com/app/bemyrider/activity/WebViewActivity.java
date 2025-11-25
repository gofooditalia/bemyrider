package com.app.bemyrider.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityWebViewBinding;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;


public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";
    private ActivityWebViewBinding binding;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(WebViewActivity.this, R.layout.activity_web_view, null);

        initViews();

        Boolean isTermsAndConditions = getIntent().getBooleanExtra("isTermsAndConditions", false);
        String url = getIntent().getStringExtra("webUrl");

        if (isTermsAndConditions) {
            loadUrlInWebView(url);
        } else {
            renderWebPage(url);
        }
    }

    private void initViews() {
        context = WebViewActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getIntent().getStringExtra("title"),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // Custom method to render a web page
    private void loadUrlInWebView(String urlToRender) {
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                binding.pb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                binding.pb.setVisibility(View.GONE);
            }

        });
        binding.webView.loadUrl(urlToRender);

    }

    protected void renderWebPage(String urlToRender) {
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Do something on page loading started
                // Visible the progressbar
                binding.pb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                /*Hide header in website*/
                view.evaluateJavascript(
                        "var element = document.getElementsByTagName('header'), index;" +
                                "for (index = element.length - 1; index >= 0; index--) {" +
                                "element[index].parentNode.removeChild(element[index]);" +
                                "}", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                Log.e(TAG, "onReceiveValue: " + value);
                            }
                        });
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                /*Hide header in website*/
                view.evaluateJavascript(
                        "var element = document.getElementsByTagName('header'), index;" +
                                "for (index = element.length - 1; index >= 0; index--) {" +
                                "element[index].parentNode.removeChild(element[index]);" +
                                "}", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                Log.e(TAG, "onReceiveValue: " + value);
                            }
                        });
            }

        });

        binding.webView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int newProgress) {

                binding.pb.setProgress(newProgress);
                if (newProgress == 100) {
                    binding.pb.setVisibility(View.GONE);
                }
            }
        });

        // Enable the javascript
        binding.webView.getSettings().setJavaScriptEnabled(true);

        // Render the web page
        binding.webView.loadUrl(urlToRender);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}

