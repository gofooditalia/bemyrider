package com.app.bemyrider.activity.user;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.util.Linkify;

import com.app.bemyrider.databinding.ActivityUserProfileBinding;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;

import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;

/**
 * Created by nct121 on 5/12/16.
 * Modified by Hardik Talaviya on 9/12/19.
 */

public class UserProfileActivity extends AppCompatActivity {

    private ActivityUserProfileBinding binding;
    private ProfileItem profileData;
    private AsyncTask profileDataAsync;
    private ConnectionManager connectionManager;
    private Context context;
    private Activity activity;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = UserProfileActivity.this;
        activity = UserProfileActivity.this;

        binding = DataBindingUtil.setContentView(activity, R.layout.activity_user_profile, null);

        init();

        if (getIntent().getStringExtra("userId") != null) {
            binding.TxtProfileNumber.setAutoLinkMask(Linkify.PHONE_NUMBERS);
            binding.TxtProfileMail.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
        }

        if (new ConnectionCheck().isNetworkConnected(this)) {
            getProfileData();
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

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
        }*/

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
    protected void getProfileData() {
        binding.llUserProfileDetail.setVisibility(View.GONE);
        binding.linHeader.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        if (getIntent().getStringExtra("userId") != null) {
            textParams.put("profile_id", getIntent().getStringExtra("userId"));
        } else {
            textParams.put("profile_id", PrefsUtil.with(activity).readString("UserId"));
        }

        new WebServiceCall(this, WebServiceUrl.URL_PROFILE, textParams, ProfilePojo.class,
                false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.linHeader.setVisibility(View.VISIBLE);
                binding.llUserProfileDetail.setVisibility(View.VISIBLE);
                binding.linHeader.setVisibility(View.VISIBLE);
                if (status) {
                    profileData = ((ProfilePojo) obj).getData();
                    binding.TxtProfileNname.setText(profileData.getUserName());
                    binding.TxtProfileNumber.setText(profileData.getCountryCode() + " " + profileData.getContactMask());
                    binding.TxtProfileMail.setText(profileData.getEmailMask());
                    /*if (profileData.getPaymentMode().equalsIgnoreCase("w")) {
                        PrefsUtil.with(activity).write("PaymentMode", "w");
                        binding.TxtPaymethodName.setText(R.string.wallet);
                    } else if (profileData.getPaymentMode().equalsIgnoreCase("c")) {
                        PrefsUtil.with(activity).write("PaymentMode", "c");
                        binding.TxtPaymethodName.setText(R.string.cash);
                    }*/
                    if (!profileData.getAddress().equals("")) {
                        PrefsUtil.with(activity).write("customer_address", profileData.getAddress());
                        binding.TxtProfileAddress.setText(profileData.getAddress());
                    } else {
                        binding.TxtProfileAddress.setText("N/A");
                    }
                    if (profileData.getProfileImg() != null && profileData.getProfileImg().length() > 0) {
                        Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.imgProfile);
                        // Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.imgBack);
                    } else {
                        Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.imgProfile);
                        // Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.imgBack);
                    }

                    if (!profileData.getTaskAssigned().equals("")) {
                        binding.txtAssignedTasks.setText(profileData.getTaskAssigned());
                    } else {
                        binding.txtAssignedTasks.setText("N/A");
                    }

                    PrefsUtil.with(activity).write("fb_id", profileData.getFbId());
                    PrefsUtil.with(activity).write("g_id", profileData.getGmailId());
                    PrefsUtil.with(activity).write("ln_id", profileData.getLinkedinId());

                } else {
                    Toast.makeText(activity, (String) obj,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                profileDataAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                profileDataAsync = null;
            }
        });
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
            Log.e("Offline", "onMessageEvent: My Resolution");
            File f = new File(getFilesDir().getPath() + "/" + "offline.json");
            //check whether file exists
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

            binding.TxtProfileNname.setText(profileData.getUserName());
            binding.TxtProfileNumber.setText(String.format("%s %s", profileData.getCountryCode(), profileData.getContactNumber()));
            binding.TxtProfileMail.setText(profileData.getEmail());
            /*if (profileData.getPaymentMode().equalsIgnoreCase("w")) {
                PrefsUtil.with(activity).write("PaymentMode", "w");
                binding.TxtPaymethodName.setText(R.string.wallet);
            } else if (profileData.getPaymentMode().equalsIgnoreCase("c")) {
                PrefsUtil.with(activity).write("PaymentMode", "c");
                binding.TxtPaymethodName.setText(R.string.cash);

            }*/
            if (!profileData.getAddress().equals("")) {
                PrefsUtil.with(activity).write("customer_address", profileData.getAddress());
                binding.TxtProfileAddress.setText(profileData.getAddress());
            } else {
                binding.TxtProfileAddress.setText("N/A");
            }
            if (profileData.getProfileImg() != null && profileData.getProfileImg().length() > 0) {
                Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.imgProfile);
                // Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.imgBack);
            } else {
                Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.imgProfile);
                // Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.imgBack);
            }

            if (!profileData.getTaskAssigned().equals("")) {
                binding.txtAssignedTasks.setText(profileData.getTaskAssigned());
            } else {
                binding.txtAssignedTasks.setText("N/A");
            }

            PrefsUtil.with(activity).write("fb_id", profileData.getFbId());
            PrefsUtil.with(activity).write("g_id", profileData.getGmailId());
            PrefsUtil.with(activity).write("ln_id", profileData.getLinkedinId());
//            new ConnectionCheck().showDialogWithMessage(activity, getString(R.string.sync_data_message)).show();


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
        Utils.cancelAsyncTask(profileDataAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
