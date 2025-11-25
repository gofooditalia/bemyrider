package com.app.bemyrider.activity.user;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;

import java.util.LinkedHashMap;

public class RedeemActivity extends AppCompatActivity {

    private Button Btn_submit;
    private AsyncTask redeemRequestAsync;
    private Context context;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reedem);

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.redeem_request),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        initViews();

        Btn_submit.setOnClickListener(view -> serviceCallRedeem());


    }

    private void initViews() {
        context = RedeemActivity.this;

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        Btn_submit = findViewById(R.id.Btn_submit);
    }

    private void serviceCallRedeem() {

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(RedeemActivity.this).readString("UserId"));

        new WebServiceCall(RedeemActivity.this, WebServiceUrl.URL_REDDEMRE_REQUEST,
                textParams, CommonPojo.class, true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    Toast.makeText(RedeemActivity.this, ((CommonPojo) obj).getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RedeemActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                redeemRequestAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                redeemRequestAsync = null;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (redeemRequestAsync != null) {
            redeemRequestAsync.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
