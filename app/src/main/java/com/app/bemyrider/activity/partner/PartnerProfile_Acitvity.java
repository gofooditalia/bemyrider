package com.app.bemyrider.activity.partner;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.text.HtmlCompat;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Objects;

import coil.Coil;
import coil.request.ImageRequest;

public class PartnerProfile_Acitvity extends AppCompatActivity {

    private static final String TAG = "PartnerProfileActivity";
    private ImageView img_profile;
    private TextView txt_username, txt_usercontactno, txt_useremail, txt_servicetime,
            txt_rating, txt_tota_review, txt_viewall, txt_about_user, txt_available_days, txt_address_pro, txt_worked_on;
    private final Context mContext = this;
    private SwitchCompat switch_available_now;
    private WebServiceCall availableStatusAsync, getProfileAsync; // MODIFICA QUI: Da AsyncTask a WebServiceCall
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.partner_activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.provider_profile), HtmlCompat.FROM_HTML_MODE_LEGACY));
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initView();
        serviceCall();

        switch_available_now.setOnCheckedChangeListener((compoundButton, b) -> serviceCallChangeStatus(b ? "y" : "n"));

        findViewById(R.id.fab_edit).setOnClickListener(v -> Toast.makeText(mContext, "Edit profile is not available.", Toast.LENGTH_SHORT).show());

        txt_viewall.setOnClickListener(v -> startActivity(new Intent(mContext, PartnerReviewsActivity.class)));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void serviceCallChangeStatus(final String switchstatus) {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("user_id", PrefsUtil.with(mContext).readString("UserId"));
        textParams.put("isAvailable", switchstatus);

        availableStatusAsync = new WebServiceCall(this, WebServiceUrl.URL_AVAILABLE_NOW,
                textParams, CommonPojo.class, true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    switch_available_now.setChecked("y".equals(switchstatus));
                }
            }
            @Override public void onAsync(AsyncTask asyncTask) { availableStatusAsync = null; }
            @Override public void onCancelled() { availableStatusAsync = null; }
        });
    }

    private void serviceCall() {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("profile_id", PrefsUtil.with(mContext).readString("UserId"));

        getProfileAsync = new WebServiceCall(mContext, WebServiceUrl.URL_PROFILE, textParams, ProfilePojo.class,
                true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (!status) {
                    Toast.makeText(mContext, Objects.toString(obj, getString(R.string.server_error)), Toast.LENGTH_SHORT).show();
                    return;
                }
                ProfilePojo response_profile = (ProfilePojo) obj;
                if (response_profile.getData() == null) {
                    Toast.makeText(mContext, R.string.profile_loading_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                updateUI(response_profile.getData());
            }
            @Override public void onAsync(AsyncTask asyncTask) { getProfileAsync = null; }
            @Override public void onCancelled() { getProfileAsync = null; }
        });
    }

    private void updateUI(@NonNull ProfileItem data) {
        switch_available_now.setChecked("y".equals(data.getIsAvailable()));

        findViewById(R.id.img_verify_facebook).setVisibility(View.GONE);
        findViewById(R.id.img_verify_gmail).setVisibility(View.GONE);
        findViewById(R.id.img_verify_linkedin).setVisibility(View.GONE);

        ImageRequest.Builder profileBuilder = new ImageRequest.Builder(mContext)
                .placeholder(R.drawable.loading).error(R.mipmap.user).target(img_profile);
        if (data.getProfileImg() != null && !data.getProfileImg().isEmpty()) {
            profileBuilder.data(data.getProfileImg());
        } else {
            profileBuilder.data(R.mipmap.user);
        }
        Coil.imageLoader(mContext).enqueue(profileBuilder.build());

        txt_useremail.setText(data.getEmail());
        txt_tota_review.setText(String.format(" %s", data.getTotalReview()));
        txt_username.setText(data.getUserName());
        txt_usercontactno.setText(String.format("%s %s", data.getCountryCode(), data.getContactNumber()));
        txt_worked_on.setText(data.getTaskAssigned());
        txt_viewall.setVisibility("0".equals(data.getTotalReview()) ? View.GONE : View.VISIBLE);
        txt_rating.setText(data.getPositiveRating().isEmpty() ? "0 " : data.getStartRating() + " ");
        txt_about_user.setText(!data.getDescription().isEmpty() ? data.getDescription() : "N/A");
        txt_servicetime.setText(!data.getAvailableTimeStart().isEmpty() ? data.getAvailableTimeStart() + " - " + data.getAvailableTimeEnd() : "N/A");
        txt_available_days.setText(!data.getAvailableDays().isEmpty() ? data.getAvailableDaysList() : "N/A");
        txt_address_pro.setText(!data.getAddress().isEmpty() ? data.getAddress() : "N/A");

        PrefsUtil.with(mContext).write("userAddress", data.getAddress());
        PrefsUtil.with(mContext).write("userContactno", data.getContactNumber());
        PrefsUtil.with(mContext).write("userEmail", data.getEmail());
        PrefsUtil.with(mContext).write("total_review", data.getTotalReview());
        PrefsUtil.with(mContext).write("total_rating", data.getPositiveRating());
        PrefsUtil.with(mContext).write("userAbout", data.getDescription());
        PrefsUtil.with(mContext).write("userfname", data.getFirstName());
        PrefsUtil.with(mContext).write("userlname", data.getLastName());
        PrefsUtil.with(mContext).write("start_time", data.getAvailableTimeStart());
        PrefsUtil.with(mContext).write("end_time", data.getAvailableTimeEnd());
        PrefsUtil.with(mContext).write("UserImg", data.getProfileImg());
        PrefsUtil.with(mContext).write("userlatitude", data.getLatitude());
        PrefsUtil.with(mContext).write("userlongitude", data.getLongitude());
        PrefsUtil.with(mContext).write("userAvalDay", data.getAvailableDays());
    }

    private void initView() {
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        txt_about_user = findViewById(R.id.txt_about_user);
        txt_username = findViewById(R.id.txt_username);
        txt_usercontactno = findViewById(R.id.txt_usercontactno);
        txt_useremail = findViewById(R.id.txt_useremail);
        txt_servicetime = findViewById(R.id.txt_servicetime);
        txt_rating = findViewById(R.id.txt_rating);
        txt_tota_review = findViewById(R.id.txt_tota_review);
        txt_viewall = findViewById(R.id.txt_viewall);
        txt_available_days = findViewById(R.id.txt_available_days);
        txt_address_pro = findViewById(R.id.txt_address_pro);
        txt_worked_on = findViewById(R.id.txt_worked_on);
        switch_available_now = findViewById(R.id.switch_available_now);
        img_profile = findViewById(R.id.img_profile);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (connectionManager != null) {
            try {
                connectionManager.unregisterReceiver();
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
        Utils.cancelAsyncTask(availableStatusAsync);
        Utils.cancelAsyncTask(getProfileAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
