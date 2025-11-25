package com.app.bemyrider.activity.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;

import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;

import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.app.bemyrider.activity.PaypalwebviewActivity;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.model.WalletDetailsPojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;

public class DepositFundActivity extends AppCompatActivity {

    //Deposit Fund Activity
    RelativeLayout relPayPal;
    EditText Edt_depositeAmount, Edt_depositeAmount_commission;
    private TextView Txt_fundvalue, txt_admin_commission_wallet, Txt_fundvalue_service_price;
    private LinearLayout layout_direct_deposit;

    private SharedPreferences preferences;

    private DecimalFormat df = new DecimalFormat("#.##");

    private float walletCommission;
    private AsyncTask walletDetailAsync;
    private Context context;
    private ConnectionManager connectionManager;
    ActivityResultLauncher<Intent> myActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposite_fund);

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.deposit_fund),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        initViews();

        try {
            if (getIntent().getStringExtra("customer_commission_wallet") != null
                    || !getIntent().getStringExtra("customer_commission_wallet").equals("")) {
                layout_direct_deposit.setVisibility(View.VISIBLE);
                float total = 0.0f, requiredAmount = 0.0f;
                if ((getIntent().getStringExtra("serviceMasterType").equalsIgnoreCase("fixed"))) {
                    float serviceCommission = (Float.parseFloat(getIntent().getStringExtra("service_amount_wallet")) * Float.parseFloat(getIntent().getStringExtra("customer_commission_wallet"))) / 100;
                    Log.e("SERVICE COMMISSION", serviceCommission + "");
                    walletCommission = (((Float.parseFloat(getIntent().getStringExtra("service_amount_wallet"))
                            - Float.parseFloat(getIntent().getStringExtra("wallet_amount_wallet")))
                            + serviceCommission)
                            * Float.parseFloat(getIntent().getStringExtra("deposit_commission_wallet"))) / 100;
                    Log.e("WALLET COMMISSION", walletCommission + "");
                    total = serviceCommission + walletCommission;
                    requiredAmount = Float.parseFloat(getIntent().getStringExtra("service_amount_wallet")) + total;

                } else if ((getIntent().getStringExtra("serviceMasterType").equalsIgnoreCase("hourly"))) {
                    float serviceCommission = (Float.parseFloat(getIntent().getStringExtra("service_amount_wallet")) * Float.parseFloat(PrefsUtil.with(DepositFundActivity.this).readString("sel_hours_wallet")) * Float.parseFloat(getIntent().getStringExtra("customer_commission_wallet"))) / 100;
                    walletCommission = ((((Float.parseFloat(getIntent().getStringExtra("service_amount_wallet"))
                            * Float.parseFloat(PrefsUtil.with(DepositFundActivity.this).readString("sel_hours_wallet")))
                            + serviceCommission)
                            - Float.parseFloat(getIntent().getStringExtra("wallet_amount_wallet"))) * Float.parseFloat(getIntent().getStringExtra("deposit_commission_wallet"))) / 100;
                    total = Float.valueOf(serviceCommission + walletCommission);
                    requiredAmount = (Float.parseFloat(getIntent().getStringExtra("service_amount_wallet")) * Float.parseFloat(PrefsUtil.with(DepositFundActivity.this).readString("sel_hours_wallet"))) + total;
                }

                if ((getIntent().getStringExtra("serviceMasterType").equalsIgnoreCase("fixed"))) {
                    try {
                        Txt_fundvalue_service_price.setText(PrefsUtil.with(DepositFundActivity.this).readString("CurrencySign") + df.format(getIntent().getStringExtra("service_amount_wallet")));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
//                        DecimalFormat df1 = new DecimalFormat("#");
                        Txt_fundvalue_service_price.setText(PrefsUtil.with(DepositFundActivity.this).readString("CurrencySign") + getIntent().getStringExtra("service_amount_wallet"));
                    }
                } else if ((getIntent().getStringExtra("serviceMasterType").equalsIgnoreCase("hourly"))) {
                    Txt_fundvalue_service_price.setText(PrefsUtil.with(DepositFundActivity.this).readString("CurrencySign") + String.valueOf(Float.parseFloat(getIntent().getStringExtra("service_amount_wallet")) * Float.parseFloat(PrefsUtil.with(DepositFundActivity.this).readString("sel_hours_wallet"))));
                }
                Txt_fundvalue.setText(PrefsUtil.with(DepositFundActivity.this).readString("CurrencySign") + getIntent().getStringExtra("wallet_amount_wallet"));
                txt_admin_commission_wallet.setText(getIntent().getStringExtra("deposit_commission_wallet") + " % " + getString(R.string.deposit_commission) + " + " + getIntent().getStringExtra("customer_commission_wallet") + " % " + getString(R.string.admin_feesa));
//                float total = (finalamount* Float.parseFloat(getIntent().getStringExtra("deposit_commission_wallet"))) / 100;
                Edt_depositeAmount_commission.setText(String.valueOf(df.format(total)));

                float finalamount = requiredAmount - Float.parseFloat(getIntent().getStringExtra("wallet_amount_wallet"));
                finalamount = finalamount + 0.01f;
                Edt_depositeAmount.setText(df.format(finalamount));
                Edt_depositeAmount.setFocusable(false);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        relPayPal.setOnClickListener(view -> {
            if (!Edt_depositeAmount.getText().toString().trim().isEmpty()) {
                if (Float.parseFloat(Edt_depositeAmount.getText().toString()) > 0) {
                   /* Log.e("Deposit_amount",Edt_depositeAmount.getText().toString().trim());
                    Log.e("Deposit_deposit_commission",walletCommission+"");
                    Log.e("Deposit_serviceId",getIntent().getStringExtra("serviceId"));*/
                    Intent i = new Intent(DepositFundActivity.this, PaypalwebviewActivity.class);
                    i.putExtra("amount", Edt_depositeAmount.getText().toString().trim());
                    i.putExtra("deposit_commission", walletCommission);
                    i.putExtra("serviceId", getIntent().getStringExtra("serviceId"));
                    myActivityResultLauncher.launch(i);

                } else {
                    Edt_depositeAmount.setError(getString(R.string.please_enter_proper_deposit_amount));
                }
            } else {
                Edt_depositeAmount.setError(getString(R.string.please_enter_deposit_amount_first));
            }
        });
    }

    private void initViews() {
        context = DepositFundActivity.this;

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        myActivityResult();

        preferences = getSharedPreferences("Unique", MODE_PRIVATE);

        relPayPal = findViewById(R.id.relPayPal);
        Edt_depositeAmount = findViewById(R.id.Edt_depositeAmount);
        Edt_depositeAmount_commission = findViewById(R.id.Edt_depositeAmount_commission);
        Txt_fundvalue = findViewById(R.id.Txt_fundvalue);
        Txt_fundvalue_service_price = findViewById(R.id.Txt_fundvalue_service_price);
        txt_admin_commission_wallet = findViewById(R.id.txt_admin_commission_wallet);
        Txt_fundvalue_service_price = findViewById(R.id.Txt_fundvalue_service_price);
        layout_direct_deposit = findViewById(R.id.layout_direct_deposit);

        Txt_fundvalue.setText(getIntent().getStringExtra("actual_amount"));


    }

    private void myActivityResult() {
        myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Log.e("onActivityResult", "onActivityResult: 555");
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.e("onActivityResult", "onActivityResult: Activity.RESULT_OK");
                    if (getIntent().hasExtra("customer_commission_wallet")) {
                        if (getIntent().getStringExtra("customer_commission_wallet") != null || !getIntent().getStringExtra("customer_commission_wallet").equals("")) {
                            setResult(RESULT_OK, new Intent());
                            finish();
                        } else {
                            setResult(RESULT_OK, new Intent());
                            finish();
                        }
                    } else {
                        setResult(RESULT_OK, new Intent());
                        finish();
                    }
                } else {
                    try {
                        Toast.makeText(DepositFundActivity.this, result.getData().getStringExtra("MESSAGE"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                }
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


    private void getWalletDetails() {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(DepositFundActivity.this).readString("UserId"));

        textParams.put("lId", preferences.getString("lanId", "1"));

        new WebServiceCall(DepositFundActivity.this, WebServiceUrl.URL_GET_WALLET_DETAILS,
                textParams, WalletDetailsPojo.class, true,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            WalletDetailsPojo walletDetailsPojo = (WalletDetailsPojo) obj;
                            Txt_fundvalue.setText(PrefsUtil.with(DepositFundActivity.this).readString("CurrencySign") + walletDetailsPojo.getData().getWalletAmount());

                        } else {
                            Toast.makeText(DepositFundActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        walletDetailAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        walletDetailAsync = null;
                    }
                });
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (walletDetailAsync != null) {
            walletDetailAsync.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
