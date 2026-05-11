package com.app.bemyrider.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.bemyrider.R;
import com.app.bemyrider.viewmodel.SplashViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.activity.partner.ProviderHomeActivity;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.model.VersionDataPOJO;
import com.app.bemyrider.viewmodel.SplashViewModel;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.SecurePrefsUtil;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BLAZING FAST SPLASH SCREEN - Refactored 2024
 * - Fast path directly to Home if logged in (with minimal delay for forced update check)
 * - Background data sync
 */
public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";
    private Context context;
    private ConnectionManager connectionManager;
    private SecurePrefsUtil securePrefs;
    private ExecutorService diskExecutor = Executors.newSingleThreadExecutor();
    
    private SplashViewModel viewModel;
    private boolean isUpdateRequired = false;
    private boolean isVersionChecked = false;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable fallbackNavigationTask;

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

        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        viewModel.getSiteSettings().observe(this, this::handleVersionCheck);
        viewModel.getOfflineData().observe(this, data -> {
            if (data != null) saveOfflineDataToFile(data);
        });

        // ==========================================================
        // Avvia i task in background
        // ==========================================================
        saveOfflineDataInBackground();
        
        // ==========================================================
        // Safe Fast Path: Controlla aggiornamenti critici prima di navigare
        // ==========================================================
        checkVersionAndNavigate();
        
        // Timeout di sicurezza: se la connessione è instabile, naviga comunque dopo 2.5s
        fallbackNavigationTask = () -> {
            if (!isVersionChecked && !isDestroyed()) {
                Log.w(TAG, "Timeout check versione, procedo con la navigazione standard.");
                navigateToNextScreen();
            }
        };
        mainHandler.postDelayed(fallbackNavigationTask, 2500);
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
        if (!securePrefs.readBoolean("migrated_to_secure")) {
            PrefsUtil legacyPrefs = PrefsUtil.with(context);
            if (legacyPrefs.readString("UserId") != null && !legacyPrefs.readString("UserId").isEmpty()) {
                Log.i(TAG, "Migrating legacy preferences to encrypted storage...");
                securePrefs.migrateFromLegacy(legacyPrefs);
            } else {
                securePrefs.write("migrated_to_secure", true);
            }
        }
        
        // Fix for previously corrupted users: restore the missing UserId in legacy
        securePrefs.syncToLegacyIfNeeded();
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

    private void navigateToNextScreen() {
        if (isUpdateRequired) return; // Blocca la navigazione se serve un update

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
            Log.d(TAG, "🚀 Fast Path: User already logged in, navigating to Home.");
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

    private void checkVersionAndNavigate() {
        viewModel.loadSiteSettings();
    }

    private void handleVersionCheck(VersionDataPOJO versionDataPOJO) {
        if (isDestroyed()) return;
        isVersionChecked = true;
        mainHandler.removeCallbacks(fallbackNavigationTask);
        if (versionDataPOJO != null) {
            try {
                if (versionDataPOJO.isStatus() && "y".equalsIgnoreCase(versionDataPOJO.getVersionData().getForcedUpdate())) {
                    int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                    int newVersion = Integer.parseInt(versionDataPOJO.getVersionData().getAppVersion());
                    if (versionCode < newVersion) {
                        isUpdateRequired = true;
                        Toast.makeText(this, "Aggiornamento app richiesto. Reindirizzamento in corso...", Toast.LENGTH_LONG).show();
                        final String pkg = getPackageName();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        try { startActivity(intent); }
                        catch (android.content.ActivityNotFoundException anfe) {
                            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pkg));
                            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(webIntent);
                        }
                        finish();
                        return;
                    }
                }
            } catch (Exception e) { Log.e(TAG, "Error checking version", e); }
        }
        navigateToNextScreen();
    }

    private void saveOfflineDataInBackground() {
        String userId = securePrefs.readString("UserId");
        if (userId == null || userId.isEmpty() || userId.equals("0")) return;
        viewModel.loadOfflineData(userId);
    }

    private void saveOfflineDataToFile(String data) {
        diskExecutor.execute(() -> {
            try {
                File offlineFile = new File(getFilesDir().getPath(), "/offline.json");
                if (!offlineFile.exists()) offlineFile.createNewFile();
                FileWriter fw = new FileWriter(offlineFile);
                fw.write(data);
                fw.flush();
                fw.close();
                Log.d(TAG, "✅ Offline data saved in background");
            } catch (Exception e) { Log.e(TAG, "Error saving offline data", e); }
        });
    }

    @Override
    protected void onDestroy() {
        try {
            if (connectionManager != null) connectionManager.unregisterReceiver();
            if (diskExecutor != null && !diskExecutor.isShutdown()) diskExecutor.shutdown();
            if (mainHandler != null && fallbackNavigationTask != null) {
                mainHandler.removeCallbacks(fallbackNavigationTask);
            }
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