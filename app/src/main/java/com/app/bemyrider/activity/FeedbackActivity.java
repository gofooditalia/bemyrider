package com.app.bemyrider.activity;

import static com.app.bemyrider.utils.Utils.EMOJI_FILTER;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.partner.ProviderHomeActivity;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.databinding.ActivityFeedbackBinding;
import com.app.bemyrider.helper.LogMaster;
import com.app.bemyrider.helper.PermissionUtils;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.FileUtilPOJO;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.FileUtils;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 7/12/19.
 */

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedBackActivity";
    private Context mContext;
    private Activity mActivity;

    private PermissionUtils permissionUtils;
    private ConnectionManager connectionManager;

    private ActivityFeedbackBinding binding;
    private WebServiceCall sendFeedbackAsync;
    private String selectedImagePath = "";

    private ActivityResultLauncher<Uri> actResCamera;
    private ActivityResultLauncher<Intent> actResGallery;
    private ActivityResultLauncher<Intent> actResCropper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(FeedbackActivity.this, R.layout.activity_feedback);

        initViews();

        binding.edtFnameFeedback.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtLnameFeedback.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtFnameFeedback.setText(PrefsUtil.with(FeedbackActivity.this).readString("FirstName"));
        binding.edtLnameFeedback.setText(PrefsUtil.with(FeedbackActivity.this).readString("LastName"));
        binding.edtEmailFeedback.setText(PrefsUtil.with(FeedbackActivity.this).readString("eMail"));

        permissionUtils = new PermissionUtils(mActivity, mContext, new PermissionUtils.OnPermissionGrantedListener() {
            @Override
            public void onCameraPermissionGranted() {
                selectedImagePath = Utils.openCamera(mContext, actResCamera);
            }

            @Override
            public void onStoragePermissionGranted() {
                selectedImagePath = "";
                Utils.openImagesDocument(actResGallery);
            }
        });

        initActivityResult();

        binding.btnSubmitFeedback.setOnClickListener(view -> {
            if (checkValidation()) {
                Utils.hideSoftKeyboard(FeedbackActivity.this);
                serviceCallSendFeedback();
            }
        });

        binding.edtUploadFeedback.setOnClickListener(view -> openCameraGalleryDialog());

        binding.edtFnameFeedback.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { binding.tilFnameFeedback.setError(""); }
            @Override public void afterTextChanged(Editable editable) {}
        });

        binding.edtLnameFeedback.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { binding.tilLnameFeedback.setError(""); }
            @Override public void afterTextChanged(Editable editable) {}
        });

        binding.edtEmailFeedback.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { binding.tilEmailFeedback.setError(""); }
            @Override public void afterTextChanged(Editable editable) {}
        });

        binding.edtFeedback.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { binding.tilFeedback.setError(""); }
            @Override public void afterTextChanged(Editable editable) {}
        });
    }

    private void serviceCallSendFeedback() {
        binding.btnSubmitFeedback.setClickable(false);
        binding.pgSubmit.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        LinkedHashMap<String, File> fileParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(FeedbackActivity.this).readString("UserId"));
        textParams.put("email", binding.edtEmailFeedback.getText().toString().trim());
        textParams.put("message", Utils.encodeEmoji(binding.edtFeedback.getText().toString().trim()));
        textParams.put("firstName", binding.edtFnameFeedback.getText().toString().trim());
        textParams.put("lastName", binding.edtLnameFeedback.getText().toString().trim());
        if (!selectedImagePath.isEmpty()) {
            fileParams.put("user_img", new File(selectedImagePath));
        }

        new WebServiceCall(FeedbackActivity.this, WebServiceUrl.URL_SEND_FEEDBACK, textParams,
                fileParams, CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgSubmit.setVisibility(View.GONE);
                binding.btnSubmitFeedback.setClickable(true);
                if (status) {
                    Toast.makeText(FeedbackActivity.this, ((CommonPojo) obj).getMessage(), Toast.LENGTH_SHORT).show();
                    if (PrefsUtil.with(FeedbackActivity.this).readString("UserType").equals("p")) {
                        Intent intent = new Intent(FeedbackActivity.this, ProviderHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if (PrefsUtil.with(FeedbackActivity.this).readString("UserType").equals("c")) {
                        Intent intent = new Intent(FeedbackActivity.this, CustomerHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Toast.makeText(FeedbackActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(Object obj) {
                sendFeedbackAsync = null;
            }

            @Override
            public void onCancelled() {
                sendFeedbackAsync = null;
            }
        });
    }

    private boolean checkValidation() {
        if (binding.edtFnameFeedback.getText().toString().trim().equals("")) {
            binding.tilFnameFeedback.setError(getString(R.string.error_required));
            binding.edtFnameFeedback.requestFocus();
            return false;
        } else if (binding.edtLnameFeedback.getText().toString().trim().equals("")) {
            binding.tilLnameFeedback.setError(getString(R.string.error_required));
            binding.edtLnameFeedback.requestFocus();
            return false;
        } else if (binding.edtEmailFeedback.getText().toString().trim().equals("")) {
            binding.tilEmailFeedback.setError(getString(R.string.error_required));
            binding.edtEmailFeedback.requestFocus();
            return false;
        } else if (!Utils.isEmailValid(binding.edtEmailFeedback.getText().toString().trim())) {
            binding.tilEmailFeedback.setError(getString(R.string.error_valid_email));
            binding.edtEmailFeedback.requestFocus();
            return false;
        } else if (selectedImagePath.equals("")) {
            Toast.makeText(this, R.string.please_upload_your_photo, Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.edtFeedback.getText().toString().trim().equals("")) {
            binding.tilFeedback.setError(getString(R.string.error_required));
            binding.edtFeedback.requestFocus();
            return false;
        }
        return true;
    }

    private void initViews() {
        mContext = this;
        mActivity = this;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.feedback), HtmlCompat.FROM_HTML_MODE_LEGACY));
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

    }

    private void initActivityResult() {
        actResCamera = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && !selectedImagePath.isEmpty()) {
                Uri uri = Uri.parse(selectedImagePath);
                openCropActivity(uri, uri);
            }
        });

        actResGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            if (result.getResultCode() == RESULT_OK && data != null) {
                try {
                    Uri sourceUri = data.getData();
                    File file = Utils.createTempFileInAppPackage(mContext);
                    openCropActivity(sourceUri, Uri.fromFile(file));
                } catch (Exception e) {
                    Log.e(TAG, "actResGallery:" + e.getMessage());
                }
            }
        });

        actResCropper = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                showImage(UCrop.getOutput(result.getData()));
            }
        });
    }

    private void openCameraGalleryDialog() {
        final Dialog d = new Dialog(mActivity);
        d.setContentView(getLayoutInflater().inflate(R.layout.dialog_camera_gallery, null));
        Window window = d.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
        d.findViewById(R.id.linCamera).setOnClickListener(view -> { d.dismiss(); permissionUtils.checkCameraPermission(); });
        d.findViewById(R.id.linGallery).setOnClickListener(view -> { d.dismiss(); permissionUtils.checkStoragePermission(); });
        d.show();
    }

    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setActiveControlsWidgetColor(ContextCompat.getColor(mContext, R.color.button));
        options.setToolbarTitle("Edit Photo");
        options.setToolbarColor(ContextCompat.getColor(mContext, R.color.white));
        options.setToolbarWidgetColor(ContextCompat.getColor(mContext, R.color.button));
        actResCropper.launch(UCrop.of(sourceUri, destinationUri).withOptions(options).withAspectRatio(5f, 5f).getIntent(mContext));
    }

    private void showImage(Uri imageUri) {
        try {
            FileUtilPOJO fileUtils = FileUtils.getPath(mContext, imageUri);
            if (fileUtils != null) {
                selectedImagePath = fileUtils.getPath();
                binding.edtUploadFeedback.setText(selectedImagePath);
                LogMaster.e("ZZZ", selectedImagePath);
            }
        } catch (Exception e) {
            Log.e(TAG, "showImage: " + e.getMessage());
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
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        Utils.cancelAsyncTask(sendFeedbackAsync);
        Utils.clearCameraCache(mContext);
        super.onDestroy();
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(LocaleManager.onAttach(newBase)); }
}
