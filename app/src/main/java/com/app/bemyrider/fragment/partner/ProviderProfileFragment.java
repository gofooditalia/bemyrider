package com.app.bemyrider.fragment.partner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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
import com.app.bemyrider.utils.Utils;
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

    private static final String TAG = "ProviderProfileFragment"; // AGGIUNTO
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
                        binding.switchAvailableNow.setChecked(!binding.switchAvailableNow.isChecked());
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
                               performFetchProfile(); // Refresh profile
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
        // ... (metodo updateUI omesso per brevità, rimane quasi invariato)
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connectionManager != null) {
            connectionManager.unregisterReceiver();
        }
    }
}
