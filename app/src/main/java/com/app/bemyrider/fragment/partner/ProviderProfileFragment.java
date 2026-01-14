package com.app.bemyrider.fragment.partner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.app.bemyrider.R;
import com.app.bemyrider.activity.partner.EditProfileActivity;
import com.app.bemyrider.activity.partner.PartnerReviewsActivity;
import com.app.bemyrider.databinding.FragmentProviderProfileBinding;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.viewmodel.ProviderProfileViewModel;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

public class ProviderProfileFragment extends Fragment {

    private static final String TAG = "ProviderProfileFragment";
    private FragmentProviderProfileBinding binding;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private ConnectionManager connectionManager;
    private Context context;
    private AppCompatActivity activity;
    private ProviderProfileViewModel viewModel;
    private ProfileItem profilePojoData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_provider_profile, container, false);
        viewModel = new ViewModelProvider(this).get(ProviderProfileViewModel.class);
        
        context = getContext();
        activity = (AppCompatActivity) getActivity();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);

        initView();
        setupObservers();
        performFetchProfile();

        return binding.getRoot();
    }

    private void initView() {
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                });

        binding.switchAvailableNow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            performUpdateAvailability(isChecked ? "y" : "n");
        });

        binding.fabEdit.setOnClickListener(v -> {
            if (profilePojoData != null) {
                Intent i = new Intent(context, EditProfileActivity.class);
                i.putExtra("isFromEdit", true);
                i.putExtra("profilePojoData", profilePojoData);
                i.putExtra("Edit", "true");
                startActivity(i);
            }
        });

        binding.txtViewall.setOnClickListener(v -> startActivity(new Intent(context, PartnerReviewsActivity.class)));
        binding.imgVerifyGmail.setOnClickListener(view -> signInWithGoogle());
        
        binding.imgShare.setOnClickListener(v -> shareProfile());
    }

    private void shareProfile() {
        if (profilePojoData != null && profilePojoData.getId() != null) {
            String shareBody = "Ciao! Prenota il mio servizio su Bemyrider: https://bemyrider.it/rider?id=" + profilePojoData.getId();
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Il mio profilo Bemyrider");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Condividi via"));
        } else {
            Toast.makeText(context, "Profilo non caricato, impossibile condividere.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupObservers() {
        viewModel.getProfile(PrefsUtil.with(context).readString("UserId")).observe(getViewLifecycleOwner(), profilePojo -> {
            binding.progressProfileDetail.setVisibility(View.GONE);
            binding.llPartnerProfileDetail.setVisibility(View.VISIBLE);
            binding.fabEdit.setVisibility(View.VISIBLE);

            if (profilePojo != null && profilePojo.isStatus()) {
                profilePojoData = profilePojo.getData();
                updateUI(profilePojoData);
            } else {
                Toast.makeText(context, "Errore caricamento profilo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performFetchProfile() {
        binding.progressProfileDetail.setVisibility(View.VISIBLE);
        binding.llPartnerProfileDetail.setVisibility(View.GONE);
        binding.fabEdit.setVisibility(View.GONE);
        viewModel.getProfile(PrefsUtil.with(context).readString("UserId"));
    }

    private void performUpdateAvailability(String status) {
        binding.progressAvailable.setVisibility(View.VISIBLE);
        binding.switchAvailableNow.setVisibility(View.GONE);
        viewModel.updateAvailabilityStatus(PrefsUtil.with(context).readString("UserId"), status)
                .observe(getViewLifecycleOwner(), commonPojo -> {
                    binding.progressAvailable.setVisibility(View.GONE);
                    binding.switchAvailableNow.setVisibility(View.VISIBLE);
                    if (commonPojo == null || !commonPojo.isStatus()) {
                        Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show();
                        binding.switchAvailableNow.setOnCheckedChangeListener(null);
                        binding.switchAvailableNow.setChecked(!binding.switchAvailableNow.isChecked());
                        binding.switchAvailableNow.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            performUpdateAvailability(isChecked ? "y" : "n");
                        });
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                viewModel.socialSignIn(account.getEmail(), account.getGivenName(), account.getFamilyName(),
                        account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "", "g", account.getId(), PrefsUtil.with(context).readString("UserId"))
                        .observe(getViewLifecycleOwner(), newLoginPojo -> {
                           if(newLoginPojo != null && newLoginPojo.isStatus()){
                               Toast.makeText(context, "Google account connected!", Toast.LENGTH_SHORT).show();
                               performFetchProfile();
                           } else {
                               Toast.makeText(context, "Failed to connect Google account", Toast.LENGTH_SHORT).show();
                           }
                        });
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void updateUI(ProfileItem data) {
        if (data == null) return;

        if (data.getProfileImg() != null && !data.getProfileImg().isEmpty()) {
            Picasso.get()
                   .load(data.getProfileImg())
                   .placeholder(R.mipmap.ic_launcher_round)
                   .error(R.mipmap.ic_launcher_round)
                   .into(binding.imgProfile);
        }

        binding.txtUsername.setText(String.format("%s %s", data.getFirstName(), data.getLastName()));
        binding.txtUseremail.setText(data.getEmail());
        binding.txtUsercontactno.setText(data.getContactNumber());

        binding.switchAvailableNow.setOnCheckedChangeListener(null);
        boolean isAvail = "y".equalsIgnoreCase(data.getIsAvailable()) || "1".equals(data.getIsAvailable());
        binding.switchAvailableNow.setChecked(isAvail);
        binding.switchAvailableNow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            performUpdateAvailability(isChecked ? "y" : "n");
        });

        binding.txtAvailableDays.setText(data.getAvailableDaysList());
        binding.txtAddressPro.setText(data.getAddress());
        binding.txtWorkedOn.setText(data.getTotalService());
        binding.txtCompany.setText(data.getCompanyName());
        binding.txtVat.setText(data.getVat());
        binding.txtInvoiceRecipient.setText(data.getReceiptCode());
        binding.txtCertifiedEmail.setText(data.getCertifiedEmail());
        
        binding.txtCityOfBirth.setText(data.getCity_of_birth());
        binding.txtDateOfBirth.setText(data.getDate_of_birth());
        binding.txtCityOfResidence.setText(data.getCity_of_residence());
        binding.txtResidentialAddress.setText(data.getResidential_address());
        
        binding.txtRating.setText(data.getStartRating());
        binding.txtTotaReview.setText("(" + data.getTotalReview() + " Reviews)");

        if (data.getAvailableTimeStart() != null && data.getAvailableTimeEnd() != null) {
            binding.txtServicetime.setText(data.getAvailableTimeStart() + " to " + data.getAvailableTimeEnd());
        }

        StringBuilder deliveryType = new StringBuilder();
        if ("y".equalsIgnoreCase(data.getSmallDelivery())) deliveryType.append(getString(R.string.small)).append(", ");
        if ("y".equalsIgnoreCase(data.getMediumDelivery())) deliveryType.append(getString(R.string.medium)).append(", ");
        if ("y".equalsIgnoreCase(data.getLargeDelivery())) deliveryType.append(getString(R.string.large)).append(", ");
        
        if (deliveryType.length() > 2) {
            binding.txtDeliveryType.setText(deliveryType.substring(0, deliveryType.length() - 2));
        } else {
            binding.txtDeliveryType.setText(getString(R.string.none));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connectionManager != null) {
            connectionManager.unregisterReceiver();
        }
    }
}
