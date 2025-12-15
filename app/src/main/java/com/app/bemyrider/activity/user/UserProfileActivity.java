package com.app.bemyrider.activity.user;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by nct121 on 5/12/16.
 * Modified by Hardik Talaviya on 9/12/19.
 * Modernized by Gemini on 2024.
 */

public class UserProfileActivity extends AppCompatActivity {

    private ActivityUserProfileBinding binding;
    private ConnectionManager connectionManager;
    private Context context;
    private Activity activity;
    private UserProfileViewModel viewModel;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = UserProfileActivity.this;
        activity = UserProfileActivity.this;

        binding = DataBindingUtil.setContentView(activity, R.layout.activity_user_profile, null);
        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        init();

        if (getIntent().getStringExtra("userId") != null) {
            binding.TxtProfileNumber.setAutoLinkMask(Linkify.PHONE_NUMBERS);
            binding.TxtProfileMail.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
        }

        connectionManager = new ConnectionManager(context);
        
        // Verifica connessione usando il metodo statico corretto
        if (ConnectionManager.getConnectivityStatus(this) != ConnectionManager.TYPE_NOT_CONNECTED) {
            fetchProfileData();
        } else {
            getOfflineDetails();
        }

        binding.imgBack.setOnClickListener(v -> {
            onBackPressed();
        });

    }

    protected void init() {
        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // collapsed
                if (!binding.TxtProfileNname.getText().equals(""))
                    binding.txtHeaderName.setText(binding.TxtProfileNname.getText());
                else
                    binding.txtHeaderName.setText(getResources().getString(R.string.user_profile));

                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_bg_color));
                binding.txtHeaderName.setTextColor(getResources().getColor(R.color.white));
            } else {
                // expanded
                binding.txtHeaderName.setText("");
                binding.txtHeaderName.setTextColor(getResources().getColor(R.color.white));
                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*---------------- Profile Detail Api Call ---------------------*/
    protected void fetchProfileData() {
        binding.llUserProfileDetail.setVisibility(View.GONE);
        binding.linHeader.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        String profileId;
        if (getIntent().getStringExtra("userId") != null) {
            profileId = getIntent().getStringExtra("userId");
        } else {
            profileId = PrefsUtil.with(activity).readString("UserId");
        }

        viewModel.getProfile(profileId).observe(this, profilePojo -> {
            binding.progress.setVisibility(View.GONE);
            binding.linHeader.setVisibility(View.VISIBLE);
            binding.llUserProfileDetail.setVisibility(View.VISIBLE);
            
            if (profilePojo != null && profilePojo.isStatus()) {
                updateUI(profilePojo.getData());
            } else {
                Toast.makeText(activity, profilePojo != null ? "Errore caricamento profilo" : getString(R.string.server_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI(ProfileItem profileData) {
        binding.TxtProfileNname.setText(profileData.getUserName());
        binding.TxtProfileNumber.setText(profileData.getCountryCode() + " " + profileData.getContactMask());
        binding.TxtProfileMail.setText(profileData.getEmailMask());

        if (!profileData.getAddress().equals("")) {
            PrefsUtil.with(activity).write("customer_address", profileData.getAddress());
            binding.TxtProfileAddress.setText(profileData.getAddress());
        } else {
            binding.TxtProfileAddress.setText("N/A");
        }
        if (profileData.getProfileImg() != null && profileData.getProfileImg().length() > 0) {
            Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.imgProfile);
        } else {
            Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.imgProfile);
        }

        if (!profileData.getTaskAssigned().equals("")) {
            binding.txtAssignedTasks.setText(profileData.getTaskAssigned());
        } else {
            binding.txtAssignedTasks.setText("N/A");
        }

        PrefsUtil.with(activity).write("fb_id", profileData.getFbId());
        PrefsUtil.with(activity).write("g_id", profileData.getGmailId());
        PrefsUtil.with(activity).write("ln_id", profileData.getLinkedinId());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    getOfflineDetails();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getOfflineDetails() {
        try {
            binding.progress.setVisibility(View.GONE);
            binding.linHeader.setVisibility(View.VISIBLE);
            binding.llUserProfileDetail.setVisibility(View.VISIBLE);
            
            File f = new File(getFilesDir().getPath() + "/" + "offline.json");
            //check whether file exists
            if (!f.exists()) return;
            
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String s = new String(buffer);
            JSONObject object = new JSONObject(s);
            JSONObject dataObj = object.getJSONObject("data");
            JSONObject serviceList = dataObj.getJSONObject("profileData");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
            Gson gson = gsonBuilder.create();
            ProfileItem profileData = gson.fromJson(serviceList.toString(), ProfileItem.class);

            // Usa lo stesso metodo di aggiornamento UI per consistenza
            // Nota: L'oggetto ProfileItem offline potrebbe avere campi diversi (es. ContactNumber invece di ContactMask)
            // Quindi replico la logica offline specifica qui sotto
            
            binding.TxtProfileNname.setText(profileData.getUserName());
            binding.TxtProfileNumber.setText(String.format("%s %s", profileData.getCountryCode(), profileData.getContactNumber()));
            binding.TxtProfileMail.setText(profileData.getEmail());
            
            if (!profileData.getAddress().equals("")) {
                PrefsUtil.with(activity).write("customer_address", profileData.getAddress());
                binding.TxtProfileAddress.setText(profileData.getAddress());
            } else {
                binding.TxtProfileAddress.setText("N/A");
            }
            if (profileData.getProfileImg() != null && profileData.getProfileImg().length() > 0) {
                Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.imgProfile);
            } else {
                Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.imgProfile);
            }

            if (!profileData.getTaskAssigned().equals("")) {
                binding.txtAssignedTasks.setText(profileData.getTaskAssigned());
            } else {
                binding.txtAssignedTasks.setText("N/A");
            }

            PrefsUtil.with(activity).write("fb_id", profileData.getFbId());
            PrefsUtil.with(activity).write("g_id", profileData.getGmailId());
            PrefsUtil.with(activity).write("ln_id", profileData.getLinkedinId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
