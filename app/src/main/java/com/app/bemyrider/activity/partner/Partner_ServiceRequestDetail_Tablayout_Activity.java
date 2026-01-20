package com.app.bemyrider.activity.partner;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.user.UserProfileActivity;
import com.app.bemyrider.databinding.PartnerActivityServiceRequestDetailBinding;
import com.app.bemyrider.fragment.partner.Fragment_ServiceRequest_ReviewDetail;
import com.app.bemyrider.fragment.partner.Fragment_ServiceRequest_ServiceDetail;
import com.app.bemyrider.model.DownloadInvoicePojo;
import com.app.bemyrider.model.ProviderHistoryPojoItem;
import com.app.bemyrider.model.ProviderServiceRequestPojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.LinkedHashMap;
import java.util.Objects;

import coil.Coil;
import coil.request.ImageRequest;

public class Partner_ServiceRequestDetail_Tablayout_Activity extends AppCompatActivity {

    private static final String TAG = "PServiceRequestDetail";
    private PartnerActivityServiceRequestDetailBinding binding;
    private ProviderHistoryPojoItem serviceDetailData;

    private final int[] tabIcons = {
            R.drawable.tabicon_servicedetail_style,
            R.drawable.tabicon_costsummary_style
    };
    private WebServiceCall downloadInvoiceAsync, serviceDetailAsync;
    private ConnectionManager connectionManager;
    private String serviceRequestId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.partner_activity_service_request_detail);

        initView();
        serviceCallGetDetail();

        View.OnClickListener profileClickListener = v -> {
            if (serviceDetailData != null && !"du".equalsIgnoreCase(serviceDetailData.getIsActive())) {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra("userId", serviceDetailData.getCustomerId());
                startActivity(intent);
            }
        };

        binding.imgUserProfile.setOnClickListener(profileClickListener);
        binding.layoutName.setOnClickListener(profileClickListener);

        binding.btnDownloadInvoiceCompleted.setOnClickListener(view -> serviceCallDownloadInvoice());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void serviceCallDownloadInvoice() {
        binding.btnDownloadInvoiceCompleted.setClickable(false);
        binding.pgDownloadInvoice.setVisibility(View.VISIBLE);

        String url = WebServiceUrl.URL_DOWNLOAD_INVOICE + "/" + serviceRequestId;
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("user_id", PrefsUtil.with(this).readString("UserId"));
        
        downloadInvoiceAsync = new WebServiceCall(this, url, textParams, DownloadInvoicePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgDownloadInvoice.setVisibility(View.GONE);
                binding.btnDownloadInvoiceCompleted.setClickable(true);
                if (status) {
                    DownloadInvoicePojo pojo = (DownloadInvoicePojo) obj;
                    if(pojo.getData() != null && pojo.getData().getFileName() != null) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(pojo.getData().getFileName()));
                        startActivity(i);
                    }
                }
            }
            @Override public void onAsync(Object asyncTask) { downloadInvoiceAsync = null; }
            @Override public void onCancelled() { downloadInvoiceAsync = null; }
        });
    }

    private void serviceCallGetDetail() {
        binding.rlMain.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("user_id", PrefsUtil.with(this).readString("UserId"));
        textParams.put("service_request_id", serviceRequestId);

        serviceDetailAsync = new WebServiceCall(this, WebServiceUrl.URL_SERVICEDETAILS, textParams, ProviderServiceRequestPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.progress.setVisibility(View.GONE);
                        binding.rlMain.setVisibility(View.VISIBLE);
                        if (status) {
                            ProviderServiceRequestPojo result = (ProviderServiceRequestPojo) obj;
                            if (result.getData() != null) {
                                serviceDetailData = result.getData();
                                if (getSupportActionBar() != null) {
                                    getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + result.getData().getServiceName(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                                }
                                manageData();
                            }
                        } else {
                            Toast.makeText(Partner_ServiceRequestDetail_Tablayout_Activity.this, Objects.toString(obj, getString(R.string.server_error)), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onAsync(Object asyncTask) { serviceDetailAsync = null; }
                    @Override public void onCancelled() { serviceDetailAsync = null; }
                });
    }

    private void manageData() {
        binding.txtUserName.setText(String.format("%s %s", serviceDetailData.getCustomerFname(), serviceDetailData.getCustomerLname()));

        ImageRequest.Builder profileBuilder = new ImageRequest.Builder(this)
                .placeholder(R.drawable.loading)
                .error(R.mipmap.user)
                .target(binding.imgUserProfile);

        String imageUrl = serviceDetailData.getCustomerImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            profileBuilder.data(imageUrl);
        } else {
            profileBuilder.data(R.mipmap.user);
        }
        Coil.imageLoader(this).enqueue(profileBuilder.build());

        if ("completed".equalsIgnoreCase(serviceDetailData.getServiceStatus())) {
            binding.btnTabAccept.setVisibility(View.GONE);
            binding.btnTabCancel.setVisibility(View.GONE);
            binding.btnTabDispute.setVisibility(View.GONE);
            binding.btnTabEndservice.setVisibility(View.GONE);
            binding.btnTabRejected.setVisibility(View.GONE);
            binding.llDownloadInvoice.setVisibility(View.VISIBLE);
            binding.txtTabServicestatus.setText(serviceDetailData.getServiceStatusDisplayName());
            binding.txtTabServicestatus.setBackgroundResource(R.color.status_completed);
        }

        setupViewPager();

        if (getIntent().getBooleanExtra("fromReviewNotification", false)) {
            binding.pager.setCurrentItem(1);
        }
    }

    private void initView() {
        serviceRequestId = getIntent().getStringExtra("serviceRequestId");

        String title = getIntent().getStringExtra("serviceName");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + (title != null ? title : getString(R.string.service_detail)), HtmlCompat.FROM_HTML_MODE_LEGACY));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        connectionManager = new ConnectionManager(this);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(this);
    }

    private void setupViewPager() {
        binding.pager.setAdapter(new ViewPagerAdapter(this));
        new TabLayoutMediator(binding.tabLayout, binding.pager, (tab, position) -> tab.setIcon(tabIcons[position])).attach();
        
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
             @Override
            public void onTabSelected(TabLayout.Tab tab) {
                 if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(Partner_ServiceRequestDetail_Tablayout_Activity.this, R.color.button), PorterDuff.Mode.SRC_IN));
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {
                if(tab.getIcon() != null) tab.getIcon().clearColorFilter();
            }
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        TabLayout.Tab selectedTab = binding.tabLayout.getTabAt(binding.tabLayout.getSelectedTabPosition());
        if(selectedTab != null && selectedTab.getIcon() != null) {
             selectedTab.getIcon().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.button), PorterDuff.Mode.SRC_IN));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
        Utils.cancelAsyncTask(downloadInvoiceAsync);
        Utils.cancelAsyncTask(serviceDetailAsync);
        super.onDestroy();
    }
    
    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull AppCompatActivity activity) { super(activity); }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("data", serviceDetailData);
            Fragment fragment;
            if (position == 0) {
                fragment = new Fragment_ServiceRequest_ServiceDetail();
            } else {
                fragment = new Fragment_ServiceRequest_ReviewDetail();
            }
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
