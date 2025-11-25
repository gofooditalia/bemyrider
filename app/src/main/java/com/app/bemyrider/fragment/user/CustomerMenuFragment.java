package com.app.bemyrider.fragment.user;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.bemyrider.Adapter.MenuItemAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.AccountSettingActivity;
import com.app.bemyrider.activity.ContactUsActivity;
import com.app.bemyrider.activity.FeedbackActivity;

import com.app.bemyrider.activity.InfoPageActivity;
import com.app.bemyrider.activity.NotificationListingActivity;
import com.app.bemyrider.activity.SignupActivity;
import com.app.bemyrider.activity.user.CustomerProfileActivity;
import com.app.bemyrider.activity.user.DisputeListActivity;
import com.app.bemyrider.activity.user.EditProfileActivity;
import com.app.bemyrider.activity.user.PaymentHistoryActivity;

import com.app.bemyrider.databinding.FragmentCustomerMenuBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.myinterfaces.MenuItemClickListener;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.LinkedHashMap;

public class CustomerMenuFragment extends Fragment implements MenuItemClickListener {

    FragmentCustomerMenuBinding binding;
    private Context context;
    private AppCompatActivity activity;
    private AsyncTask profileDataAsync, userLogoutAsync;
    private ConnectionManager connectionManager;
    private ModelForDrawer[] drawerItem;

