package com.app.bemyrider.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.WebServices.WebServiceUrl;


import com.app.bemyrider.R;
import com.app.bemyrider.activity.partner.ProviderHomeActivity;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.activity.user.EditProfileActivity;
import com.app.bemyrider.databinding.ActivityLoginBinding;
import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Created by nct121 on 5/12/16.
 * Modified by Hardik Talaviya on 3/12/19.
 */

public class LoginActivity extends AppCompatActivity {
    private static final String host = "api.linkedin.com";
    private static final String url = "https://" + host
            + "/v1/people/~:" +
            "(id,first-name,last-name,email-address,phone-numbers,picture-url)";

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private CallbackManager callbackmanager;
    private String strUsername, strPassword;
    private String clicktype = "";
    private Context context = LoginActivity.this;
    private Activity activity = LoginActivity.this;
    private GoogleApiClient mGoogleApiClient;
    private AsyncTask resendMailAsync, forgotPasswordAsync, userLoginAsync, socialSignInAsync, offlineDataAsync;
    private ConnectionManager connectionManager;

    ActivityResultLauncher<Intent> gmailActivityResult;
    ActivityResultLauncher<Intent> linkedInActivityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(activity, R.layout.activity_login, null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        printHashKey(this);

        if (PrefsUtil.with(activity).readString("UserId") != null && PrefsUtil.with(activity).readString("UserId").length() > 0) {
            boolean isProfileCompleted = PrefsUtil.with(context).readBoolean("isProfileCompleted");
            if (PrefsUtil.with(activity).readString("UserType").equalsIgnoreCase("c") && isProfileCompleted) {
                Intent i = new Intent(activity, CustomerHomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            } else if (PrefsUtil.with(activity).readString("UserType").equalsIgnoreCase("p") && isProfileCompleted) {
                Intent i = new Intent(activity, ProviderHomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        }

        initView();

        callbackmanager = CallbackManager.Factory.create();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(LoginActivity.this, connectionResult -> {

                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        textchangeListner();

        binding.txtSignup.setOnClickListener(v -> {
            //startActivity(new Intent(context, SignupActivity.class));
            finish();
        });

        binding.btnLogin.setOnClickListener(v -> {
            if (checkValidation()) {
                Utils.hideSoftKeyboard(activity);
                LoginServiceCall();
            }
        });

        binding.imgForgot.setOnClickListener(v -> {
            final Dialog d = new Dialog(context);
            d.setContentView(getLayoutInflater().inflate(R.layout.dialog_forgot_password, null));

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = d.getWindow();
            lp.copyFrom(window.getAttributes());
            //This makes the dialog take up the full width
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);

            ProgressBar progressBar = d.findViewById(R.id.progressBar);
            Button btn_resetPass = d.findViewById(R.id.btn_resetPass);
            final TextInputLayout til_uname = d.findViewById(R.id.til_uname);
            final EditText edt_uname = (EditText) d.findViewById(R.id.edt_uname);

            edt_uname.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    til_uname.setError("");
                    til_uname.setErrorEnabled(false);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            btn_resetPass.setOnClickListener(v1 -> {
                if (edt_uname.getText().toString().length() > 0) {
                    if (Utils.isEmailValid(edt_uname.getText().toString())) {
                        Utils.hideSoftKeyboard(activity);
                        btn_resetPass.setClickable(false);
                        forgotPass(edt_uname.getText().toString(), d, progressBar, btn_resetPass);
                    } else {
                        til_uname.setErrorEnabled(true);
                        til_uname.setError(getResources().getString(R.string.error_valid_email));
                    }
                } else {
                    til_uname.setErrorEnabled(true);
                    til_uname.setError(getResources().getString(R.string.error_required));
                }
            });
            d.show();

        });

        binding.txtResendActivationMail.setOnClickListener(view -> {
            final Dialog d = new Dialog(context);
            d.setContentView(getLayoutInflater().inflate(R.layout.dialog_forgot_password, null));

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = d.getWindow();
            lp.copyFrom(window.getAttributes());
            //This makes the dialog take up the full width
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);

            ProgressBar progressBar = d.findViewById(R.id.progressBar);
            Button btn_resetPass = d.findViewById(R.id.btn_resetPass);
            btn_resetPass.setText(R.string.resend_activation_mail);
            final TextInputLayout til_uname = d.findViewById(R.id.til_uname);
            til_uname.setHint(getString(R.string.email_id));
            final EditText edt_uname = d.findViewById(R.id.edt_uname);

            edt_uname.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    til_uname.setError("");
                    til_uname.setErrorEnabled(false);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            btn_resetPass.setOnClickListener(v -> {
                if (edt_uname.getText().toString().length() > 0) {
                    if (Utils.isEmailValid(edt_uname.getText().toString())) {
                        Utils.hideSoftKeyboard(activity);
                        btn_resetPass.setClickable(false);
                        resendActivationMail(edt_uname.getText().toString(), d, progressBar, btn_resetPass);
                    } else {
                        til_uname.setErrorEnabled(true);
                        til_uname.setError(getResources().getString(R.string.error_valid_email));
                    }
                } else {
                    til_uname.setErrorEnabled(true);
                    til_uname.setError(getResources().getString(R.string.error_required));
                }
            });
            d.show();
        });

        binding.imgFb.setOnClickListener(v -> {
            clicktype = "f";
            loginWithFacebook();
        });

        binding.imgGoogle.setOnClickListener(v -> {
            clicktype = "g";
            loginWithGooglePlus();
        });

        binding.imgLinkdin.setOnClickListener(view -> {
            clicktype = "l";
            loginWithLinkedIn();
        });
    }

    private void loginWithLinkedIn() {
        /*Intent intent = new Intent(activity, LinkedInLoginWebViewActivity.class);
        linkedInActivityResult.launch(intent);*/
    }

    private void textchangeListner() {

        binding.etLoginUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilLoginUsername.setErrorEnabled(false);
                binding.tilLoginUsername.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etLoginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilLoginPassword.setErrorEnabled(false);
                binding.tilLoginPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean checkValidation() {

        strUsername = binding.etLoginUsername.getText().toString().trim();
        strPassword = binding.etLoginPassword.getText().toString().trim();

        if (strUsername.isEmpty()) {
            binding.tilLoginUsername.setErrorEnabled(true);
            binding.tilLoginUsername.setError(getString(R.string.error_required));
            binding.etLoginUsername.requestFocus();
            return false;
        } else if (!Utils.isEmailValid(strUsername)) {
            binding.tilLoginUsername.setErrorEnabled(true);
            binding.tilLoginUsername.setError(getString(R.string.error_valid_email));
            binding.etLoginUsername.requestFocus();
            return false;
        } else if (strPassword.isEmpty()) {
            binding.tilLoginPassword.setErrorEnabled(true);
            binding.tilLoginPassword.setError(getString(R.string.error_required));
            binding.etLoginPassword.requestFocus();
            return false;
        } else if (strPassword.length() < 6) {
            binding.tilLoginPassword.setErrorEnabled(true);
            binding.tilLoginPassword.setError(getString(R.string.error_min_pwd));
            binding.etLoginPassword.requestFocus();
            return false;
        }
        return true;
    }


    /*--------------- Resend link Api Call ----------------*/
    private void resendActivationMail(String email, final Dialog d, ProgressBar progressResendLink, Button btn_resetPass) {
        progressResendLink.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("email", email);

        new WebServiceCall(this, WebServiceUrl.URL_RESEND_ACTIVATION_MAIL, textParams,
                NewLoginPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                progressResendLink.setVisibility(View.GONE);
                btn_resetPass.setClickable(true);
                if (status) {
                    d.dismiss();
                    NewLoginPojo pojo = (NewLoginPojo) obj;

                    /*For DeActive/Delete user redirect to contact us page*/
                    if (pojo.getData().getIsUserActive() != null && pojo.getData().getIsUserActive().equalsIgnoreCase("d")) {
                        PrefsUtil.with(activity).write("eMail", email);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) ->
                                startActivity(new Intent(activity, ContactUsActivity.class))).setMessage(pojo.getMessage()).show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(pojo.getMessage())
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage((String) obj)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                resendMailAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                resendMailAsync = null;
            }
        });
    }

    /*---------------- Forgot Password Api Call -----------------*/
    private void forgotPass(String email, final Dialog d, ProgressBar progressResetPass, Button btn_resetPass) {
        progressResetPass.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("email", email);

        new WebServiceCall(this, WebServiceUrl.URL_FORGET_PASSWORD, textParams,
                NewLoginPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                progressResetPass.setVisibility(View.GONE);
                btn_resetPass.setClickable(true);
                if (status) {
                    d.dismiss();
                    NewLoginPojo pojo = (NewLoginPojo) obj;

                    /*For DeActive/Delete user redirect to contact us page*/
                    if (pojo.getData().getIsUserActive() != null && pojo.getData().getIsUserActive().equalsIgnoreCase("d")) {
                        PrefsUtil.with(activity).write("eMail", email);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) ->
                                startActivity(new Intent(activity, ContactUsActivity.class))).setMessage(pojo.getMessage()).show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(pojo.getMessage())
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage((String) obj)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                forgotPasswordAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                forgotPasswordAsync = null;
            }
        });
    }

