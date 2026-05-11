package com.app.bemyrider.activity.partner;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.DisputeDetailItemAdapter;
import com.app.bemyrider.AsyncTask.DownloadAsync;
import com.app.bemyrider.R;
import com.app.bemyrider.activity.user.ServiceHistoryActivity;
import com.app.bemyrider.databinding.PartnerActivityDisputeDetailBinding;
import com.app.bemyrider.helper.PermissionUtils;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.model.DisputeDetailPojoItem;
import com.app.bemyrider.model.DisputeDetailPojo;
import com.app.bemyrider.model.FileUtilPOJO;
import com.app.bemyrider.viewmodel.DisputeDetailViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.FileUtils;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;

/**
 * Modified by Hardik Talaviya on 4/12/19.
 */

public class Partner_DisputeDetail_Activity extends AppCompatActivity {

    private Context mContext;
    private Activity mActivity;

    private PermissionUtils permissionUtils;
    private ConnectionManager connectionManager;

    private PartnerActivityDisputeDetailBinding binding;
    private ArrayList<DisputeDetailPojoItem> detailPojoItems = new ArrayList<>();
    private DisputeDetailItemAdapter adapter;
    private String serviceRequestId = "", fileName, realPath = "";
    private boolean attachedFile = false;
    private boolean pendingClear = false;
    private boolean isAcceptAction = false;
    private DisputeDetailViewModel viewModel;


    private LinearLayoutManager linearLayoutManager;

    /*pagination vars start*/
    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisiblesItems = 0, visibleItemCount, totalItemCount;

