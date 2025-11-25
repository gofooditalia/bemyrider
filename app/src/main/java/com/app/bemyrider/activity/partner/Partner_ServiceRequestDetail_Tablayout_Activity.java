package com.app.bemyrider.activity.partner;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.app.bemyrider.fragment.partner.Fragment_ServiceRequest_ReviewDetail;
import com.app.bemyrider.fragment.partner.Fragment_ServiceRequest_ServiceDetail;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.user.UserProfileActivity;
import com.app.bemyrider.databinding.PartnerActivityServiceRequestDetailBinding;
import com.app.bemyrider.model.DownloadInvoicePojo;
import com.app.bemyrider.model.ProviderHistoryPojoItem;
import com.app.bemyrider.model.ProviderServiceRequestPojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Modified by Hardik Talaviya on 3/12/19.
 */

public class Partner_ServiceRequestDetail_Tablayout_Activity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private PartnerActivityServiceRequestDetailBinding binding;

    private int[] tabIcons =
            {
                    R.drawable.tabicon_servicedetail_style,
                    R.drawable.tabicon_costsummary_style
            };
    private AsyncTask downloadInvoiceAsync, serviceDetailAsync;
    private Context context;
    private ProviderHistoryPojoItem serviceDetailData;
    private ConnectionManager connectionManager;
    private String serviceRequestId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(Partner_ServiceRequestDetail_Tablayout_Activity.this, R.layout.partner_activity_service_request_detail, null);

        initView();

        serviceCallGetDetail();

        /*---------- Redirection of customer profile -----------*/
        binding.imgUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(serviceDetailData.getIsActive().equalsIgnoreCase("du"))) {
                    Intent intent = new Intent(Partner_ServiceRequestDetail_Tablayout_Activity.this, UserProfileActivity.class);
                    intent.putExtra("userId", serviceDetailData.getCustomerId());
                    startActivity(intent);
                }
            }
        });

        binding.layoutName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(serviceDetailData.getIsActive().equalsIgnoreCase("du"))) {
                    Intent intent = new Intent(Partner_ServiceRequestDetail_Tablayout_Activity.this, UserProfileActivity.class);
                    intent.putExtra("userId", serviceDetailData.getCustomerId());
                    startActivity(intent);
                }
            }
        });
        /*---------- End Redirection of customer profile -----------*/

        binding.btnDownloadInvoiceCompleted.setOnClickListener(view -> serviceCallDownloadInvoice());

    }

    /*---------------- Download Invoice Api Call --------------------*/
    private void serviceCallDownloadInvoice() {
        binding.btnDownloadInvoiceCompleted.setClickable(false);
        binding.pgDownloadInvoice.setVisibility(View.VISIBLE);

        String url = WebServiceUrl.URL_DOWNLOAD_INVOICE + "/" + serviceRequestId;

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(Partner_ServiceRequestDetail_Tablayout_Activity.this).readString("UserId"));
        textParams.put("user_type", PrefsUtil.with(Partner_ServiceRequestDetail_Tablayout_Activity.this).readString("UserType"));
        textParams.put("request_type", "app");
        textParams.put("invoice", getString(R.string.invoice));
        textParams.put("service_start_time", getString(R.string.service_s_time));
        textParams.put("service_end_time", getString(R.string.service_e_time));
        textParams.put("booking_id", getString(R.string.booking_id));
        textParams.put("booking_details", getString(R.string.booking_details));
        textParams.put("booking_amount", getString(R.string.booking_amount));
        textParams.put("admin_fees", getString(R.string.admin_feesb));
        textParams.put("payment_type", (getString(R.string.payment_type)));
        textParams.put("total_payable_amount", getString(R.string.total_payable_amount));
        textParams.put("total_receivable_amount", getString(R.string.total_receivable_amount));
        textParams.put("wallet", getString(R.string.wallet));
        textParams.put("cash", getString(R.string.cash));
        textParams.put("complete", getString(R.string.status_completed));

        new WebServiceCall(Partner_ServiceRequestDetail_Tablayout_Activity.this, url,
                textParams, DownloadInvoicePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgDownloadInvoice.setVisibility(View.GONE);
                binding.btnDownloadInvoiceCompleted.setClickable(true);
                if (status) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(((DownloadInvoicePojo) obj).getData().getFileName()));
                    startActivity(i);
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                downloadInvoiceAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                downloadInvoiceAsync = null;
            }
        });
    }

    /*--------------- Get Service Detail Api Call --------------------*/
    private void serviceCallGetDetail() {
        binding.rlMain.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(Partner_ServiceRequestDetail_Tablayout_Activity.this).readString("UserId"));
        textParams.put("service_request_id", serviceRequestId);

        new WebServiceCall(Partner_ServiceRequestDetail_Tablayout_Activity.this,
                WebServiceUrl.URL_SERVICEDETAILS, textParams, ProviderServiceRequestPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.progress.setVisibility(View.GONE);
                        binding.rlMain.setVisibility(View.VISIBLE);
                        if (status) {
                            ProviderServiceRequestPojo result = (ProviderServiceRequestPojo) obj;
                            serviceDetailData = result.getData();
                            setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + result.getData().getServiceName(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                            manageData();
                        } else {
                            Toast.makeText(Partner_ServiceRequestDetail_Tablayout_Activity.this, (String) obj, Toast.LENGTH_SHORT).show();
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

    private void manageData() {
        binding.txtUserName.setText(String.format("%s %s", serviceDetailData.getCustomerFname(), serviceDetailData.getCustomerLname()));
        if (serviceDetailData.getCustomerImage().equalsIgnoreCase("")) {
            binding.imgUserProfile.setImageResource(R.mipmap.user);
        } else if (!serviceDetailData.getCustomerImage().equalsIgnoreCase("")) {
            try {
                Picasso.get().load(serviceDetailData.getCustomerImage()).placeholder(R.drawable.loading).into(binding.imgUserProfile);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        String statusDisplayName = serviceDetailData.getServiceStatusDisplayName();
        if (serviceDetailData.getServiceStatus().equalsIgnoreCase("completed")) {
            binding.btnTabAccept.setVisibility(View.GONE);
            binding.btnTabCancel.setVisibility(View.GONE);
            binding.btnTabDispute.setVisibility(View.GONE);
            binding.btnTabEndservice.setVisibility(View.GONE);
            binding.btnTabRejected.setVisibility(View.GONE);
            binding.llDownloadInvoice.setVisibility(View.VISIBLE);
            binding.txtTabServicestatus.setText(statusDisplayName);
            binding.txtTabServicestatus.setBackgroundResource(R.color.status_completed);
        }

        setupViewPager();

        binding.tabLayout.setupWithViewPager(binding.pager);

        setupTabIcons();

        if (getIntent().getBooleanExtra("fromReviewNotification", false)) {
            binding.pager.setCurrentItem(1);
        }
    }

    private void initView() {
        context = Partner_ServiceRequestDetail_Tablayout_Activity.this;

        serviceRequestId = getIntent().getStringExtra("serviceRequestId");

        try {
            String title = "";
            title = getIntent().getStringExtra("serviceName");
            if (title != null && !"".equals(title)) {
                setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + title, HtmlCompat.FROM_HTML_MODE_LEGACY));
            } else {
                setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>", HtmlCompat.FROM_HTML_MODE_LEGACY));
            }

            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", serviceDetailData);

        Fragment_ServiceRequest_ServiceDetail requestServiceDetail = new Fragment_ServiceRequest_ServiceDetail();
        Fragment_ServiceRequest_ReviewDetail requestReviewDetail = new Fragment_ServiceRequest_ReviewDetail();
        requestServiceDetail.setArguments(bundle);
        requestReviewDetail.setArguments(bundle);

        adapter.addFrag(requestServiceDetail, "ServiceDetail");
        adapter.addFrag(requestReviewDetail, "Summary");
        binding.pager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        binding.tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        binding.tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        binding.pager.setCurrentItem(tab.getPosition());
        int tabIconColor = ContextCompat.getColor(this, R.color.button);
        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(downloadInvoiceAsync);
        Utils.cancelAsyncTask(serviceDetailAsync);
        super.onDestroy();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //Display Title with Icon
            // return mFragmentTitleList.get(position);
            return null;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
