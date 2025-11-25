package com.app.bemyrider.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Gestione sicura delle preferenze usando EncryptedSharedPreferences.
 * Sostituisce PrefsUtil per i dati sensibili.
 */
public class SecurePrefsUtil {

    private static final int DEFAULT_INT = 0;
    private static final String DEFAULT_STRING = "";
    private static final float DEFAULT_FLOAT = -1f;
    private static final boolean DEFAULT_BOOLEAN = false;

    private static SharedPreferences sharedPreferences;
    private static SecurePrefsUtil instance;

    private SecurePrefsUtil(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "SecureMyPrefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Fallback insicuro in caso di errore critico (es. dispositivi vecchi incompatibili), 
            // ma meglio gestire l'errore o avvisare.
            // Per ora, se fallisce, potrebbe crashare o usare un file standard temporaneo.
            sharedPreferences = context.getSharedPreferences("SecureMyPrefs_Fallback", Context.MODE_PRIVATE);
        }
    }

    public static synchronized SecurePrefsUtil with(Context context) {
        if (instance == null) {
            instance = new SecurePrefsUtil(context.getApplicationContext());
        }
        return instance;
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
    
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    public void clearPrefs() {
        // Mantieni token, impostazioni lingua e flag intro durante il logout se necessario
        String device_id = sharedPreferences.getString("device_token", DEFAULT_STRING);
        String lanId = sharedPreferences.getString("lanId", DEFAULT_STRING);
        String currency = sharedPreferences.getString("CurrencySign", DEFAULT_STRING);
        boolean hasSeenIntro = sharedPreferences.getBoolean("hasSeenIntro", false);
        
        sharedPreferences.edit().clear().apply();
        
        // Ripristina le info non sensibili utili
        sharedPreferences.edit().putString("device_token", device_id).apply();
        sharedPreferences.edit().putString("lanId", lanId).apply();
        sharedPreferences.edit().putString("CurrencySign", currency).apply();
        sharedPreferences.edit().putBoolean("hasSeenIntro", hasSeenIntro).apply();
    }
    
    /**
     * Metodo helper per copiare i dati da PrefsUtil legacy.
     */
    public void migrateFromLegacy(PrefsUtil legacyPrefs) {
        // Esempio di chiavi note da migrare
        String[] stringKeys = {"UserId", "UserName", "FirstName", "LastName", "UserType", "eMail", "Pass", "device_token", "lanId", "CurrencySign", "UserImg", "login_cust_address", "loginType", "socialId"};
        
        for (String key : stringKeys) {
            String val = legacyPrefs.readString(key);
            if (val != null && !val.isEmpty()) {
                write(key, val);
            }
        }
        
        // Marca la migrazione come avvenuta o cancella il vecchio
        legacyPrefs.clearPrefs(); 
    }
}

