package com.app.bemyrider.fragment.partner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.bemyrider.Adapter.MenuItemAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.activity.AccountSettingActivity;
import com.app.bemyrider.activity.ContactUsActivity;
import com.app.bemyrider.activity.FeedbackActivity;
import com.app.bemyrider.activity.InfoPageActivity;
import com.app.bemyrider.activity.LoginActivity;
import com.app.bemyrider.activity.MyStripeConnectActivity;
import com.app.bemyrider.activity.NotificationListingActivity;
import com.app.bemyrider.activity.partner.EditProfileActivity;
import com.app.bemyrider.activity.partner.PartnerPaymentHistoryActivity;
import com.app.bemyrider.activity.partner.Partner_FinancialInfo_Activity;
import com.app.bemyrider.activity.partner.Partner_MyServices_Activity;
import com.app.bemyrider.activity.partner.ResolutionActivity;
import com.app.bemyrider.databinding.FragmentProviderMenuBinding;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.myinterfaces.MenuItemClickListener;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.SecurePrefsUtil;
import com.app.bemyrider.viewmodel.ProviderMenuViewModel;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ProviderMenuFragment extends Fragment implements MenuItemClickListener {

    private FragmentProviderMenuBinding binding;
    private Context context;
    private AppCompatActivity activity;
    private ConnectionManager connectionManager;
    private ProviderMenuViewModel viewModel;
    private static final String TAG = "ProviderMenuFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_provider_menu, container, false);
        viewModel = new ViewModelProvider(this).get(ProviderMenuViewModel.class);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        activity = (AppCompatActivity) getActivity();
        context = getContext();

        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar);
        }

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        updateUI();
        binding.llEdit.setOnClickListener(v -> performGetProfileData());
        setUpMenuItems();
    }

    private void updateUI() {
        String userImg = PrefsUtil.with(activity).readString("UserImg");
        if (userImg != null && !userImg.isEmpty()) {
            binding.imgProfile.setColorFilter(ContextCompat.getColor(context, R.color.transparent));
            Picasso.get().load(userImg).placeholder(R.drawable.loading).into(binding.imgProfile);
        } else {
            binding.imgProfile.setColorFilter(ContextCompat.getColor(context, R.color.white));
            Picasso.get().load(R.drawable.ic_user_menu).placeholder(R.drawable.loading).into(binding.imgProfile);
        }

        String userName = PrefsUtil.with(activity).readString("UserName");
        binding.txtUserName.setText(userName != null && !userName.isEmpty() ? userName : "N/A");
        
        String address = PrefsUtil.with(activity).readString("login_cust_address");
        binding.txtAddress.setText(address != null && !address.isEmpty() ? address : "N/A");
    }

    void setUpMenuItems() {
        ModelForDrawer[] drawerItem = {
                new ModelForDrawer(R.drawable.ic_stripe_menu, getString(R.string.stripe_conncent)),
                new ModelForDrawer(R.drawable.ic_my_service_menu, getString(R.string.my_services)),
                new ModelForDrawer(R.drawable.ic_finance_info_menu, getString(R.string.financial_info)),
                new ModelForDrawer(R.drawable.ic_notification_menu, getString(R.string.notifications_title)),
                new ModelForDrawer(R.drawable.ic_resolution_menu, getString(R.string.resolution_center)),
                new ModelForDrawer(R.drawable.ic_payment_history_menu, getString(R.string.payment_history)),
                new ModelForDrawer(R.drawable.ic_account_settings_menu, getString(R.string.account_settings)),
                new ModelForDrawer(R.drawable.ic_info_menu, getString(R.string.info)),
                new ModelForDrawer(R.drawable.ic_feedback_menu, getString(R.string.feedback)),
                new ModelForDrawer(R.drawable.ic_contact_us_menu, getString(R.string.comtact_us)),
                new ModelForDrawer(R.drawable.ic_logout_menu, getString(R.string.logout))
        };

        binding.recProMenu.setLayoutManager(new LinearLayoutManager(context));
        binding.recProMenu.setItemAnimator(new DefaultItemAnimator());
        MenuItemAdapter adapter = new MenuItemAdapter(context, activity, this, drawerItem);
        binding.recProMenu.setAdapter(adapter);
    }

    protected void performGetProfileData() {
        binding.pgEdit.setVisibility(View.VISIBLE);
        String profileId = PrefsUtil.with(activity).readString("UserId");

        viewModel.getProfile(profileId).observe(getViewLifecycleOwner(), profilePojo -> {
            binding.pgEdit.setVisibility(View.GONE);
            if (profilePojo != null && profilePojo.isStatus()) {
                ProfileItem item = profilePojo.getData();
                
                PrefsUtil.with(context).write("userContactno", item.getContactNumber());
                PrefsUtil.with(context).write("userEmail", item.getEmail());

                Intent i = new Intent(context, EditProfileActivity.class);
                i.putExtra("isFromEdit", true);
                i.putExtra("profilePojoData", item);
                i.putExtra("Edit", "true");
                startActivity(i);
            } else {
                Toast.makeText(context, "Errore nel caricamento del profilo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLocalLogout() {
        try {
            File offlineFile = new File(activity.getFilesDir().getPath(), "/offline.json");
            if (offlineFile.exists()) {
                offlineFile.delete();
            }

            SecurePrefsUtil.with(activity).clearPrefs();
            PrefsUtil.with(activity).clearPrefs();

            Intent intent = new Intent(activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            activity.finish(); // Chiudi l'Activity contenitore
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback estremo se qualcosa va storto nel logout
            activity.finish();
        }
    }

    private void performCheckStripeStatus() {
        String userId = PrefsUtil.with(activity).readString("UserId");
        viewModel.checkStripeStatus(userId).observe(getViewLifecycleOwner(), stripePojo -> {
            if (stripePojo != null && stripePojo.getStatus()) {
                String connectUrl = null;
                if (stripePojo.getData() != null) {
                    connectUrl = stripePojo.getData().getConnectUrl();
                }

                if (!TextUtils.isEmpty(connectUrl)) {
                    Log.i(TAG, "Stripe Connect URL received: " + connectUrl);
                    Intent intent = new Intent(activity, MyStripeConnectActivity.class);
                    intent.putExtra("StripeUrl", connectUrl);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "Stripe Connect URL is null or empty. Message: " + stripePojo.getMessage());
                    Toast.makeText(context, "Impossibile connettersi a Stripe. URL non disponibile.", Toast.LENGTH_LONG).show();
                }
            } else {
                String message = (stripePojo != null) ? stripePojo.getMessage() : "Errore di connessione a Stripe. Riprova.";
                Log.e(TAG, "Stripe API call failed. Message: " + message);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        try {
            if (connectionManager != null) {
                connectionManager.unregisterReceiver();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onMenuItemClick(ModelForDrawer modelForDrawer, int position) {
        switch (position) {
            case 0:
                // Quando si clicca su Stripe Connect, chiamiamo la funzione che controlla lo stato e avvia l'activity
                performCheckStripeStatus();
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
                // Logica di logout puramente locale e immediata
                new AlertDialog.Builder(activity)
                        .setMessage(R.string.sure_logout)
                        .setPositiveButton(R.string.yes, (dialog, i) -> performLocalLogout())
                        .setNegativeButton(R.string.no, null)
                        .show();
                break;
            default:
                break;
        }
    }
}