    /*--------------- Login Api Call --------------------*/
    private void LoginServiceCall() {
        binding.progressLogin.setVisibility(View.VISIBLE);
        binding.btnLogin.setClickable(false);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("email", binding.etLoginUsername.getText().toString().trim());
        textParams.put("password", binding.etLoginPassword.getText().toString().trim());
        textParams.put("device_token", PrefsUtil.with(activity).readString("device_token"));

        new WebServiceCall(context, WebServiceUrl.URL_LOGIN, textParams, NewLoginPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.progressLogin.setVisibility(View.GONE);
                        binding.btnLogin.setClickable(true);
                        if (status) {
                            NewLoginPojo login_response = ((NewLoginPojo) obj);
                            NewLoginPojoItem item = login_response.getData();

                            /*For DeActive/Delete user redirect to contact us page*/
                            if (login_response.getData().getIsUserActive() != null && login_response.getData().getIsUserActive().equalsIgnoreCase("d")) {
                                PrefsUtil.with(activity).write("eMail", binding.etLoginUsername.getText().toString().trim());
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) ->
                                        startActivity(new Intent(activity, ContactUsActivity.class))).setMessage(login_response.getMessage()).show();
                            }
                            /*Normal Login Flow*/
                            else {
                                PrefsUtil.with(context).clearPrefs(); // manual login
                                PrefsUtil.with(context).write("isProfileCompleted", false);
                                PrefsUtil.with(context).write("UserId", login_response.getData().getUserId());
                                PrefsUtil.with(context).write("countrycodeid", login_response.getData().getCountryCodeId());
                                PrefsUtil.with(context).write("UserName", login_response.getData().getUserName());
                                PrefsUtil.with(context).write("FirstName", login_response.getData().getFirstName());
                                PrefsUtil.with(context).write("LastName", login_response.getData().getLastName());
                                PrefsUtil.with(context).write("UserType", login_response.getData().getUserType());
                                PrefsUtil.with(context).write("eMail", login_response.getData().getEmailId());
                                PrefsUtil.with(context).write("Pass", binding.etLoginPassword.getText().toString().trim());
                                PrefsUtil.with(context).write("CurrencySign", getResources().getString(R.string.currency));
                                PrefsUtil.with(context).write("UserImg", login_response.getData().getProfileImg());
                                PrefsUtil.with(activity).write("login_cust_address", login_response.getData().getAddress());

                                if (item.getUserType().equals("c")) {
                                    if (item.getFirstName().equals("") || item.getLastName().equals("") || item.getContactNumber().equals("")) {
                                            /*|| item.getCompanyName().equals("") || item.getVat().equals("") || item.getTaxId().equals("") ||
                                            item.getCertifiedEmail().equals("") || item.getReceiptCode().equals("")) {*/

                                        Intent intent = new Intent(context, EditProfileActivity.class);
                                        intent.putExtra("isFromEdit", false);
                                        intent.putExtra("loginPojoData", item);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        PrefsUtil.with(context).write("isProfileCompleted", true);
                                        startActivity(new Intent(context, CustomerHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    }
                                    finish();
                                } else {
                                    if (item.getFirstName().equals("") || item.getLastName().equals("") || item.getContactNumber().equals("") ||
                                            item.getCompanyName().equals("") || item.getVat().equals("") /*|| item.getTaxId().equals("") || item.getCertifiedEmail().equals("") || item.getReceiptCode().equals("")*/
                                            ) {

                                        Intent intent = new Intent(context, com.app.bemyrider.activity.partner.EditProfileActivity.class);
                                        intent.putExtra("isFromEdit", false);
                                        intent.putExtra("loginPojoData", item);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        PrefsUtil.with(context).write("isProfileCompleted", true);
                                        startActivity(new Intent(context, ProviderHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    }
                                    finish();
                                }
                                saveOfflineData();
//                                finish();
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss()).setMessage((String) obj).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        userLoginAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        userLoginAsync = null;
                    }
                });
    }

    /*------------- Social Sign in Api Call ---------------*/
    public void socialSignIn(final String email, final String firstName, final String lastName,
                             String profileImageUrl, final String logintype, final String social_id) {
        PrefsUtil.with(activity).clearPrefs(); // social login
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        LoginManager.getInstance().logOut();

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
        textParams.put("picture", profileImageUrl);
        textParams.put("device_token", PrefsUtil.with(activity).readString("device_token"));


        new WebServiceCall(context, WebServiceUrl.URL_SOCIAL_LOGIN, textParams, NewLoginPojo.class,
                true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    NewLoginPojo resultObj = (NewLoginPojo) obj;
                    if (resultObj.isStatus()) {
                        NewLoginPojoItem loginData = resultObj.getData();

                        /*For DeActive/Delete user redirect to contact us page*/
                        if (resultObj.getData().getIsUserActive() != null && resultObj.getData().getIsUserActive().equalsIgnoreCase("d")) {
                            PrefsUtil.with(activity).write("eMail", email);
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) ->
                                    startActivity(new Intent(activity, ContactUsActivity.class))).setMessage(resultObj.getMessage()).show();
                        }
                        /*Normal Login Flow*/
                        else {
                            if (loginData.getUserType().equals("")) {
                                Intent intent = new Intent(activity, SignupActivity.class);
                                intent.putExtra("SocialFlag", "true");
                                intent.putExtra("Email", email);
                                intent.putExtra("Fname", firstName);
                                intent.putExtra("Lname", lastName);
                                intent.putExtra("loginType", logintype);
                                intent.putExtra("socialId", social_id);
                                intent.putExtra("uId", loginData.getUserId());
                                startActivity(intent);
                                finish();
                            } else {
                                PrefsUtil.with(activity).clearPrefs(); // social login
                                PrefsUtil.with(activity).write("UserId", loginData.getUserId());
                                PrefsUtil.with(activity).write("CurrencySign", loginData.getCurrencySign());
                                PrefsUtil.with(activity).write("UserName", loginData.getUserName());
                                PrefsUtil.with(activity).write("FirstName", loginData.getFirstName());
                                PrefsUtil.with(activity).write("LastName", loginData.getLastName());
                                PrefsUtil.with(activity).write("UserType", loginData.getUserType());
                                PrefsUtil.with(activity).write("eMail", loginData.getEmailId());
                                PrefsUtil.with(activity).write("loginType", logintype);
                                PrefsUtil.with(activity).write("socialId", social_id);
                                PrefsUtil.with(context).write("CurrencySign", getResources().getString(R.string.currency));
                                PrefsUtil.with(context).write("UserImg", loginData.getProfileImg());
                                PrefsUtil.with(activity).write("login_cust_address", loginData.getAddress());

                                if (PrefsUtil.with(activity).readString("UserType")
                                        .equalsIgnoreCase("c")) {
                                    startActivity(new Intent(context, CustomerHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                } else {
                                    startActivity(new Intent(context, ProviderHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                }
                                saveOfflineData();
//                            finish();
                            }
                        }
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss()).setMessage((String) obj).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                socialSignInAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                socialSignInAsync = null;
            }
        });
    }

    /*-------------------- Save Offline Data Api Call ----------------------*/
    private void saveOfflineData() {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(activity).readString("UserId"));

        new WebServiceCall(context, WebServiceUrl.URL_GETOFFLINEDATA,
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

    //Login with Facebook
    public void loginWithFacebook() {
        // Set permissions
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackmanager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request1 = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(), (json, response) -> {
                                    if (response.getError() != null) {
                                        // handle error
                                        Log.e("Response ERROR : ", "JSON Result" + json.toString());
                                        Log.e("Response ERROR : ", "GraphResponse Result" + response.toString());
                                    } else {
                                        Log.e("Response  SUCCESS", "JSON Result" + json.toString());
                                        try {
                                            String jsonresult = String.valueOf(json);
                                            String str_email = null;
                                            try {
                                                str_email = json.getString("email");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            String str_id = json.getString("id");
                                            String str_firstname = json.getString("first_name");
                                            String str_lastname = json.getString("last_name");
                                            socialSignIn(str_email, str_firstname
                                                    , str_lastname, "https://graph.facebook.com/" + str_id + "/picture?type=normal", "f", str_id);
                                            LoginManager.getInstance().logOut();
                                        } catch (JSONException e) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss()).setMessage(getString(R.string.fb_error)).show();
                                            e.printStackTrace();
                                        }
                                    }
                                    LoginManager.getInstance().logOut();
                                }
                        );
                        Bundle parameter = new Bundle();
                        parameter.putString("fields", "id,name,email,first_name,last_name");
                        request1.setParameters(parameter);
                        request1.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Log.d("Cancel", "On cancel");
                    }


                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(context, R.string.signup_with_email, Toast.LENGTH_LONG).show();
                        Log.d("ERROR", error.toString());
                        if (error instanceof FacebookAuthorizationException) {
                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                }
        );
    }

    //Login with Google Plus
    public void loginWithGooglePlus() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        Log.e("IN GOOGLE PLUS", "TRUE");
        gmailActivityResult.launch(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (clicktype.equals("f")) {
            callbackmanager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.e("GOOGLE SIGN IN", "handleSignInResult:" + result.getStatus().getStatusCode());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.e(TAG, "DisplayName :: " + acct.getDisplayName());
            Log.e(TAG, "DisplayName :: " + acct.getGivenName());
            Log.e(TAG, "Email :: " + acct.getEmail());
            if (acct.getPhotoUrl() != null) {
                socialSignIn(acct.getEmail(), acct.getGivenName(), acct.getFamilyName(),
                        acct.getPhotoUrl().toString(), "g", acct.getId());
            } else {
                socialSignIn(acct.getEmail(), acct.getGivenName(), acct.getFamilyName(),
                        "", "g", acct.getId());
            }
//            if (!(TextUtils.isEmpty(acct.getGivenName())) && !(Objects.requireNonNull(acct.getGivenName()).equalsIgnoreCase("null")) && (acct.getGivenName() != null)) {
//            } else {
//                loginWithGooglePlus();
//            }
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                }
            }
        } else {
            Log.e("GOOGLE SIGN IN", "handleSignInResult:" + result.getStatus().getStatusCode());

            // Signed out, show unauthenticated UI.
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initView() {
        getSupportActionBar().hide();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        binding.etLoginPassword.setTransformationMethod(new PasswordTransformationMethod());


        gmailActivityResult();
        linkedInActivityResult();
    }

    void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }
    }


    private void linkedInActivityResult() {
        linkedInActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                try {
                    Intent data = result.getData();
                    if (clicktype.equals("l")) {
                        if (result.getResultCode() == RESULT_OK) {
                            socialSignIn(data.getStringExtra("Email"),
                                    data.getStringExtra("FirstName"),
                                    data.getStringExtra("LatName"),
                                    data.getStringExtra("Profile Url"),
                                    "l", data.getStringExtra("User Id"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void gmailActivityResult() {
        gmailActivityResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            if (clicktype.equals("g")) {
                                GoogleSignInResult gResult = Auth.GoogleSignInApi.getSignInResultFromIntent(result.getData());
                                Log.e("LOG FOR G+", gResult + "");
                                handleSignInResult(gResult);

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(resendMailAsync);
        Utils.cancelAsyncTask(forgotPasswordAsync);
        Utils.cancelAsyncTask(userLoginAsync);
        Utils.cancelAsyncTask(socialSignInAsync);
//        Utils.cancelAsyncTask(offlineDataAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
