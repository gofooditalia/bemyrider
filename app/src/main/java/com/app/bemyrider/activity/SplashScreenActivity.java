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
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);

        context = SplashScreenActivity.this;
        printHashKey(this);

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            try {
                PrefsUtil.with(SplashScreenActivity.this).write("device_token", token);
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        });

        sp_select_lan = findViewById(R.id.sp_select_lan);
        btn_continue = findViewById(R.id.btn_continue);
        lanadapter = new ArrayAdapter<>
                (SplashScreenActivity.this,
                        R.layout.spinner_item_inverse,
                        languagePojoItems);
        lanadapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        sp_select_lan.setAdapter(lanadapter);

        lanId = PrefsUtil.with(SplashScreenActivity.this).readString("lanId");
        Log.e("SPLASH SCRENN ID", lanId + "ADv");

        sp_select_lan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PrefsUtil.with(SplashScreenActivity.this).write("lanId", languagePojoItems.get(i).getId());
                ((TextView) adapterView.getChildAt(0)).setTextColor(getResources().getColor(R.color.white));
                lanId = languagePojoItems.get(i).getId();
                Log.e("SEcond SPLASH", lanId + "vdef");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        checkVersion();

        btn_continue.setOnClickListener(view -> {
            if (lanId.equals("2")) {
                LocaleManager.setLocale(context, "fr");
                Intent intent = new Intent(SplashScreenActivity.this, IntroductionActivity.class);
                startActivity(intent);
                finish();
            } else if (lanId.equals("3")) {
                LocaleManager.setLocale(context, "pt");
                Intent intent = new Intent(SplashScreenActivity.this, IntroductionActivity.class);
                startActivity(intent);
                finish();
            } else if (lanId.equals("4")) {
                LocaleManager.setLocale(context, "it");
                Intent intent = new Intent(SplashScreenActivity.this, IntroductionActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(SplashScreenActivity.this, IntroductionActivity.class);
                startActivity(intent);
                finish();
            }

        });
    }

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
        textParams.put("email", PrefsUtil.with(SplashScreenActivity.this).readString("eMail"));
        textParams.put("password", PrefsUtil.with(SplashScreenActivity.this).readString("Pass"));
        textParams.put("device_token", PrefsUtil.with(SplashScreenActivity.this).readString("device_token"));

        new WebServiceCall(context, WebServiceUrl.URL_LOGIN, textParams, NewLoginPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    NewLoginPojo login_response = ((NewLoginPojo) obj);
                    NewLoginPojoItem item = login_response.getData();

//                    PrefsUtil.with(SplashScreenActivity.this).clearPrefs(); // it will clear password also so.. Do not clear here
                    PrefsUtil.with(SplashScreenActivity.this).write("UserId", login_response.getData().getUserId());
                    PrefsUtil.with(SplashScreenActivity.this).write("countrycodeid", login_response.getData().getCountryCodeId());
                    PrefsUtil.with(SplashScreenActivity.this).write("UserName", login_response.getData().getUserName());
                    PrefsUtil.with(SplashScreenActivity.this).write("FirstName", login_response.getData().getFirstName());
                    PrefsUtil.with(SplashScreenActivity.this).write("LastName", login_response.getData().getLastName());
                    PrefsUtil.with(SplashScreenActivity.this).write("UserType", login_response.getData().getUserType());
                    PrefsUtil.with(SplashScreenActivity.this).write("eMail", login_response.getData().getEmailId());
                    PrefsUtil.with(SplashScreenActivity.this).write("UserImg", login_response.getData().getProfileImg());
                    PrefsUtil.with(SplashScreenActivity.this).write("login_cust_address", login_response.getData().getAddress());

                    if (lanId.equals("2")) {
                        LocaleManager.setLocale(context, "fr");
                    } else if (lanId.equals("3")) {
                        LocaleManager.setLocale(context, "pt");
                    } else if (lanId.equals("4")) {
                        LocaleManager.setLocale(context, "it");
                    } else {
                        LocaleManager.setLocale(context, "en");
                    }

                    if (item.getUserType().equals("c")) {
                        if (item.getFirstName().equals("") || item.getLastName().equals("") || item.getContactNumber().equals("")) {
                                /*|| item.getCompanyName().equals("") || item.getVat().equals("") || item.getTaxId().equals("") ||
                                item.getCertifiedEmail().equals("") || item.getReceiptCode().equals("")) {*/

                            Intent intent = new Intent(context, EditProfileActivity.class);
                            intent.putExtra("isFromEdit", false);
                            intent.putExtra("loginPojoData", item);
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(context, CustomerHomeActivity.class));
                        }
                        finish();
                    } else {
                        if (item.getFirstName().equals("") || item.getLastName().equals("") || item.getContactNumber().equals("") ||
                                item.getCompanyName().equals("") || item.getVat().equals("") /*|| item.getTaxId().equals("") ||
                                item.getCertifiedEmail().equals("") || item.getReceiptCode().equals("")*/) {

                            Intent intent = new Intent(context, com.app.bemyrider.activity.partner.EditProfileActivity.class);
                            intent.putExtra("isFromEdit", false);
                            intent.putExtra("loginPojoData", item);
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(context, ProviderHomeActivity.class));
                        }
                        finish();
                    }
                } else {
                    if (lanId.equals("2")) {
                        LocaleManager.setLocale(context, "fr");
                        Intent intent = new Intent(SplashScreenActivity.this, SignupActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (lanId.equals("3")) {
                        LocaleManager.setLocale(context, "pt");
                        Intent intent = new Intent(SplashScreenActivity.this, SignupActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (lanId.equals("4")) {
                        LocaleManager.setLocale(context, "it");
                        startActivity(new Intent(SplashScreenActivity.this,
                                SignupActivity.class));
                        finish();
                    } else {
                        LocaleManager.setLocale(context, "en");
                        Intent intent = new Intent(SplashScreenActivity.this, SignupActivity.class);
                        startActivity(intent);
                        finish();
                    }

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
        PrefsUtil.with(SplashScreenActivity.this).clearPrefs();
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
        textParams.put("device_token", PrefsUtil.with(SplashScreenActivity.this).readString("device_token"));


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
                            PrefsUtil.with(SplashScreenActivity.this).clearPrefs();
                            PrefsUtil.with(SplashScreenActivity.this).write("UserId", loginData.getUserId());
                            PrefsUtil.with(SplashScreenActivity.this).write("CurrencySign", loginData.getCurrencySign());
                            PrefsUtil.with(SplashScreenActivity.this).write("UserName", loginData.getUserName());
                            PrefsUtil.with(SplashScreenActivity.this).write("FirstName", loginData.getFirstName());
                            PrefsUtil.with(SplashScreenActivity.this).write("LastName", loginData.getLastName());
                            PrefsUtil.with(SplashScreenActivity.this).write("UserType", loginData.getUserType());
                            PrefsUtil.with(SplashScreenActivity.this).write("eMail", loginData.getEmailId());
                            PrefsUtil.with(SplashScreenActivity.this).write("loginType", logintype);
                            PrefsUtil.with(SplashScreenActivity.this).write("socialId", social_id);
                            PrefsUtil.with(SplashScreenActivity.this).write("UserImg", loginData.getProfileImg());
                            PrefsUtil.with(SplashScreenActivity.this).write("login_cust_address", loginData.getAddress());


                            if (PrefsUtil.with(SplashScreenActivity.this).readString("UserType").equalsIgnoreCase("c")) {
                                startActivity(new Intent(SplashScreenActivity.this, CustomerHomeActivity.class));
                            } else {
                                startActivity(new Intent(SplashScreenActivity.this, ProviderHomeActivity.class));
                            }
                        }
                    }
                } else {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(SplashScreenActivity.this);
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
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(SplashScreenActivity.this).readString("UserId"));

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
                            Log.e("TAG", "Success" + new File(getFilesDir().getPath() + "/offline.json").exists());
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
                                        newVersion = Integer.parseInt(versionDataPOJO.getVersionData().getAppVersion());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (versionCode < newVersion) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
                                        //builder.setTitle("Error");
                                        builder.setMessage("The version that you are using is expired. Kindly update the app.");
                                        builder.setPositiveButton("Update", (dialog, which) -> {
                                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                            }
                                            finish();
                                        });
                                        builder.setNegativeButton("Cancel", (dialogInterface, i) -> finish());
                                        builder.show();
                                    } else {
                                        if (PrefsUtil.with(SplashScreenActivity.this).readString("lanId").equals("")) {
                                            sp_select_lan.setVisibility(View.VISIBLE);
                                            btn_continue.setVisibility(View.VISIBLE);
                                            serviceCallGetLanguage();
                                        } else {
                                            doLogin();
                                        }
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (PrefsUtil.with(SplashScreenActivity.this).readString("lanId").equals("")) {
                                    sp_select_lan.setVisibility(View.VISIBLE);
                                    btn_continue.setVisibility(View.VISIBLE);
                                    serviceCallGetLanguage();
                                } else {
                                    doLogin();
                                }
                            }
                        } else {
                            if (PrefsUtil.with(SplashScreenActivity.this).readString("lanId").equals("")) {
                                sp_select_lan.setVisibility(View.VISIBLE);
                                btn_continue.setVisibility(View.VISIBLE);
                                serviceCallGetLanguage();
                            } else {
                                doLogin();
                            }
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

    private void doLogin() {
        if (PrefsUtil.with(SplashScreenActivity.this).readString("loginType") != null
                && PrefsUtil.with(SplashScreenActivity.this)
                .readString("loginType").length() > 0
                && PrefsUtil.with(SplashScreenActivity.this).readString("socialId") != null
                && PrefsUtil.with(SplashScreenActivity.this)
                .readString("socialId").length() > 0) {
            serviceCallSocialLogin(
                    PrefsUtil.with(SplashScreenActivity.this).readString("eMail"),
                    PrefsUtil.with(SplashScreenActivity.this).readString("FirstName"),
                    PrefsUtil.with(SplashScreenActivity.this).readString("LastName"),
                    PrefsUtil.with(SplashScreenActivity.this).readString("loginType"),
                    PrefsUtil.with(SplashScreenActivity.this).readString("socialId"));
        } else {
            serviceCallLogin();
        }
        saveOfflineData();
    }

    private void serviceCallGetLanguage() {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        new WebServiceCall(SplashScreenActivity.this, WebServiceUrl.URL_GET_LANGUAGE,
                textParams, LanguagePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    LanguagePojo languagePojo = (LanguagePojo) obj;
                    for (int i = 0; i < languagePojo.getData().size(); i++) {
                        Log.e("Splash", "onResult: " + languagePojo.getData().get(i).getLanguageName());
                        if (languagePojo.getData().get(i).getLanguageName().trim().equalsIgnoreCase("french") ||
                                languagePojo.getData().get(i).getLanguageName().trim().equalsIgnoreCase("english") ||
                                languagePojo.getData().get(i).getLanguageName().trim().equalsIgnoreCase("Portuguese") ||
                                languagePojo.getData().get(i).getLanguageName().trim().equalsIgnoreCase("Italian")) {
                            languagePojoItems.add(languagePojo.getData().get(i));
                            Collections.reverse(languagePojoItems);
                            lanadapter.notifyDataSetChanged();
                        }
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
