package com.app.bemyrider.activity.user;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.os.Build;
import android.graphics.Color;
import androidx.core.view.WindowInsetsControllerCompat;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.bemyrider.activity.partner.Partner_DisputeDetail_Activity;
import com.app.bemyrider.fragment.user.BookedDetailFragment;
import com.app.bemyrider.fragment.user.ImageFragment;
import com.app.bemyrider.fragment.user.ReviewFragment;
import com.app.bemyrider.fragment.user.UserDetailFragment;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityBookedServiceDetailBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.DownloadInvoicePojo;
import com.app.bemyrider.model.ProviderServiceDetailPOJO;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.model.ProviderServiceReviewDataItem;
import com.app.bemyrider.model.WithoutBalancePojo;
import com.app.bemyrider.model.partner.EditProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Modified by Hardik Talaviya on 11/12/19.
 */

public class BookedServiceDetailActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final String TAG = "BookedServiceDetail";
    private ActivityBookedServiceDetailBinding binding;
    //    private ArrayAdapter hoursAdapter;
    private int defaultTab = 0;
    private ProviderServiceDetailsItem serviceDetailData;
    private ArrayList<ProviderServiceReviewDataItem> reviewArrayList = new ArrayList<>();
    /*private String[] hours_array = {"Select Hours*", "1 Hour", "2 Hours", "3 Hours", "4 Hours",
            "5 Hours", "6 Hours", "7 Hours", "8 Hours", "9 Hours", "10 Hours", "11 Hours",
            "12 Hours", "13 Hours", "14 Hours", "15 Hours", "16 Hours", "17 Hours", "18 Hours",
            "19 Hours", "20 Hours", "21 Hours", "22 Hours", "23 Hours", "24 Hours"};*/
    private int[] tabIcons = {
            R.drawable.tabicon_servicedetail_style,
            R.drawable.tabicon_userdetail_style,
            R.drawable.tabicon_review_style,
            R.drawable.tabicon_images_style
    };
    private AsyncTask extendPaymentAsync, cancelServiceAsync, downloadInvoiceAsync,
            extendServiceAsync, giveReviewAsync, bookNowAsync, serviceDetailAsync,
            actionFavouriteAsync;
    private Context context;
    private ConnectionManager connectionManager;

    ActivityResultLauncher<Intent> myActivityResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(BookedServiceDetailActivity.this, R.layout.activity_booked_service_detail, null);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        init();

        getDetails();
    }

    /*--------------------- Get Details Api Call ------------------------*/
    protected void getDetails() {
        binding.rlMain.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("provider_service_id", getIntent().getStringExtra("providerServiceId"));
        //textParams.put("provider_id", getIntent().getStringExtra("providerServiceId"));
        textParams.put("service_request_id", getIntent().getStringExtra("serviceRequestId"));
        textParams.put("user_id", PrefsUtil.with(BookedServiceDetailActivity.this).readString("UserId"));

        new WebServiceCall(this, WebServiceUrl.MY_SERVICE_DETAILS, textParams,
                ProviderServiceDetailPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.rlMain.setVisibility(View.VISIBLE);
                if (status) {
                    ProviderServiceDetailPOJO mainDetail = (ProviderServiceDetailPOJO) obj;
                    serviceDetailData = mainDetail.getData();
                    reviewArrayList.clear();
                    reviewArrayList.addAll(mainDetail.getData().getReviewData());
                    Log.e(TAG, "Review Item Size :: " + serviceDetailData.getReviewData().size());
                    setData();

                } else {
                    Toast.makeText(BookedServiceDetailActivity.this, (String) obj, Toast.LENGTH_LONG).show();
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

    /*----------- Favourite/UnFavourite Service Api Call ------------*/
    protected void favouriteToggle() {
        binding.imgFav.setVisibility(View.GONE);
        binding.pgFavourite.setVisibility(View.VISIBLE);

        String url = WebServiceUrl.URL_FAVOURITETOGGLE;
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("service_id", getIntent().getStringExtra("providerServiceId"));
        // textParams.put("service_id", serviceDetailData.getServiceId());
        textParams.put("provider_id", serviceDetailData.getProviderId());


        textParams.put("user_id", PrefsUtil.with(BookedServiceDetailActivity.this).readString("UserId"));
        textParams.put("delivery_type", serviceDetailData.getDeliveryType());
        textParams.put("request_type", serviceDetailData.getRequestType());

        if (binding.imgFav.getTag().equals("1")) {
            textParams.put("fvrt_val", "0");
        } else if (binding.imgFav.getTag().equals("0")) {
            textParams.put("fvrt_val", "1");
        } else {
            return;
        }

        new WebServiceCall(this, url, textParams, EditProfilePojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.pgFavourite.setVisibility(View.GONE);
                        binding.imgFav.setVisibility(View.VISIBLE);
                        if (status) {
                            EditProfilePojo editProfilePojo = (EditProfilePojo) obj;
                            Toast.makeText(context, ((EditProfilePojo) obj).getMessage(), Toast.LENGTH_SHORT).show();
                            if (binding.imgFav.getTag().equals("1")) {
                                Picasso.get().load(R.mipmap.ic_heart_fill).placeholder(R.drawable.loading).into(binding.imgFav);
                                binding.imgFav.setTag("0");
                            } else if (binding.imgFav.getTag().equals("0")) {
                                Picasso.get().load(R.mipmap.ic_heart_empty).placeholder(R.drawable.loading).into(binding.imgFav);
                                binding.imgFav.setTag("1");
                            }
                        } else {
                            Toast.makeText(BookedServiceDetailActivity.this, (String) obj, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        actionFavouriteAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        actionFavouriteAsync = null;
                    }
                });
    }

    /*-------------- Extend Payment Api Call ----------------*/
    private void serviceCallExtendPayment() {
        binding.btnExtendPayment.setClickable(false);
        binding.pgExtendPayment.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("extend_id", serviceDetailData.getExtendServiceData().get(0).getExtendId());
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(serviceDetailData.getServiceRequestId().getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            textParams.put("service_request_token", hexString.toString());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        new WebServiceCall(BookedServiceDetailActivity.this,
                WebServiceUrl.URL_EXTEND_SERVICE_PAYMENT, textParams,
                CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgExtendPayment.setVisibility(View.GONE);
                binding.btnExtendPayment.setClickable(true);
                if (status) {
                    PrefsUtil.with(context).write("service", "true");
                    Intent intent = new Intent(context, CustomerHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    /*Intent i = new Intent(context, ServiceHistoryActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();*/
                } else {
                    Toast.makeText(BookedServiceDetailActivity.this, (String) obj,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                extendPaymentAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                extendPaymentAsync = null;
            }
        });
    }

    /*----------------- Cancel Service Api Call ------------------*/
    private void serviceCallCancelService(final DialogInterface dialogInterface) {
        binding.btnCancel.setClickable(false);
        binding.pgCancel.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("service_id", getIntent().getStringExtra("serviceRequestId"));

        textParams.put("user_id", PrefsUtil.with(BookedServiceDetailActivity.this).readString("UserId"));

        new WebServiceCall(BookedServiceDetailActivity.this,
                WebServiceUrl.URL_CANCEL_SERVICE, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.pgCancel.setVisibility(View.GONE);
                        binding.btnCancel.setClickable(true);
                        if (status) {
                            dialogInterface.dismiss();
                            PrefsUtil.with(context).write("service", "true");
                            Intent intent = new Intent(context, CustomerHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            /*Intent i = new Intent(context, ServiceHistoryActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();*/
                        } else {
                            Toast.makeText(BookedServiceDetailActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        cancelServiceAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        cancelServiceAsync = null;
                    }
                });
    }

    /*------------------- Download Invoice Api Call --------------------*/
    private void serviceCallDownloadInvoice() {
        binding.btnDownloadInvoice.setClickable(false);
        binding.pgDownloadInvoice.setVisibility(View.VISIBLE);

        String url = WebServiceUrl.URL_DOWNLOAD_INVOICE + "/" + serviceDetailData.getServiceRequestId();

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(BookedServiceDetailActivity.this).readString("UserId"));
        textParams.put("user_type", PrefsUtil.with(BookedServiceDetailActivity.this).readString("UserType"));
        textParams.put("request_type", "app");
        textParams.put("invoice", getString(R.string.invoice));
        textParams.put("service_start_time", getString(R.string.service_s_time));
        textParams.put("service_end_time", getString(R.string.service_e_time));
        textParams.put("booking_id", getString(R.string.booking_id));
        textParams.put("booking_details", getString(R.string.booking_details));
        textParams.put("booking_amount", getString(R.string.booking_amount));
        textParams.put("admin_fees", getString(R.string.admin_feesb));
        textParams.put("payment_type", getString(R.string.payment_type));
        textParams.put("total_payable_amount", getString(R.string.total_payable_amount));
        textParams.put("total_receivable_amount", getString(R.string.total_receivable_amount));
        textParams.put("wallet", getString(R.string.wallet));
        textParams.put("cash", getString(R.string.cash));
        textParams.put("complete", getString(R.string.status_completed));


        new WebServiceCall(BookedServiceDetailActivity.this, url, textParams,
                DownloadInvoicePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgDownloadInvoice.setVisibility(View.GONE);
                binding.btnDownloadInvoice.setClickable(true);
                if (status) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(((DownloadInvoicePojo) obj).getData().getFileName()));
                    startActivity(i);
                } else {
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
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

    /*----------------- Extend Service Api Call ---------------------*/
    private void serviceCallExtendService(String selectedHours, final Dialog dialog) {
        (dialog.findViewById(R.id.btn_extend_send)).setClickable(false);
        (dialog.findViewById(R.id.pgSend)).setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("txt_service_request_id", serviceDetailData.getServiceRequestId());
        textParams.put("sel_hours", selectedHours);

        new WebServiceCall(BookedServiceDetailActivity.this,
                WebServiceUrl.URL_EXTEND_SERVICE, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        (dialog.findViewById(R.id.pgSend)).setVisibility(View.GONE);
                        (dialog.findViewById(R.id.btn_extend_send)).setClickable(true);
                        if (status) {
                            dialog.dismiss();
                            PrefsUtil.with(context).write("service", "true");
                            Intent intent = new Intent(context, CustomerHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            /*Intent i = new Intent(context, ServiceHistoryActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();*/
                        } else {
                            Toast.makeText(BookedServiceDetailActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        extendServiceAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        extendServiceAsync = null;
                    }
                });
    }

    /*-------------------- Give Review Api Call ----------------------*/
    private void serviceCallGiveReview(String serviceRequestId, float rating, String desc, final Dialog dialog) {
        dialog.setCancelable(false);
        (dialog.findViewById(R.id.btn_save_rating)).setClickable(false);
        (dialog.findViewById(R.id.pgSaveRating)).setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(BookedServiceDetailActivity.this).readString("UserId"));
        textParams.put("service_id", serviceRequestId);
        textParams.put("txt_ratting", String.valueOf(rating));
        textParams.put("txt_description", Utils.encodeEmoji(desc));

        new WebServiceCall(BookedServiceDetailActivity.this, WebServiceUrl.URL_ADD_REVIEW,
                textParams, CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                (dialog.findViewById(R.id.pgSaveRating)).setVisibility(View.GONE);
                (dialog.findViewById(R.id.btn_save_rating)).setClickable(true);
                if (status) {
                    dialog.dismiss();
                    binding.llBtnAddReview.setVisibility(View.GONE);
                    defaultTab = 2;
                    getDetails();
                } else {
                    Toast.makeText(BookedServiceDetailActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                giveReviewAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                giveReviewAsync = null;
            }
        });
    }

    /*------------------ Book Now Api Call ---------------------*/
    private void serviceCallBookNow() {
        binding.btnBook.setClickable(false);
        binding.pgBookNow.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(BookedServiceDetailActivity.this).readString("UserId"));
        textParams.put("service_id", getIntent().getStringExtra("serviceRequestId"));

        new WebServiceCall(BookedServiceDetailActivity.this,
                WebServiceUrl.URL_BOOK_SERVICE, textParams, WithoutBalancePojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.pgBookNow.setVisibility(View.GONE);
                        binding.btnBook.setClickable(true);
                        if (status) {
                            WithoutBalancePojo pojo = (WithoutBalancePojo) obj;
                            if (pojo.getData().getCustomerCommission().equals("")) {
                                PrefsUtil.with(context).write("service", "true");
                                Intent intent = new Intent(context, CustomerHomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                                /*Intent i = new Intent(context, ServiceHistoryActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();*/
                            } else {
                                if ((serviceDetailData.getServiceMasterType().equalsIgnoreCase("hourly"))) {
                                    PrefsUtil.with(BookedServiceDetailActivity.this).write("sel_hours_wallet", serviceDetailData.getBookingHours());
                                }
                                Intent intent = new Intent(BookedServiceDetailActivity.this, StripePaymentActivity.class);
                                intent.putExtra("sub_total", pojo.getData().getSubTotal());
                                intent.putExtra("fees", pojo.getData().getTotalFees());
                                intent.putExtra("booking_amount", pojo.getData().getBookingAmount());
                                intent.putExtra("customer_commission_wallet", pojo.getData().getCustomerCommission());
                                intent.putExtra("provider_commission", pojo.getData().getProviderCommission());
                                intent.putExtra("total_amount_to_charge", pojo.getData().getTotalAmountToCharge());
                                intent.putExtra("total_amount_to_charge_full", pojo.getData().getTotalAmountToChargeFull());
                                // payment_url param avse
                                // intent.putExtra("payment_url", pojo.getData().getPaymentUrl());
                                intent.putExtra("paymentIntentClientSecret", pojo.getData().getPaymentIntentClientSecret());
                                intent.putExtra("serviceId", getIntent().getStringExtra("serviceRequestId"));
                                intent.putExtra("serviceMasterType", serviceDetailData.getServiceMasterType());
                                myActivityResultLauncher.launch(intent);
                            }
                        } else {
                            Toast.makeText(BookedServiceDetailActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        bookNowAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        bookNowAsync = null;
                    }
                });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle dataBundle = new Bundle();
        dataBundle.putSerializable("data", serviceDetailData);

        Fragment detailFragment = new BookedDetailFragment();
        Fragment userDetailFragment = new UserDetailFragment();
        Fragment reviewFragment = new ReviewFragment(reviewArrayList);
        Fragment imageFragment = new ImageFragment();

        detailFragment.setArguments(dataBundle);
        userDetailFragment.setArguments(dataBundle);
        reviewFragment.setArguments(dataBundle);
        imageFragment.setArguments(dataBundle);
        adapter.addFrag(detailFragment, "ServiceDetail");
        adapter.addFrag(userDetailFragment, "UserDetail");
        adapter.addFrag(reviewFragment, "Review");
        adapter.addFrag(imageFragment, "Images");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        adapter.notifyDataSetChanged();
    }

    private void setupTabIcons() {
        binding.tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        binding.tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        binding.tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        binding.tabLayout.getTabAt(3).setIcon(tabIcons[3]);
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

    private void init() {
        context = BookedServiceDetailActivity.this;

        try {
            String title = "";
            title = getIntent().getStringExtra("serviceName");
            if (title != null && !"".equals(title)) {
                setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + title,HtmlCompat.FROM_HTML_MODE_LEGACY));
            } else {
                setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" ,HtmlCompat.FROM_HTML_MODE_LEGACY));
            }

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        myActivityResult();

    }

    private void myActivityResult() {
        myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    PrefsUtil.with(context).write("service", "true");
                    Intent intent = new Intent(context, CustomerHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    /*Intent intent = new Intent();
                    setResult(RESULT_OK,intent);
                    finish();*/
                }
            }
        });
    }

    private void setData() {
        String statusDisplayName = serviceDetailData.getServiceStatusDisplayName();

        if (!Utils.isNullOrEmpty(serviceDetailData.getProviderImage())) {
            Picasso.get().load(serviceDetailData.getProviderImage())
                    .placeholder(R.drawable.loading).into(binding.imgProfile);
        }

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + serviceDetailData.getServiceName(),HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (Integer.parseInt(serviceDetailData.getTotalFavorite()) > 0) {
            binding.imgFav.setImageResource(R.mipmap.ic_heart_fill);
            binding.imgFav.setTag("0");
        } else {
            binding.imgFav.setImageResource(R.mipmap.ic_heart_empty);
            binding.imgFav.setTag("1");
        }

        binding.txtRatingShow.setText(String.valueOf(serviceDetailData.getAvgRating()));
        binding.txtName.setText(String.format("%s %s", serviceDetailData.getFirstName(), serviceDetailData.getLastName()));

        setupViewPager(binding.pager);

        binding.tabLayout.setupWithViewPager(binding.pager);

        setupTabIcons();

        if (defaultTab != 0) {
            binding.pager.setCurrentItem(defaultTab);
            defaultTab = 0;
        }

        Log.e(TAG, "setData Status :: " + serviceDetailData.getServiceStatus());
        if (serviceDetailData.getServiceStatus().equalsIgnoreCase("hired")) {
            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_hired);

            if (serviceDetailData.getPaymentPreference().equalsIgnoreCase("wallet")) {
                binding.llDispute.setVisibility(View.VISIBLE);
                binding.llViewDispute.setVisibility(View.VISIBLE);
            }

            binding.llMessage.setVisibility(View.VISIBLE);
            binding.llViewMessage.setVisibility(View.VISIBLE);

            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.VISIBLE);

            binding.linlayBookAgain.setVisibility(View.GONE);

        }

        if (serviceDetailData.getServiceStatus().equals("pending")) {

            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_pending);

            binding.llDispute.setVisibility(View.INVISIBLE);
            binding.llViewDispute.setVisibility(View.INVISIBLE);
            binding.llMessage.setVisibility(View.INVISIBLE);
            binding.llViewMessage.setVisibility(View.INVISIBLE);

            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);

            binding.linlayBookAgain.setVisibility(View.GONE);
        }

        if (serviceDetailData.getServiceStatus().equals("accepted")) {

            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_accepted);

            binding.llDispute.setVisibility(View.INVISIBLE);
            binding.llViewDispute.setVisibility(View.INVISIBLE);
            binding.llBtnBookNow.setVisibility(View.VISIBLE);

            binding.linlayBookAgain.setVisibility(View.GONE);
        }

        if (serviceDetailData.getServiceStatus().equals("expired")) {
            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_expired);

            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);

            binding.linlayBookAgain.setVisibility(View.GONE);
        }

        if (serviceDetailData.getServiceStatus().equals("rejected")) {

            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_rejected);

            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);

            binding.linlayBookAgain.setVisibility(View.GONE);
        }

        if (serviceDetailData.getServiceStatus().equals("completed")) {

            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_completed);

            binding.llMessage.setVisibility(View.GONE);
            binding.llViewMessage.setVisibility(View.GONE);

            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);
            if (serviceDetailData.getIsReviewGiven().equals("n")) {
                binding.llBtnAddReview.setVisibility(View.VISIBLE);
            }
