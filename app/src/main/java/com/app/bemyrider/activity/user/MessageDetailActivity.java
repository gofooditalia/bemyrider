package com.app.bemyrider.activity.user;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.MessageDetailItemAdapter;
import com.app.bemyrider.AsyncTask.DownloadAsync;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityMessageDetailBinding;
import com.app.bemyrider.helper.PermissionUtils;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.model.EventBusMessage;
import com.app.bemyrider.model.FileUtilPOJO;
import com.app.bemyrider.model.MessageListDetailPojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.FileUtils;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.MessageDetailViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Map;

public class MessageDetailActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    private Context mContext;
    private PermissionUtils permissionUtils;
    private ConnectionManager connectionManager;
    private ActivityResultLauncher<PickVisualMediaRequest> actResPhotoPicker;
    private ActivityMessageDetailBinding binding;
    private MessageDetailItemAdapter messageDetailItemAdapter;
    private ArrayList<MessageListDetailPojoItem> messageDetailPojoItems = new ArrayList<>();
    private String realPath = "", fileName = "", serviceId = "";
    private LinearLayoutManager layoutManager;
    private boolean attachedFile = false;
    private MessageDetailViewModel viewModel;

    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisiblesItems = 0, visibleItemCount, totalItemCount;
    private boolean pendingClear = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_message_detail);
        mContext = this;

        permissionUtils = new PermissionUtils(this, mContext, new PermissionUtils.OnPermissionGrantedListener() {
            @Override public void onCameraPermissionGranted() {}
            @Override public void onStoragePermissionGranted() { openAndPickFile(); }
        });

        initViews();

        viewModel = new ViewModelProvider(this).get(MessageDetailViewModel.class);
        observeViewModel();
        loadDetail(true);

        binding.recyclerMessageDetails.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    if (loading && (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;
                        if (messageDetailPojoItems.size() < total_records) {
                            page++;
                            loadDetail(false);
                        }
                    }
                }
            }
        });

        binding.layoutBottompanel.imgAttachFiles.setOnClickListener(v -> openAndPickFile());

        binding.layoutBottompanel.ImgSend.setOnClickListener(v -> {
            String text = binding.layoutBottompanel.edtMessage.getText().toString().trim();
            if (!text.isEmpty() || attachedFile) {
                sendMessage(text);
            } else {
                Toast.makeText(mContext, getString(R.string.please_enter_message_first), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getDetail().observe(this, pojo -> {
            binding.progress.setVisibility(View.GONE);
            if (pojo == null || pojo.getData() == null) return;

            binding.recyclerMessageDetails.setVisibility(View.VISIBLE);

            if (pendingClear) {
                messageDetailPojoItems.clear();
                pendingClear = false;
                messageDetailItemAdapter = new MessageDetailItemAdapter(
                        messageDetailPojoItems, this,
                        pojo.getData().getMyProfileImg(),
                        pojo.getData().getToUserName(),
                        pojo.getData().getToProfileImg());
                binding.recyclerMessageDetails.setAdapter(messageDetailItemAdapter);
            }

            serviceId = pojo.getData().getServiceId();

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(HtmlCompat.fromHtml(
                        "<font color=#FFFFFF>" + pojo.getData().getServiceName(),
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
            }

            boolean inactive = pojo.getData().getIsActive().equalsIgnoreCase("du") ||
                    pojo.getData().getSerActive().equalsIgnoreCase("n");
            binding.layoutBottompanel.llMainBottomPanel.setVisibility(inactive ? View.GONE : View.VISIBLE);

            int prevSize = messageDetailPojoItems.size();
            messageDetailPojoItems.addAll(pojo.getData().getMessageList());
            messageDetailItemAdapter.notifyDataSetChanged();
            loading = true;

            if (!messageDetailPojoItems.isEmpty()) {
                binding.txtNoRecordMes.setVisibility(View.GONE);
                binding.recyclerMessageDetails.scrollToPosition(prevSize);
            } else {
                binding.txtNoRecordMes.setVisibility(View.VISIBLE);
                binding.recyclerMessageDetails.setVisibility(View.GONE);
            }
            try { total_records = pojo.getData().getPagination().getTotalRecords(); } catch (Exception ignored) {}
        });

        viewModel.getSendResult().observe(this, pojo -> {
            binding.layoutBottompanel.pgSend.setVisibility(View.GONE);
            binding.layoutBottompanel.ImgSend.setVisibility(View.VISIBLE);
            if (pojo != null && pojo.getData() != null) {
                binding.txtNoRecordMes.setVisibility(View.GONE);
                binding.recyclerMessageDetails.setVisibility(View.VISIBLE);
                binding.layoutBottompanel.edtMessage.setText("");
                attachedFile = false;
                realPath = "";
                messageDetailPojoItems.add(0, pojo.getData());
                messageDetailItemAdapter.notifyDataSetChanged();
                binding.recyclerMessageDetails.scrollToPosition(0);
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                binding.progress.setVisibility(View.GONE);
                binding.layoutBottompanel.pgSend.setVisibility(View.GONE);
                binding.layoutBottompanel.ImgSend.setVisibility(View.VISIBLE);
                Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDetail(boolean clearFlag) {
        if (clearFlag) { page = 1; binding.txtNoRecordMes.setVisibility(View.GONE); binding.recyclerMessageDetails.scrollToPosition(0); }
        pendingClear = clearFlag;
        binding.progress.setVisibility(View.VISIBLE);

        String lastMsgId = null;
        if (page > 1 && !messageDetailPojoItems.isEmpty()) {
            lastMsgId = messageDetailPojoItems.get(messageDetailPojoItems.size() - 1).getMessageId();
        }
        String bookingId = getIntent().hasExtra("service_booking_id") ?
                getIntent().getStringExtra("service_booking_id") : null;

        viewModel.loadDetail(
                PrefsUtil.with(this).readString("UserId"),
                getIntent().getStringExtra("to_user"),
                getIntent().getStringExtra("master_id"),
                page, lastMsgId, bookingId);
    }

    private void sendMessage(String text) {
        binding.layoutBottompanel.ImgSend.setVisibility(View.GONE);
        binding.layoutBottompanel.pgSend.setVisibility(View.VISIBLE);
        String msgText = attachedFile ? null : Utils.encodeEmoji(text);
        String attachment = attachedFile ? realPath : null;
        viewModel.sendMessage(
                PrefsUtil.with(this).readString("UserId"),
                getIntent().getStringExtra("to_user"),
                serviceId,
                getIntent().getStringExtra("master_id"),
                msgText, attachment);
    }

    private void initViews() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>", HtmlCompat.FROM_HTML_MODE_LEGACY));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        binding.recyclerMessageDetails.setLayoutManager(layoutManager);
        photoPickerActivityResult();
    }

    private void photoPickerActivityResult() {
        actResPhotoPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                try {
                    FileUtilPOJO fileUtils = FileUtils.getPath(this, uri);
                    if (fileUtils != null) {
                        if (fileUtils.isRequiredDownload()) {
                            String[] strArr = fileUtils.getPath().split(",");
                            new DownloadAsync(this, Uri.parse(strArr[2]), strArr[0], strArr[1], result -> {
                                realPath = result;
                                if (realPath != null) {
                                    fileName = realPath.substring(realPath.lastIndexOf("/") + 1);
                                    attachedFile = true;
                                    binding.layoutBottompanel.edtMessage.setText(fileName);
                                }
                            }).execute();
                        } else {
                            realPath = fileUtils.getPath();
                            if (realPath != null) {
                                fileName = realPath.substring(realPath.lastIndexOf("/") + 1);
                                attachedFile = true;
                                binding.layoutBottompanel.edtMessage.setText(fileName);
                            } else {
                                ToastMaster.showShort(mContext, "Could not get file path.");
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in photo picker: " + e.getMessage());
                }
            }
        });
    }

    private void openAndPickFile() {
        actResPhotoPicker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onStart() { super.onStart(); if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this); }
    @Override public void onStop() { super.onStop(); EventBus.getDefault().unregister(this); }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusMessage event) {
        try {
            if (!event.getType().equalsIgnoreCase("msg")) return;
            Map<String, String> d = event.getData();
            String userId = PrefsUtil.with(this).readString("UserId");
            String userType = PrefsUtil.with(this).readString("UserType");
            String toUser = getIntent().getStringExtra("to_user");
            String masterId = getIntent().getStringExtra("master_id");
            boolean relevant = "c".equalsIgnoreCase(userType)
                    ? masterId.equalsIgnoreCase(d.get("service_id")) && userId.equalsIgnoreCase(d.get("customer_id")) && toUser.equalsIgnoreCase(d.get("provider_id"))
                    : masterId.equalsIgnoreCase(d.get("service_id")) && userId.equalsIgnoreCase(d.get("provider_id")) && toUser.equalsIgnoreCase(d.get("customer_id"));
            if (relevant) loadDetail(true);
        } catch (Exception ignored) {}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_CODE_STORAGE) {
            if (permissionUtils.verifyStorageResults(grantResults)) permissionUtils.checkStoragePermission();
            else ToastMaster.showShort(mContext, R.string.err_permission_storage);
        }
    }

    @Override
    protected void onDestroy() {
        try { if (connectionManager != null) connectionManager.unregisterReceiver(); } catch (Exception ignored) {}
        super.onDestroy();
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(LocaleManager.onAttach(newBase)); }
}
