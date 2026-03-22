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
            List<String> permissions = new ArrayList<>();
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);

            if (isAndroid14()) {
                permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            }

            boolean allGranted = true;
            for (String p : permissions) {
                if (!isGranted(p)) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                listener.onStoragePermissionGranted();
            } else {
                // Se siamo su Android 14 e l'utente ha già dato accesso parziale, consideriamolo concesso
                if (isAndroid14() && isGranted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
                     listener.onStoragePermissionGranted();
                } else {
                    ActivityCompat.requestPermissions(mActivity, permissions.toArray(new String[0]), REQ_CODE_STORAGE);
                }
            }
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
            // Su Android 13+ (e 14), se ALMENO UN permesso è concesso, l'app può continuare (es. solo immagini o accesso parziale)
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) return true;
            }
            return false;
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