    private ActivityResultLauncher<PickVisualMediaRequest> actResPhotoPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.partner_activity_dispute_detail);

        mActivity = this;
        mContext = this;

        permissionUtils = new PermissionUtils(mActivity, mContext, new PermissionUtils.OnPermissionGrantedListener() {
            @Override
            public void onCameraPermissionGranted() {

            }

            @Override
            public void onStoragePermissionGranted() {
                // Not needed with modern Photo Picker
            }
        });

        initViews();

        viewModel = new ViewModelProvider(this).get(DisputeDetailViewModel.class);
        observeViewModel();
        serviceCallGetDisputeDetails(true);

        binding.recyclerDisputeDetails.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisiblesItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            if (detailPojoItems.size() < total_records) {
                                page++;
                                serviceCallGetDisputeDetails(false);
                            }
                        }
                    }
                }
            }
        });

        binding.layoutBottompanel.ImgSend.setOnClickListener(view -> {
            if (!binding.layoutBottompanel.edtMessage.getText().toString().equals("")) {
                serviceCallSendMessage();
            } else {
                Toast.makeText(this, getResources().getString(R.string.please_enter_message_first), Toast.LENGTH_SHORT).show();
            }

        });

        binding.acceptDispute.setOnClickListener(v -> serviceCallAcceptDispute());

        binding.escalateToAdmin.setOnClickListener(v -> serviceCallEscalateToAdmin());

        binding.layoutBottompanel.imgAttachFiles.setOnClickListener(view -> {
            actResPhotoPicker.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
    }

    private void initViews() {
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.dispute_details),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        binding.acceptDispute.setClickable(false);
        binding.escalateToAdmin.setClickable(false);
        binding.layoutBottompanel.ImgSend.setClickable(false);

        linearLayoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerDisputeDetails.setLayoutManager(linearLayoutManager);

        initActivityResult();
    }

    private void initActivityResult() {
        actResPhotoPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                try {
                    FileUtilPOJO fileUtils = FileUtils.getPath(mContext, uri);
                    if (fileUtils.isRequiredDownload()) {
                        String[] strArr = fileUtils.getPath().split(",");
                        new DownloadAsync(mContext, Uri.parse(strArr[2]), strArr[0], strArr[1], downloadResult -> {
                            realPath = downloadResult;
                            fileName = realPath.substring(realPath.lastIndexOf("/") + 1);
                            attachedFile = true;
                            binding.layoutBottompanel.edtMessage.setText(fileName);
                        }).execute();

                    } else {
                        realPath = fileUtils.getPath();
                        fileName = realPath.substring(realPath.lastIndexOf("/") + 1);
                        attachedFile = true;
                        binding.layoutBottompanel.edtMessage.setText(fileName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_CODE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Not strictly needed with Photo Picker but kept for compatibility
            } else {
                ToastMaster.showShort(mContext, R.string.err_permission_storage);
            }
        }
    }

    private void observeViewModel() {
        viewModel.getDetail().observe(this, pojo -> {
            if (pojo == null || pojo.getData() == null) return;
            binding.progress.setVisibility(View.GONE);
            binding.recyclerDisputeDetails.setVisibility(View.VISIBLE);
            if (pendingClear) {
                detailPojoItems.clear();
                pendingClear = false;
                String customerName = pojo.getData().getCustomerFirstname() + " " + pojo.getData().getCustomerLastname();
                String providerName = pojo.getData().getProviderFirstname() + " " + pojo.getData().getProviderLastname();
                adapter = new DisputeDetailItemAdapter(detailPojoItems, this,
                        customerName, providerName, pojo.getData().getCustomerImage(),
                        pojo.getData().getProviderImage());
                binding.recyclerDisputeDetails.setAdapter(adapter);
            }
            setData(pojo);
            int prevSize = detailPojoItems.size();
            detailPojoItems.addAll(pojo.getData().getMessageList());
            adapter.notifyDataSetChanged();
            loading = true;
            boolean hasItems = !detailPojoItems.isEmpty();
            binding.txtNoDataDispute.setVisibility(hasItems ? View.GONE : View.VISIBLE);
            binding.recyclerDisputeDetails.setVisibility(hasItems ? View.VISIBLE : View.GONE);
            if (hasItems) binding.recyclerDisputeDetails.scrollToPosition(prevSize);
            try { total_records = pojo.getData().getPagination().getTotalRecords(); } catch (Exception ignored) {}
        });

        viewModel.getSendResult().observe(this, pojo -> {
            binding.layoutBottompanel.pgSend.setVisibility(View.GONE);
            binding.layoutBottompanel.ImgSend.setVisibility(View.VISIBLE);
            if (pojo != null && pojo.getData() != null) {
                binding.layoutBottompanel.edtMessage.setText("");
                attachedFile = false;
                realPath = "";
                detailPojoItems.add(0, pojo.getData());
                adapter.notifyDataSetChanged();
                binding.recyclerDisputeDetails.scrollToPosition(0);
            }
        });

        viewModel.getActionResult().observe(this, result -> {
            binding.pgEscalateAdmin.setVisibility(View.GONE);
            binding.pgAcceptDispute.setVisibility(View.GONE);
            binding.escalateToAdmin.setClickable(true);
            binding.acceptDispute.setClickable(true);
            if (result != null && result.isStatus()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                if (isAcceptAction) {
                    binding.acceptDispute.setVisibility(View.GONE);
                    String userType = PrefsUtil.with(this).readString("UserType");
                    Intent intent = "c".equals(userType)
                            ? new Intent(this, ServiceHistoryActivity.class)
                            : new Intent(this, Partner_ServiceRequest_TabLayout_Activity.class);
                    startActivity(intent);
                }
                finish();
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                binding.progress.setVisibility(View.GONE);
                binding.pgEscalateAdmin.setVisibility(View.GONE);
                binding.pgAcceptDispute.setVisibility(View.GONE);
                binding.layoutBottompanel.pgSend.setVisibility(View.GONE);
                binding.escalateToAdmin.setClickable(true);
                binding.acceptDispute.setClickable(true);
                binding.layoutBottompanel.ImgSend.setVisibility(View.VISIBLE);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void serviceCallEscalateToAdmin() {
        binding.escalateToAdmin.setClickable(false);
        binding.acceptDispute.setClickable(false);
        binding.pgEscalateAdmin.setVisibility(View.VISIBLE);
        isAcceptAction = false;
        viewModel.escalateToAdmin(getIntent().getStringExtra("DisputeId"), PrefsUtil.with(this).readString("UserId"));
    }

    private void serviceCallAcceptDispute() {
        binding.acceptDispute.setClickable(false);
        binding.escalateToAdmin.setClickable(false);
        binding.pgAcceptDispute.setVisibility(View.VISIBLE);
        isAcceptAction = true;
        viewModel.acceptDispute(getIntent().getStringExtra("DisputeId"), PrefsUtil.with(this).readString("UserId"));
    }

    private void serviceCallSendMessage() {
        binding.layoutBottompanel.ImgSend.setVisibility(View.GONE);
        binding.layoutBottompanel.pgSend.setVisibility(View.VISIBLE);
        String messageText = attachedFile ? null : Utils.encodeEmoji(binding.layoutBottompanel.edtMessage.getText().toString().trim());
        String attachment = attachedFile ? realPath : null;
        viewModel.sendMessage(getIntent().getStringExtra("DisputeId"), PrefsUtil.with(this).readString("UserId"), messageText, attachment);
    }

    private void serviceCallGetDisputeDetails(boolean clearFlag) {
        if (clearFlag) { page = 1; binding.txtNoDataDispute.setVisibility(View.GONE); binding.recyclerDisputeDetails.scrollToPosition(0); }
        pendingClear = clearFlag;
        binding.progress.setVisibility(View.VISIBLE);
        String lastMsgId = (page > 1 && !detailPojoItems.isEmpty())
                ? detailPojoItems.get(detailPojoItems.size() - 1).getMessageId() : null;
        viewModel.loadDetail(getIntent().getStringExtra("DisputeId"), page, lastMsgId);
    }

    private void setData(DisputeDetailPojo detailPojo) {
        if (detailPojo.getData().getDisputeCreateUserId().equals(PrefsUtil.with(this).readString("UserId"))) {
            binding.txtRaiseBy.setText(mContext.getResources().getString(R.string.raise_by_me));
        } else {
            if (detailPojo.getData().getDisputeCreateUserId().equalsIgnoreCase(detailPojo.getData().getCustomerId())) {
                binding.txtRaiseBy.setText(String.format("%s %s", detailPojo.getData().getCustomerFirstname(), detailPojo.getData().getCustomerLastname()));
            } else {
                binding.txtRaiseBy.setText(String.format("%s %s", detailPojo.getData().getProviderFirstname(), detailPojo.getData().getProviderLastname()));
            }
        }
        binding.txtDisputeSubject.setText(detailPojo.getData().getDisputeTitle());
        serviceRequestId = detailPojo.getData().getServiceRequestId();

        if (detailPojo.getData().getEscalateAdmin().equals("y")) {
            binding.acceptDispute.setClickable(false);
            binding.escalateToAdmin.setClickable(false);
            binding.layoutBottompanel.ImgSend.setClickable(false);
            binding.acceptDispute.setVisibility(View.GONE);
            binding.escalateToAdmin.setVisibility(View.GONE);
            binding.layoutAccept.setVisibility(View.GONE); // Hide container
            binding.layoutBottompanel.imgAttachFiles.setVisibility(View.GONE);
            binding.layoutBottompanel.ImgSend.setVisibility(View.GONE);
            if (detailPojo.getData().getServiceStatus().equals("completed") || detailPojo.getData().getServiceStatus().equals("closed")) {
                binding.layoutBottompanel.edtMessage.setText("Dispute has been closed by admin.");
            } else {
                binding.layoutBottompanel.edtMessage.setText(getResources().getString(R.string.dispute_has_been_escalate_to_admin));
            }
            binding.layoutBottompanel.edtMessage.setFocusable(false);
        } else {
            binding.acceptDispute.setVisibility(View.VISIBLE);
            binding.escalateToAdmin.setVisibility(View.VISIBLE);
            binding.layoutAccept.setVisibility(View.VISIBLE); // Show container
            binding.acceptDispute.setClickable(true);
            binding.escalateToAdmin.setClickable(true);
            binding.layoutBottompanel.ImgSend.setClickable(true);
        }

        if (detailPojo.getData().getCustActive().equalsIgnoreCase("du") ||
                detailPojo.getData().getProActive().equalsIgnoreCase("du")) {
            binding.layoutBottompanel.llMainBottomPanel.setVisibility(View.GONE);
            binding.layoutAccept.setVisibility(View.GONE);
        } else {
            binding.layoutBottompanel.llMainBottomPanel.setVisibility(View.VISIBLE);
            // Already handled by escalateAdmin check
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        super.onDestroy();
    }
}