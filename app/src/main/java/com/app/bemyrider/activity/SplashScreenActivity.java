package com.app.bemyrider.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.WebServices.WebServiceUrl;

import com.app.bemyrider.R;
import com.app.bemyrider.activity.partner.ProviderHomeActivity;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.activity.user.EditProfileActivity;
import com.app.bemyrider.model.LanguagePojo;
import com.app.bemyrider.model.LanguagePojoItem;
import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.model.VersionDataPOJO;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import android.util.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.SecurePrefsUtil;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";
    private Spinner sp_select_lan;
    private Button btn_continue;
    private ArrayAdapter lanadapter;
    private String lanId;
    private ArrayList<LanguagePojoItem> languagePojoItems = new ArrayList<>();
    private AsyncTask autoLoginAsync, socialLoginAsync, offlineDataAsync, checkVersionAsync, getLanguageAsync;
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

        // --- DEEP LINK HANDLING ---
        Intent deepLinkIntent = getIntent();
        if (deepLinkIntent != null && deepLinkIntent.getData() != null) {
            Uri data = deepLinkIntent.getData();
            String providerId = data.getQueryParameter("id");
            if (providerId != null && !providerId.isEmpty()) {
                securePrefs.write("pending_deeplink_id", providerId);
                Log.i(TAG, "Deep link intercepted for provider: " + providerId);
            }
        }
        // --------------------------

        PrefsUtil legacyPrefs = PrefsUtil.with(context);
        if (legacyPrefs.readString("UserId") != null && !legacyPrefs.readString("UserId").isEmpty()) {
            Log.i(TAG, "Migrating legacy preferences to encrypted storage...");
            securePrefs.migrateFromLegacy(legacyPrefs);
        }

        printHashKey(this);

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

        sp_select_lan = findViewById(R.id.sp_select_lan);
        btn_continue = findViewById(R.id.btn_continue);

        lanadapter = new ArrayAdapter<>(SplashScreenActivity.this,
                R.layout.spinner_item_inverse,
                languagePojoItems);
        lanadapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        if (sp_select_lan != null) {
            sp_select_lan.setAdapter(lanadapter);
            sp_select_lan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    securePrefs.write("lanId", languagePojoItems.get(i).getId());
                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(getResources().getColor(R.color.white));
                    }
                    lanId = languagePojoItems.get(i).getId();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        lanId = securePrefs.readString("lanId");
        if (lanId == null || lanId.isEmpty()) {
            lanId = "4"; 
            securePrefs.write("lanId", lanId);
            Log.w(TAG, "lanId was empty, initialized to default: " + lanId);
        }
        Log.e("SPLASH SCREEN ID", lanId + "ADv");

        new android.os.Handler().postDelayed(new Runnable() {

    @Override
    public void run() {
        checkVersion();
    }},3000);

    if(btn_continue!=null){btn_continue.setOnClickListener(view->{

    String langCode = "en";if("2".equals(lanId))langCode="fr";else if("3".equals(lanId))langCode="pt";else if("4".equals(lanId))langCode="it";

    LocaleManager.setLocale(context,langCode);
    Intent intent = new Intent(SplashScreenActivity.this, IntroductionActivity.class);

    startActivity(intent);
                finish();
            });

    }}

    public void printHashKey(Context pContext) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i("LoginActivity", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "printHashKey() :: " + e);
        }
    }

    private void serviceCallLogin() {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("email", securePrefs.readString("eMail"));
        textParams.put("password", securePrefs.readString("Pass"));
        textParams.put("device_token", securePrefs.readString("device_token"));

        new WebServiceCall(context, WebServiceUrl.URL_LOGIN, textParams, NewLoginPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            NewLoginPojo login_response = ((NewLoginPojo) obj);
                            NewLoginPojoItem item = login_response.getData();

                            securePrefs.write("UserId", login_response.getData().getUserId());
                            securePrefs.write("countrycodeid", login_response.getData().getCountryCodeId());
                            securePrefs.write("UserName", login_response.getData().getUserName());
                            securePrefs.write("FirstName", login_response.getData().getFirstName());
                            securePrefs.write("LastName", login_response.getData().getLastName());
                            securePrefs.write("UserType", login_response.getData().getUserType());
                            securePrefs.write("eMail", login_response.getData().getEmailId());
                            securePrefs.write("UserImg", login_response.getData().getProfileImg());
                            securePrefs.write("login_cust_address", login_response.getData().getAddress());

                            saveOfflineData();

                            String langCode = "en";
                            if ("2".equals(lanId))
                                langCode = "fr";
                            else if ("3".equals(lanId))
                                langCode = "pt";
                            else if ("4".equals(lanId))
                                langCode = "it";
                            LocaleManager.setLocale(context, langCode);

                            if (item.getUserType().equals("c")) {
                                if (item.getFirstName().equals("") || item.getLastName().equals("")
                                        || item.getContactNumber().equals("")) {
                                    Intent intent = new Intent(context, EditProfileActivity.class);
                                    intent.putExtra("isFromEdit", false);
                                    intent.putExtra("loginPojoData", item);
                                    startActivity(intent);
                                } else {
                                    startActivity(new Intent(context, CustomerHomeActivity.class));
                                }
                                finish();
                            } else {
                                if (item.getFirstName().equals("") || item.getLastName().equals("")
                                        || item.getContactNumber().equals("") ||
                                        item.getCompanyName().equals("") || item.getVat().equals("")) {

                                    Intent intent = new Intent(context,
                                            com.app.bemyrider.activity.partner.EditProfileActivity.class);
                                    intent.putExtra("isFromEdit", false);
                                    intent.putExtra("loginPojoData", item);
                                    startActivity(intent);
                                } else {
                                    startActivity(new Intent(context, ProviderHomeActivity.class));
                                }
                                finish();
                            }
                        } else {
                            String langCode = "en";
                            if ("2".equals(lanId))
                                langCode = "fr";
                            else if ("3".equals(lanId))
                                langCode = "pt";
                            else if ("4".equals(lanId))
                                langCode = "it";
                            LocaleManager.setLocale(context, langCode);

                            Intent intent = new Intent(SplashScreenActivity.this, SignupActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        autoLoginAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        autoLoginAsync = null;
                    }
                });
    }

    public void serviceCallSocialLogin(final String email, final String firstName,
            final String lastName, final String logintype,
            final String social_id) {
        securePrefs.clearPrefs();
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("first_name", firstName);
        textParams.put("last_name", lastName);
        textParams.put("email", email);
        textParams.put("login_type", logintype);
        if (logintype.equals("f")) {
            textParams.put("fbid", social_id);
        } else if (logintype.equals("g")) {
            textParams.put("googleid", social_id);
        } else if (logintype.equals("l")) {
            textParams.put("linkedinid", social_id);
        }
        textParams.put("device_token", securePrefs.readString("device_token"));

        new WebServiceCall(SplashScreenActivity.this, WebServiceUrl.URL_SOCIAL_LOGIN,
                textParams, NewLoginPojo.class, false, new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            NewLoginPojo resultObj = (NewLoginPojo) obj;
                            if (resultObj.isStatus()) {
                                NewLoginPojoItem loginData = resultObj.getData();

                                if (loginData.getUserType().equals("")) {
                                    Intent intent = new Intent(SplashScreenActivity.this, SignupActivity.class);
                                    intent.putExtra("SocialFlag", "true");
                                    intent.putExtra("Email", email);
                                    intent.putExtra("Fname", firstName);
                                    intent.putExtra("Lname", lastName);
                                    intent.putExtra("uId", loginData.getUserId());
                                    startActivity(intent);
                                    finish();
                                } else {
                                    securePrefs.clearPrefs();
                                    securePrefs.write("UserId", loginData.getUserId());
                                    securePrefs.write("CurrencySign", loginData.getCurrencySign());
                                    securePrefs.write("UserName", loginData.getUserName());
                                    securePrefs.write("FirstName", loginData.getFirstName());
                                    securePrefs.write("LastName", loginData.getLastName());
                                    securePrefs.write("UserType", loginData.getUserType());
                                    securePrefs.write("eMail", loginData.getEmailId());
                                    securePrefs.write("loginType", logintype);
                                    securePrefs.write("socialId", social_id);
                                    securePrefs.write("UserImg", loginData.getProfileImg());
                                    securePrefs.write("login_cust_address", loginData.getAddress());

                                    if (securePrefs.readString("UserType").equalsIgnoreCase("c")) {
                                        startActivity(
                                                new Intent(SplashScreenActivity.this, CustomerHomeActivity.class));
                                    } else {
                                        startActivity(
                                                new Intent(SplashScreenActivity.this, ProviderHomeActivity.class));
                                    }
                                }
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
                            builder.setPositiveButton(getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).setMessage((String) obj).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        socialLoginAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        socialLoginAsync = null;
                    }
                });
    }

    private void saveOfflineData() {
        String userId = securePrefs.readString("UserId");
        if (userId == null || userId.isEmpty() || userId.equals("0")) {
            Log.w(TAG, "saveOfflineData: UserId is empty, skipping offline data sync");
            return;
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", userId);

        new WebServiceCall(SplashScreenActivity.this, WebServiceUrl.URL_GETOFFLINEDATA,
                textParams, String.class, false, new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            try {
                                try {
                                    File offlineFile = new File(getFilesDir().getPath(), "/offline.json");
                                    if (!offlineFile.exists()) {
                                        offlineFile.createNewFile();
                                        Log.e(TAG, "Create Offline File :: ");
                                    }
                                    FileWriter file = new FileWriter(offlineFile);
                                    file.write((String) obj);
                                    file.flush();
                                    file.close();
                                    Log.e("TAG",
                                            "Success" + new File(getFilesDir().getPath() + "/offline.json").exists());
                                } catch (IOException e) {
                                    Log.e("TAG", "Error in Writing: " + e.getLocalizedMessage());
                                }
                            } catch (Exception e) {
                                Log.e("TAG", "Error");
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        offlineDataAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        offlineDataAsync = null;
                    }
                });
    }

    private void checkVersion() {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        new WebServiceCall(SplashScreenActivity.this, WebServiceUrl.URL_GETSITESETTINGDATA,
                textParams, VersionDataPOJO.class, false, new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        Log.e("TAG", "onResult: " + "asdf");
                        if (status) {
                            try {
                                VersionDataPOJO versionDataPOJO = (VersionDataPOJO) obj;
                                if (versionDataPOJO.isStatus()) {
                                    if (versionDataPOJO.getVersionData().getForcedUpdate().equalsIgnoreCase("y")) {
                                        Log.e("TAG", "onResult: " + "asdf");
                                        try {
                                            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                            int versionCode = pInfo.versionCode;
                                            int newVersion = -1;
                                            try {
                                                newVersion = Integer
                                                        .parseInt(versionDataPOJO.getVersionData().getAppVersion());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            if (versionCode < newVersion) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                                        SplashScreenActivity.this);
                                                // builder.setTitle("Error");
                                                builder.setMessage(
                                                        "The version that you are using is expired. Kindly update the app.");
                                                builder.setPositiveButton("Update", (dialog, which) -> {
                                                    final String appPackageName = getPackageName(); // getPackageName()
                                                                                                    // from Context or
                                                                                                    // Activity object
                                                    try {
                                                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                                Uri.parse("market://details?id=" + appPackageName)));
                                                    } catch (android.content.ActivityNotFoundException anfe) {
                                                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                                Uri.parse(
                                                                        "https://play.google.com/store/apps/details?id="
                                                                                + appPackageName)));
                                                    }
                                                    finish();
                                                });
                                                builder.setNegativeButton("Cancel", (dialogInterface, i) -> finish());
                                                builder.show();
                                            } else {
                                                proceedWithLoginFlow();
                                            }
                                        } catch (PackageManager.NameNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        proceedWithLoginFlow();
                                    }
                                } else {
                                    proceedWithLoginFlow();
                                }
                            } catch (Exception e) {
                                Log.e("TAG", "Error");
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        checkVersionAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        checkVersionAsync = null;
                    }
                });
    }

    private void proceedWithLoginFlow() {
        boolean hasSeenIntro = securePrefs.readBoolean("hasSeenIntro");

        if (!hasSeenIntro) {
            String langCode = "it"; 
            if (!securePrefs.readString("lanId").equals("")) {
                if ("2".equals(securePrefs.readString("lanId")))
                    langCode = "fr";
                else if ("3".equals(securePrefs.readString("lanId")))
                    langCode = "pt";
                else if ("4".equals(securePrefs.readString("lanId")))
                    langCode = "it";
                else if ("1".equals(securePrefs.readString("lanId")))
                    langCode = "en";
            }
            LocaleManager.setLocale(context, langCode);
            Intent intent = new Intent(SplashScreenActivity.this, IntroductionActivity.class);
            startActivity(intent);
            finish();
        } else if (securePrefs.readString("lanId").equals("")) {
            if (sp_select_lan != null)
                sp_select_lan.setVisibility(View.VISIBLE);
            if (btn_continue != null)
                btn_continue.setVisibility(View.VISIBLE);
            if (findViewById(R.id.progressBar) != null)
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            serviceCallGetLanguage();
        } else {
            doLogin();
        }
    }

    private void doLogin() {
        String email = securePrefs.readString("eMail");
        String password = securePrefs.readString("Pass");

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Log.d(TAG, "No saved credentials, going to login screen");
            String langCode = "it"; 
            if (!securePrefs.readString("lanId").equals("")) {
                if ("2".equals(securePrefs.readString("lanId")))
                    langCode = "fr";
                else if ("3".equals(securePrefs.readString("lanId")))
                    langCode = "pt";
                else if ("4".equals(securePrefs.readString("lanId")))
                    langCode = "it";
                else if ("1".equals(securePrefs.readString("lanId")))
                    langCode = "en";
            }
            LocaleManager.setLocale(context, langCode);
            Intent intent = new Intent(SplashScreenActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (securePrefs.readString("loginType") != null
                && securePrefs.readString("loginType").length() > 0
                && securePrefs.readString("socialId") != null
                && securePrefs.readString("socialId").length() > 0) {
            serviceCallSocialLogin(
                    securePrefs.readString("eMail"),
                    securePrefs.readString("FirstName"),
                    securePrefs.readString("LastName"),
                    securePrefs.readString("loginType"),
                    securePrefs.readString("socialId"));
        } else {
            serviceCallLogin();
        }
    }

    private void serviceCallGetLanguage() {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        new WebServiceCall(SplashScreenActivity.this, WebServiceUrl.URL_GET_LANGUAGE,
                textParams, LanguagePojo.class, false, new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            LanguagePojo languagePojo = (LanguagePojo) obj;
                            if (languagePojo != null && languagePojo.getData() != null) {
                                for (int i = 0; i < languagePojo.getData().size(); i++) {
                                    String langName = languagePojo.getData().get(i).getLanguageName();
                                    Log.e("Splash", "onResult: " + langName);
                                    if (langName != null && (langName.trim().equalsIgnoreCase("french") ||
                                            langName.trim().equalsIgnoreCase("english") ||
                                            langName.trim().equalsIgnoreCase("Portuguese") ||
                                            langName.trim().equalsIgnoreCase("Italian"))) {
                                        languagePojoItems.add(languagePojo.getData().get(i));
                                    }
                                }
                                Collections.reverse(languagePojoItems);
                                if (lanadapter != null)
                                    lanadapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(SplashScreenActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        getLanguageAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        getLanguageAsync = null;
                    }
                });
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (autoLoginAsync != null) {
            autoLoginAsync.cancel(true);
        }
        if (socialLoginAsync != null) {
            socialLoginAsync.cancel(true);
        }
        if (offlineDataAsync != null) {
            offlineDataAsync.cancel(true);
        }
        if (checkVersionAsync != null) {
            checkVersionAsync.cancel(true);
        }
        if (getLanguageAsync != null) {
            getLanguageAsync.cancel(true);
        }
        super.onDestroy();
    }

    private String getLangCodeFromLanId(String lanId) {
        if (lanId == null || lanId.isEmpty()) {
            return "it"; 
        }
        switch (lanId) {
            case "1":
                return "en";
            case "2":
                return "fr";
            case "3":
                return "pt";
            case "4":
                return "it";
            default:
                Log.w(TAG, "Unknown lanId: " + lanId + ", defaulting to Italian");
                return "it";
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }

}