package com.app.bemyrider.activity.partner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.Adapter.Partner.DrawerItemCustomAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.R;
import com.app.bemyrider.activity.LoginActivity;
import com.app.bemyrider.databinding.PartnerActivityProfileBinding;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.SecurePrefsUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class DrawerActivity extends AppCompatActivity {

    private PartnerActivityProfileBinding binding;
    private ActionBarDrawerToggle mDrawerToggle;

    private BroadcastReceiver mMessageReceiver;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        binding = DataBindingUtil.setContentView(this, R.layout.partner_activity_profile);

        initView();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (new ConnectionCheck().isNetworkConnected(context)) {
                    Log.e("HomeActivity", "connected");
                    EventBus.getDefault().post(new MessageEvent("connection", "connected"));
                } else {
                    Log.e("HomeActivity", "disconnected");
                    EventBus.getDefault().post(new MessageEvent("connection", "disconnected"));
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(binding.leftDrawer)) {
                    binding.drawerLayout.closeDrawer(binding.leftDrawer);
                } else {
                    new AlertDialog.Builder(DrawerActivity.this)
                            .setMessage("Are you sure you want to exit app?")
                            .setPositiveButton("Yes", (dialogInterface, i) -> finish())
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                            .show();
                }
            }
        });
    }

    private void initView() {
        setUpToolBar();

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                if (!binding.txtUsername.getText().equals(""))
                    binding.txtHeaderName.setText(binding.txtUsername.getText());
                else
                    binding.txtHeaderName.setText(getResources().getString(R.string.provider_profile));

                binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                binding.txtHeaderName.setTextColor(ContextCompat.getColor(this, R.color.text));
                if (mDrawerToggle != null) {
                    mDrawerToggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.text));
                }

                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.WHITE);
            } else {
                binding.txtHeaderName.setText("");
                binding.txtHeaderName.setTextColor(ContextCompat.getColor(this, R.color.white));
                if (mDrawerToggle != null) {
                    mDrawerToggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.white));
                }

                binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        });

        connectionManager = new ConnectionManager(this);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(this);

        setUpDrawer();
    }

    private void setUpToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setUpDrawer() {
        ModelForDrawer[] drawerItem = new ModelForDrawer[14];
        drawerItem[0] = new ModelForDrawer(R.mipmap.ic_profile_drawer, getString(R.string.profile));
        drawerItem[1] = new ModelForDrawer(R.mipmap.ic_service_request, getString(R.string.service_request));
        drawerItem[2] = new ModelForDrawer(R.mipmap.ic_financial_information, getString(R.string.financial_info));
        drawerItem[3] = new ModelForDrawer(R.mipmap.ic_my_services, getString(R.string.my_services));
        drawerItem[4] = new ModelForDrawer(R.mipmap.ic_messages_drawer, getString(R.string.message));
        drawerItem[5] = new ModelForDrawer(R.mipmap.ic_notification_drawer, getString(R.string.notifications));
        drawerItem[6] = new ModelForDrawer(R.mipmap.ic_resolution_center_drawer, getString(R.string.dispute_list));
        drawerItem[7] = new ModelForDrawer(R.mipmap.ic_payment_history, getString(R.string.payment_history));
        drawerItem[8] = new ModelForDrawer(R.mipmap.ic_wallet_drawer, getString(R.string.wallet));
        drawerItem[9] = new ModelForDrawer(R.mipmap.ic_account_settings, getString(R.string.account_settings));
        drawerItem[10] = new ModelForDrawer(R.mipmap.ic_feedback, getString(R.string.feedback));
        drawerItem[11] = new ModelForDrawer(R.mipmap.ic_contact_us, getString(R.string.comtact_us));
        drawerItem[12] = new ModelForDrawer(R.mipmap.ic_info_drawer, getString(R.string.info));
        drawerItem[13] = new ModelForDrawer(R.mipmap.ic_logout, getString(R.string.logout));

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.drawerlist_rowitem, drawerItem, 0);
        binding.leftDrawer.setAdapter(adapter);
        binding.leftDrawer.setOnItemClickListener(new DrawerItemClickListener());

        Toolbar toolbar = findViewById(R.id.toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, toolbar, R.string.app_name,
                R.string.app_name) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (binding.contentFramePartner != null) {
                    binding.contentFramePartner.setTranslationX(slideOffset * drawerView.getWidth());
                }
                binding.drawerLayout.bringChildToFront(drawerView);
                binding.drawerLayout.setScrimColor(Color.TRANSPARENT);
                binding.drawerLayout.requestLayout();
            }
        };

        binding.drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Implement navigation logic here
            binding.drawerLayout.closeDrawers();
        }
    }

    private void performLocalLogout() {
        try {
            File offlineFile = new File(getFilesDir().getPath(), "/offline.json");
            if (offlineFile.exists()) {
                offlineFile.delete();
            }

            SecurePrefsUtil.with(DrawerActivity.this).clearPrefs();
            PrefsUtil.with(DrawerActivity.this).clearPrefs();

            Intent intent = new Intent(DrawerActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connectionManager != null) {
            connectionManager.unregisterReceiver();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    // Handle disconnected state
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
}
