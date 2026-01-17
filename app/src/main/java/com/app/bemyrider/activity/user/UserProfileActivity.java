package com.app.bemyrider.activity.user;

import android.content.Context;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityUserProfileBinding;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.viewmodel.UserProfileViewModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

import coil.Coil;
import coil.request.ImageRequest;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    private ActivityUserProfileBinding binding;
    private ConnectionManager connectionManager;
    private UserProfileViewModel viewModel;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile);
        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        init();

        if (getIntent().getStringExtra("userId") != null) {
            binding.TxtProfileNumber.setAutoLinkMask(Linkify.PHONE_NUMBERS);
            binding.TxtProfileMail.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
        }

        if (ConnectionManager.getConnectivityStatus(this) != ConnectionManager.TYPE_NOT_CONNECTED) {
            fetchProfileData();
        } else {
            getOfflineDetails();
        }

        binding.imgBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    protected void init() {
        connectionManager = new ConnectionManager(this);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(this);

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // collapsed
                CharSequence profileName = binding.TxtProfileNname.getText();
                binding.txtHeaderName.setText(!profileName.toString().isEmpty() ? profileName : getString(R.string.user_profile));
                binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbar_bg_color));
                binding.txtHeaderName.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                // expanded
                binding.txtHeaderName.setText("");
                binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void fetchProfileData() {
        binding.llUserProfileDetail.setVisibility(View.GONE);
        binding.linHeader.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        String profileId = getIntent().getStringExtra("userId");
        if (profileId == null) {
            profileId = PrefsUtil.with(this).readString("UserId");
        }

        viewModel.getProfile(profileId).observe(this, profilePojo -> {
            binding.progress.setVisibility(View.GONE);
            binding.linHeader.setVisibility(View.VISIBLE);
            binding.llUserProfileDetail.setVisibility(View.VISIBLE);
            
            if (profilePojo != null && profilePojo.isStatus() && profilePojo.getData() != null) {
                updateUI(profilePojo.getData());
            } else {
                String errorMsg = profilePojo != null ? "Error loading profile" : getString(R.string.server_error);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI(ProfileItem profileData) {
        if (profileData == null) return;
        
        binding.TxtProfileNname.setText(profileData.getUserName());
        binding.TxtProfileNumber.setText(String.format("%s %s", profileData.getCountryCode(), profileData.getContactMask()));
        binding.TxtProfileMail.setText(profileData.getEmailMask());

        binding.TxtProfileAddress.setText(!profileData.getAddress().isEmpty() ? profileData.getAddress() : "N/A");

        ImageRequest.Builder requestBuilder = new ImageRequest.Builder(this)
                .placeholder(R.drawable.loading)
                .error(R.mipmap.user)
                .target(binding.imgProfile);

        if (profileData.getProfileImg() != null && !profileData.getProfileImg().isEmpty()) {
            requestBuilder.data(profileData.getProfileImg());
        } else {
            requestBuilder.data(R.mipmap.user);
        }
        Coil.imageLoader(this).enqueue(requestBuilder.build());

        binding.txtAssignedTasks.setText(!profileData.getTaskAssigned().isEmpty() ? profileData.getTaskAssigned() : "N/A");

        PrefsUtil.with(this).write("fb_id", profileData.getFbId());
        PrefsUtil.with(this).write("g_id", profileData.getGmailId());
        PrefsUtil.with(this).write("ln_id", profileData.getLinkedinId());
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        if(EventBus.getDefault().isRegistered(this)) {
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
            
            File f = new File(getFilesDir().getPath() + "/offline.json");
            if (!f.exists()) return;
            
            try (FileInputStream is = new FileInputStream(f)) {
                byte[] buffer = new byte[is.available()];
                if (is.read(buffer) <= 0) {
                    Log.w(TAG, "Offline file is empty.");
                    return;
                }
                String s = new String(buffer);
                JSONObject object = new JSONObject(s);
                JSONObject dataObj = object.getJSONObject("data");
                JSONObject serviceList = dataObj.getJSONObject("profileData");
                Gson gson = new GsonBuilder().setDateFormat("M/d/yy hh:mm a").create();
                ProfileItem offlineProfileData = gson.fromJson(serviceList.toString(), ProfileItem.class);
                updateUI(offlineProfileData);
            }
        } catch (IOException | org.json.JSONException e) {
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
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
