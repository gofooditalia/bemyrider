package com.app.bemyrider.helper;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;

public class ToastMaster {

    public static void showShort(Context mContext, String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showShort(Context mContext, @StringRes int msg) {
        showShort(mContext, mContext.getString(msg));
    }

    public static void showLong(Context mContext, String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    }

    public static void showLong(Context mContext, @StringRes int msg) {
        showLong(mContext, mContext.getString(msg));
    }

}
