package com.app.bemyrider.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    private Activity mActivity;
    private Context mContext;
    private OnPermissionGrantedListener listener;

//    public static int MY_PERMISSIONS_REQUEST_PERMISSION = 200;
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
//        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU);
        return false;
    }

    public void checkNotificationPermission(int REQ_CODE_NOTIFICATION) {
        /*if (isAndroid13()) {
            String permission = Manifest.permission.POST_NOTIFICATIONS;

            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        mActivity, new String[]{permission}, REQ_CODE_NOTIFICATION
                );
            }
        }*/
    }

    public void checkStoragePermission() {
//        String camera = Manifest.permission.CAMERA;
        String read = Manifest.permission.READ_EXTERNAL_STORAGE;

//        if (isAndroid13())
//            read = Manifest.permission.READ_MEDIA_IMAGES;
//        else
//            read = Manifest.permission.READ_EXTERNAL_STORAGE;

//        if (isCameraPermissionAlso) {
//            if (isGranted(read) && isGranted(camera)) {
//                listener.onPermissionGranted();
//            } else {
//                checkPermission(camera, read);
//            }
//        } else {
        if (isGranted(read)) {
            listener.onStoragePermissionGranted();
        } else {
            ActivityCompat.requestPermissions(
                    mActivity, new String[]{read}, REQ_CODE_STORAGE
            );
        }
//        }
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

    /*private void checkPermission(String camera, String read) {
        if (!isGranted(read) && !isGranted(camera)) {
            ActivityCompat.requestPermissions(
                    mActivity, new String[]{read, camera}, MY_PERMISSIONS_REQUEST_PERMISSION
            );
        } else {
            if (!isGranted(read)) {
                ActivityCompat.requestPermissions(
                        mActivity, new String[]{read}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                );
            } else if (!isGranted(camera)) {
                ActivityCompat.requestPermissions(
                        mActivity, new String[]{camera}, MY_PERMISSIONS_REQUEST_ACCESS_CAMERA
                );
            } else {
                checkStoragePermission();
            }
        }
    }*/
}
