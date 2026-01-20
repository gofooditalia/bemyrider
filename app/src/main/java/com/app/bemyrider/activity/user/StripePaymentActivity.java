package com.app.bemyrider.activity.user;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityStripePaymentBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;

public class StripePaymentActivity extends AppCompatActivity {

    private static final String TAG = "StripePaymentActivity";

    private ActivityStripePaymentBinding binding;
    private Stripe stripe;
    private ProgressDialog progressBar;
    private WebServiceCall getStripePaymentAsync;

    String subTotal="",fees="";
    String bookingAmount = "", customerCommission = "", providerCommission = "", totalAmountToCharge = "", totalAmountToChargeFull = "", paymentIntentClientSecret = "", serviceId = "", serviceMasterType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(StripePaymentActivity.this, R.layout.activity_stripe_payment);

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.stripe_payment),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getIntentData();

        progressBar = new ProgressDialog(this);
        progressBar.setTitle(getString(R.string.stripe_payment));
        progressBar.setMessage(getString(R.string.loading));
        progressBar.setCancelable(false);

        initView();
    }

    private void initView() {
        final PaymentConfiguration paymentConfiguration = PaymentConfiguration.getInstance(getApplicationContext());
        stripe = new Stripe(getApplicationContext(), Objects.requireNonNull(paymentConfiguration.getPublishableKey()));

        binding.txtPrice.setText("€" + bookingAmount);
        binding.txtFees.setText("€" + fees);
        binding.txtAmount.setText("€" + totalAmountToChargeFull);
        binding.txtPaymentDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date()));

        binding.btnPayment.setOnClickListener(view -> {
            final PaymentMethodCreateParams params = binding.cardInputWidget.getPaymentMethodCreateParams();
            if (params != null && paymentIntentClientSecret != null) {
                progressBar.show();
                final ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                stripe.confirmPayment(StripePaymentActivity.this, confirmParams);
            } else {
                displayAlert("Payment failed", "Invalid payment parameters.");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        stripe.onPaymentResult(requestCode, data, new ApiResultCallback<PaymentIntentResult>() {
            @Override
            public void onSuccess(@NonNull PaymentIntentResult paymentIntentResult) {
                PaymentIntent paymentIntent = paymentIntentResult.getIntent();
                PaymentIntent.Status status = paymentIntent.getStatus();
                if (status == PaymentIntent.Status.Succeeded) {
                    setStripePaymentApi(paymentIntent);
                } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                    if (progressBar.isShowing()) progressBar.dismiss();
                    displayAlert("Payment failed", Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage());
                }
            }

            @Override
            public void onError(@NonNull Exception e) {
                if (progressBar.isShowing()) progressBar.dismiss();
                displayAlert("Payment failed", e.getMessage());
            }
        });

    }

    private void setStripePaymentApi(PaymentIntent paymentIntent) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("service_id", serviceId);
        params.put("payment_instant_id", paymentIntent.getId());
        params.put("payment_id", paymentIntent.getPaymentMethodId());
        params.put("amount", totalAmountToCharge);
        params.put("user_id", PrefsUtil.with(StripePaymentActivity.this).readString("UserId"));

        new WebServiceCall(StripePaymentActivity.this, WebServiceUrl.URL_STRIPE_PAYMENT, params, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (progressBar.isShowing()) progressBar.dismiss();
                        try {
                            if (status) {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("MESSAGE", "Payment success");
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            } else {
                                Intent resultIntent = new Intent();
                                setResult(Activity.RESULT_CANCELED, resultIntent);
                                finish();
                            }
                            Toast.makeText(StripePaymentActivity.this, ((CommonPojo) obj).getMessage(), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    @Override public void onAsync(Object obj) { getStripePaymentAsync = null; }
                    @Override public void onCancelled() { getStripePaymentAsync = null; }
                });
    }


    private void displayAlert(@NonNull String title, @Nullable String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("Ok", null).create().show();
    }


    private void getIntentData() {
        if (getIntent() != null) {
            if (getIntent().hasExtra("sub_total")) subTotal = getIntent().getStringExtra("sub_total");
            if (getIntent().hasExtra("fees")) fees = getIntent().getStringExtra("fees");
            if (getIntent().hasExtra("booking_amount")) bookingAmount = getIntent().getStringExtra("booking_amount");
            if (getIntent().hasExtra("customer_commission_wallet")) customerCommission = getIntent().getStringExtra("customer_commission_wallet");
            if (getIntent().hasExtra("provider_commission")) providerCommission = getIntent().getStringExtra("provider_commission");
            if (getIntent().hasExtra("total_amount_to_charge")) totalAmountToCharge = getIntent().getStringExtra("total_amount_to_charge");
            if (getIntent().hasExtra("total_amount_to_charge_full")) totalAmountToChargeFull = getIntent().getStringExtra("total_amount_to_charge_full");
            if (getIntent().hasExtra("paymentIntentClientSecret")) paymentIntentClientSecret = getIntent().getStringExtra("paymentIntentClientSecret");
            if (getIntent().hasExtra("serviceId")) serviceId = getIntent().getStringExtra("serviceId");
            if (getIntent().hasExtra("serviceMasterType")) serviceMasterType = getIntent().getStringExtra("serviceMasterType");
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        Utils.cancelAsyncTask(getStripePaymentAsync);
        super.onDestroy();
    }
}
