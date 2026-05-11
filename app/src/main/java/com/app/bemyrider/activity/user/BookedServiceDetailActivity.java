package com.app.bemyrider.activity.user;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.app.bemyrider.activity.partner.Partner_MyServices_Activity;
import com.app.bemyrider.fragment.user.BookedDetailFragment;
import com.app.bemyrider.fragment.user.ImageFragment;
import com.app.bemyrider.fragment.user.ReviewFragment;
import com.app.bemyrider.fragment.user.UserDetailFragment;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.viewmodel.BookedServiceDetailViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.databinding.ActivityBookedServiceDetailBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.DownloadInvoicePojo;
import com.app.bemyrider.model.ProviderServiceDetailPOJO;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.model.ProviderServiceReviewDataItem;
import com.app.bemyrider.model.WithoutBalancePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.material.tabs.TabLayout;

import coil.Coil;
import coil.request.ImageRequest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Modified by Hardik Talaviya on 11/12/19.
 */

public class BookedServiceDetailActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final String TAG = "BookedServiceDetail";
    private ActivityBookedServiceDetailBinding binding;
    private int defaultTab = 0;
    private ProviderServiceDetailsItem serviceDetailData;
    private ArrayList<ProviderServiceReviewDataItem> reviewArrayList = new ArrayList<>();
    private int[] tabIcons = {
            R.drawable.tabicon_servicedetail_style,
            R.drawable.tabicon_userdetail_style,
            R.drawable.tabicon_review_style,
            R.drawable.tabicon_images_style
    };
    private BookedServiceDetailViewModel viewModel;
    private Context context;
    private ConnectionManager connectionManager;
    private DialogInterface pendingCancelDialog;
    private Dialog pendingExtendDialog;
    private Dialog pendingReviewDialog;

    ActivityResultLauncher<Intent> myActivityResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(BookedServiceDetailActivity.this, R.layout.activity_booked_service_detail);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        init();
        observeViewModel();
        getDetails();
    }

    private void observeViewModel() {
        viewModel.getServiceDetail().observe(this, pojo -> {
            binding.progress.setVisibility(View.GONE);
            binding.rlMain.setVisibility(View.VISIBLE);
            if (pojo != null && pojo.isStatus() && pojo.getData() != null) {
                serviceDetailData = pojo.getData();
                reviewArrayList.clear();
                reviewArrayList.addAll(pojo.getData().getReviewData());
                setData();
            }
        });

        viewModel.getFavouriteResult().observe(this, pojo -> {
            binding.pgFavourite.setVisibility(View.GONE);
            binding.imgFav.setVisibility(View.VISIBLE);
            if (pojo != null && pojo.isStatus()) {
                Toast.makeText(context, pojo.getMessage(), Toast.LENGTH_SHORT).show();
                if (binding.imgFav.getTag().equals("1")) {
                    ImageRequest request = new ImageRequest.Builder(context).data(R.mipmap.ic_heart_fill).placeholder(R.drawable.loading).target(binding.imgFav).build();
                    Coil.imageLoader(context).enqueue(request);
                    binding.imgFav.setTag("0");
                } else if (binding.imgFav.getTag().equals("0")) {
                    ImageRequest request = new ImageRequest.Builder(context).data(R.mipmap.ic_heart_empty).placeholder(R.drawable.loading).target(binding.imgFav).build();
                    Coil.imageLoader(context).enqueue(request);
                    binding.imgFav.setTag("1");
                }
            }
        });

        viewModel.getCancelResult().observe(this, pojo -> {
            binding.pgCancel.setVisibility(View.GONE);
            binding.btnCancel.setClickable(true);
            if (pojo == null) return;
            if (pojo.isStatus()) {
                if (pendingCancelDialog != null) pendingCancelDialog.dismiss();
                PrefsUtil.with(context).write("service", "true");
                Intent intent = new Intent(context, CustomerHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                if (pendingCancelDialog instanceof Dialog) {
                    Dialog d = (Dialog) pendingCancelDialog;
                    d.findViewById(R.id.pg_confirm).setVisibility(View.GONE);
                    d.findViewById(R.id.btn_yes).setVisibility(View.VISIBLE);
                    d.findViewById(R.id.btn_no).setEnabled(true);
                }
            }
        });

        viewModel.getExtendPaymentResult().observe(this, pojo -> {
            binding.pgExtendPayment.setVisibility(View.GONE);
            binding.btnExtendPayment.setClickable(true);
            if (pojo != null && pojo.isStatus()) {
                PrefsUtil.with(context).write("service", "true");
                Intent intent = new Intent(context, CustomerHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getExtendServiceResult().observe(this, pojo -> {
            if (pendingExtendDialog != null) {
                pendingExtendDialog.findViewById(R.id.pgSend).setVisibility(View.GONE);
                pendingExtendDialog.findViewById(R.id.btn_extend_send).setClickable(true);
            }
            if (pojo != null && pojo.isStatus()) {
                if (pendingExtendDialog != null) pendingExtendDialog.dismiss();
                PrefsUtil.with(context).write("service", "true");
                Intent intent = new Intent(context, CustomerHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getReviewResult().observe(this, pojo -> {
            if (pendingReviewDialog != null) {
                pendingReviewDialog.findViewById(R.id.pgSaveRating).setVisibility(View.GONE);
                pendingReviewDialog.findViewById(R.id.btn_save_rating).setClickable(true);
            }
            if (pojo != null && pojo.isStatus()) {
                if (pendingReviewDialog != null) pendingReviewDialog.dismiss();
                binding.llBtnAddReview.setVisibility(View.GONE);
                defaultTab = 2;
                getDetails();
            }
        });

        viewModel.getBookResult().observe(this, pojo -> {
            binding.pgBookNow.setVisibility(View.GONE);
            binding.btnBook.setClickable(true);
            if (pojo != null && pojo.isStatus() && pojo.getData() != null) {
                if (pojo.getData().getCustomerCommission().equals("")) {
                    PrefsUtil.with(context).write("service", "true");
                    Intent intent = new Intent(context, CustomerHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    if (serviceDetailData.getServiceMasterType().equalsIgnoreCase("hourly")) {
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
                    intent.putExtra("paymentIntentClientSecret", pojo.getData().getPaymentIntentClientSecret());
                    intent.putExtra("serviceId", getIntent().getStringExtra("serviceRequestId"));
                    intent.putExtra("serviceMasterType", serviceDetailData.getServiceMasterType());
                    myActivityResultLauncher.launch(intent);
                }
            }
        });

        viewModel.getInvoiceResult().observe(this, pojo -> {
            binding.pgDownloadInvoice.setVisibility(View.GONE);
            binding.btnDownloadInvoice.setClickable(true);
            if (pojo != null && pojo.isStatus() && pojo.getData() != null) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(pojo.getData().getFileName()));
                startActivity(i);
            }
        });

        viewModel.getError().observe(this, msg -> {
            if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        });
    }

    protected void getDetails() {
        binding.rlMain.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("provider_service_id", getIntent().getStringExtra("providerServiceId"));
        params.put("service_request_id", getIntent().getStringExtra("serviceRequestId"));
        params.put("user_id", PrefsUtil.with(this).readString("UserId"));
        viewModel.loadServiceDetail(params);
    }

    protected void favouriteToggle() {
        binding.imgFav.setVisibility(View.GONE);
        binding.pgFavourite.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("service_id", getIntent().getStringExtra("providerServiceId"));
        params.put("provider_id", serviceDetailData.getProviderId());
        params.put("user_id", PrefsUtil.with(this).readString("UserId"));
        params.put("delivery_type", serviceDetailData.getDeliveryType());
        params.put("request_type", serviceDetailData.getRequestType());

        if (binding.imgFav.getTag().equals("1")) params.put("fvrt_val", "0");
        else if (binding.imgFav.getTag().equals("0")) params.put("fvrt_val", "1");
        else return;

        viewModel.toggleFavourite(params);
    }

    private void serviceCallExtendPayment() {
        binding.btnExtendPayment.setClickable(false);
        binding.pgExtendPayment.setVisibility(View.VISIBLE);

        String extendId = serviceDetailData.getExtendServiceData().get(0).getExtendId();
        String token = "";
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(serviceDetailData.getServiceRequestId().getBytes());
            byte[] messageDigest = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String h = Integer.toHexString(0xFF & b);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }
            token = hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) { e.printStackTrace(); }

        viewModel.extendServicePayment(extendId, token);
    }

    private void serviceCallCancelService(final DialogInterface dialogInterface, String reason) {
        pendingCancelDialog = dialogInterface;
        viewModel.cancelService(
            getIntent().getStringExtra("serviceRequestId"),
            PrefsUtil.with(this).readString("UserId"),
            reason,
            "c"
        );
    }

    private void serviceCallDownloadInvoice() {
        binding.btnDownloadInvoice.setClickable(false);
        binding.pgDownloadInvoice.setVisibility(View.VISIBLE);

        String url = WebServiceUrl.URL_DOWNLOAD_INVOICE + "/" + serviceDetailData.getServiceRequestId();
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("user_id", PrefsUtil.with(this).readString("UserId"));
        params.put("user_type", PrefsUtil.with(this).readString("UserType"));
        params.put("request_type", "app");
        params.put("invoice", getString(R.string.invoice));
        params.put("service_start_time", getString(R.string.service_s_time));
        params.put("service_end_time", getString(R.string.service_e_time));
        params.put("booking_id", getString(R.string.booking_id));
        params.put("booking_details", getString(R.string.booking_details));
        params.put("booking_amount", getString(R.string.booking_amount));
        params.put("admin_fees", getString(R.string.admin_feesb));
        params.put("payment_type", getString(R.string.payment_type));
        params.put("total_payable_amount", getString(R.string.total_payable_amount));
        params.put("total_receivable_amount", getString(R.string.total_receivable_amount));
        params.put("wallet", getString(R.string.wallet));
        params.put("cash", getString(R.string.cash));
        params.put("complete", getString(R.string.status_completed));
        viewModel.downloadInvoice(url, params);
    }

    private void serviceCallExtendService(String selectedHours, final Dialog dialog) {
        pendingExtendDialog = dialog;
        (dialog.findViewById(R.id.btn_extend_send)).setClickable(false);
        (dialog.findViewById(R.id.pgSend)).setVisibility(View.VISIBLE);
        viewModel.extendService(serviceDetailData.getServiceRequestId(), selectedHours);
    }

    private void serviceCallGiveReview(String serviceRequestId, float rating, String desc, final Dialog dialog) {
        pendingReviewDialog = dialog;
        dialog.setCancelable(false);
        (dialog.findViewById(R.id.btn_save_rating)).setClickable(false);
        (dialog.findViewById(R.id.pgSaveRating)).setVisibility(View.VISIBLE);
        viewModel.addReview(
            PrefsUtil.with(this).readString("UserId"),
            serviceRequestId,
            String.valueOf(rating),
            Utils.encodeEmoji(desc)
        );
    }

    private void serviceCallBookNow() {
        binding.btnBook.setClickable(false);
        binding.pgBookNow.setVisibility(View.VISIBLE);
        viewModel.bookServiceRequest(
            PrefsUtil.with(this).readString("UserId"),
            getIntent().getStringExtra("serviceRequestId")
        );
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
        if (binding.tabLayout.getTabAt(0) != null) binding.tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        if (binding.tabLayout.getTabAt(1) != null) binding.tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        if (binding.tabLayout.getTabAt(2) != null) binding.tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        if (binding.tabLayout.getTabAt(3) != null) binding.tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    @Override public void onTabSelected(TabLayout.Tab tab) { binding.pager.setCurrentItem(tab.getPosition()); if (tab.getIcon() != null) tab.getIcon().setColorFilter(ContextCompat.getColor(this, R.color.button), PorterDuff.Mode.SRC_IN); }
    @Override public void onTabUnselected(TabLayout.Tab tab) {}
    @Override public void onTabReselected(TabLayout.Tab tab) {}

    private void init() {
        context = this;
        viewModel = new ViewModelProvider(this).get(BookedServiceDetailViewModel.class);
        try {
            String title = getIntent().getStringExtra("serviceName");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + (title != null ? title : ""), HtmlCompat.FROM_HTML_MODE_LEGACY));
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) { e.printStackTrace(); }
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);
        myActivityResult();
    }

    private void myActivityResult() {
        myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                PrefsUtil.with(context).write("service", "true");
                Intent intent = new Intent(context, CustomerHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setData() {
        String statusDisplayName = serviceDetailData.getServiceStatusDisplayName();
        if (!Utils.isNullOrEmpty(serviceDetailData.getProviderImage())) {
            ImageRequest request = new ImageRequest.Builder(context).data(serviceDetailData.getProviderImage()).placeholder(R.drawable.loading).target(binding.imgProfile).build();
            Coil.imageLoader(context).enqueue(request);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + serviceDetailData.getServiceName(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

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

        String status = serviceDetailData.getServiceStatus();
        if (status.equalsIgnoreCase("hired")) {
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
        } else if (status.equals("pending")) {
            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_pending);
            binding.llDispute.setVisibility(View.INVISIBLE);
            binding.llViewDispute.setVisibility(View.INVISIBLE);
            binding.llMessage.setVisibility(View.INVISIBLE);
            binding.llViewMessage.setVisibility(View.INVISIBLE);
            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);
            binding.linlayBookAgain.setVisibility(View.GONE);
        } else if (status.equals("accepted")) {
            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_accepted);
            binding.llDispute.setVisibility(View.INVISIBLE);
            binding.llViewDispute.setVisibility(View.INVISIBLE);
            binding.llBtnBookNow.setVisibility(View.VISIBLE);
            binding.linlayBookAgain.setVisibility(View.GONE);
        } else if (status.equals("expired")) {
            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_expired);
            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);
            binding.linlayBookAgain.setVisibility(View.GONE);
        } else if (status.equals("rejected")) {
            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_rejected);
            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);
            binding.linlayBookAgain.setVisibility(View.GONE);
        } else if (status.equals("completed")) {
            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_completed);
            binding.llMessage.setVisibility(View.GONE);
            binding.llViewMessage.setVisibility(View.GONE);
            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);
            if (serviceDetailData.getIsReviewGiven().equals("n")) binding.llBtnAddReview.setVisibility(View.VISIBLE);
            binding.llBtnDownloadInvoice.setVisibility(View.VISIBLE);
            binding.linlayBookAgain.setVisibility(View.GONE);
        } else if (status.equals("cancelled")) {
            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_cancelled);
            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);
            binding.linlayBookAgain.setVisibility(View.GONE);
        } else if (status.equals("closed")) {
            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_closed);
            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);
            binding.linlayBookAgain.setVisibility(View.GONE);
        } else if (status.equals("dispute")) {
            binding.txtStatus.setText(statusDisplayName);
            binding.txtStatus.setBackgroundResource(R.color.status_dispute);
            binding.llMessage.setVisibility(View.VISIBLE);
            binding.llViewMessage.setVisibility(View.VISIBLE);
            binding.llBtnBookNow.setVisibility(View.GONE);
            binding.llBtnCancel.setVisibility(View.GONE);
            binding.linlayBookAgain.setVisibility(View.GONE);
        } else if (status.equals("ongoing")) {
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
            if (serviceDetailData.getExtendServiceData().size() > 0) {
                binding.llBtnExtendService.setVisibility(View.GONE);
                if (serviceDetailData.getExtendServiceData().get(0).getServiceStatus().equals("accepted")) binding.llBtnExtendPayment.setVisibility(View.VISIBLE);
            }
        }

        binding.btnExtendPayment.setOnClickListener(view -> serviceCallExtendPayment());
        binding.btnCancel.setOnClickListener(view -> {
            final Dialog dialog = new Dialog(BookedServiceDetailActivity.this);
            dialog.setContentView(R.layout.dialog_cancel_booking);
            dialog.setCancelable(false);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            if (dialog.getWindow() != null) {
                params.copyFrom(dialog.getWindow().getAttributes());
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes(params);
            }
            final EditText edtConfirm = dialog.findViewById(R.id.edt_confirm_cancel);
            final CheckBox chkAccept = dialog.findViewById(R.id.chk_accept_terms);
            final TextView txtTerms = dialog.findViewById(R.id.txt_cancel_terms);
            final ProgressBar pgConfirm = dialog.findViewById(R.id.pg_confirm);
            final AppCompatButton btnYes = dialog.findViewById(R.id.btn_yes);
            final AppCompatButton btnNo = dialog.findViewById(R.id.btn_no);
            
            final Spinner spinnerReason = dialog.findViewById(R.id.spinner_cancel_reason);
            final EditText edtOther = dialog.findViewById(R.id.edt_other_reason);

            // Gestione visibilità "Altro" motivo
            spinnerReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("Other") || 
                        parent.getItemAtPosition(position).toString().equalsIgnoreCase("Altro")) {
                        edtOther.setVisibility(View.VISIBLE);
                    } else {
                        edtOther.setVisibility(View.GONE);
                    }
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });

            // Rendering HTML termini
            txtTerms.setText(HtmlCompat.fromHtml(getString(R.string.accept_cancel_terms), HtmlCompat.FROM_HTML_MODE_LEGACY));
            txtTerms.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bemyrider.it/app/termini-e-condizioni-bemyrider/#s4"));
                startActivity(browserIntent);
            });

            btnNo.setOnClickListener(v -> dialog.dismiss());
            btnYes.setOnClickListener(v -> {
                if (spinnerReason.getSelectedItemPosition() == 0) {
                    Toast.makeText(BookedServiceDetailActivity.this, R.string.select_cancel_reason, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!chkAccept.isChecked()) {
                    Toast.makeText(BookedServiceDetailActivity.this, R.string.please_accept_terms, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!edtConfirm.getText().toString().trim().equalsIgnoreCase("CANCELLA")) {
                    Toast.makeText(BookedServiceDetailActivity.this, R.string.confirm_cancel_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                String finalReason = spinnerReason.getSelectedItem().toString();
                if (edtOther.getVisibility() == View.VISIBLE) {
                    if (edtOther.getText().toString().trim().isEmpty()) {
                        edtOther.setError(getString(R.string.hint_other_reason));
                        return;
                    }
                    finalReason = edtOther.getText().toString().trim();
                }

                Utils.hideSoftKeyboard(BookedServiceDetailActivity.this);
                pgConfirm.setVisibility(View.VISIBLE);
                btnYes.setVisibility(View.INVISIBLE);
                btnNo.setEnabled(false);
                
                serviceCallCancelService(dialog, finalReason);
            });
            dialog.show();
        });
        binding.btnDownloadInvoice.setOnClickListener(view -> serviceCallDownloadInvoice());
        binding.btnExtendService.setOnClickListener(view -> {
            final Dialog dialog = new Dialog(BookedServiceDetailActivity.this);
            dialog.setContentView(R.layout.extend_service_dialog);
            dialog.setCancelable(false);
            dialog.setTitle(R.string.extend_service);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            if (dialog.getWindow() != null) {
                params.copyFrom(dialog.getWindow().getAttributes());
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes(params);
            }
            final Spinner spinner = dialog.findViewById(R.id.spinner_extend_hours);
            dialog.findViewById(R.id.btn_extend_cancel).setOnClickListener(v -> dialog.cancel());
            dialog.findViewById(R.id.btn_extend_send).setOnClickListener(v -> {
                if (spinner.getSelectedItemPosition() != 0) {
                    Utils.hideSoftKeyboard(BookedServiceDetailActivity.this);
                    v.setClickable(false);
                    serviceCallExtendService(String.valueOf(spinner.getSelectedItemPosition()), dialog);
                } else Toast.makeText(BookedServiceDetailActivity.this, R.string.please_select_hours_first, Toast.LENGTH_SHORT).show();
            });
            dialog.show();
        });
        binding.txtName.setOnClickListener(v -> { Intent i = new Intent(context, PartnerProfileActivity.class); i.putExtra(Utils.PROVIDER_ID, serviceDetailData.getProviderId()); startActivity(i); });
        binding.layoutProfile.setOnClickListener(v -> { Intent i = new Intent(context, PartnerProfileActivity.class); i.putExtra(Utils.PROVIDER_ID, serviceDetailData.getProviderId()); startActivity(i); });
        binding.llFavourite.setOnClickListener(v -> favouriteToggle());
        binding.llDispute.setOnClickListener(v -> { Intent intent = new Intent(BookedServiceDetailActivity.this, RaiseDisputeActivity.class); intent.putExtra("RequestID", getIntent().getStringExtra("serviceRequestId")); startActivity(intent); finish(); });
        binding.llMessage.setOnClickListener(v -> {
            if (serviceDetailData.getServiceStatus().equals("dispute")) {
                Intent intent = new Intent(BookedServiceDetailActivity.this, Partner_DisputeDetail_Activity.class);
                intent.putExtra("DisputeId", serviceDetailData.getDisputeId());
                intent.putExtra("serviceId", serviceDetailData.getServiceId());
                intent.putExtra("serviceRequestId", serviceDetailData.getServiceRequestId());
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
            if (dialog.getWindow() != null) {
                params.copyFrom(dialog.getWindow().getAttributes());
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes(params);
            }
            final RatingBar ratingBar = dialog.findViewById(R.id.rate_provider);
            final EditText editText = dialog.findViewById(R.id.edt_review_provider);
            dialog.findViewById(R.id.btn_save_rating).setOnClickListener(view -> {
                if (ratingBar.getRating() != 0.0) {
                    if (!editText.getText().toString().trim().equals("")) {
                        Utils.hideSoftKeyboard(BookedServiceDetailActivity.this);
                        serviceCallGiveReview(serviceDetailData.getServiceRequestId(), ratingBar.getRating(), editText.getText().toString().trim(), dialog);
                    } else editText.setError(getString(R.string.please_provide_review));
                } else Toast.makeText(BookedServiceDetailActivity.this, R.string.please_select_rating, Toast.LENGTH_SHORT).show();
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
        });
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) { if (item.getItemId() == android.R.id.home) { finish(); return true; } return super.onOptionsItemSelected(item); }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        super.onDestroy();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragmentList = new ArrayList<>();
        public ViewPagerAdapter(FragmentManager manager) { super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT); }
        @Override public Fragment getItem(int position) { return mFragmentList.get(position); }
        @Override public int getCount() { return mFragmentList.size(); }
        public void addFrag(Fragment fragment, String title) { mFragmentList.add(fragment); }
        @Override public CharSequence getPageTitle(int position) { return null; }
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(LocaleManager.onAttach(newBase)); }
}