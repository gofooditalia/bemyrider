package com.app.bemyrider.activity.user;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityCustomerHomeBinding;
import com.app.bemyrider.fragment.user.CustomerHomeFragment;
import com.app.bemyrider.fragment.user.CustomerMenuFragment;
import com.app.bemyrider.fragment.user.CustomerMessagesFragment;
import com.app.bemyrider.fragment.user.FavouriteFragment;
import com.app.bemyrider.fragment.user.ServiceHistoryFragment;
import com.app.bemyrider.helper.PermissionUtils;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;

public class CustomerHomeActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener, CustomerHomeFragment.CustomerHomeFragmentListener {

    private static final String TAG = "CustomerHomeActivity";
    private Context mContext;
    private Activity mActivity;

    private PermissionUtils permissionUtils;

    private ActivityCustomerHomeBinding binding;
    private Menu menu;
    private MenuItem navProfile;

    private boolean isFirst = true;
    boolean doubleBackToExitPressedOnce = false;
    private BroadcastReceiver mMessageReceiver;
    private ConnectionManager connectionManager;

    CustomerHomeFilterPassData homeFilterPassData;

    CustomerHomePositionData homePositionData;
    private String address = "", latitude = "", longitude = "", strAsc = "", strDesc = "", strSearch = "", strRating = "";
    ActivityResultLauncher<Intent> myIntentActivityResultLauncher;
    private int REQ_CODE_NOTIFICATION = 143;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(CustomerHomeActivity.this, R.layout.activity_customer_home, null);
        mContext = CustomerHomeActivity.this;
        mActivity = CustomerHomeActivity.this;

        permissionUtils = new PermissionUtils(mActivity, mContext, new PermissionUtils.OnPermissionGrantedListener() {
            @Override
            public void onStoragePermissionGranted() {

            }

            @Override
            public void onCameraPermissionGranted() {

            }
        });

        initView();

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

        permissionUtils.checkNotificationPermission(REQ_CODE_NOTIFICATION);
    }

    private void initView() {
        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        binding.bottomNavigationView.setOnItemSelectedListener(this);
        menu = binding.bottomNavigationView.getMenu();

        increaseCenterSize();

        if (PrefsUtil.with(mContext).readString("service").equals("true")) {
            PrefsUtil.with(mContext).write("service", "false");
            displaySelectedScreen(R.id.nav_service_c);
            binding.bottomNavigationView.getMenu().findItem(R.id.nav_service_c).setChecked(true);
            Log.e(TAG, PrefsUtil.with(mContext).readString("service") + " _1");
        } else {
            Log.e(TAG, PrefsUtil.with(mContext).readString("service") + " _2");
            displaySelectedScreen(R.id.nav_home_c);
        }

        myActivityResult();
    }

    void increaseCenterSize() {
        /*BottomNavigationMenuView menuView = (BottomNavigationMenuView) binding.bottomNavigationView.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            Log.e(TAG,menuView.getChildCount()+"");
            Log.e(TAG,menuView.getChildAt(i)+"");
            final View iconView = menuView.getChildAt(i).findViewById(R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            if (i == 2){
                // set your height here
                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, displayMetrics);
                // set your width here
                layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, displayMetrics);
            }
            else {
                // set your height here
                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, displayMetrics);
                // set your width here
                layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, displayMetrics);
            }
            iconView.setLayoutParams(layoutParams);
        }*/
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

    @Override
    protected void onResume() {
        registerReceiver(mMessageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
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
        //creating fragment object
        Fragment fragment = null;
        //initializing the fragment object which is selected
        if (itemId == R.id.nav_home_c) {
            isFirst = true;
            fragment = new CustomerHomeFragment();
        } else if (itemId == R.id.nav_favourite_c) {
            isFirst = false;
            fragment = new FavouriteFragment();
        } else if (itemId == R.id.nav_service_c) {
            isFirst = false;
            fragment = new ServiceHistoryFragment();
        } else if (itemId == R.id.nav_message_c) {
            isFirst = false;
            fragment = new CustomerMessagesFragment();
        } else if (itemId == R.id.nav_menu_c) {
            isFirst = false;
            fragment = new CustomerMenuFragment();
        }

        //replacing the fragment
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
            Fragment fragment = new CustomerHomeFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            binding.bottomNavigationView.getMenu().findItem(R.id.nav_home_c).setChecked(true);
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

    public void setOnHomeFilterData(CustomerHomeFilterPassData homeFilterPassData) {
        this.homeFilterPassData = homeFilterPassData;
    }

    public void setOnHomePositionData(CustomerHomePositionData homePositionData) {
        this.homePositionData = homePositionData;
    }

    @Override
    public void onFilterClick() {
        Intent i = new Intent(CustomerHomeActivity.this, FilterDeliveryActivity.class);
        i.putExtra("address", address);
        i.putExtra("latitude", latitude);
        i.putExtra("longitude", longitude);
        i.putExtra("strAsc", strAsc);
        i.putExtra("strDesc", strDesc);
        i.putExtra("searchKeyWord", strSearch);
        i.putExtra("rating", strRating);
        myIntentActivityResultLauncher.launch(i);
    }

    @Override
    public void setCurrentPosition(int position) {
        if (homePositionData != null)
            homePositionData.onPageChanged(position);
    }

    public interface CustomerHomeFilterPassData {
        void onFilterChanged(String address, String latitude, String longitude, String strAsc, String strDesc, String strSearch, String strRating);
    }

    public interface CustomerHomeSearchPassData {
        void onSearchKeyWordChanged(String strSearch);
    }

    public interface CustomerHomePositionData {
        void onPageChanged(int position);
    }

    public interface CustomerHomeApiData {
        void onApiDataPageChanged(int position);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_NOTIFICATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastMaster.showShort(mContext, R.string.err_permission);
            }
        }
    }
}