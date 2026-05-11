package com.app.bemyrider.activity.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.R;
import com.app.bemyrider.activity.partner.PartnerReviewsActivity;
import com.app.bemyrider.databinding.PartnerProfileAcitvityBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.PartnerProfileViewModel;

import androidx.lifecycle.ViewModelProvider;

import coil.Coil;
import coil.request.ImageRequest;


public class PartnerProfileActivity extends AppCompatActivity {

    private static final String TAG = "PartnerProfileActivity";
    private PartnerProfileAcitvityBinding binding;
    private final Context mContext = this;
    private String partnerId = "";
    private PartnerProfileViewModel viewModel;
    private ConnectionManager connectionManager;
    private String isFlag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.partner_profile_acitvity);

        String providerId = getIntent().getStringExtra(Utils.PROVIDER_ID);
        if (providerId != null && !providerId.isEmpty()) {
            partnerId = providerId;
        } else {
            finish();
            return;
        }

        initView();

        viewModel = new ViewModelProvider(this).get(PartnerProfileViewModel.class);
        observeViewModel();
        viewModel.loadPartnerProfile(PrefsUtil.with(this).readString("UserId"), partnerId);

        binding.relReportUser.setOnClickListener(v -> {
            binding.imgReportUser.setVisibility(View.GONE);
            binding.pgReportUser.setVisibility(View.VISIBLE);
            viewModel.reportUser(PrefsUtil.with(this).readString("UserId"), partnerId);
        });
    }

    private void observeViewModel() {
        viewModel.getProfile().observe(this, pojo -> {
            binding.progress.setVisibility(View.GONE);
            binding.llMain.setVisibility(View.VISIBLE);
            binding.relReportUser.setVisibility(View.VISIBLE);
            if (pojo != null && pojo.getData() != null) {
                updateUI(pojo.getData());
            } else if (pojo != null) {
                Toast.makeText(mContext, R.string.profile_loading_error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getFlagResult().observe(this, result -> {
            binding.pgReportUser.setVisibility(View.GONE);
            binding.imgReportUser.setVisibility(View.VISIBLE);
            if (result != null && result.isStatus()) {
                if ("y".equalsIgnoreCase(isFlag)) {
                    binding.imgReportUser.setImageResource(R.mipmap.ic_report_user);
                    isFlag = "n";
                } else {
                    binding.imgReportUser.setImageResource(R.mipmap.ic_report_user_filled);
                    isFlag = "y";
                }
                Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                binding.progress.setVisibility(View.GONE);
                binding.pgReportUser.setVisibility(View.GONE);
                binding.imgReportUser.setVisibility(View.VISIBLE);
                Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                binding.progress.setVisibility(View.VISIBLE);
                binding.llMain.setVisibility(View.GONE);
                binding.relReportUser.setVisibility(View.GONE);
            }
        });
    }

    private void updateUI(@NonNull ProfileItem pItem) {
        ImageRequest.Builder profileBuilder = new ImageRequest.Builder(mContext)
                .placeholder(R.drawable.loading)
                .error(R.mipmap.user)
                .target(binding.ppaImgProfile);

        if (pItem.getProfileImg() != null && !pItem.getProfileImg().isEmpty()) {
            profileBuilder.data(pItem.getProfileImg());
        } else {
            profileBuilder.data(R.mipmap.user);
        }
        Coil.imageLoader(mContext).enqueue(profileBuilder.build());

        StringBuilder strDeliveryType = new StringBuilder();
        if (pItem.getSmallDelivery() != null && pItem.getSmallDelivery().equals("y")) {
            strDeliveryType.append(getString(R.string.small));
        }
        if (pItem.getMediumDelivery() != null && pItem.getMediumDelivery().equals("y")) {
            if (strDeliveryType.length() > 0) strDeliveryType.append(" , ");
            strDeliveryType.append(getString(R.string.medium));
        }
        if (pItem.getLargeDelivery() != null && pItem.getLargeDelivery().equals("y")) {
            if (strDeliveryType.length() > 0) strDeliveryType.append(" , ");
            strDeliveryType.append(getString(R.string.large));
        }
        binding.txtDeliveryType.setText(strDeliveryType.toString());

        isFlag = pItem.getIs_flag();
        binding.imgReportUser.setImageResource("y".equalsIgnoreCase(isFlag) ? R.mipmap.ic_report_user_filled : R.mipmap.ic_report_user);

        binding.ppaTxtUsername.setText(pItem.getUserName() != null && !pItem.getUserName().isEmpty() ? pItem.getUserName() : "-");
        binding.ppaTxtAddress.setText(pItem.getAddress() != null && !pItem.getAddress().isEmpty() ? pItem.getAddress() : "-");
        binding.ppaTxtPositiveRatings.setText(pItem.getPositiveRating() != null && !pItem.getPositiveRating().isEmpty() ?
                String.format("%s %s", pItem.getPositiveRating(), getString(R.string.positive_ratings)) : getString(R.string.no_positive_ratings));

        binding.ppaTxtWorkedOn.setText(pItem.getTaskAssigned() != null && !pItem.getTaskAssigned().isEmpty() ?
                String.format("%s %s %s", getString(R.string.str_worked_on), pItem.getTaskAssigned(), getString(R.string.tasks)) :
                String.format("%s 0 %s", getString(R.string.str_worked_on), getString(R.string.tasks)));

        binding.ppaTxtAboutUser.setText(pItem.getDescription() != null && !pItem.getDescription().isEmpty() ?
                Utils.decodeEmoji(pItem.getDescription()) : "-");

        binding.ppaTxtAvailableDays.setText(pItem.getAvailableDays().length() > 0 ? pItem.getAvailableDaysList() : "-");

        if (pItem.getAvailableTimeStart() != null && !pItem.getAvailableTimeStart().isEmpty() &&
                pItem.getAvailableTimeEnd() != null && !pItem.getAvailableTimeEnd().isEmpty()) {
            binding.ppaTxtServiceTime.setText(String.format("%s - %s", pItem.getAvailableTimeStart(), pItem.getAvailableTimeEnd()));
        } else {
            binding.ppaTxtServiceTime.setText("-");
        }

        binding.ppaTxtRating.setText(pItem.getStartRating() != null && !pItem.getStartRating().isEmpty() ?
                String.format("%s ", pItem.getStartRating()) : "0 ");
        binding.ppaTxtServiceCount.setText(pItem.getTotalService() != null && !pItem.getTotalService().isEmpty() ? pItem.getTotalService() : "-");

        binding.ppaTxtViewAllReviews.setOnClickListener(v -> {
            Intent i = new Intent(mContext, PartnerReviewsActivity.class);
            i.putExtra(Utils.PROVIDER_ID, pItem.getId());
            startActivity(i);
        });

        binding.ppaTxtViewAllServices.setOnClickListener(v -> {
            Intent i = new Intent(mContext, UserServicesActivity.class);
            i.putExtra(Utils.PROVIDER_ID, pItem.getId());
            i.putExtra("providerImage", pItem.getProfileImg());
            startActivity(i);
        });
    }


    private void initView() {
        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                CharSequence username = binding.ppaTxtUsername.getText();
                binding.txtHeaderName.setText(username.length() > 0 ? username : getString(R.string.provider_profile));
                binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbar_bg_color));
                binding.txtHeaderName.setTextColor(ContextCompat.getColor(this, R.color.white));
                binding.llMain.setPadding(0, 50, 0, 0);
            } else {
                binding.txtHeaderName.setText("");
                binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
                binding.llMain.setPadding(0, 0, 0, 0);
            }
        });

        getStatusBarHeight();
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        binding.imgBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void getStatusBarHeight() {
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        View contentView = window.findViewById(Window.ID_ANDROID_CONTENT);
        if (contentView != null) {
            int contentViewTop = contentView.getTop();
            int titleBarHeight = contentViewTop - statusBarHeight;
            binding.linHeader.setPadding(0, titleBarHeight + 50, 0, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (connectionManager != null) {
            try {
                connectionManager.unregisterReceiver();
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering connection manager", e);
            }
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}