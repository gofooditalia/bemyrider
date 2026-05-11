package com.app.bemyrider.fragment.user;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.bemyrider.Adapter.MenuItemAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.viewmodel.CustomerMenuViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.activity.AccountSettingActivity;
import com.app.bemyrider.activity.ContactUsActivity;
import com.app.bemyrider.activity.FeedbackActivity;
import com.app.bemyrider.activity.InfoPageActivity;
import com.app.bemyrider.activity.NotificationListingActivity;
import com.app.bemyrider.activity.LoginActivity;
import com.app.bemyrider.activity.user.CustomerProfileActivity;
import com.app.bemyrider.activity.user.DisputeListActivity;
import com.app.bemyrider.activity.user.EditProfileActivity;
import com.app.bemyrider.activity.user.MyJobsActivity;
import com.app.bemyrider.activity.user.PaymentHistoryActivity;
import com.app.bemyrider.databinding.FragmentCustomerMenuBinding;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.myinterfaces.MenuItemClickListener;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.SecurePrefsUtil;
import com.app.bemyrider.utils.Utils;
import coil.Coil;
import coil.request.ImageRequest;

import java.io.File;

public class CustomerMenuFragment extends Fragment implements MenuItemClickListener {

    FragmentCustomerMenuBinding binding;
    private Context context;
    private AppCompatActivity activity;
    private CustomerMenuViewModel viewModel;
    private ConnectionManager connectionManager;
    private ModelForDrawer[] drawerItem;

    ActivityResultLauncher<Intent> myActivityResultLauncher;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_menu, container, false);

        initView();

        viewModel = new ViewModelProvider(this).get(CustomerMenuViewModel.class);
        observeViewModel();

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
        String userImg = PrefsUtil.with(activity).readString("UserImg");
        ImageRequest.Builder profileBuilder = new ImageRequest.Builder(context)
                .placeholder(R.drawable.loading)
                .target(binding.imgProfile);

        if (userImg != null && !userImg.isEmpty()) {
            binding.imgProfile.setColorFilter(ContextCompat.getColor(context, android.R.color.transparent));
            profileBuilder.data(userImg);
        } else {
            binding.imgProfile.setColorFilter(ContextCompat.getColor(context, R.color.white));
            profileBuilder.data(R.drawable.ic_user_menu);
        }
        Coil.imageLoader(context).enqueue(profileBuilder.build());

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
        drawerItem = new ModelForDrawer[9];
        drawerItem[0] = new ModelForDrawer(R.drawable.ic_notification_menu, getString(R.string.notifications_title));
        drawerItem[1] = new ModelForDrawer(R.drawable.ic_service_menu, getString(R.string.my_job_posts)); // Fix: ic_service_request_menu -> ic_service_menu
        drawerItem[2] = new ModelForDrawer(R.drawable.ic_resolution_menu, getString(R.string.resolution_center));
        drawerItem[3] = new ModelForDrawer(R.drawable.ic_payment_history_menu, getString(R.string.payment_history));
        drawerItem[4] = new ModelForDrawer(R.drawable.ic_account_settings_menu, getString(R.string.account_settings));
        drawerItem[5] = new ModelForDrawer(R.drawable.ic_info_menu, getString(R.string.info));
        drawerItem[6] = new ModelForDrawer(R.drawable.ic_feedback_menu, getString(R.string.feedback));
        drawerItem[7] = new ModelForDrawer(R.drawable.ic_contact_us_menu, getString(R.string.comtact_us));
        drawerItem[8] = new ModelForDrawer(R.drawable.ic_logout_menu, getString(R.string.logout));

        binding.recCustMenu.setLayoutManager(new LinearLayoutManager(context));
        binding.recCustMenu.setItemAnimator(new DefaultItemAnimator());

        MenuItemAdapter adapter = new MenuItemAdapter(context, activity, this, drawerItem);
        binding.recCustMenu.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getProfile().observe(getViewLifecycleOwner(), pojo -> {
            binding.pgEdit.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                EditProfileActivity.profileData = pojo.getData();
                Intent i = new Intent(activity, EditProfileActivity.class);
                i.putExtra("Edit", "true");
                i.putExtra("isFromEdit", true);
                myActivityResultLauncher.launch(i);
            } else {
                Toast.makeText(context, R.string.can_not_edit_now, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getLogoutResult().observe(getViewLifecycleOwner(), result -> {
            File offlineFile = new File(activity.getFilesDir().getPath(), "/offline.json");
            if (offlineFile.exists()) offlineFile.delete();
            SecurePrefsUtil.with(activity).clearPrefs();
            PrefsUtil.with(activity).clearPrefs();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            activity.finish();
        });

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                binding.pgEdit.setVisibility(View.GONE);
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void getProfileData() {
        binding.pgEdit.setVisibility(View.VISIBLE);
        viewModel.loadProfile(PrefsUtil.with(activity).readString("UserId"));
    }

    @Override
    public void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onMenuItemClick(ModelForDrawer modelForDrawer, int position) {
        switch (position) {
            case 0:
                startActivity(new Intent(activity, NotificationListingActivity.class));
                break;
            case 1:
                startActivity(new Intent(activity, MyJobsActivity.class));
                break;
            case 2:
                startActivity(new Intent(activity, DisputeListActivity.class));
                break;
            case 3:
                startActivity(new Intent(activity, PaymentHistoryActivity.class));
                break;
            case 4:
                startActivity(new Intent(activity, AccountSettingActivity.class));
                break;
            case 5:
                startActivity(new Intent(activity, InfoPageActivity.class));
                break;
            case 6:
                startActivity(new Intent(activity, FeedbackActivity.class));
                break;
            case 7:
                startActivity(new Intent(activity, ContactUsActivity.class));
                break;
            case 8:
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.sure_logout)
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> serviceCallLogout())
                        .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel()).show();
                break;
            default:
                break;
        }
    }

    private void serviceCallLogout() {
        SecurePrefsUtil securePrefs = SecurePrefsUtil.with(activity);
        PrefsUtil prefsUtil = PrefsUtil.with(activity);
        String userId = securePrefs.readString("UserId");
        if (userId == null || userId.isEmpty()) userId = prefsUtil.readString("UserId");
        viewModel.logout(userId != null ? userId : "");
    }
}