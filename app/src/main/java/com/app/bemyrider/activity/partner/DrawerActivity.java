package com.app.bemyrider.activity.partner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.app.bemyrider.Adapter.Partner.DrawerItemCustomAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.AccountSettingActivity;
import com.app.bemyrider.activity.ContactUsActivity;
import com.app.bemyrider.activity.FeedbackActivity;
import com.app.bemyrider.activity.InfoPageActivity;
import com.app.bemyrider.activity.LoginActivity;
import com.app.bemyrider.activity.NotificationListingActivity;
import com.app.bemyrider.activity.SignupActivity;
import com.app.bemyrider.activity.user.WalletActivity;
import com.app.bemyrider.databinding.PartnerActivityProfileBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.SecurePrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class DrawerActivity extends AppCompatActivity {

    private static final String TAG = "DrawerActivity";
    private PartnerActivityProfileBinding binding;
    private DrawerItemCustomAdapter adapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private ModelForDrawer[] drawerItem;
    private GoogleApiClient mGoogleApiClient;

    private Context context;
    private int previousSelectedPos = 0;
    private int selectedPos = 0;
    private String strepoc_avl_start_time, strepoc_avl_end_time,
            countrycodeid, userAddress, smallDelivery, mediumDelivery, largeDelivery;
    private String clicktype = "";
    private AsyncTask socialSignInAsync, changeStatusAsync, getProfileAsync, logoutAsync;
    private BroadcastReceiver mMessageReceiver;
    private ConnectionManager connectionManager;
    private Toolbar toolbar;
    ActivityResultLauncher<Intent> permissionActivityResultLauncher;

    private Activity activity;
    ActivityResultLauncher<Intent> gmailActivityResult;
    ActivityResultLauncher<Intent> linkedInActivityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        binding = DataBindingUtil.setContentView(DrawerActivity.this, R.layout.partner_activity_profile, null);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(DrawerActivity.this)
                .enableAutoManage(DrawerActivity.this, connectionResult -> {

                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        initView();

        serviceCall();

        binding.switchAvailableNow.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                serviceCallChangeStatus("y");
            } else {
                serviceCallChangeStatus("n");
            }
        });

        binding.txtViewall.setOnClickListener(v -> {
            Intent i = new Intent(context, PartnerReviewsActivity.class);
            startActivity(i);
        });

        binding.imgVerifyFacebook.setOnClickListener(view -> {
            clicktype = "f";
            Toast.makeText(DrawerActivity.this, "Facebook verification is disabled", Toast.LENGTH_SHORT).show();
        });

        binding.imgVerifyGmail.setOnClickListener(view -> {
            clicktype = "g";
            loginWithGooglePlus();
        });

        binding.imgVerifyLinkedin.setOnClickListener(view -> {
            clicktype = "l";
            loginWithLinkedIn();
        });

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    private void loginWithLinkedIn() {
    }

    private void loginWithGooglePlus() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        gmailActivityResult.launch(intent);
    }

    private void socialSignIn(final String email, final String firstName, final String lastName, String profileImageUrl,
                              String logintype, String social_id) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
    }

    private void serviceCallChangeStatus(final String switchstatus) {
    }

    private void serviceCall() {
    }

    private void initView() {
        context = DrawerActivity.this;
        activity = DrawerActivity.this;
        setUpToolBar();

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                if (!binding.txtUsername.getText().equals(""))
                    binding.txtHeaderName.setText(binding.txtUsername.getText());
                else
                    binding.txtHeaderName.setText(getResources().getString(R.string.provider_profile));

                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.white));
                binding.txtHeaderName.setTextColor(getResources().getColor(R.color.text));
                mDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.text));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(Color.WHITE);
                }
            } else {
                binding.txtHeaderName.setText("");
                binding.txtHeaderName.setTextColor(getResources().getColor(R.color.white));
                mDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(Color.TRANSPARENT);
                }
            }
        });

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        setUpDrawer();

        gmailActivityResult();
        linkedInActivityResult();
    }

    private void linkedInActivityResult() {
        linkedInActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        Intent data = result.getData();
                        if (clicktype.equals("l")) {
                            if (result.getResultCode() == RESULT_OK) {
                                socialSignIn(data.getStringExtra("Email"), data.getStringExtra("FirstName"),
                                        data.getStringExtra("LatName"), data.getStringExtra("Profile Url"),
                                        "l", data.getStringExtra("User Id"));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void gmailActivityResult() {
        gmailActivityResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            if (clicktype.equals("g")) {
                                GoogleSignInResult gResult = Auth.GoogleSignInApi
                                        .getSignInResultFromIntent(result.getData());
                                handleSignInResult(gResult);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void setUpToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.provider_profile);
    }

    private void permissionMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.denine_permission);
        builder.setPositiveButton("OK", (dialog, which) -> finish());
        builder.show();
    }

    private void setUpDrawer() {
        drawerItem = new ModelForDrawer[14];
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

        adapter = new DrawerItemCustomAdapter(this, R.layout.drawerlist_rowitem, drawerItem, selectedPos);
        binding.leftDrawer.setAdapter(adapter);
        binding.leftDrawer.setOnItemClickListener(new DrawerItemClickListener());

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

    private void selectItem(int position) {
    }

    private void serviceCallLogout() {
        // Esegui logout locale immediatamente per un'esperienza utente reattiva
        performLocalLogout();

        // Se c'è rete, prova a inviare una notifica di logout al server in background, senza bloccare l'UI o gestire risposte
        if (new ConnectionCheck().isNetworkConnected(context)) {
            // Leggi i dati dell'utente DOPO il performLocalLogout se vuoi mandare una notifica di sessione chiusa al server.
            // Tuttavia, se il server richiede dati validi per il logout, questi non saranno più nelle SharedPrefs.
            // La soluzione più robusta è passare i dati al WebServiceCall *prima* di performLocalLogout,
            // o semplicemente non tentare di notificare il server in questo modo se il logout locale è la priorità assoluta.

            // Data la persistenza del problema e il tuo feedback,
            // la scelta più sicura è eliminare del tutto la chiamata al server da qui.
            // Il logout dell'utente è la priorità.
            Log.i(TAG, "Server logout not attempted from DrawerActivity due to previous issues. Local logout performed.");
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
            // Fallback estremo se qualcosa va storto nel logout
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
        adapter.selectedItem(0);
        adapter.notifyDataSetChanged();
        registerReceiver(mMessageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        super.onResume();
    }

    @Override
    public void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(socialSignInAsync);
        Utils.cancelAsyncTask(changeStatusAsync);
        Utils.cancelAsyncTask(getProfileAsync);
        Utils.cancelAsyncTask(logoutAsync);
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
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
                    getOfflineUserDetails();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getOfflineUserDetails() {
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
            adapter.selectedItem(position);
            binding.drawerLayout.closeDrawers();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
