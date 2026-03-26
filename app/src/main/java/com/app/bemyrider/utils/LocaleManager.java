package com.app.bemyrider.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

public class LocaleManager {

    private static final String TAG = "LocaleManager";
    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, "it");
        return setLocale(context, lang);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, "it");
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);
        
        // Sincronizza con SecurePrefs solo se siamo oltre la fase di avvio critico
        if (context.getApplicationContext() != null) {
            syncToSecurePrefs(context, language);
        }
        
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale currentLocale = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) 
                ? configuration.getLocales().get(0) 
                : configuration.locale;
        
        if (currentLocale.getLanguage().equals(language)) {
            return context;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }
        return updateResourcesLegacy(context, language);
    }
    
    private static void syncToSecurePrefs(Context context, String language) {
        try {
            SecurePrefsUtil securePrefs = SecurePrefsUtil.with(context);
            String lanId = convertLangCodeToLanId(language);
            if (lanId != null) {
                securePrefs.write("lanId", lanId);
            }
        } catch (Exception e) {
            Log.w(TAG, "Sync to SecurePrefs delayed/failed: " + e.getMessage());
        }
    }
    
    private static String convertLangCodeToLanId(String langCode) {
        if ("en".equals(langCode)) return "1";
        if ("fr".equals(langCode)) return "2";
        if ("pt".equals(langCode)) return "3";
        if ("it".equals(langCode)) return "4";
        return null;
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        // Durante l'avvio (attachBaseContext), usiamo SEMPRE SharedPreferences standard.
        // È più veloce e sicuro per evitare deadlock o NPE con SecurePrefs.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String savedLang = preferences.getString(SELECTED_LANGUAGE, null);
        
        if (!TextUtils.isEmpty(savedLang)) {
            return savedLang;
        }

        // Se non troviamo nulla, e abbiamo l'app context, proviamo a recuperare da SecurePrefs (migrazione)
        if (context.getApplicationContext() != null) {
            try {
                SecurePrefsUtil securePrefs = SecurePrefsUtil.with(context);
                String lanId = securePrefs.readString("lanId");
                if (!TextUtils.isEmpty(lanId)) {
                    String langCode = convertLanIdToLangCode(lanId);
                    if (langCode != null) {
                        persist(context, langCode); 
                        return langCode;
                    }
                }
            } catch (Exception ignored) {}
        }
        
        return defaultLanguage;
    }

    private static String convertLanIdToLangCode(String lanId) {
        if ("1".equals(lanId)) return "en";
        if ("2".equals(lanId)) return "fr";
        if ("3".equals(lanId)) return "pt";
        if ("4".equals(lanId)) return "it";
        return null;
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Configuration configuration = context.getResources().getConfiguration();
        LocaleList localeList = new LocaleList(locale);
        localeList.setDefault(localeList);
        configuration.setLocales(localeList);
        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        configuration.setLayoutDirection(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }
}
