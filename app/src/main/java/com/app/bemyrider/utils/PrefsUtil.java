package com.app.bemyrider.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * Created by Yash on 10-May-16.
 */
public class PrefsUtil {

    private static final int DEFAULT_INT = 0;
    private static final String DEFAULT_STRING = "";
    private static final float DEFAULT_FLOAT = -1f;
    private static final boolean DEFAULT_BOOLEAN = false;

    private static SharedPreferences sharedPreferences;
    private static PrefsUtil prefsUtil;

    private PrefsUtil(@NonNull Context mContext) {
        if (sharedPreferences == null) {
            sharedPreferences = mContext.getApplicationContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        }
    }

    public static PrefsUtil with(@NonNull Context context) {
        if (prefsUtil == null) {
            prefsUtil = new PrefsUtil(context);
        }
        return prefsUtil;
    }

    public static boolean isUserLogin() {
        if (prefsUtil.readString("UserId") != null
                && prefsUtil.readString("UserId").length() > 0){
            return true;
        } else {
            return false;
        }
    }

    public void write(String name, int number) {
        sharedPreferences.edit().putInt(name, number).apply();
    }

    public void write(String name, String str) {
        sharedPreferences.edit().putString(name, str).apply();
    }

    public void write(String name, float number) {
        sharedPreferences.edit().putFloat(name, number).apply();
    }

    public void write(String name, boolean bool) {
        sharedPreferences.edit().putBoolean(name, bool).apply();
    }

    public int readInt(String name) {
        return sharedPreferences.getInt(name, DEFAULT_INT);
    }

    public String readString(String name) {
        return sharedPreferences.getString(name, DEFAULT_STRING);
    }

    public float readFloat(String name) {
        return sharedPreferences.getFloat(name, DEFAULT_FLOAT);
    }

    public boolean readBoolean(String name) {
        return sharedPreferences.getBoolean(name, DEFAULT_BOOLEAN);
    }

    public void clearPrefs() {
        String device_id = sharedPreferences.getString("device_token", DEFAULT_STRING);
        String lanId = sharedPreferences.getString("lanId", DEFAULT_STRING);
        String currency = sharedPreferences.getString("CurrencySign", DEFAULT_STRING);
        sharedPreferences.edit().clear().apply();
        sharedPreferences.edit().putString("device_token", device_id).apply();
        sharedPreferences.edit().putString("lanId", lanId).apply();
        sharedPreferences.edit().putString("CurrencySign", currency).apply();
    }

}