package com.app.bemyrider.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    private Activity mActivity;
    private Context mContext;
    private OnPermissionGrantedListener listener;

    public static int REQ_CODE_STORAGE = 201;
    public static int REQ_CODE_CAMERA = 203;

    public static String camPermission = Manifest.permission.CAMERA;

    public interface OnPermissionGrantedListener {
        void onStoragePermissionGranted();
        void onCameraPermissionGranted();
    }

    public PermissionUtils(Activity mActivity, Context mContext, OnPermissionGrantedListener listener) {
        this.mActivity = mActivity;
        this.mContext = mContext;
        this.listener = listener;
    }

    public static boolean isAndroid13() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU);
    }

    public static boolean isAndroid14() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE);
    }

    public void checkStoragePermission() {
        if (isAndroid13()) {
            // Con Photo Picker non servono permessi su Android 13+
            listener.onStoragePermissionGranted();
        } else {
            String read = Manifest.permission.READ_EXTERNAL_STORAGE;
            if (isGranted(read)) {
                listener.onStoragePermissionGranted();
            } else {
                ActivityCompat.requestPermissions(mActivity, new String[]{read}, REQ_CODE_STORAGE);
            }
        }
    }

    /**
     * Helper to verify storage permission results, especially for Android 14.
     */
    public boolean verifyStorageResults(int[] grantResults) {
        if (grantResults.length == 0) return false;

        if (isAndroid13()) {
            // Su Android 13+, i permessi READ_MEDIA_* sono stati rimossi dall'app.
            // Se questa funzione viene chiamata, assumiamo che non siano più necessari per il Photo Picker.
            return true;
        } else {
            return grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void checkCameraPermission() {
        if (isGranted(camPermission)) {
            listener.onCameraPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(
                    mActivity, new String[]{camPermission}, REQ_CODE_CAMERA
            );
        }
    }

    public Boolean isGranted(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