//            if (PrefsUtil.with(context).readString("UserType").equals("p"))
            binding.llBtnDownloadInvoice.setVisibility(View.VISIBLE);

            binding.linlayBookAgain.setVisibility(View.GONE);

        }

        if (serviceDetailData.getServiceStatus().equals("cancelled")) {

            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_cancelled);

            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);

            binding.linlayBookAgain.setVisibility(View.GONE);
        }

        if (serviceDetailData.getServiceStatus().equals("closed")) {

            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_closed);

            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);

            binding.linlayBookAgain.setVisibility(View.GONE);
        }

        if (serviceDetailData.getServiceStatus().equals("dispute")) {

            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_dispute);

            binding.llMessage.setVisibility(View.VISIBLE);
            binding.llViewMessage.setVisibility(View.VISIBLE);

            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);

            binding.linlayBookAgain.setVisibility(View.GONE);
        }

        if (serviceDetailData.getServiceStatus().equals("ongoing")) {

            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_ongoing);

            if (serviceDetailData.getPaymentPreference().equalsIgnoreCase("wallet")) {
                binding.llDispute.setVisibility(View.VISIBLE);
                binding.llViewDispute.setVisibility(View.VISIBLE);
            }

            binding.llMessage.setVisibility(View.VISIBLE);
            binding.llViewMessage.setVisibility(View.VISIBLE);

            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);

            binding.linlayBookAgain.setVisibility(View.VISIBLE);

