package com.app.bemyrider.activity.partner;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
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
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.user.ServiceHistoryActivity;
import com.app.bemyrider.databinding.PartnerActivityDisputeDetailBinding;
import com.app.bemyrider.helper.PermissionUtils;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.DisputeDetailPojo;
import com.app.bemyrider.model.DisputeDetailPojoItem;
import com.app.bemyrider.model.FileUtilPOJO;
import com.app.bemyrider.model.SendDisputeMessagePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.FileUtils;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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
    private AsyncTask escalateToAdminAsync, acceptDisputeAsync, sendMessageAsync, disputeDetailAsync;


    private LinearLayoutManager linearLayoutManager;

    /*pagination vars start*/
    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisiblesItems = 0, visibleItemCount, totalItemCount;

    private ActivityResultLauncher<Intent> actResGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(Partner_DisputeDetail_Activity.this, R.layout.partner_activity_dispute_detail, null);

        permissionUtils = new PermissionUtils(mActivity, mContext, new PermissionUtils.OnPermissionGrantedListener() {
            @Override
            public void onCameraPermissionGranted() {

            }

            @Override
            public void onStoragePermissionGranted() {
                openAndPickFile();
            }
        });

        initViews();

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
                            //Do pagination.. i.e. fetch new data
                        }
                    }
                }
            }
        });

        binding.layoutBottompanel.ImgSend.setOnClickListener(view -> {
            if (!binding.layoutBottompanel.edtMessage.getText().toString().equals("")) {
                serviceCallSendMessage();
            } else {
                Toast.makeText(Partner_DisputeDetail_Activity.this, getResources().getString(R.string.please_enter_message_first), Toast.LENGTH_SHORT).show();
            }

        });

        binding.acceptDispute.setOnClickListener(v -> serviceCallAcceptDispute());

        binding.escalateToAdmin.setOnClickListener(v -> serviceCallEscalateToAdmin());

        binding.layoutBottompanel.imgAttachFiles.setOnClickListener(view -> {
            permissionUtils.checkStoragePermission();
        });
    }

    private void initViews() {
        mContext = Partner_DisputeDetail_Activity.this;
        mActivity = Partner_DisputeDetail_Activity.this;

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

        binding.acceptDispute.getPaint().setUnderlineText(true);
        if (PrefsUtil.with(Partner_DisputeDetail_Activity.this).readString("UserType").equals("p")) {
            binding.acceptDispute.setText(HtmlCompat.fromHtml(getResources().getString(R.string.accept_dispute),HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else if (PrefsUtil.with(Partner_DisputeDetail_Activity.this).readString("UserType").equals("c")) {
            binding.acceptDispute.setText(HtmlCompat.fromHtml(getResources().getString(R.string.cancel_dispute),HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

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
        actResGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try {
                            Intent data = result.getData();
                            if (result.getResultCode() == RESULT_OK) {
                                if (data != null) {
                                    FileUtilPOJO fileUtils = FileUtils.getPath(mContext, data.getData());
                                    if (fileUtils.isRequiredDownload()) {
                                        String[] strArr = fileUtils.getPath().split(",");
                                        new DownloadAsync(mContext, Uri.parse(strArr[2]), strArr[0], strArr[1], downloadResult -> {
                                            realPath = downloadResult;
                                            fileName = realPath.substring(realPath.lastIndexOf("/") + 1);
                                            attachedFile = true;
                                            binding.layoutBottompanel.edtMessage.setText(fileName);
                                            Log.e("PAAAAAAAAAAAAAAAAAAATH", realPath);
                                        }).execute();

                                    } else {
                                        realPath = fileUtils.getPath();
                                        fileName = realPath.substring(realPath.lastIndexOf("/") + 1);
                                        attachedFile = true;
                                        binding.layoutBottompanel.edtMessage.setText(fileName);
                                        Log.e("PAAAAAAAAAAAAAAAAAAATH", realPath);
                                    }
                                }
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
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastMaster.showShort(mContext, R.string.err_permission_storage);
            } else {
                permissionUtils.checkStoragePermission();
            }
        }
    }

    private void permissionMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setMessage(getString(R.string.cancelling_granted)).show();
    }

    private void openAndPickFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        actResGallery.launch(Intent.createChooser(intent, "select multiple images"));
    }

    /*----------------- Escalate To Admin Api Call -------------------*/
    private void serviceCallEscalateToAdmin() {
        binding.escalateToAdmin.setClickable(false);
        binding.acceptDispute.setClickable(false);
        binding.pgEscalateAdmin.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("service_id", serviceRequestId);
        textParams.put("user_id", PrefsUtil.with(Partner_DisputeDetail_Activity.this).readString("UserId"));

        new WebServiceCall(Partner_DisputeDetail_Activity.this,
                WebServiceUrl.URL_ESCALAPERTO_ADMIN, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.pgEscalateAdmin.setVisibility(View.GONE);
                        binding.escalateToAdmin.setClickable(true);
                        binding.acceptDispute.setClickable(true);
                        if (status) {
                            CommonPojo commonPojo = (CommonPojo) obj;
                            Toast.makeText(Partner_DisputeDetail_Activity.this, commonPojo.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(Partner_DisputeDetail_Activity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        escalateToAdminAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        escalateToAdminAsync = null;
                    }
                });


    }

    /*------------------- Accept Dispute Api Call ---------------------*/
    private void serviceCallAcceptDispute() {
        binding.acceptDispute.setClickable(false);
        binding.escalateToAdmin.setClickable(false);
        binding.pgAcceptDispute.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("service_id", serviceRequestId);
        textParams.put("user_id", PrefsUtil.with(Partner_DisputeDetail_Activity.this).readString("UserId"));

        new WebServiceCall(Partner_DisputeDetail_Activity.this,
                WebServiceUrl.URL_ACCEPT_DISPUTE, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.pgAcceptDispute.setVisibility(View.GONE);
                        binding.acceptDispute.setClickable(true);
                        binding.escalateToAdmin.setClickable(true);
                        if (status) {
                            CommonPojo pojo = (CommonPojo) obj;
                            binding.acceptDispute.setVisibility(View.GONE);
                            if (PrefsUtil.with(Partner_DisputeDetail_Activity.this).readString("UserType").equals("c")) {
                                Intent intent = new Intent(Partner_DisputeDetail_Activity.this, ServiceHistoryActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(Partner_DisputeDetail_Activity.this, Partner_ServiceRequest_TabLayout_Activity.class);
                                startActivity(intent);
                            }
                            finish();
                        } else {
                            Toast.makeText(Partner_DisputeDetail_Activity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        acceptDisputeAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        acceptDisputeAsync = null;
                    }
                });
    }

    /*-------------------- Send Message Api Call -----------------------*/
    private void serviceCallSendMessage() {
        binding.layoutBottompanel.ImgSend.setVisibility(View.GONE);
        binding.layoutBottompanel.pgSend.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        LinkedHashMap<String, File> fileParams = new LinkedHashMap<>();

        textParams.put("dispute_id", getIntent().getStringExtra("DisputeId"));
        textParams.put("message_text", binding.layoutBottompanel.edtMessage.getText().toString().trim());
        textParams.put("user_id", PrefsUtil.with(Partner_DisputeDetail_Activity.this).readString("UserId"));

        if (attachedFile) {
            fileParams.put("attachment", new File(realPath));
        } else {
            textParams.put("message_text", Utils.encodeEmoji(binding.layoutBottompanel.edtMessage.getText().toString().trim()));
        }

        new WebServiceCall(Partner_DisputeDetail_Activity.this,
                WebServiceUrl.URL_SEND_DISPUTE_MESSAGE, textParams, fileParams, SendDisputeMessagePojo.class,
                false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.layoutBottompanel.pgSend.setVisibility(View.GONE);
                binding.layoutBottompanel.ImgSend.setVisibility(View.VISIBLE);
                if (status) {
                    SendDisputeMessagePojo sendDisputeMessagePojo = (SendDisputeMessagePojo) obj;
                    binding.layoutBottompanel.edtMessage.setText("");
                    detailPojoItems.add(0, sendDisputeMessagePojo.getData());
                    adapter.notifyDataSetChanged();
                    binding.recyclerDisputeDetails.scrollToPosition(0);
                } else {
                    Toast.makeText(Partner_DisputeDetail_Activity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                sendMessageAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                sendMessageAsync = null;
            }
        });
    }

    /*---------------- Get Dispute Details Api Call --------------------*/
    private void serviceCallGetDisputeDetails(boolean clearFlag) {
        if (clearFlag) {
            page = 1;
            binding.txtNoDataDispute.setVisibility(View.GONE);
            binding.recyclerDisputeDetails.scrollToPosition(0);
        }
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        if (page > 1) {
            textParams.put("last_message_id", detailPojoItems.get(detailPojoItems.size() - 1).getMessageId());
        }
        textParams.put("dispute_id", getIntent().getStringExtra("DisputeId"));
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(Partner_DisputeDetail_Activity.this,
                WebServiceUrl.URL_DISPUTE_DETAILS, textParams, DisputeDetailPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            DisputeDetailPojo detailPojo = (DisputeDetailPojo) obj;
                            binding.progress.setVisibility(View.GONE);
                            binding.recyclerDisputeDetails.setVisibility(View.VISIBLE);
                            if (clearFlag) {
                                detailPojoItems.clear();
                                String customerName = detailPojo.getData().getCustomerFirstname() + " " + detailPojo.getData().getCustomerLastname();
                                String providerName = detailPojo.getData().getProviderFirstname() + " " + detailPojo.getData().getProviderLastname();

                                /*Init Recycler View*/
                                adapter = new DisputeDetailItemAdapter(detailPojoItems, Partner_DisputeDetail_Activity.this,
                                        customerName, providerName, detailPojo.getData().getCustomerImage(),
                                        detailPojo.getData().getProviderImage());
                                binding.recyclerDisputeDetails.setAdapter(adapter);
                            }

                            setData(detailPojo);

                            detailPojoItems.addAll(detailPojo.getData().getMessageList());
                            adapter.notifyDataSetChanged();

                            loading = true;

                            if (detailPojoItems.size() > 0) {
                                binding.txtNoDataDispute.setVisibility(View.GONE);
                                binding.recyclerDisputeDetails.setVisibility(View.VISIBLE);
                                binding.recyclerDisputeDetails.scrollToPosition(detailPojoItems.size() - detailPojo.getData().getMessageList().size());
                            } else {
                                binding.recyclerDisputeDetails.setVisibility(View.GONE);
                                binding.txtNoDataDispute.setVisibility(View.VISIBLE);
                            }
                            try {
                                total_records = detailPojo.getData().getPagination().getTotalRecords();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            binding.progress.setVisibility(View.GONE);
                            Toast.makeText(Partner_DisputeDetail_Activity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        disputeDetailAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        disputeDetailAsync = null;
                    }
                });
    }

    /*----------- Set up data --------------*/
    private void setData(DisputeDetailPojo detailPojo) {
        /*--------- Set dispute created user name ----------*/
        if (detailPojo.getData().getDisputeCreateUserId()
                .equals(PrefsUtil.with(Partner_DisputeDetail_Activity.this)
                        .readString("UserId"))) {
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
            binding.acceptDispute.setClickable(true);
            binding.escalateToAdmin.setClickable(true);
            binding.layoutBottompanel.ImgSend.setClickable(true);
        }

        /*---------- For deleted user ------------*/
        if (detailPojo.getData().getCustActive().equalsIgnoreCase("du") ||
                detailPojo.getData().getProActive().equalsIgnoreCase("du")) {
            binding.layoutBottompanel.llMainBottomPanel.setVisibility(View.GONE);
            binding.layoutAccept.setVisibility(View.GONE);
        } else {
            binding.layoutBottompanel.llMainBottomPanel.setVisibility(View.VISIBLE);
            binding.layoutAccept.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(escalateToAdminAsync);
        Utils.cancelAsyncTask(acceptDisputeAsync);
        Utils.cancelAsyncTask(sendMessageAsync);
        Utils.cancelAsyncTask(disputeDetailAsync);
        super.onDestroy();
    }


}
