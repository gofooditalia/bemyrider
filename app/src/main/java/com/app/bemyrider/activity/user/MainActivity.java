package com.app.bemyrider.activity.user;

import static com.app.bemyrider.utils.Utils.PROVIDER_ID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.bemyrider.Adapter.User.DrawerItemCustomAdapter;
import com.app.bemyrider.Adapter.User.SelectCategoryAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.AccountSettingActivity;
import com.app.bemyrider.activity.ContactUsActivity;
import com.app.bemyrider.activity.FeedbackActivity;
import com.app.bemyrider.activity.InfoPageActivity;
import com.app.bemyrider.activity.NotificationListingActivity;
import com.app.bemyrider.activity.LoginActivity;
import com.app.bemyrider.activity.user.SearchServiceActivity;
import com.app.bemyrider.activity.user.FavouriteServicesActivity;
import com.app.bemyrider.activity.user.MessagesActivity;
import com.app.bemyrider.activity.user.PaymentHistoryActivity;
import com.app.bemyrider.activity.user.ServiceHistoryActivity;
import com.app.bemyrider.activity.user.UserProfileActivity;
import com.app.bemyrider.activity.user.WalletActivity;
import com.app.bemyrider.databinding.ActivityMainBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.model.user.CategoryDataItem;
import com.app.bemyrider.model.user.CategoryListPOJO;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.SecurePrefsUtil;
import com.app.bemyrider.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private Toolbar toolbar;
    private DrawerItemCustomAdapter adapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private ModelForDrawer[] drawerItem;
    private BroadcastReceiver mMessageReceiver;
    private int selectedPos = 0;
    private ArrayList<CategoryDataItem> categoryDataItems = new ArrayList<>();
    private SelectCategoryAdapter categoryAdapter;
    private WebServiceCall userLogoutAsync, categoryListAsync;
    private Context context;
    private Activity activity;
    private ConnectionManager connectionManager;


    private String providerId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);

        if (getIntent().hasExtra(PROVIDER_ID)) {
            if (getIntent().getStringExtra(PROVIDER_ID) != null
                    && getIntent().getStringExtra(PROVIDER_ID).length() > 0) {
                providerId = getIntent().getStringExtra(PROVIDER_ID);
            } else {
                finish();
                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            }
        } else {
            finish();
            Toast.makeText(context,
                    getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
        }

        init();

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (new ConnectionCheck().isNetworkConnected(context)) {
                    Log.e("HomeActivity", "connected");
                    EventBus.getDefault().post(new MessageEvent("connection", "connected"));
                } else {
//                    new ConnectionCheck().showDialogWithMessage(context, getString(R.string.sync_data_message)).show();
                    Log.e("HomeActivity", "disconnected");
                    EventBus.getDefault().post(new MessageEvent("connection", "disconnected"));
                }
            }
        };

        getCategory();
    }

    private boolean checkOS() {
        return (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    private void init() {
        context = MainActivity.this;
        activity = MainActivity.this;

        setUpToolBar();

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);


        categoryAdapter = new SelectCategoryAdapter(MainActivity.this, categoryDataItems, providerId);

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.rvCategories.setItemAnimator(new DefaultItemAnimator());
        binding.rvCategories.setAdapter(categoryAdapter);

        setUpDrawer();
    }

    private void permissionMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.denine_permission);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    private void setUpToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.category),HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    private void setUpDrawer() {
        // to hide drawer
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        drawerItem = new ModelForDrawer[15];
        drawerItem[0] = new ModelForDrawer(R.mipmap.ic_home_drawer, getString(R.string.home));
        drawerItem[1] = new ModelForDrawer(R.mipmap.ic_search_drawer, getString(R.string.search_service));
        drawerItem[2] = new ModelForDrawer(R.mipmap.ic_profile_drawer, getString(R.string.profile));
        drawerItem[3] = new ModelForDrawer(R.mipmap.ic_wallet_drawer, getString(R.string.wallet));
        drawerItem[4] = new ModelForDrawer(R.mipmap.ic_services, getString(R.string.services));
        drawerItem[5] = new ModelForDrawer(R.mipmap.ic_fav_services_drawer, getString(R.string.favorite_services));
        drawerItem[6] = new ModelForDrawer(R.mipmap.ic_messages_drawer, getString(R.string.message));
        drawerItem[7] = new ModelForDrawer(R.mipmap.ic_notification_drawer, getString(R.string.notifications));
        drawerItem[8] = new ModelForDrawer(R.mipmap.ic_resolution_center_drawer, getString(R.string.dispute_list));
        drawerItem[9] = new ModelForDrawer(R.mipmap.ic_payment_history, getString(R.string.payment_history));
        drawerItem[10] = new ModelForDrawer(R.mipmap.ic_account_settings, getString(R.string.account_settings));
        drawerItem[11] = new ModelForDrawer(R.mipmap.ic_feedback, getString(R.string.feedback));
        drawerItem[12] = new ModelForDrawer(R.mipmap.ic_contact_us, getString(R.string.comtact_us));
        drawerItem[13] = new ModelForDrawer(R.mipmap.ic_info_drawer, getString(R.string.info));
        drawerItem[14] = new ModelForDrawer(R.mipmap.ic_logout, getString(R.string.logout));

        adapter = new DrawerItemCustomAdapter(this, R.layout.drawerlist_rowitem, drawerItem, selectedPos);
        binding.leftDrawer.setAdapter(adapter);
        binding.leftDrawer.setOnItemClickListener(new DrawerItemClickListener());

        binding.drawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                break;
            case 1:
                startActivity(new Intent(MainActivity.this, SearchServiceActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 2:
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 3:
                startActivity(new Intent(MainActivity.this, WalletActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 4:
                startActivity(new Intent(MainActivity.this, ServiceHistoryActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 5:
                startActivity(new Intent(MainActivity.this, FavouriteServicesActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 6:
                startActivity(new Intent(MainActivity.this, MessagesActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 7:
                startActivity(new Intent(MainActivity.this, NotificationListingActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 8:
                startActivity(new Intent(MainActivity.this, DisputeListActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 9:
                startActivity(new Intent(MainActivity.this, PaymentHistoryActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 10:
                startActivity(new Intent(MainActivity.this, AccountSettingActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 11:
                startActivity(new Intent(MainActivity.this, FeedbackActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 12:
                startActivity(new Intent(MainActivity.this, ContactUsActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 13:
                startActivity(new Intent(MainActivity.this, InfoPageActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 14:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.sure_logout)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                serviceCallLogout();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).show();
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            selectedPos = position;
            setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + drawerItem[position].getName(),HtmlCompat.FROM_HTML_MODE_LEGACY));
            binding.drawerLayout.closeDrawer(binding.leftDrawer);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    private void getCategory() {
        binding.rvCategories.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("provider_id", providerId);

        new WebServiceCall(this, WebServiceUrl.URL_CATEGORYLIST, textParams,
                CategoryListPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.rvCategories.setVisibility(View.VISIBLE);
                if (status) {
                    CategoryListPOJO listPojo = (CategoryListPOJO) obj;
                    categoryDataItems.clear();
                    categoryDataItems.addAll(listPojo.getData());
                    categoryAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onAsync(Object obj) { categoryListAsync = null; }
            @Override public void onCancelled() { categoryListAsync = null; }
        });
    }

    private void serviceCallLogout() {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        SecurePrefsUtil securePrefs = SecurePrefsUtil.with(MainActivity.this);
        PrefsUtil prefsUtil = PrefsUtil.with(MainActivity.this);
        
        String userId = securePrefs.readString("UserId");
        if (userId == null || userId.isEmpty()) {
            userId = prefsUtil.readString("UserId");
        }
        
        String deviceToken = securePrefs.readString("device_token");
        if (deviceToken == null || deviceToken.isEmpty()) {
            deviceToken = prefsUtil.readString("device_token");
        }
        
        textParams.put("user_id", userId != null ? userId : "");
        textParams.put("device_token", deviceToken != null ? deviceToken : "");


        new WebServiceCall(MainActivity.this, WebServiceUrl.URL_LOGOUT, textParams,
                CommonPojo.class, true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                File offlineFile = new File(getFilesDir().getPath(), "/offline.json");
                if (offlineFile.exists()) {
                    Log.e(TAG, "Delete Offline File :: ");
                    offlineFile.delete();
                }
                
                SecurePrefsUtil.with(MainActivity.this).clearPrefs();
                PrefsUtil.with(MainActivity.this).clearPrefs();
                
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                
                if (!status) {
                    Toast.makeText(MainActivity.this, getString(R.string.logout), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(Object obj) { userLogoutAsync = null; }

            @Override
            public void onCancelled() {
                userLogoutAsync = null;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        adapter.selectedItem(0);
        adapter.notifyDataSetChanged();
        registerReceiver(mMessageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(userLogoutAsync);
        Utils.cancelAsyncTask(categoryListAsync);
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
            adapter.selectedItem(position);
            binding.drawerLayout.closeDrawer(binding.leftDrawer);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
