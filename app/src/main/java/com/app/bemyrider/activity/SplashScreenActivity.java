package com.app.bemyrider.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.partner.ProviderHomeActivity;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.model.VersionDataPOJO;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.SecurePrefsUtil;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;

/**
 * BLAZING FAST SPLASH SCREEN - Refactored 2024
 * - No 3-second delay
 * - No synchronous blocking API calls
 * - Fast path directly to Home if logged in
 * - Background data sync
 */
public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";
    private Context context;
    private ConnectionManager connectionManager;
    private SecurePrefsUtil securePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        context = SplashScreenActivity.this;
        securePrefs = SecurePrefsUtil.with(context);

        handleDeepLink();
        migrateLegacyPrefs();
        setupFirebaseAndConnection();

        // Set language default if missing
        String lanId = securePrefs.readString("lanId");
        if (lanId == null || lanId.isEmpty()) {
            securePrefs.write("lanId", "4"); // Default to Italian
        }

        // ==========================================================
        // FAST PATH: Start background tasks (Non-Blocking)
        // ==========================================================
        performBackgroundTasks();

        // ==========================================================
        // FAST PATH: Immediate Navigation
        // ==========================================================
        navigateToNextScreen();
    }

    private void handleDeepLink() {
        Intent deepLinkIntent = getIntent();
        if (deepLinkIntent != null && deepLinkIntent.getData() != null) {
            Uri data = deepLinkIntent.getData();
            String providerId = data.getQueryParameter("id");
            if (providerId != null && !providerId.isEmpty()) {
                securePrefs.write("pending_deeplink_id", providerId);
                Log.i(TAG, "Deep link intercepted for provider: " + providerId);
            }
        }
    }

    private void migrateLegacyPrefs() {
        PrefsUtil legacyPrefs = PrefsUtil.with(context);
        if (legacyPrefs.readString("UserId") != null && !legacyPrefs.readString("UserId").isEmpty()) {
            Log.i(TAG, "Migrating legacy preferences to encrypted storage...");
            securePrefs.migrateFromLegacy(legacyPrefs);
        }
    }

    private void setupFirebaseAndConnection() {
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            try {
                securePrefs.write("device_token", token);
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        });
    }

    private void performBackgroundTasks() {
        Log.d(TAG, "🔄 Starting background tasks (Version Check & Offline Data)");
        checkVersionInBackground();
        saveOfflineDataInBackground();
    }

    private void navigateToNextScreen() {
        String userId = securePrefs.readString("UserId");
        String userType = securePrefs.readString("UserType");
        boolean hasSeenIntro = securePrefs.readBoolean("hasSeenIntro");

        // Apply Language
        String lanId = securePrefs.readString("lanId");
        String langCode = "it";
        if ("2".equals(lanId)) langCode = "fr";
        else if ("3".equals(lanId)) langCode = "pt";
        else if ("1".equals(lanId)) langCode = "en";
        LocaleManager.setLocale(context, langCode);

        if (userId != null && !userId.isEmpty() && !userId.equals("0")) {
            // ✅ FAST PATH: User is logged in! Go to Home directly.
            Log.d(TAG, "🚀 Fast Path: User already logged in, navigating to Home IMMEDIATELY.");
            if ("c".equalsIgnoreCase(userType)) {
                startActivity(new Intent(context, CustomerHomeActivity.class));
            } else {
                startActivity(new Intent(context, ProviderHomeActivity.class));
            }
        } else {
            // ❌ User not logged in
            Log.d(TAG, "📝 User not logged in, navigating to Login/Intro.");
            if (!hasSeenIntro) {
                startActivity(new Intent(context, IntroductionActivity.class));
            } else {
                startActivity(new Intent(context, SignupActivity.class));
            }
        }
        
        // Close splash screen instantly
        finish();
    }

    private void checkVersionInBackground() {
        Context appContext = getApplicationContext();
        new WebServiceCall(appContext, WebServiceUrl.URL_GETSITESETTINGDATA,
                new LinkedHashMap<>(), VersionDataPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    try {
                        VersionDataPOJO versionDataPOJO = (VersionDataPOJO) obj;
                        if (versionDataPOJO.isStatus() && "y".equalsIgnoreCase(versionDataPOJO.getVersionData().getForcedUpdate())) {
                            int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                            int newVersion = Integer.parseInt(versionDataPOJO.getVersionData().getAppVersion());
                            
                            if (versionCode < newVersion) {
                                Log.d(TAG, "⚠️ Forced update needed. Launching Play Store.");
                                Toast.makeText(appContext, "Aggiornamento app richiesto. Reindirizzamento in corso...", Toast.LENGTH_LONG).show();
                                
                                final String appPackageName = getPackageName();
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                try {
                                    appContext.startActivity(intent);
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                                    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    appContext.startActivity(webIntent);
                                }
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            @Override public void onAsync(Object obj) {}
            @Override public void onCancelled() {}
        });
    }

    private void saveOfflineDataInBackground() {
        String userId = securePrefs.readString("UserId");
        if (userId == null || userId.isEmpty() || userId.equals("0")) return;

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("user_id", userId);
        
        Context appContext = getApplicationContext();
        new WebServiceCall(appContext, WebServiceUrl.URL_GETOFFLINEDATA,
                textParams, String.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    try {
                        File offlineFile = new File(appContext.getFilesDir().getPath(), "/offline.json");
                        if (!offlineFile.exists()) offlineFile.createNewFile();
                        FileWriter file = new FileWriter(offlineFile);
                        file.write((String) obj);
                        file.flush();
                        file.close();
                        Log.d(TAG, "✅ Offline data saved in background");
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            @Override public void onAsync(Object obj) {}
            @Override public void onCancelled() {}
        });
    }

    @Override
    protected void onDestroy() {
        try {
            if (connectionManager != null) connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
