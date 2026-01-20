package com.app.bemyrider.activity.user;

import android.app.Activity;
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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.MessageDetailItemAdapter;
import com.app.bemyrider.AsyncTask.DownloadAsync;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityMessageDetailBinding;
import com.app.bemyrider.helper.PermissionUtils;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.model.FileUtilPOJO;
import com.app.bemyrider.model.MessageDetailPojo;
import com.app.bemyrider.model.MessageListDetailPojoItem;
import com.app.bemyrider.model.SendMessagePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.FileUtils;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Modified by Hardik Talaviya on 4/12/19.
 */

public class MessageDetailActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    private Context mContext;
    private Activity mActivity;

    private PermissionUtils permissionUtils;
    private ConnectionManager connectionManager;

    private ActivityResultLauncher<Intent> actResGallery;

    private ActivityMessageDetailBinding binding;
    private MessageDetailItemAdapter messageDetailItemAdapter;
    private ArrayList<MessageListDetailPojoItem> messageDetailPojoItems = new ArrayList<>();
    private String realPath = "", fileName, serviceId = "";

    private LinearLayoutManager layoutManager;
    private boolean attachedFile = false;
    private WebServiceCall sendMessageAsync, messageDetailAsync;

    /*pagination vars start*/
    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisiblesItems = 0, visibleItemCount, totalItemCount;
    /*pagination vars end*/

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_message_detail);
        mContext = MessageDetailActivity.this;
        mActivity = MessageDetailActivity.this;

        permissionUtils = new PermissionUtils(mActivity, mContext, new PermissionUtils.OnPermissionGrantedListener() {
            @Override
            public void onCameraPermissionGranted() {}
            @Override
            public void onStoragePermissionGranted() { openAndPickFile(); }
        });

        initViews();

        serviceCallGetMessageDetail(true);

        binding.recyclerMessageDetails.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            if (messageDetailPojoItems.size() < total_records) {
                                page++;
                                serviceCallGetMessageDetail(false);
                            }
                        }
                    }
                }
            }
        });

        binding.layoutBottompanel.imgAttachFiles.setOnClickListener(view -> permissionUtils.checkStoragePermission());

        binding.layoutBottompanel.ImgSend.setOnClickListener(v -> {
            if (!binding.layoutBottompanel.edtMessage.getText().toString().trim().equals("")) {
                serviceCallSendMessage();
            } else {
                Toast.makeText(MessageDetailActivity.this, getString(R.string.please_enter_message_first), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>",HtmlCompat.FROM_HTML_MODE_LEGACY));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        layoutManager = new LinearLayoutManager(MessageDetailActivity.this, RecyclerView.VERTICAL, false);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        binding.recyclerMessageDetails.setLayoutManager(layoutManager);

        galleryActivityResult();
    }

    private void galleryActivityResult() {
        actResGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    try {
                        Intent data = result.getData();
                        if (result.getResultCode() == RESULT_OK && data != null) {
                            FileUtilPOJO fileUtils = FileUtils.getPath(MessageDetailActivity.this, data.getData());
                            if (fileUtils.isRequiredDownload()) {
                                String[] strArr = fileUtils.getPath().split(",");
                                new DownloadAsync(MessageDetailActivity.this, Uri.parse(strArr[2]), strArr[0], strArr[1], downloadResult -> {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void openAndPickFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        actResGallery.launch(Intent.createChooser(intent, "select multiple images"));
    }

    private void serviceCallSendMessage() {
        binding.layoutBottompanel.ImgSend.setVisibility(View.GONE);
        binding.layoutBottompanel.pgSend.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        LinkedHashMap<String, File> fileParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(MessageDetailActivity.this).readString("UserId"));
        textParams.put("to_user_id", getIntent().getStringExtra("to_user"));
        textParams.put("service_id", serviceId);
        textParams.put("service_master_id", getIntent().getStringExtra("master_id"));
        if (attachedFile) {
            fileParams.put("attachment", new File(realPath));
        } else {
            textParams.put("message_text", Utils.encodeEmoji(binding.layoutBottompanel.edtMessage.getText().toString().trim()));
        }

        new WebServiceCall(MessageDetailActivity.this, WebServiceUrl.URL_SEND_MESSAGE,
                textParams, fileParams, SendMessagePojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.layoutBottompanel.pgSend.setVisibility(View.GONE);
                        binding.layoutBottompanel.ImgSend.setVisibility(View.VISIBLE);
                        if (status) {
                            binding.txtNoRecordMes.setVisibility(View.GONE);
                            binding.recyclerMessageDetails.setVisibility(View.VISIBLE);
                            SendMessagePojo sendMessagePojo = (SendMessagePojo) obj;
                            binding.layoutBottompanel.edtMessage.setText("");
                            messageDetailPojoItems.add(0, sendMessagePojo.getData());
                            messageDetailItemAdapter.notifyDataSetChanged();
                            binding.recyclerMessageDetails.scrollToPosition(0);
                        } else {
                            Toast.makeText(MessageDetailActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onAsync(Object obj) { sendMessageAsync = null; }
                    @Override public void onCancelled() { sendMessageAsync = null; }
                });
    }

    private void serviceCallGetMessageDetail(boolean clearFlag) {
        if (clearFlag) {
            page = 1;
            binding.txtNoRecordMes.setVisibility(View.GONE);
            binding.recyclerMessageDetails.scrollToPosition(0);
        }
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        if (page > 1) {
            textParams.put("last_message_id", messageDetailPojoItems.get(messageDetailPojoItems.size() - 1).getMessageId());
        }
        textParams.put("from_user_id", PrefsUtil.with(MessageDetailActivity.this).readString("UserId"));
        textParams.put("to_user_id", getIntent().getStringExtra("to_user"));
        textParams.put("service_master_id", getIntent().getStringExtra("master_id"));
        if (getIntent().hasExtra("service_booking_id")) {
            textParams.put("service_booking_id", getIntent().getStringExtra("service_booking_id"));
        }
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(MessageDetailActivity.this,
                WebServiceUrl.URL_GET_MESSAGE_DETAILS, textParams, MessageDetailPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            MessageDetailPojo messageDetailPojo = (MessageDetailPojo) obj;
                            binding.progress.setVisibility(View.GONE);
                            binding.recyclerMessageDetails.setVisibility(View.VISIBLE);

                            if (clearFlag) {
                                messageDetailPojoItems.clear();
                                messageDetailItemAdapter = new MessageDetailItemAdapter(messageDetailPojoItems, MessageDetailActivity.this,
                                        messageDetailPojo.getData().getMyProfileImg(), messageDetailPojo.getData().getToUserName(),
                                        messageDetailPojo.getData().getToProfileImg());
                                binding.recyclerMessageDetails.setAdapter(messageDetailItemAdapter);
                            }

                            serviceId = messageDetailPojo.getData().getServiceId();

                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + messageDetailPojo.getData().getServiceName(),HtmlCompat.FROM_HTML_MODE_LEGACY));
                            }

                            if (messageDetailPojo.getData().getIsActive().equalsIgnoreCase("du") ||
                                    messageDetailPojo.getData().getSerActive().equalsIgnoreCase("n")) {
                                binding.layoutBottompanel.llMainBottomPanel.setVisibility(View.GONE);
                            } else {
                                binding.layoutBottompanel.llMainBottomPanel.setVisibility(View.VISIBLE);
                            }

                            messageDetailPojoItems.addAll(messageDetailPojo.getData().getMessageList());
                            messageDetailItemAdapter.notifyDataSetChanged();

                            loading = true;

                            if (messageDetailPojoItems.size() > 0) {
                                binding.txtNoRecordMes.setVisibility(View.GONE);
                                binding.recyclerMessageDetails.setVisibility(View.VISIBLE);
                                binding.recyclerMessageDetails.scrollToPosition(messageDetailPojoItems.size() - messageDetailPojo.getData().getMessageList().size());
                            } else {
                                binding.txtNoRecordMes.setVisibility(View.VISIBLE);
                                binding.recyclerMessageDetails.setVisibility(View.GONE);
                            }
                            try {
                                total_records = messageDetailPojo.getData().getPagination().getTotalRecords();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            binding.progress.setVisibility(View.GONE);
                            Toast.makeText(MessageDetailActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(Object obj) { messageDetailAsync = null; }
                    @Override public void onCancelled() { messageDetailAsync = null; }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onStart() { super.onStart(); if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this); }
    @Override public void onStop() { super.onStop(); EventBus.getDefault().unregister(this); }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            if (event.getType().equalsIgnoreCase("msg")) {
                Map<String, String> remoteMessage = event.getData();
                if (PrefsUtil.with(MessageDetailActivity.this).readString("UserType").equalsIgnoreCase("c")) {
                    if (remoteMessage.get("service_id").equalsIgnoreCase(getIntent().getStringExtra("master_id"))
                            && remoteMessage.get("customer_id").equalsIgnoreCase(PrefsUtil.with(MessageDetailActivity.this).readString("UserId"))
                            && remoteMessage.get("provider_id").equalsIgnoreCase(getIntent().getStringExtra("to_user"))) {
                        serviceCallGetMessageDetail(true);
                    }
                } else {
                    if (remoteMessage.get("service_id").equalsIgnoreCase(getIntent().getStringExtra("master_id"))
                            && remoteMessage.get("provider_id").equalsIgnoreCase(PrefsUtil.with(MessageDetailActivity.this).readString("UserId"))
                            && remoteMessage.get("customer_id").equalsIgnoreCase(getIntent().getStringExtra("to_user"))) {
                        serviceCallGetMessageDetail(true);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_CODE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) permissionUtils.checkStoragePermission();
            else ToastMaster.showShort(mContext, R.string.err_permission_storage);
        }
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        Utils.cancelAsyncTask(sendMessageAsync);
        Utils.cancelAsyncTask(messageDetailAsync);
        super.onDestroy();
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(LocaleManager.onAttach(newBase)); }
}
