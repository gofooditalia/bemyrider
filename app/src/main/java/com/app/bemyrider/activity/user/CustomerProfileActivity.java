package com.app.bemyrider.activity.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityCustomerProfileBinding;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
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
import java.util.Objects;

import coil.Coil;
import coil.request.ImageRequest;

public class CustomerProfileActivity extends AppCompatActivity {

    private static final String TAG = "CustomerProfile";
    private ActivityCustomerProfileBinding binding;

    private ProfileItem profileData;
    private WebServiceCall profileDataAsync;
    private ConnectionManager connectionManager;
    private Context context;
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_customer_profile);

        init();
        setupActivityLaunchers();

        if (new ConnectionCheck().isNetworkConnected(context)) {
            getProfileData();
        } else {
            getOfflineDetails();
        }

        binding.ImgEdit.setOnClickListener(v -> {
            if (profileData != null) {
                Intent i = new Intent(this, EditProfileActivity.class);
                i.putExtra("Edit", "true");
                i.putExtra("isFromEdit", true);
                editProfileLauncher.launch(i);
            } else {
                Toast.makeText(context, R.string.can_not_edit_now, Toast.LENGTH_LONG).show();
            }
        });

        binding.imgBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void setupActivityLaunchers() {
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        getProfileData();
                    }
                });
    }

    protected void init() {
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                CharSequence profileName = binding.TxtProfileNname.getText();
                binding.txtHeaderName.setText(!profileName.toString().isEmpty() ? profileName : getResources().getString(R.string.user_profile));
                binding.toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.toolbar_bg_color));
                binding.txtHeaderName.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                binding.txtHeaderName.setText("");
                binding.toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
            }
        });
    }

    protected void getProfileData() {
        binding.llUserProfileDetail.setVisibility(View.GONE);
        binding.linHeader.setVisibility(View.GONE);
        binding.ImgEdit.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("profile_id", PrefsUtil.with(this).readString("UserId"));

        profileDataAsync = new WebServiceCall(context, WebServiceUrl.URL_PROFILE, textParams, ProfilePojo.class,
                false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.linHeader.setVisibility(View.VISIBLE);
                binding.llUserProfileDetail.setVisibility(View.VISIBLE);
                binding.ImgEdit.setVisibility(View.VISIBLE);

                if (!status) {
                    String errorMessage = (obj instanceof String) ? (String) obj : getString(R.string.server_error);
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                ProfilePojo profilePojo = (ProfilePojo) obj;
                if(profilePojo.getData() == null) return;
                
                profileData = profilePojo.getData();
                EditProfileActivity.profileData = profileData;

                updateUI(profileData);
            }

            @Override
            public void onAsync(Object asyncTask) {
                profileDataAsync = null;
            }

            @Override
            public void onCancelled() {
                profileDataAsync = null;
            }
        });
    }

    private void updateUI(ProfileItem data) {
        if (data == null) return;

        binding.TxtProfileNname.setText(data.getUserName());
        binding.TxtProfileNumber.setText(String.format("%s %s", data.getCountryCode(), data.getContactNumber()));
        binding.TxtProfileMail.setText(data.getEmail());

        binding.TxtProfileAddress.setText(!data.getAddress().isEmpty() ? data.getAddress() : "N/A");
        binding.txtAssignedTasks.setText(!data.getTaskAssigned().isEmpty() ? data.getTaskAssigned() : "N/A");
        binding.txtCompany.setText(!data.getCompanyName().isEmpty() ? data.getCompanyName() : "N/A");
        binding.txtCityOfCompany.setText(!data.getCity_of_company().isEmpty() ? data.getCity_of_company() : "N/A");
        binding.txtVat.setText(!data.getVat().isEmpty() ? data.getVat() : "N/A");
        binding.txtInvoiceRecipient.setText(!data.getReceiptCode().isEmpty() ? data.getReceiptCode() : "N/A");
        binding.txtCertifiedEmail.setText(!data.getCertifiedEmail().isEmpty() ? data.getCertifiedEmail() : "N/A");

        ImageRequest.Builder profileBuilder = new ImageRequest.Builder(context)
                .placeholder(R.drawable.loading)
                .error(R.mipmap.user)
                .target(binding.imgProfile);

        if (data.getProfileImg() != null && !data.getProfileImg().isEmpty()) {
            profileBuilder.data(data.getProfileImg());
        } else {
            profileBuilder.data(R.mipmap.user);
        }
        Coil.imageLoader(context).enqueue(profileBuilder.build());

        PrefsUtil.with(this).write("UserName", data.getUserName());
        PrefsUtil.with(this).write("customer_address", data.getAddress());
        PrefsUtil.with(this).write("login_cust_address", data.getAddress());
        PrefsUtil.with(this).write("UserImg", data.getProfileImg());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (Objects.equals(event.getType(), "connection") && Objects.equals(event.getMessage(), "disconnected")) {
                getOfflineDetails();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling message event", e);
        }
    }

    private void getOfflineDetails() {
        try {
            binding.progress.setVisibility(View.GONE);
            binding.linHeader.setVisibility(View.VISIBLE);
            binding.llUserProfileDetail.setVisibility(View.VISIBLE);
            binding.ImgEdit.setVisibility(View.VISIBLE);
            Log.d(TAG, "Loading offline profile data.");
            java.io.File f = new java.io.File(getFilesDir().getPath() + "/" + "offline.json");
            if (!f.exists()) return;

            try (FileInputStream is = new FileInputStream(f)) {
                byte[] buffer = new byte[is.available()];
                if (is.read(buffer) <= 0) return;
                String s = new String(buffer);
                JSONObject object = new JSONObject(s);
                JSONObject dataObj = object.getJSONObject("data");
                JSONObject serviceList = dataObj.getJSONObject("profileData");
                Gson gson = new GsonBuilder().setDateFormat("M/d/yy hh:mm a").create();
                ProfileItem offlineProfileData = gson.fromJson(serviceList.toString(), ProfileItem.class);
                updateUI(offlineProfileData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading offline details", e);
        }
    }

    @Override
    protected void onDestroy() {
        if (connectionManager != null) {
            try {
                connectionManager.unregisterReceiver();
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering connection manager", e);
            }
        }
        Utils.cancelAsyncTask(profileDataAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}