    ActivityResultLauncher<Intent> myActivityResultLauncher;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_menu, container, false);

        initView();

        return binding.getRoot();
    }

    private void initView() {
        activity = (AppCompatActivity) getActivity();
        context = getContext();

        activity.setSupportActionBar(binding.toolbar);

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        myActivityResult();

        setProfileData();

        binding.llEdit.setOnClickListener(v -> getProfileData());
        binding.imgProfile.setOnClickListener(v -> myActivityResultLauncher.launch(new Intent(context, CustomerProfileActivity.class)));
        binding.linMiddle.setOnClickListener(v -> binding.imgProfile.performClick());

        setUpMenuItems();
    }

    private void setProfileData() {
        if (PrefsUtil.with(activity).readString("UserImg") != null && !"".equals(PrefsUtil.with(activity).readString("UserImg"))) {
            binding.imgProfile.setColorFilter(ContextCompat.getColor(context,
                    R.color.transparent));
            Picasso.get().load(PrefsUtil.with(activity).readString("UserImg")).placeholder(R.drawable.loading).into(binding.imgProfile);
        } else {
            binding.imgProfile.setColorFilter(ContextCompat.getColor(context,
                    R.color.white));
            Picasso.get().load(R.drawable.ic_user_menu).placeholder(R.drawable.loading).into(binding.imgProfile);
        }

        if (PrefsUtil.with(activity).readString("UserName") != null && !"".equals(PrefsUtil.with(activity).readString("UserName"))) {
            binding.txtUserName.setText(PrefsUtil.with(activity).readString("UserName"));
        } else {
            binding.txtUserName.setText("N/A");
        }

        if (PrefsUtil.with(activity).readString("login_cust_address") != null && !"".equals(PrefsUtil.with(activity).readString("login_cust_address"))) {
            binding.txtAddress.setText(PrefsUtil.with(activity).readString("login_cust_address"));
        } else {
            binding.txtAddress.setText("N/A");
        }

    }

    private void myActivityResult() {
        myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                setProfileData();
            }
        });
    }


    void setUpMenuItems() {
        drawerItem = new ModelForDrawer[8];
        //drawerItem[0] = new ModelForDrawer(R.drawable.ic_search_menu, getString(R.string.search_service));
       // drawerItem[0] = new ModelForDrawer(R.drawable.ic_service_menu, getString(R.string.services));
        //drawerItem[0] = new ModelForDrawer(R.drawable.wallet_icon, getString(R.string.wallet));
        drawerItem[0] = new ModelForDrawer(R.drawable.ic_notification_menu, getString(R.string.notifications_title));
        drawerItem[1] = new ModelForDrawer(R.drawable.ic_resolution_menu, getString(R.string.resolution_center));
        drawerItem[2] = new ModelForDrawer(R.drawable.ic_payment_history_menu, getString(R.string.payment_history));
        drawerItem[3] = new ModelForDrawer(R.drawable.ic_account_settings_menu, getString(R.string.account_settings));
        drawerItem[4] = new ModelForDrawer(R.drawable.ic_info_menu, getString(R.string.info));
        drawerItem[5] = new ModelForDrawer(R.drawable.ic_feedback_menu, getString(R.string.feedback));
        drawerItem[6] = new ModelForDrawer(R.drawable.ic_contact_us_menu, getString(R.string.comtact_us));
        drawerItem[7] = new ModelForDrawer(R.drawable.ic_logout_menu, getString(R.string.logout));

        binding.recCustMenu.setLayoutManager(new LinearLayoutManager(context));
        binding.recCustMenu.setItemAnimator(new DefaultItemAnimator());

        MenuItemAdapter adapter = new MenuItemAdapter(context, activity, this, drawerItem);
        binding.recCustMenu.setAdapter(adapter);
    }

    /*---------------- Profile Detail Api Call ---------------------*/
    protected void getProfileData() {
        binding.pgEdit.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("profile_id", PrefsUtil.with(activity).readString("UserId"));

        new WebServiceCall(context, WebServiceUrl.URL_PROFILE, textParams, ProfilePojo.class,
                false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgEdit.setVisibility(View.GONE);
                if (status) {
                    EditProfileActivity.profileData = ((ProfilePojo) obj).getData();
                    if (EditProfileActivity.profileData != null) {
                        Intent i = new Intent(activity, EditProfileActivity.class);
                        i.putExtra("Edit", "true");
                        i.putExtra("isFromEdit", true);
                        myActivityResultLauncher.launch(i);
                    } else {
                        Toast.makeText(context, R.string.can_not_edit_now, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, (String) obj,
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
    public void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(profileDataAsync);
        Utils.cancelAsyncTask(userLogoutAsync);
        super.onDestroy();
    }

    @Override
    public void onMenuItemClick(ModelForDrawer modelForDrawer, int position) {
        switch (position) {
            /*case 0:
                startActivity(new Intent(activity, SearchServiceActivity.class));
                break;*/
           /* case 0:
                startActivity(new Intent(activity, ServiceHistoryActivity.class));
                break;*/
            /*case 0:
                startActivity(new Intent(activity, WalletActivity.class));
                break;*/
            case 0:
                startActivity(new Intent(activity, NotificationListingActivity.class));
                break;
            case 1:
                startActivity(new Intent(activity, DisputeListActivity.class));
                break;
            case 2:
                startActivity(new Intent(activity, PaymentHistoryActivity.class));
                break;
            case 3:
                startActivity(new Intent(activity, AccountSettingActivity.class));
                break;
            case 4:
                startActivity(new Intent(activity, InfoPageActivity.class));
                break;
            case 5:
                startActivity(new Intent(activity, FeedbackActivity.class));
                break;
            case 6:
                startActivity(new Intent(activity, ContactUsActivity.class));
                break;
            case 7:
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
                break;
            default:
                break;
        }
    }

    /*--------------------- Log Out Api Call ----------------------*/
    private void serviceCallLogout() {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(activity).readString("UserId"));
        textParams.put("device_token", PrefsUtil.with(activity).readString("device_token"));


        new WebServiceCall(context, WebServiceUrl.URL_LOGOUT, textParams,
                CommonPojo.class, true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    File offlineFile = new File(activity.getFilesDir().getPath(), "/offline.json");
                    if (offlineFile.exists()) {
                        offlineFile.delete();
                    }
                    PrefsUtil.with(activity).clearPrefs();
                    startActivity(new Intent(activity, SignupActivity.class));
                    activity.finish();
                } else {
                    Toast.makeText(context, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                userLogoutAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                userLogoutAsync = null;
            }
        });
    }
}
