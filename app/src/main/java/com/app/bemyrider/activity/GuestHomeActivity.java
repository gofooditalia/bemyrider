package com.app.bemyrider.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.app.bemyrider.Adapter.MenuItemAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.R;

import com.app.bemyrider.activity.partner.ProviderHomeActivity;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.databinding.ActivityGuestHomeBinding;
import com.app.bemyrider.fragment.GuestHomeFragment;
import com.app.bemyrider.fragment.GuestMenuFragment;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.myinterfaces.MenuItemClickListener;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;

import org.greenrobot.eventbus.EventBus;

public class GuestHomeActivity extends AppCompatActivity implements MenuItemClickListener/*implements BottomNavigationView.OnItemSelectedListener, GuestHomeFragment.GuestHomeFragmentListener*/ {

    private static final String TAG = "GuestHomeActivity";
    ActivityGuestHomeBinding binding;
    private Context context;
    private Activity activity;
    private ConnectionManager connectionManager;
    private boolean isFirst = true;
    boolean doubleBackToExitPressedOnce = false;
    private BroadcastReceiver mMessageReceiver;
    private Menu menu;
    private MenuItem navProfile;

    GuestHomeFilterPassData homeFilterPassData;
    GuestHomePositionData homePositionData;

    private String address = "", latitude = "", longitude = "", strAsc = "", strDesc = "", strSearch = "", strRating = "";
    ActivityResultLauncher<Intent> myIntentActivityResultLauncher;
    private ModelForDrawer[] drawerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(GuestHomeActivity.this, R.layout.activity_guest_home, null);
        context = GuestHomeActivity.this;
        activity = GuestHomeActivity.this;

        if (getIntent().hasExtra("from")) {
            if (getIntent().getStringExtra("from") != null && getIntent().getStringExtra("from").equals("intro")) {
                startActivity(new Intent(activity, SignupActivity.class));
            }
        }

        if (PrefsUtil.with(activity).readString("UserId") != null
                && PrefsUtil.with(activity).readString("UserId").length() > 0) {
            boolean isProfileCompleted = PrefsUtil.with(activity).readBoolean("isProfileCompleted");
            if (PrefsUtil.with(activity)
                    .readString("UserType").equalsIgnoreCase("c")) {
                Intent i = new Intent(activity, CustomerHomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            } else {
                Intent i = new Intent(activity, ProviderHomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
            finish();
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

    }

    private void init() {

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        setSupportActionBar(binding.toolbar);

        setUpMenuItems();

       /* binding.bottomNavigationView.setOnItemSelectedListener(this);
        menu = binding.bottomNavigationView.getMenu();

        displaySelectedScreen(R.id.nav_home_c);*/

        myActivityResult();

    }

    void setUpMenuItems() {
        drawerItem = new ModelForDrawer[3];
        drawerItem[0] = new ModelForDrawer(R.drawable.ic_contact_us_menu, getString(R.string.comtact_us));
        drawerItem[1] = new ModelForDrawer(R.drawable.ic_info_menu, getString(R.string.info));
        drawerItem[2] = new ModelForDrawer(R.drawable.ic_logout_menu, getString(R.string.login_menu));

        binding.recGuestMenu.setLayoutManager(new LinearLayoutManager(context));
        binding.recGuestMenu.setItemAnimator(new DefaultItemAnimator());

        MenuItemAdapter adapter = new MenuItemAdapter(context,activity, this,drawerItem);
        binding.recGuestMenu.setAdapter(adapter);
    }

    @Override
    public void onMenuItemClick(ModelForDrawer modelForDrawer, int position) {
        if (position == 0) {
            startActivity(new Intent(activity, ContactUsActivity.class));
        } else if (position == 1) {
            startActivity(new Intent(activity, InfoPageActivity.class));
        } else if (position == 2) {
            startActivity(new Intent(activity, LoginActivity.class));
        }
    }
    //keval.sakariya@ncrypted.com
    //mayur.maheriya@ncrypted.com

    @Override
    protected void onResume() {
        try {
            registerReceiver(mMessageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }

    /*@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        displaySelectedScreen(menuItem.getItemId());
        return true;
    }*/

    private void displaySelectedScreen(int itemId) {
        //creating fragment object
        Fragment fragment = null;
        //initializing the fragment object which is selected
        if (itemId == R.id.nav_home_c) {
            isFirst = true;
            fragment = new GuestHomeFragment();
        } else if (itemId == R.id.nav_menu_c) {
            isFirst = false;
            fragment = new GuestMenuFragment();
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

    }

    private void myActivityResult() {
        myIntentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                try {
                    address = result.getData().getStringExtra("address");
                    latitude = result.getData().getStringExtra("latitude");
                    longitude = result.getData().getStringExtra("longitude");
                    strAsc = result.getData().getStringExtra("strAsc");
                    strDesc = result.getData().getStringExtra("strDesc");
                    strSearch = result.getData().getStringExtra("searchKeyWord");
                    strRating = result.getData().getStringExtra("rating");
                    homeFilterPassData.onFilterChanged(address, latitude, longitude, strAsc, strDesc, strSearch, strRating);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setOnHomeFilterData(GuestHomeFilterPassData homeFilterPassData) {
        this.homeFilterPassData = homeFilterPassData;
    }

    public void setOnHomePositionData(GuestHomePositionData homePositionData) {
        this.homePositionData = homePositionData;
    }

    /*@Override
    public void onFilterClick() {
        Intent i = new Intent(context, FilterDeliveryActivity.class);
        i.putExtra("address", address);
        i.putExtra("latitude", latitude);
        i.putExtra("longitude", longitude);
        i.putExtra("strAsc", strAsc);
        i.putExtra("strDesc", strDesc);
        i.putExtra("searchKeyWord", strSearch);
        i.putExtra("rating", strRating);
        myIntentActivityResultLauncher.launch(i);
    }*/

    /*@Override
    public void setCurrentPosition(int position) {
        if (homePositionData != null)
            homePositionData.onPageChanged(position);
    }*/

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        /*if (isFirst) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        } else {
            isFirst = true;
            Fragment fragment = new GuestHomeFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            binding.bottomNavigationView.getMenu().findItem(R.id.nav_home_c).setChecked(true);
            ft.commit();
        }*/
    }

    public interface GuestHomeFilterPassData {
        void onFilterChanged(String address, String latitude, String longitude, String strAsc, String strDesc, String strSearch, String strRating);
    }

    public interface GuestHomePositionData {
        void onPageChanged(int position);
    }

}