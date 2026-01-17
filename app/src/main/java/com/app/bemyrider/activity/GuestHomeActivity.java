package com.app.bemyrider.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.bemyrider.Adapter.MenuItemAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.R;
import com.app.bemyrider.activity.partner.ProviderHomeActivity;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.databinding.ActivityGuestHomeBinding;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.myinterfaces.MenuItemClickListener;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

public class GuestHomeActivity extends AppCompatActivity implements MenuItemClickListener {

    private static final String TAG = "GuestHomeActivity";
    private ActivityGuestHomeBinding binding;
    private ConnectionManager connectionManager;
    private boolean doubleBackToExitPressedOnce = false;
    private BroadcastReceiver mMessageReceiver;

    private GuestHomeFilterPassData homeFilterPassData;
    ActivityResultLauncher<Intent> myIntentActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_guest_home);
        Context context = this;
        Activity activity = this;

        if (getIntent().hasExtra("from")) {
            if ("intro".equals(getIntent().getStringExtra("from"))) {
                startActivity(new Intent(activity, SignupActivity.class));
            }
        }

        // Redirect logged-in users
        String userId = PrefsUtil.with(activity).readString("UserId");
        if (userId != null && !userId.isEmpty()) {
            if ("c".equalsIgnoreCase(PrefsUtil.with(activity).readString("UserType"))) {
                startActivity(new Intent(activity, CustomerHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            } else {
                startActivity(new Intent(activity, ProviderHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
            finish();
            return;
        }

        init(context);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (new ConnectionCheck().isNetworkConnected(context)) {
                    Log.d(TAG, "Network connected");
                    EventBus.getDefault().post(new MessageEvent("connection", "connected"));
                } else {
                    Log.d(TAG, "Network disconnected");
                    EventBus.getDefault().post(new MessageEvent("connection", "disconnected"));
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finish();
                    return;
                }
                doubleBackToExitPressedOnce = true;
                Toast.makeText(GuestHomeActivity.this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        });
    }

    private void init(Context context) {
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        setSupportActionBar(binding.toolbar);
        setUpMenuItems(context);
        setupActivityResultLauncher();
    }

    void setUpMenuItems(Context context) {
        ModelForDrawer[] drawerItem = {
                new ModelForDrawer(R.drawable.ic_contact_us_menu, getString(R.string.comtact_us)),
                new ModelForDrawer(R.drawable.ic_info_menu, getString(R.string.info)),
                new ModelForDrawer(R.drawable.ic_logout_menu, getString(R.string.login_menu))
        };

        binding.recGuestMenu.setLayoutManager(new LinearLayoutManager(context));
        binding.recGuestMenu.setItemAnimator(new DefaultItemAnimator());

        MenuItemAdapter adapter = new MenuItemAdapter(context, this, this, drawerItem);
        binding.recGuestMenu.setAdapter(adapter);
    }

    @Override
    public void onMenuItemClick(ModelForDrawer modelForDrawer, int position) {
        switch (position) {
            case 0:
                startActivity(new Intent(this, ContactUsActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, InfoPageActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            registerReceiver(mMessageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        } catch (Exception e) {
            Log.e(TAG, "Error registering receiver", e);
        }
    }

    @Override
    public void onDestroy() {
        if (connectionManager != null) {
            try {
                connectionManager.unregisterReceiver();
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering connection manager receiver", e);
            }
        }
        if (mMessageReceiver != null) {
            try {
                unregisterReceiver(mMessageReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering message receiver", e);
            }
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }

    private void setupActivityResultLauncher() {
        myIntentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                String address = data.getStringExtra("address");
                String latitude = data.getStringExtra("latitude");
                String longitude = data.getStringExtra("longitude");
                String strAsc = data.getStringExtra("strAsc");
                String strDesc = data.getStringExtra("strDesc");
                String strSearch = data.getStringExtra("searchKeyWord");
                String strRating = data.getStringExtra("rating");

                if (homeFilterPassData != null) {
                    homeFilterPassData.onFilterChanged(address, latitude, longitude, strAsc, strDesc, strSearch, strRating);
                }
            }
        });
    }

    public void setOnHomeFilterData(GuestHomeFilterPassData homeFilterPassData) {
        this.homeFilterPassData = homeFilterPassData;
    }

    public interface GuestHomeFilterPassData {
        void onFilterChanged(String address, String latitude, String longitude, String strAsc, String strDesc, String strSearch, String strRating);
    }
}
