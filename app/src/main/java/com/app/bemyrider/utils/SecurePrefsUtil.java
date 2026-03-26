package com.app.bemyrider.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Gestione sicura delle preferenze usando EncryptedSharedPreferences.
 * Optimized by Gemini - 2024.
 */
public class SecurePrefsUtil {

    private static final int DEFAULT_INT = 0;
    private static final String DEFAULT_STRING = "";
    private static final float DEFAULT_FLOAT = -1f;
    private static final boolean DEFAULT_BOOLEAN = false;

    private SharedPreferences sharedPreferences;
    private static SecurePrefsUtil instance;
    private Context mContext;
    private boolean isFallback = false;

    private SecurePrefsUtil(Context context) {
        this.mContext = context;
        init(context);
    }

    private void init(Context context) {
        if (context.getApplicationContext() == null) {
            this.sharedPreferences = context.getSharedPreferences("SecureMyPrefs_Fallback", Context.MODE_PRIVATE);
            this.isFallback = true;
            return;
        }

        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            this.sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "SecureMyPrefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            this.isFallback = false;
        } catch (GeneralSecurityException | IOException | RuntimeException e) {
            e.printStackTrace();
            this.sharedPreferences = context.getSharedPreferences("SecureMyPrefs_Fallback", Context.MODE_PRIVATE);
            this.isFallback = true;
        }
    }

    public static synchronized SecurePrefsUtil with(Context context) {
        if (instance == null) {
            instance = new SecurePrefsUtil(context);
        } else if (instance.isFallback && context.getApplicationContext() != null) {
            instance.init(context);
        }
        return instance;
    }

    public void write(String name, int number) {
        sharedPreferences.edit().putInt(name, number).apply();
        try { PrefsUtil.with(mContext).write(name, number); } catch (Exception ignored) {}
    }

    public void write(String name, String str) {
        sharedPreferences.edit().putString(name, str).apply();
        try { PrefsUtil.with(mContext).write(name, str); } catch (Exception ignored) {}
    }

    public void write(String name, float number) {
        sharedPreferences.edit().putFloat(name, number).apply();
        try { PrefsUtil.with(mContext).write(name, number); } catch (Exception ignored) {}
    }

    public void write(String name, boolean bool) {
        sharedPreferences.edit().putBoolean(name, bool).apply();
        try { PrefsUtil.with(mContext).write(name, bool); } catch (Exception ignored) {}
    }

    public int readInt(String name) { return sharedPreferences.getInt(name, DEFAULT_INT); }
    public String readString(String name) { return sharedPreferences.getString(name, DEFAULT_STRING); }
    public float readFloat(String name) { return sharedPreferences.getFloat(name, DEFAULT_FLOAT); }
    public boolean readBoolean(String name) { return sharedPreferences.getBoolean(name, DEFAULT_BOOLEAN); }
    public boolean contains(String key) { return sharedPreferences.contains(key); }

    public void clearPrefs() {
        String device_id = sharedPreferences.getString("device_token", DEFAULT_STRING);
        String lanId = sharedPreferences.getString("lanId", DEFAULT_STRING);
        String currency = sharedPreferences.getString("CurrencySign", DEFAULT_STRING);
        boolean hasSeenIntro = sharedPreferences.getBoolean("hasSeenIntro", false);

        sharedPreferences.edit().clear().apply();
        try { PrefsUtil.with(mContext).clearPrefs(); } catch (Exception ignored) {}

        write("device_token", device_id);
        write("lanId", lanId);
        write("CurrencySign", currency);
        write("hasSeenIntro", hasSeenIntro);
    }

    public void migrateFromLegacy(PrefsUtil legacyPrefs) {
        String[] stringKeys = { "UserId", "UserName", "FirstName", "LastName", "UserType", "eMail", "Pass",
                "device_token", "lanId", "CurrencySign", "UserImg", "login_cust_address", "loginType", "socialId" };

        for (String key : stringKeys) {
            String val = legacyPrefs.readString(key);
            if (val != null && !val.isEmpty()) {
                write(key, val);
            }
        }
        legacyPrefs.clearPrefs();
    }
}
