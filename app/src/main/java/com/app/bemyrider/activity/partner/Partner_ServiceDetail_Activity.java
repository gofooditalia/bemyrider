package com.app.bemyrider.activity.partner;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;

import com.app.bemyrider.fragment.partner.Fragment_Partner_ServiceDetail;
import com.app.bemyrider.fragment.partner.Fragment_Partner_serviceImage;
import com.app.bemyrider.fragment.partner.Fragment_Partner_serviceReview;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.app.bemyrider.Adapter.Partner.ViewPagerAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerActivityMyServiceDetailBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.ProviderServiceDetailPOJO;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 6/12/19.
 */

public class Partner_ServiceDetail_Activity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private PartnerActivityMyServiceDetailBinding binding;
    private ProviderServiceDetailsItem serviceDetailData;

    private Context mContext = Partner_ServiceDetail_Activity.this;
    private int[] tabIcons = {
            R.drawable.tabicon_servicedetail_style,
            R.drawable.tabicon_review_style,
            R.drawable.tabicon_images_style
    };
    private AsyncTask serviceDetailAsync, deleteServiceAsync;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(Partner_ServiceDetail_Activity.this, R.layout.partner_activity_my_service_detail, null);

        initView();

        try {
            Picasso.get().load(PrefsUtil.with(mContext).readString("UserImg"))
                    .placeholder(R.drawable.loading).into(binding.imgUprofile);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        binding.TxtName.setText(PrefsUtil.with(mContext).readString("UserName"));

        serviceCall();

        binding.imgDelete.setOnClickListener(view -> {
            AlertDialog alertDialog = new AlertDialog.Builder(Partner_ServiceDetail_Activity.this).create();
            alertDialog.setMessage(getResources().getString(R.string.confirm_delete_service));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.delete),
                    (dialog, which) -> {
                        dialog.dismiss();
                        binding.imgDelete.setClickable(false);
                        binding.btnEditService.setClickable(false);
                        deleteServiceCall();
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        });

        binding.btnEditService.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, AddNewService_Activity.class);
            intent.putExtra("data", serviceDetailData);
            startActivity(intent);
            finish();
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /*---------------- Service Detail Api Call ---------------------*/
    private void serviceCall() {
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("provider_service_id", getIntent().getStringExtra("providerServiceId"));
        //textParams.put("provider_id", getIntent().getStringExtra("providerServiceId"));

        new WebServiceCall(mContext, WebServiceUrl.MY_SERVICE_DETAILS, textParams,
                ProviderServiceDetailPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.rlMain.setVisibility(View.VISIBLE);
                if (status) {
                    Log.e("STATUS", "TRUE");
                    ProviderServiceDetailPOJO pojo = (ProviderServiceDetailPOJO) obj;

                    serviceDetailData = pojo.getData();

                    binding.TxtRatingShow.setText(String.valueOf(pojo.getData().getAvgRating()));
                    setupViewPager(binding.pager);
                    binding.tabLayout.setupWithViewPager(binding.pager);
                    setupTabIcons();

                } else {
                    Toast.makeText(mContext, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                serviceDetailAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                serviceDetailAsync = null;
            }
        });
    }

    /*--------------- Delete Service Api Call -------------------*/
    private void deleteServiceCall() {
        binding.imgDelete.setVisibility(View.GONE);
        binding.pgDelete.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("provider_service_id", getIntent().getStringExtra("providerServiceId"));
        textParams.put("user_id", PrefsUtil.with(Partner_ServiceDetail_Activity.this).readString("UserId"));

        new WebServiceCall(mContext, WebServiceUrl.MY_SERVICE_DELETE, textParams, CommonPojo.class, false, new WebServiceCall.OnResultListener() {
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
                    Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                deleteServiceAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                deleteServiceAsync = null;
            }
        });
    }

    private void initView() {

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getIntent().getStringExtra("serviceName"),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.service_detail));
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);
    }

    private void setupTabIcons() {
        binding.tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        binding.tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        binding.tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        binding.pager.setCurrentItem(tab.getPosition());
        int tabIconColor = ContextCompat.getColor(this, R.color.button);
        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle b = new Bundle();
        b.putSerializable("data", serviceDetailData);
        Fragment serviceDetail = new Fragment_Partner_ServiceDetail();
        Fragment serviceReview = new Fragment_Partner_serviceReview();
        Fragment serviceImage = new Fragment_Partner_serviceImage();
        serviceDetail.setArguments(b);
        serviceReview.setArguments(b);
        serviceImage.setArguments(b);
        adapter.addFrag(serviceDetail, "ServiceDetail");
        adapter.addFrag(serviceReview, "Review");
        adapter.addFrag(serviceImage, "Images");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(serviceDetailAsync);
        Utils.cancelAsyncTask(deleteServiceAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
