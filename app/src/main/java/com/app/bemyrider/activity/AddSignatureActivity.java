package com.app.bemyrider.activity;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.partner.ProviderHomeActivity;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.databinding.ActivityAddSignatureBinding;
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
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;


public class AddSignatureActivity extends AppCompatActivity {

    private static final String TAG = "FeedBackActivity";
    private Context mContext;
    private Activity mActivity;

    private PermissionUtils permissionUtils;
    private ConnectionManager connectionManager;

    private ActivityResultLauncher<Uri> actResCamera;
    private ActivityResultLauncher<Intent> actResGallery;
    private ActivityResultLauncher<Intent> actResCropper;

    private ActivityAddSignatureBinding binding;
    private Uri mCropImageUri, resultUri;
    private String selectedImagePath = "", path = "";
    private AsyncTask sendFeedbackAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(AddSignatureActivity.this, R.layout.activity_add_signature, null);
        mContext = AddSignatureActivity.this;
        mActivity = AddSignatureActivity.this;

        permissionUtils = new PermissionUtils(mActivity, mContext, new PermissionUtils.OnPermissionGrantedListener() {
            @Override
            public void onCameraPermissionGranted() {
//                selectedImagePath = Utils.openCamera(mContext, actResCamera);
            }

            @Override
            public void onStoragePermissionGranted() {
                selectedImagePath = "";
//                Utils.openImagesDocument(actResGallery);
            }
        });

        initViews();

        binding.btnReset.setOnClickListener(view -> {
            binding.signatureView.clear();
        });

        binding.btnSubmit.setOnClickListener(view -> {
            if (binding.signatureView.isEmpty()) {
                Toast.makeText(mContext, getString(R.string.err_msg_signature), Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = binding.signatureView.getSignatureBitmap();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime());
            String fileName = timeStamp + ".jpg";

            File tempFile = new File(mContext.getCacheDir(), fileName);
            // Initialize streams
            FileOutputStream oStream = null;
            InputStream inputStream = null;

            try {
                tempFile.createNewFile();
                oStream = new FileOutputStream(tempFile);
//                inputStream = mContext.getContentResolver().openInputStream(contentUri);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, oStream);
                oStream.flush();
                oStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e("ZZZ", "TmpFile::" + tempFile);
            Intent data = new Intent();
            data.putExtra("tmpPath",tempFile.toString());
            setResult(Activity.RESULT_OK,data);
            finish();
//            binding.imgTest.setImageBitmap(binding.signatureView.getSignatureBitmap());
            /*if (checkValidation()) {
                Utils.hideSoftKeyboard(AddSignatureActivity.this);
                serviceCallSendFeedback();
            }*/
        });

        /*binding.edtUploadFeedback.setOnClickListener(view -> {
            checkPermission();
        });*/
    }


    private void initViews() {
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.lbl_add_signature),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        initActivityResult();
    }

    private void initActivityResult() {
        actResCamera = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    Uri uri = Uri.parse(selectedImagePath);
                    openCropActivity(uri, uri);
                }
            }
        });

        actResGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        try {
                            Uri sourceUri = data.getData();
                            File file = Utils.createTempFileInAppPackage(mContext);
                            Uri destinationUri = Uri.fromFile(file);
                            openCropActivity(sourceUri, destinationUri);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: " + e.getMessage());
                        }
                    }
                });

        actResCropper = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent data = result.getData();
                try {
                    Uri uri = UCrop.getOutput(data);
                    showImage(uri);
                } catch (Exception e) {
                    Log.e(TAG, "onActivityResult: " + e.getMessage());
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_CODE_CAMERA) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastMaster.showShort(mContext, R.string.err_permission_camera);
            } else {
                permissionUtils.checkCameraPermission();
            }
        } else if (requestCode == PermissionUtils.REQ_CODE_STORAGE) {
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


    private void openCameraGalleryDialog() {
        final Dialog d = new Dialog(mActivity);
        d.setContentView(getLayoutInflater().inflate(R.layout.dialog_camera_gallery, null));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = d.getWindow();
        lp.copyFrom(window.getAttributes());
        // This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        LinearLayoutCompat linCamera = d.findViewById(R.id.linCamera);
        LinearLayoutCompat linGallery = d.findViewById(R.id.linGallery);

        linCamera.setOnClickListener(view -> {
            d.dismiss();
            permissionUtils.checkCameraPermission();
        });

        linGallery.setOnClickListener(view -> {
            d.dismiss();
            permissionUtils.checkStoragePermission();
        });
        d.show();
    }

    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setActiveControlsWidgetColor(ContextCompat.getColor(mContext, R.color.button));
        options.setToolbarTitle("Edit Photo");
        options.setStatusBarColor(ContextCompat.getColor(mContext, R.color.white));
        options.setToolbarColor(ContextCompat.getColor(mContext, R.color.white));
        options.setToolbarWidgetColor(ContextCompat.getColor(mContext, R.color.button));
        Intent myIntent = UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .withAspectRatio(5f, 5f)
                .getIntent(mContext);
        actResCropper.launch(myIntent);
    }

    private void showImage(Uri imageUri) {
        try {
            File file;
            FileUtilPOJO fileUtils = FileUtils.getPath(mContext, imageUri);
            file = new File(fileUtils.getPath());
            InputStream inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            //binding.imgUserprofile.setImageBitmap(bitmap);
            selectedImagePath = file.getAbsolutePath();
//            binding.edtUploadFeedback.setText(selectedImagePath);
        } catch (Exception e) {
            Log.e(TAG, "showImage: " + e.getMessage());
        }
    }

    private void serviceCallSendFeedback() {
        binding.btnSubmit.setClickable(false);
        binding.pgSubmit.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        LinkedHashMap<String, File> fileParams = new LinkedHashMap<>();

//        textParams.put("user_id", PrefsUtil.with(AddSignatureActivity.this).readString("UserId"));
//        textParams.put("email", binding.edtEmailFeedback.getText().toString().trim());
//        textParams.put("message", Utils.encodeEmoji(binding.edtFeedback.getText().toString().trim()));
//        textParams.put("firstName", binding.edtFnameFeedback.getText().toString().trim());
//        textParams.put("lastName", binding.edtLnameFeedback.getText().toString().trim());
//        fileParams.put("user_img", new File(selectedImagePath));

        new WebServiceCall(AddSignatureActivity.this, WebServiceUrl.URL_SEND_FEEDBACK, textParams,
                fileParams, CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgSubmit.setVisibility(View.GONE);
                binding.btnSubmit.setClickable(true);
                Toast.makeText(AddSignatureActivity.this, ((CommonPojo) obj).getMessage(), Toast.LENGTH_SHORT).show();
                if (status) {
                    if (PrefsUtil.with(AddSignatureActivity.this).readString("UserType").equals("p")) {
                        Intent intent = new Intent(AddSignatureActivity.this, ProviderHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if (PrefsUtil.with(AddSignatureActivity.this).readString("UserType").equals("c")) {
                        Intent intent = new Intent(AddSignatureActivity.this, CustomerHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(AddSignatureActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                sendFeedbackAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                sendFeedbackAsync = null;
            }
        });
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
        Utils.cancelAsyncTask(sendFeedbackAsync);

        /** clear cache dir of picture which is taken photo from camera */
        Utils.clearCameraCache(mContext);
        super.onDestroy();
    }
}
