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
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.app.bemyrider.Adapter.Partner.DrawerItemCustomAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.PartnerActivityProfileBinding;
import com.app.bemyrider.fragment.partner.JobBoardFragment;
import com.app.bemyrider.fragment.partner.ProviderProfileFragment; // Assunto che esista
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
            }
        };

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(binding.leftDrawer)) {
                    binding.drawerLayout.closeDrawer(binding.leftDrawer);
                } else {
                    new AlertDialog.Builder(DrawerActivity.this)
                            .setMessage(getString(R.string.sure_exit_app))
                            .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> finish())
                            .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel())
                            .show();
                }
            }
        });
        
        // Default fragment
        displaySelectedScreen(0);
    }

    private void initView() {
        setUpToolBar();

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                if (!binding.txtUsername.getText().toString().equals(""))
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
        ModelForDrawer[] drawerItem = new ModelForDrawer[15];
        drawerItem[0] = new ModelForDrawer(R.mipmap.ic_profile_drawer, getString(R.string.profile));
        drawerItem[1] = new ModelForDrawer(R.mipmap.ic_search_drawer, getString(R.string.job_board)); // Nuovo
        drawerItem[2] = new ModelForDrawer(R.mipmap.ic_service_request, getString(R.string.service_request));
        drawerItem[3] = new ModelForDrawer(R.mipmap.ic_financial_information, getString(R.string.financial_info));
        drawerItem[4] = new ModelForDrawer(R.mipmap.ic_my_services, getString(R.string.my_services));
        drawerItem[5] = new ModelForDrawer(R.mipmap.ic_messages_drawer, getString(R.string.message));
        drawerItem[6] = new ModelForDrawer(R.mipmap.ic_notification_drawer, getString(R.string.notifications));
        drawerItem[7] = new ModelForDrawer(R.mipmap.ic_resolution_center_drawer, getString(R.string.dispute_list));
        drawerItem[8] = new ModelForDrawer(R.mipmap.ic_payment_history, getString(R.string.payment_history));
        drawerItem[9] = new ModelForDrawer(R.mipmap.ic_wallet_drawer, getString(R.string.wallet));
        drawerItem[10] = new ModelForDrawer(R.mipmap.ic_account_settings, getString(R.string.account_settings));
        drawerItem[11] = new ModelForDrawer(R.mipmap.ic_feedback, getString(R.string.feedback));
        drawerItem[12] = new ModelForDrawer(R.mipmap.ic_contact_us, getString(R.string.comtact_us));
        drawerItem[13] = new ModelForDrawer(R.mipmap.ic_info_drawer, getString(R.string.info));
        drawerItem[14] = new ModelForDrawer(R.mipmap.ic_logout, getString(R.string.logout));

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
                binding.contentFramePartner.setTranslationX(slideOffset * drawerView.getWidth());
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
            displaySelectedScreen(position);
            binding.drawerLayout.closeDrawers();
        }
    }

    private void displaySelectedScreen(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                // fragment = new ProviderProfileFragment(); // Se esiste
                break;
            case 1:
                fragment = new JobBoardFragment();
                break;
            // Altri casi da implementare...
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame_partner, fragment);
            ft.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
    public void onMessageEvent(MessageEvent ignoredEvent) {
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}