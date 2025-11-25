package com.app.bemyrider.fragment.partner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import com.app.bemyrider.activity.MyStripeConnectActivity;
import com.app.bemyrider.activity.NotificationListingActivity;
import com.app.bemyrider.activity.SignupActivity;
import com.app.bemyrider.activity.partner.EditProfileActivity;
import com.app.bemyrider.activity.partner.PartnerPaymentHistoryActivity;
import com.app.bemyrider.activity.partner.Partner_FinancialInfo_Activity;
import com.app.bemyrider.activity.partner.Partner_MyServices_Activity;
import com.app.bemyrider.activity.partner.ResolutionActivity;
import com.app.bemyrider.databinding.FragmentProviderMenuBinding;
import com.app.bemyrider.model.CheckStripeConnectedPojo;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.myinterfaces.MenuItemClickListener;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.LinkedHashMap;

public class ProviderMenuFragment extends Fragment implements MenuItemClickListener {

    FragmentProviderMenuBinding binding;
    private Context context;
    private AppCompatActivity activity;
    private AsyncTask profileDataAsync, userLogoutAsync,getStripeConnectAsync;
    private ConnectionManager connectionManager;
    private ModelForDrawer[] drawerItem;
    private String strepoc_avl_start_time, strepoc_avl_end_time,
            countrycodeid, userAddress, smallDelivery, mediumDelivery, largeDelivery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_provider_menu, container, false);
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

        binding.llEdit.setOnClickListener(v -> getProfileData());
        setUpMenuItems();
    }

    void setUpMenuItems() {
        drawerItem = new ModelForDrawer[11];
        drawerItem[0] = new ModelForDrawer(R.drawable.ic_stripe_menu, getString(R.string.stripe_conncent));
        drawerItem[1] = new ModelForDrawer(R.drawable.ic_my_service_menu, getString(R.string.my_services));
        drawerItem[2] = new ModelForDrawer(R.drawable.ic_finance_info_menu, getString(R.string.financial_info));
        drawerItem[3] = new ModelForDrawer(R.drawable.ic_notification_menu, getString(R.string.notifications_title));
        drawerItem[4] = new ModelForDrawer(R.drawable.ic_resolution_menu, getString(R.string.resolution_center));
        drawerItem[5] = new ModelForDrawer(R.drawable.ic_payment_history_menu, getString(R.string.payment_history));
        drawerItem[6] = new ModelForDrawer(R.drawable.ic_account_settings_menu, getString(R.string.account_settings));
        drawerItem[7] = new ModelForDrawer(R.drawable.ic_info_menu, getString(R.string.info));
        drawerItem[8] = new ModelForDrawer(R.drawable.ic_feedback_menu, getString(R.string.feedback));
        drawerItem[9] = new ModelForDrawer(R.drawable.ic_contact_us_menu, getString(R.string.comtact_us));
        drawerItem[10] = new ModelForDrawer(R.drawable.ic_logout_menu, getString(R.string.logout));

        binding.recProMenu.setLayoutManager(new LinearLayoutManager(context));
        binding.recProMenu.setItemAnimator(new DefaultItemAnimator());

        MenuItemAdapter adapter = new MenuItemAdapter(context, activity, this, drawerItem);
        binding.recProMenu.setAdapter(adapter);
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
                    ProfilePojo response_profile = (ProfilePojo) obj;
                    ProfileItem item = response_profile.getData();

                    smallDelivery = response_profile.getData().getSmallDelivery();
                    mediumDelivery = response_profile.getData().getMediumDelivery();
                    largeDelivery = response_profile.getData().getLargeDelivery();
                    strepoc_avl_start_time = response_profile.getData().getAvailableTimeStart();
                    strepoc_avl_end_time = response_profile.getData().getAvailableTimeEnd();
                    countrycodeid = response_profile.getData().getCountryCode();
                    userAddress = response_profile.getData().getAddress();


                    PrefsUtil.with(context).write("userContactno", response_profile.getData().getContactNumber());
                    PrefsUtil.with(context).write("userEmail", response_profile.getData().getEmail());
                    PrefsUtil.with(context).write("total_review", response_profile.getData().getTotalReview());
                    PrefsUtil.with(context).write("total_rating", response_profile.getData().getPositiveRating());
                    PrefsUtil.with(context).write("userAddress", response_profile.getData().getAddress());
                    PrefsUtil.with(context).write("userAbout", response_profile.getData().getDescription());
                    PrefsUtil.with(context).write("userfname", response_profile.getData().getFirstName());
                    PrefsUtil.with(context).write("userlname", response_profile.getData().getLastName());
                    PrefsUtil.with(context).write("start_time", response_profile.getData().getAvailableTimeStart());
                    PrefsUtil.with(context).write("end_time", response_profile.getData().getAvailableTimeEnd());
                    PrefsUtil.with(context).write("userlatitude", response_profile.getData().getLatitude());
                    PrefsUtil.with(context).write("userlongitude", response_profile.getData().getLongitude());
                    PrefsUtil.with(context).write("userAvalDay", response_profile.getData().getAvailableDays());
                    //PrefsUtil.with(context).write("paypalEmailId", response_profile.getData().getPaypalEmail());
                    PrefsUtil.with(context).write("lat", response_profile.getData().getLatitude());
                    PrefsUtil.with(context).write("long", response_profile.getData().getLongitude());
                    PrefsUtil.with(context).write("UserImg", response_profile.getData().getProfileImg());

                    Intent i = new Intent(context, EditProfileActivity.class);
                    i.putExtra("isFromEdit", true);
                    i.putExtra("profilePojoData", item);

                    i.putExtra("smallDelivery", smallDelivery);
                    i.putExtra("mediumDelivery", mediumDelivery);
                    i.putExtra("largeDelivery", largeDelivery);
                    i.putExtra("strepoc_avl_start_time", strepoc_avl_start_time);
                    i.putExtra("strepoc_avl_end_time", strepoc_avl_end_time);
                    i.putExtra("userAddress", userAddress);
                    i.putExtra("countrycodeId", countrycodeid);
                    i.putExtra("Edit", "true");
                    startActivity(i);
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
        Utils.cancelAsyncTask(getStripeConnectAsync);
        super.onDestroy();
    }

    @Override
    public void onMenuItemClick(ModelForDrawer modelForDrawer, int position) {
        switch (position) {
            case 0:
                callAPICheckConnectedStripeAccount();
                break;
            case 1:
                startActivity(new Intent(activity, Partner_MyServices_Activity.class));
                break;
            case 2:
                startActivity(new Intent(activity, Partner_FinancialInfo_Activity.class));
                break;
            case 3:
                startActivity(new Intent(activity, NotificationListingActivity.class));
                break;
            case 4:
                startActivity(new Intent(activity, ResolutionActivity.class));
                break;
            case 5:
                startActivity(new Intent(activity, PartnerPaymentHistoryActivity.class));
                break;
            case 6:
                startActivity(new Intent(activity, AccountSettingActivity.class));
                break;
            case 7:
                startActivity(new Intent(activity, InfoPageActivity.class));
                break;
            case 8:
                startActivity(new Intent(activity, FeedbackActivity.class));
                break;
            case 9:
                startActivity(new Intent(activity, ContactUsActivity.class));
                break;
            case 10:
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.sure_logout)
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> serviceCallLogout())
                        .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel()).show();
                break;
            default:
                break;
        }
    }

    private void callAPICheckConnectedStripeAccount() {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("user_id", PrefsUtil.with(context).readString("UserId"));

        new WebServiceCall(context,  WebServiceUrl.URL_STRIPE_CONNECT, params, CheckStripeConnectedPojo.class, true,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        try {
                            if (status) {
                                CheckStripeConnectedPojo checkStripeConnectedResponse = (CheckStripeConnectedPojo) obj;
                                if(checkStripeConnectedResponse.getData().getConnectUrl() != null) {
                                    Intent intent = new Intent(context, MyStripeConnectActivity.class);
                                    intent.putExtra("connect_url",checkStripeConnectedResponse.getData().getConnectUrl());
                                    startActivity(intent);
                                }
                            } else {
                                Toast.makeText(context, (String) obj, Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        getStripeConnectAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        getStripeConnectAsync = null;
                    }
                });
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