//            bookingEndTime

            if (serviceDetailData.getServiceMasterType().equalsIgnoreCase("hourly")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");
                Date d1 = null;
                try {
                    d1 = dateFormat.parse(serviceDetailData.getEndTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar calendar = Calendar.getInstance();
                if (d1 != null) {
                    calendar.setTime(d1);
                }
                calendar.add(Calendar.MINUTE, -30);
                Log.e("ACtual Time", calendar.getTime().toString());

                Calendar calendar1 = Calendar.getInstance();
                /*if (calendar.getTime().before(calendar1.getTime())) {
                    binding.llBtnExtendService.setVisibility(View.VISIBLE);
                } */
                Log.e("CALENDER TIME", calendar1.getTime().toString());
            }

            if (serviceDetailData.getExtendServiceData().size() > 0) {
                binding.llBtnExtendService.setVisibility(View.GONE);
                if (serviceDetailData.getExtendServiceData().get(0)
                        .getServiceStatus().equals("accepted")) {
                    binding.llBtnExtendPayment.setVisibility(View.VISIBLE);
                }
            }

        }

        binding.btnExtendPayment.setOnClickListener(view -> serviceCallExtendPayment());

        binding.btnCancel.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(BookedServiceDetailActivity.this);
            builder.setMessage(R.string.are_you_cancel)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            serviceCallCancelService(dialogInterface);
                        }
                    })
                    .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();

        });

        binding.btnDownloadInvoice.setOnClickListener(view -> {
//            if (PrefsUtil.with(context).readString("UserType").equals("p"))
            serviceCallDownloadInvoice();
        });

        binding.btnExtendService.setOnClickListener(view -> {
            final Dialog dialog = new Dialog(BookedServiceDetailActivity.this);
            dialog.setContentView(R.layout.extend_service_dialog);
            dialog.setCancelable(false);
            dialog.setTitle(R.string.extend_service);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(dialog.getWindow().getAttributes());
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);

            final Spinner spinner = dialog.findViewById(R.id.spinner_extend_hours);

//            hoursAdapter = new ArrayAdapter<>(BookedServiceDetailActivity.this, android.R.layout.simple_spinner_item, hours_array);
//            spinner.setAdapter(hoursAdapter);
//            hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            AppCompatButton btn_extend_cancel = dialog.findViewById(R.id.btn_extend_cancel);
            AppCompatButton btn_extend_send = dialog.findViewById(R.id.btn_extend_send);

            btn_extend_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (spinner.getSelectedItemPosition() != 0) {
                        String tmp = String.valueOf(spinner.getSelectedItemPosition());
//                            String hour[] =tmp.split(" ");
                        Utils.hideSoftKeyboard(BookedServiceDetailActivity.this);
                        btn_extend_send.setClickable(false);
                        serviceCallExtendService(tmp, dialog);
//                            serviceCallSendPraposal(hour[0], editText.getText().toString().trim(), dialog);

                    } else {
                        Toast.makeText(BookedServiceDetailActivity.this, R.string.please_select_hours_first, Toast.LENGTH_SHORT).show();
                    }
                }
            });

