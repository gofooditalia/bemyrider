package com.app.bemyrider.activity.partner;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityProviderHomeBinding;
import com.app.bemyrider.fragment.partner.ProviderHomeFragment;
import com.app.bemyrider.fragment.partner.ProviderMenuFragment;
import com.app.bemyrider.fragment.partner.ProviderMessageFragment;
import com.app.bemyrider.fragment.partner.ProviderProfileFragment;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ProviderHomeActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    private static final String TAG = "ProviderHomeActivity";
    private Context mContext;
    private Activity mActivity;

    ActivityProviderHomeBinding binding;
    private BroadcastReceiver mMessageReceiver;
    private ConnectionManager connectionManager;
    private Menu menu;
    private boolean isFirst = true;
    boolean doubleBackToExitPressedOnce = false;
    
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Le notifiche sono disabilitate. Puoi attivarle dalle impostazioni.", Toast.LENGTH_LONG).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(ProviderHomeActivity.this, R.layout.activity_provider_home, null);
        mContext = binding.getRoot().getContext();
        mActivity = ProviderHomeActivity.this;

        initView();
        checkAndRequestNotificationPermission();

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // EventBus events are handled via @Subscribe methods
            }
        };
    }
    
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void initView() {
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        binding.bottomNavigationView.setOnItemSelectedListener(this);
        menu = binding.bottomNavigationView.getMenu();

        displaySelectedScreen(R.id.nav_home_p);
    }

    @Override
    protected void onResume() {
        registerReceiver(mMessageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        super.onResume();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("provider_edit_profile")) {
                if (event.getMessage().equalsIgnoreCase("refresh")) {
                    isFirst = false;
                    Fragment fragment = new ProviderProfileFragment();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, fragment);
                    binding.bottomNavigationView.getMenu().findItem(R.id.nav_profile_p).setChecked(true);
                    ft.commitAllowingStateLoss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        displaySelectedScreen(menuItem.getItemId());
        return true;
    }

    private void displaySelectedScreen(int itemId) {
        Fragment fragment = null;
        if (itemId == R.id.nav_home_p) {
            isFirst = true;
            fragment = new ProviderHomeFragment();
        } else if (itemId == R.id.nav_message_p) {
            isFirst = false;
            fragment = new ProviderMessageFragment();
        } else if (itemId == R.id.nav_profile_p) {
            isFirst = false;
            fragment = new ProviderProfileFragment();
        } else if (itemId == R.id.nav_menu_p) {
            isFirst = false;
            fragment = new ProviderMenuFragment();
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

    }

    @Override
    public void onBackPressed() {
        if (isFirst) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            ToastMaster.showShort(mContext, R.string.back_press_msg);
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        } else {
            isFirst = true;
            Fragment fragment = new ProviderHomeFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            binding.bottomNavigationView.getMenu().findItem(R.id.nav_home_p).setChecked(true);
            ft.commit();
        }
    }
}
