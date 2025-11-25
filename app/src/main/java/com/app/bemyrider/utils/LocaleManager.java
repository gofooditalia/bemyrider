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
        String lang = getPersistedData(context, "it"); // Default italiano invece di lingua dispositivo
        return setLocale(context, lang);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, "it"); // Default italiano
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);
        // Sincronizza anche in SecurePrefsUtil se possibile
        syncToSecurePrefs(context, language);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }
        return updateResourcesLegacy(context, language);
    }
    
    /**
     * Sincronizza la lingua in SecurePrefsUtil (salva il lanId corrispondente)
     */
    private static void syncToSecurePrefs(Context context, String language) {
        try {
            SecurePrefsUtil securePrefs = SecurePrefsUtil.with(context);
            String lanId = convertLangCodeToLanId(language);
            if (lanId != null) {
                securePrefs.write("lanId", lanId);
                Log.d(TAG, "Synced language to SecurePrefsUtil (lanId=" + lanId + " for lang=" + language + ")");
            }
        } catch (Exception e) {
            Log.w(TAG, "Error syncing language to SecurePrefsUtil", e);
        }
    }
    
    /**
     * Converte codice lingua in lanId
     * en = 1, fr = 2, pt = 3, it = 4
     */
    private static String convertLangCodeToLanId(String langCode) {
        if ("en".equals(langCode)) return "1";
        if ("fr".equals(langCode)) return "2";
        if ("pt".equals(langCode)) return "3";
        if ("it".equals(langCode)) return "4";
        return null;
    }

    /**
     * Legge la lingua salvata, controllando prima SecurePrefsUtil (lanId) e poi SharedPreferences standard
     */
    private static String getPersistedData(Context context, String defaultLanguage) {
        try {
            // Prima prova a leggere da SecurePrefsUtil (sistema nuovo)
            SecurePrefsUtil securePrefs = SecurePrefsUtil.with(context);
            String lanId = securePrefs.readString("lanId");
            
            if (!TextUtils.isEmpty(lanId)) {
                // Converti lanId in codice lingua
                String langCode = convertLanIdToLangCode(lanId);
                if (langCode != null) {
                    Log.d(TAG, "Language from SecurePrefsUtil (lanId=" + lanId + "): " + langCode);
                    // Sincronizza anche in SharedPreferences standard per compatibilità
                    persist(context, langCode);
                    return langCode;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error reading from SecurePrefsUtil, falling back to SharedPreferences", e);
        }
        
        // Fallback a SharedPreferences standard
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String savedLang = preferences.getString(SELECTED_LANGUAGE, null);
        if (!TextUtils.isEmpty(savedLang)) {
            Log.d(TAG, "Language from SharedPreferences: " + savedLang);
            return savedLang;
        }
        
        Log.d(TAG, "Using default language: " + defaultLanguage);
        return defaultLanguage;
    }

    /**
     * Converte lanId in codice lingua
     * 1 = en, 2 = fr, 3 = pt, 4 = it
     */
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
        Log.d(TAG, "Language persisted: " + language);
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
