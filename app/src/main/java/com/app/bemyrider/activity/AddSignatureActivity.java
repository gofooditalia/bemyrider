package com.app.bemyrider.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityAddSignatureBinding;
import com.app.bemyrider.helper.PermissionUtils;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class AddSignatureActivity extends AppCompatActivity {

    private static final String TAG = "AddSignatureActivity";
    private Context mContext;
    private ConnectionManager connectionManager;

    private ActivityAddSignatureBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_signature);
        mContext = this;

        // Modern OnBackPressed callback
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        initViews();

        binding.btnReset.setOnClickListener(view -> binding.signatureView.clear());

        binding.btnSubmit.setOnClickListener(view -> {
            if (binding.signatureView.isEmpty()) {
                Toast.makeText(mContext, getString(R.string.err_msg_signature), Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = binding.signatureView.getSignatureBitmap();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime());
            String fileName = timeStamp + ".jpg";
            File tempFile = new File(mContext.getCacheDir(), fileName);

            try (FileOutputStream oStream = new FileOutputStream(tempFile)) {
                if (!tempFile.exists()) {
                    boolean created = tempFile.createNewFile();
                    if (!created) {
                        Log.w(TAG, "Could not create temp file for signature.");
                    }
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, oStream);
                oStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error saving signature to file", e);
            }

            Log.d(TAG, "Signature TempFile Path: " + tempFile.getAbsolutePath());
            Intent data = new Intent();
            data.putExtra("tmpPath", tempFile.toString());
            setResult(Activity.RESULT_OK, data);
            finish();
        });
    }


    private void initViews() {
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.lbl_add_signature), HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_CODE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastMaster.showShort(mContext, R.string.err_permission_camera);
            }
        } else if (requestCode == PermissionUtils.REQ_CODE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastMaster.showShort(mContext, R.string.err_permission_storage);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
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
        if (connectionManager != null) {
            try {
                connectionManager.unregisterReceiver();
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering connection manager", e);
            }
        }
        /** clear cache dir of picture which is taken photo from camera */
        Utils.clearCameraCache(mContext);
        super.onDestroy();
    }
}
