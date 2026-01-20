package com.app.bemyrider.activity.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;

public class DepositFundActivity extends AppCompatActivity {

    RelativeLayout relPayPal;
    EditText Edt_depositeAmount, Edt_depositeAmount_commission;
    private TextView Txt_fundvalue, txt_admin_commission_wallet, Txt_fundvalue_service_price;
    private LinearLayout layout_direct_deposit;

    private SharedPreferences preferences;

    private DecimalFormat df = new DecimalFormat("#.##");

    private float walletCommission;
    private WebServiceCall walletDetailAsync;
    private Context context;
    private ConnectionManager connectionManager;
    ActivityResultLauncher<Intent> myActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposite_fund);

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.deposit_fund),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        try {
            String commissionStr = getIntent().getStringExtra("customer_commission_wallet");
            if (commissionStr != null && !commissionStr.equals("")) {
                layout_direct_deposit.setVisibility(View.VISIBLE);
                float total = 0.0f, requiredAmount = 0.0f;
                String masterType = getIntent().getStringExtra("serviceMasterType");
                String serviceAmount = getIntent().getStringExtra("service_amount_wallet");
                String walletAmount = getIntent().getStringExtra("wallet_amount_wallet");
                String depositCommission = getIntent().getStringExtra("deposit_commission_wallet");

                if (masterType.equalsIgnoreCase("fixed")) {
                    float serviceCommission = (Float.parseFloat(serviceAmount) * Float.parseFloat(commissionStr)) / 100;
                    walletCommission = (((Float.parseFloat(serviceAmount) - Float.parseFloat(walletAmount)) + serviceCommission) * Float.parseFloat(depositCommission)) / 100;
                    total = serviceCommission + walletCommission;
                    requiredAmount = Float.parseFloat(serviceAmount) + total;

                } else if (masterType.equalsIgnoreCase("hourly")) {
                    float selHours = Float.parseFloat(PrefsUtil.with(DepositFundActivity.this).readString("sel_hours_wallet"));
                    float serviceCommission = (Float.parseFloat(serviceAmount) * selHours * Float.parseFloat(commissionStr)) / 100;
                    walletCommission = (((Float.parseFloat(serviceAmount) * selHours + serviceCommission) - Float.parseFloat(walletAmount)) * Float.parseFloat(depositCommission)) / 100;
                    total = serviceCommission + walletCommission;
                    requiredAmount = (Float.parseFloat(serviceAmount) * selHours) + total;
                }

                if (masterType.equalsIgnoreCase("fixed")) {
                    try {
                        Txt_fundvalue_service_price.setText(PrefsUtil.with(this).readString("CurrencySign") + df.format(Double.parseDouble(serviceAmount)));
                    } catch (Exception e) {
                        Txt_fundvalue_service_price.setText(PrefsUtil.with(this).readString("CurrencySign") + serviceAmount);
                    }
                } else if (masterType.equalsIgnoreCase("hourly")) {
                    float selHours = Float.parseFloat(PrefsUtil.with(this).readString("sel_hours_wallet"));
                    Txt_fundvalue_service_price.setText(PrefsUtil.with(this).readString("CurrencySign") + (Float.parseFloat(serviceAmount) * selHours));
                }
                Txt_fundvalue.setText(PrefsUtil.with(this).readString("CurrencySign") + walletAmount);
                txt_admin_commission_wallet.setText(depositCommission + " % " + getString(R.string.deposit_commission) + " + " + commissionStr + " % " + getString(R.string.admin_feesa));
                Edt_depositeAmount_commission.setText(df.format(total));

                float finalamount = requiredAmount - Float.parseFloat(walletAmount) + 0.01f;
                Edt_depositeAmount.setText(df.format(finalamount));
                Edt_depositeAmount.setFocusable(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        relPayPal.setOnClickListener(view -> {
            String amountStr = Edt_depositeAmount.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                if (Float.parseFloat(amountStr) > 0) {
                    Intent i = new Intent(DepositFundActivity.this, PaypalwebviewActivity.class);
                    i.putExtra("amount", amountStr);
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
        context = this;
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
        layout_direct_deposit = findViewById(R.id.layout_direct_deposit);

        Txt_fundvalue.setText(getIntent().getStringExtra("actual_amount"));
    }

    private void myActivityResult() {
        myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                setResult(RESULT_OK, new Intent());
                finish();
            } else {
                if (result.getData() != null && result.getData().hasExtra("MESSAGE")) {
                    Toast.makeText(DepositFundActivity.this, result.getData().getStringExtra("MESSAGE"), Toast.LENGTH_LONG).show();
                }
                finish();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void getWalletDetails() {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("user_id", PrefsUtil.with(this).readString("UserId"));
        textParams.put("lId", preferences.getString("lanId", "1"));

        new WebServiceCall(this, WebServiceUrl.URL_GET_WALLET_DETAILS,
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
                    @Override public void onAsync(Object obj) { walletDetailAsync = null; }
                    @Override public void onCancelled() { walletDetailAsync = null; }
                });
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        Utils.cancelAsyncTask(walletDetailAsync);
        super.onDestroy();
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(LocaleManager.onAttach(newBase)); }
}