//            btn_extend_send.setOnClickListener(view1 -> serviceCallExtendService(spinner.getSelectedItemPosition()
//                    + " " + getString(R.string.hours), dialog));

            btn_extend_cancel.setOnClickListener(view12 -> dialog.cancel());
            dialog.show();
        });

        binding.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PartnerProfileActivity.class);
                i.putExtra(Utils.PROVIDER_ID, serviceDetailData.getProviderId());
                startActivity(i);
            }
        });

        binding.layoutProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PartnerProfileActivity.class);
                i.putExtra(Utils.PROVIDER_ID, serviceDetailData.getProviderId());
                startActivity(i);
            }
        });

        binding.llFavourite.setOnClickListener(v -> favouriteToggle());

        binding.llDispute.setOnClickListener(v -> {
            Intent intent = new Intent(BookedServiceDetailActivity.this, RaiseDisputeActivity.class);
            intent.putExtra("RequestID", getIntent().getStringExtra("serviceRequestId"));
            startActivity(intent);
            finish();

        });

        binding.llMessage.setOnClickListener(v -> {
            if (serviceDetailData.getServiceStatus().equals("dispute")) {
                Intent intent = new Intent(BookedServiceDetailActivity.this, Partner_DisputeDetail_Activity.class);
                intent.putExtra("DisputeId", serviceDetailData.getDisputeId());
                startActivity(intent);
            } else {
                Intent intent = new Intent(BookedServiceDetailActivity.this, MessageDetailActivity.class);
                intent.putExtra("to_user", serviceDetailData.getProviderId());
                intent.putExtra("master_id", serviceDetailData.getServiceId());
                intent.putExtra("service_booking_id", serviceDetailData.getServiceBookingId());
                startActivity(intent);
            }
        });

        binding.btnAddReview.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(BookedServiceDetailActivity.this);
            dialog.setContentView(R.layout.activity_rating_review);
            dialog.setTitle(getString(R.string.add_review));

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(dialog.getWindow().getAttributes());
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);

            final RatingBar ratingBar = dialog.findViewById(R.id.rate_provider);
            final EditText editText = dialog.findViewById(R.id.edt_review_provider);

            Button button = dialog.findViewById(R.id.btn_save_rating);
            button.setOnClickListener(view -> {
                if (ratingBar.getRating() != 0.0) {
                    if (!editText.getText().toString().trim().equals("")) {
                        Utils.hideSoftKeyboard(BookedServiceDetailActivity.this);
                        serviceCallGiveReview(serviceDetailData.getServiceRequestId(),
                                ratingBar.getRating(), editText.getText().toString().trim(), dialog);
                    } else {
                        editText.setError(getString(R.string.please_provide_review));
                    }
                } else {
                    Toast.makeText(BookedServiceDetailActivity.this,
                            R.string.please_select_rating, Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        });

        binding.btnBook.setOnClickListener(v -> serviceCallBookNow());

        binding.btnBookAgain.setOnClickListener(v -> {
            PrefsUtil.with(context).write("request_type", serviceDetailData.getRequestType());
            PrefsUtil.with(context).write("delivery_type", serviceDetailData.getDeliveryType());

            Intent intent = new Intent(context, ServiceDetailActivity.class);
            intent.putExtra("providerServiceId", getIntent().getStringExtra("providerServiceId"));
            intent.putExtra("providerId",serviceDetailData.getProviderId());
            startActivity(intent);
//            myIntentActivityResultLauncher.launch(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
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
        Utils.cancelAsyncTask(extendPaymentAsync);
        Utils.cancelAsyncTask(cancelServiceAsync);
        Utils.cancelAsyncTask(downloadInvoiceAsync);
        Utils.cancelAsyncTask(extendServiceAsync);
        Utils.cancelAsyncTask(giveReviewAsync);
        Utils.cancelAsyncTask(bookNowAsync);
        Utils.cancelAsyncTask(serviceDetailAsync);
        Utils.cancelAsyncTask(actionFavouriteAsync);
        super.onDestroy();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragmentList = new ArrayList<>();
        private List<String> mFragmentTitleList = new ArrayList<>();


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
