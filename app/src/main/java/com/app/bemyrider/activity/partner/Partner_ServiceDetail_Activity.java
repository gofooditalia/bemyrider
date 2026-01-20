package com.app.bemyrider.activity.partner;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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
import com.app.bemyrider.databinding.PartnerActivityMyServiceDetailBinding;
import com.app.bemyrider.fragment.partner.Fragment_Partner_ServiceDetail;
import com.app.bemyrider.fragment.partner.Fragment_Partner_serviceImage;
import com.app.bemyrider.fragment.partner.Fragment_Partner_serviceReview;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.ProviderServiceDetailPOJO;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
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

public class Partner_ServiceDetail_Activity extends AppCompatActivity {

    private static final String TAG = "PServiceDetailActivity";
    private PartnerActivityMyServiceDetailBinding binding;
    private ProviderServiceDetailsItem serviceDetailData;

    private final Context mContext = this;
    private final int[] tabIcons = {
            R.drawable.tabicon_servicedetail_style,
            R.drawable.tabicon_review_style,
            R.drawable.tabicon_images_style
    };
    private WebServiceCall serviceDetailAsync, deleteServiceAsync;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.partner_activity_my_service_detail);

        initView();

        String userImg = PrefsUtil.with(mContext).readString("UserImg");
        if (userImg != null && !userImg.isEmpty()) {
            ImageRequest request = new ImageRequest.Builder(mContext)
                    .data(userImg)
                    .placeholder(R.drawable.loading)
                    .error(R.mipmap.user)
                    .target(binding.imgUprofile)
                    .build();
            Coil.imageLoader(mContext).enqueue(request);
        } else {
            binding.imgUprofile.setImageResource(R.mipmap.user);
        }

        binding.TxtName.setText(PrefsUtil.with(mContext).readString("UserName"));

        serviceCall();

        binding.imgDelete.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete_service)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    binding.imgDelete.setClickable(false);
                    binding.btnEditService.setClickable(false);
                    deleteServiceCall();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show());

        binding.btnEditService.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, AddNewService_Activity.class);
            intent.putExtra("data", serviceDetailData);
            startActivity(intent);
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void serviceCall() {
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("provider_service_id", getIntent().getStringExtra("providerServiceId"));

        serviceDetailAsync = new WebServiceCall(mContext, WebServiceUrl.MY_SERVICE_DETAILS, textParams,
                ProviderServiceDetailPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.rlMain.setVisibility(View.VISIBLE);
                if (status) {
                    ProviderServiceDetailPOJO pojo = (ProviderServiceDetailPOJO) obj;
                    if (pojo != null && pojo.getData() != null) {
                        serviceDetailData = pojo.getData();
                        binding.TxtRatingShow.setText(String.valueOf(pojo.getData().getAvgRating()));
                        setupViewPager();
                    } else {
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, Objects.toString(obj, getString(R.string.server_error)), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onAsync(Object asyncTask) { serviceDetailAsync = null; }
            @Override public void onCancelled() { serviceDetailAsync = null; }
        });
    }

    private void deleteServiceCall() {
        binding.imgDelete.setVisibility(View.GONE);
        binding.pgDelete.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("provider_service_id", getIntent().getStringExtra("providerServiceId"));
        textParams.put("user_id", PrefsUtil.with(this).readString("UserId"));

        deleteServiceAsync = new WebServiceCall(mContext, WebServiceUrl.MY_SERVICE_DELETE, textParams, CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgDelete.setVisibility(View.GONE);
                binding.imgDelete.setVisibility(View.VISIBLE);
                binding.imgDelete.setClickable(true);
                binding.btnEditService.setClickable(true);
                if (status) {
                    Intent i = new Intent(mContext, Partner_MyServices_Activity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(mContext, Objects.toString(obj, getString(R.string.server_error)), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onAsync(Object asyncTask) { deleteServiceAsync = null; }
            @Override public void onCancelled() { deleteServiceAsync = null; }
        });
    }

    private void initView() {
        String serviceName = getIntent().getStringExtra("serviceName");
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + (serviceName != null ? serviceName : ""), HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.service_detail));
        }

        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);
    }

    private void setupViewPager() {
        binding.pager.setAdapter(new ViewPagerAdapter(this));
        new TabLayoutMediator(binding.tabLayout, binding.pager, (tab, position) -> tab.setIcon(tabIcons[position])).attach();
        
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
             @Override
            public void onTabSelected(TabLayout.Tab tab) {
                 if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(Partner_ServiceDetail_Activity.this, R.color.button), PorterDuff.Mode.SRC_IN));
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
    protected void onDestroy() {
        if (connectionManager != null) {
            try {
                connectionManager.unregisterReceiver();
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
        Utils.cancelAsyncTask(serviceDetailAsync);
        Utils.cancelAsyncTask(deleteServiceAsync);
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
            switch(position) {
                case 1: fragment = new Fragment_Partner_serviceReview(); break;
                case 2: fragment = new Fragment_Partner_serviceImage(); break;
                default: fragment = new Fragment_Partner_ServiceDetail(); break;
            }
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 3; 
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
