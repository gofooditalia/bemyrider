package com.app.bemyrider.activity.user;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
//import com.app.bemyrider.activity.LinkedInLoginWebViewActivity;
import com.app.bemyrider.activity.SignupActivity;
import com.app.bemyrider.databinding.ActivityCustomerProfileBinding;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
// import com.facebook.*; // REMOVED
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class CustomerProfileActivity extends AppCompatActivity {

    ActivityCustomerProfileBinding binding;

    private ProfileItem profileData;
    private GoogleApiClient mGoogleApiClient;
    private String clicktype = "";
    // private CallbackManager callbackmanager; // REMOVED
    private AsyncTask socialSignInAsync, profileDataAsync;
    private ConnectionManager connectionManager;
    private Context context;
    private AppCompatActivity activity;
    ActivityResultLauncher<Intent> gmailActivityResult;
    ActivityResultLauncher<Intent> linkedInActivityResult;
    ActivityResultLauncher<Intent> myActivityResultLauncher;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = CustomerProfileActivity.this;
        activity = CustomerProfileActivity.this;

        binding = DataBindingUtil.setContentView(activity, R.layout.activity_customer_profile, null);

        init();

        // callbackmanager = CallbackManager.Factory.create(); // REMOVED

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(activity, connectionResult -> {

                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if (new ConnectionCheck().isNetworkConnected(context)) {
            getProfileData();
        } else {
            getOfflineDetails();
        }

        binding.ImgEdit.setOnClickListener(v -> {
            if (profileData != null) {
                Intent i = new Intent(activity, EditProfileActivity.class);
//                    i.putExtra("userAddress", userAddress);
//                    i.putExtra("countrycodeId", );
                i.putExtra("Edit", "true");
                i.putExtra("isFromEdit", true);
//                    i.putExtra("profileData", (Serializable) profileData);
                myActivityResultLauncher.launch(i);
            } else {
                Toast.makeText(context, R.string.can_not_edit_now, Toast.LENGTH_LONG).show();
            }
        });

        binding.imgVerifyFacebookU.setOnClickListener(view -> {
            clicktype = "f";
            // loginWithFacebook(); // REMOVED
            Toast.makeText(context, "Facebook verification is disabled", Toast.LENGTH_SHORT).show();
        });

        binding.imgVerifyGmailU.setOnClickListener(view -> {
            clicktype = "g";
            loginWithGooglePlus();
        });

        binding.imgVerifyLinkedinU.setOnClickListener(view -> {
            clicktype = "l";
            loginWithLinkedIn();
        });

        binding.imgBack.setOnClickListener(v -> {
            onBackPressed();
        });

    }

    private void loginWithLinkedIn() {
       /* Intent intent = new Intent(activity, LinkedInLoginWebViewActivity.class);
        linkedInActivityResult.launch(intent);*/
    }

    private void loginWithGooglePlus() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        Log.e("IN GOOGLE PLUS", "TRUE");
        gmailActivityResult.launch(intent);
    }

    /*
    private void loginWithFacebook() {
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
                                        } catch (JSONException e) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss()).setMessage(R.string.fb_error).show();
                                            e.printStackTrace();
                                        }
                                    }
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
    */

    private void socialSignIn(final String email, final String firstName, final String lastName, String profileImageUrl, String logintype, String social_id) {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("first_name", firstName);
        textParams.put("last_name", lastName);
        textParams.put("user_id", PrefsUtil.with(activity).readString("UserId"));
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

        new WebServiceCall(context, WebServiceUrl.URL_SOCIAL_LOGIN,
                textParams, NewLoginPojo.class, true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    NewLoginPojo resultObj = (NewLoginPojo) obj;
                    if (resultObj.isStatus()) {
                        NewLoginPojoItem loginData = resultObj.getData();

                        if (loginData.getUserType().equals("")) {
                            Intent intent = new Intent(activity, SignupActivity.class);
                            intent.putExtra("SocialFlag", "true");
                            intent.putExtra("Email", email);
                            intent.putExtra("Fname", firstName);
                            intent.putExtra("Lname", lastName);
                            intent.putExtra("uId", loginData.getUserId());
                            startActivity(intent);
                            finish();
                        } else {
                            PrefsUtil.with(activity).clearPrefs();
                            PrefsUtil.with(activity).write("UserId", loginData.getUserId());
                            PrefsUtil.with(activity).write("CurrencySign", loginData.getCurrencySign());
                            PrefsUtil.with(activity).write("UserName", loginData.getUserName());
                            PrefsUtil.with(activity).write("FirstName", loginData.getFirstName());
                            PrefsUtil.with(activity).write("LastName", loginData.getLastName());
                            PrefsUtil.with(activity).write("UserType", loginData.getUserType());
                            PrefsUtil.with(activity).write("eMail", loginData.getEmailId());
                            PrefsUtil.with(activity).write("loginType", logintype);
                            PrefsUtil.with(activity).write("socialId", social_id);
                            PrefsUtil.with(activity).write("UserImg", loginData.getProfileImg());
                            PrefsUtil.with(activity).write("login_cust_address", loginData.getAddress());

                        }
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage((String) obj)
                            .setPositiveButton(HtmlCompat.fromHtml("<font color=#51AE54>" + "OK",HtmlCompat.FROM_HTML_MODE_LEGACY), (dialogInterface, i) -> dialogInterface.dismiss()).show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (clicktype.equals("f")) {
            // callbackmanager.onActivityResult(requestCode, resultCode, data); // REMOVED
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.e("GOOGLE SIGN IN", "handleSignInResult:" + result.getStatus().getStatusCode());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.e("DisplayName : ", acct.getDisplayName());
            Log.e("Email : ", acct.getEmail());
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

    protected void init() {
        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        myActivityResult();

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
        }*/

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // collapsed
                if (!binding.TxtProfileNname.getText().equals(""))
                    binding.txtHeaderName.setText(binding.TxtProfileNname.getText());
                else
                    binding.txtHeaderName.setText(getResources().getString(R.string.user_profile));

                binding.toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.toolbar_bg_color));
                binding.txtHeaderName.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                // expanded
                binding.txtHeaderName.setText("");
                binding.txtHeaderName.setTextColor(ContextCompat.getColor(context, R.color.white));
                binding.toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
            }
        });

        gmailActivityResult();
        linkedInActivityResult();

    }

    private void myActivityResult() {
        myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                getProfileData();
            }
        });
    }

    private void linkedInActivityResult() {
        linkedInActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            try {
                Intent data = result.getData();
                if (clicktype.equals("l")) {
                    if (result.getResultCode() == RESULT_OK) {
                        socialSignIn(data.getStringExtra("Email"), data.getStringExtra("FirstName"),
                                data.getStringExtra("LatName"), data.getStringExtra("Profile Url"),
                                "l", data.getStringExtra("User Id"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void gmailActivityResult() {
        gmailActivityResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
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

    /*---------------- Profile Detail Api Call ---------------------*/
    protected void getProfileData() {
        binding.llUserProfileDetail.setVisibility(View.GONE);
        binding.linHeader.setVisibility(View.GONE);
        binding.ImgEdit.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("profile_id", PrefsUtil.with(activity).readString("UserId"));

        new WebServiceCall(context, WebServiceUrl.URL_PROFILE, textParams, ProfilePojo.class,
                false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.linHeader.setVisibility(View.VISIBLE);
                binding.llUserProfileDetail.setVisibility(View.VISIBLE);

                binding.ImgEdit.setVisibility(View.VISIBLE);

                binding.linHeader.setVisibility(View.VISIBLE);
                if (status) {
                    profileData = ((ProfilePojo) obj).getData();
                    EditProfileActivity.profileData = profileData;

                    if (!profileData.getFbId().equalsIgnoreCase("")) {
                        binding.imgVerifyFacebookU.setImageResource(R.mipmap.fb_verify);
                        binding.imgVerifyFacebookU.setClickable(false);
                    } else {
                        binding.imgVerifyFacebookU.setClickable(true);
                    }

                    if (!profileData.getGmailId().equalsIgnoreCase("")) {
                        binding.imgVerifyGmailU.setImageResource(R.mipmap.google_verify);
                        binding.imgVerifyGmailU.setClickable(false);
                    } else {
                        binding.imgVerifyGmailU.setClickable(true);
                    }

                    if (!profileData.getLinkedinId().equalsIgnoreCase("")) {
                        binding.imgVerifyLinkedinU.setImageResource(R.mipmap.linkedin_verify);
                        binding.imgVerifyLinkedinU.setClickable(false);
                    } else {
                        binding.imgVerifyLinkedinU.setClickable(true);
                    }

                    binding.TxtProfileNname.setText(profileData.getUserName());
                    PrefsUtil.with(activity).write("UserName", profileData.getUserName());
                    binding.TxtProfileNumber.setText(profileData.getCountryCode() + " " + profileData.getContactNumber());
                    binding.TxtProfileMail.setText(profileData.getEmail());

                    /*if (profileData.getPaymentMode().equalsIgnoreCase("w")) {
                        PrefsUtil.with(activity).write("PaymentMode", "w");
                        binding.TxtPaymethodName.setText(R.string.wallet);
                        Picasso.get().load(R.mipmap.ic_wallet);
                    } else if (profileData.getPaymentMode().equalsIgnoreCase("c")) {
                        PrefsUtil.with(activity).write("PaymentMode", "c");
                        binding.TxtPaymethodName.setText(R.string.cash);
                        Picasso.get().load(R.mipmap.ic_cash);
                    }*/

                    if (!profileData.getAddress().equals("")) {
                        PrefsUtil.with(activity).write("customer_address", profileData.getAddress());
                        PrefsUtil.with(activity).write("login_cust_address", profileData.getAddress());
                        binding.TxtProfileAddress.setText(profileData.getAddress());
                    } else {
                        binding.TxtProfileAddress.setText("N/A");
                    }
                    if (profileData.getProfileImg() != null && profileData.getProfileImg().length() > 0) {
                        PrefsUtil.with(activity).write("UserImg", profileData.getProfileImg());
                        Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.imgProfile);
                        // Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.imgBack);
                    } else {
                        Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.imgProfile);
                        // Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.imgBack);
                    }

                    if (!profileData.getTaskAssigned().equals("")) {
                        binding.txtAssignedTasks.setText(profileData.getTaskAssigned());
                    } else {
                        binding.txtAssignedTasks.setText("N/A");
                    }

                    if (!profileData.getCompanyName().equals("")) {
                        binding.txtCompany.setText(profileData.getCompanyName());
                    } else {
                        binding.txtCompany.setText("N/A");
                    }

                    if (!profileData.getCity_of_company().equals("")) {
                        binding.txtCityOfCompany.setText(profileData.getCity_of_company());
                    } else {
                        binding.txtCityOfCompany.setText("N/A");
                    }

                    if (!profileData.getVat().equals("")) {
                        binding.txtVat.setText(profileData.getVat());
                    } else {
                        binding.txtVat.setText("N/A");
                    }
                    /*if (!profileData.getTaxId().equals("")) {
                        binding.txtTaxIdCode.setText(profileData.getTaxId());
                    } else {
                        binding.txtTaxIdCode.setText("N/A");
                    }*/
                    if (!profileData.getReceiptCode().equals("")) {
                        binding.txtInvoiceRecipient.setText(profileData.getReceiptCode());
                    } else {
                        binding.txtInvoiceRecipient.setText("N/A");
                    }
                    if (!profileData.getCertifiedEmail().equals("")) {
                        binding.txtCertifiedEmail.setText(profileData.getCertifiedEmail());
                    } else {
                        binding.txtCertifiedEmail.setText("N/A");
                    }

                    PrefsUtil.with(activity).write("fb_id", profileData.getFbId());
                    PrefsUtil.with(activity).write("g_id", profileData.getGmailId());
                    PrefsUtil.with(activity).write("ln_id", profileData.getLinkedinId());

                } else {
                    Toast.makeText(context, (String) obj,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                profileDataAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                profileDataAsync = null;
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(activity);
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    getOfflineDetails();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getOfflineDetails() {
        try {
            binding.progress.setVisibility(View.GONE);
            binding.linHeader.setVisibility(View.VISIBLE);
            binding.llUserProfileDetail.setVisibility(View.VISIBLE);
            binding.ImgEdit.setVisibility(View.VISIBLE);
            Log.e("Offline", "onMessageEvent: My Resolution");
            File f = new File(activity.getFilesDir().getPath() + "/" + "offline.json");
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String s = new String(buffer);
            JSONObject object = new JSONObject(s);
            JSONObject dataObj = object.getJSONObject("data");
            JSONObject serviceList = dataObj.getJSONObject("profileData");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
            Gson gson = gsonBuilder.create();
            ProfileItem profileData = gson.fromJson(serviceList.toString(), ProfileItem.class);

            EditProfileActivity.profileData = profileData;

            if (!profileData.getFbId().equals("")) {
                binding.imgVerifyFacebookU.setImageResource(R.mipmap.fb_verify);
            }

            if (!profileData.getGmailId().equals("")) {
                binding.imgVerifyGmailU.setImageResource(R.mipmap.google_verify);
            }

            if (!profileData.getLinkedinId().equals("")) {
                binding.imgVerifyLinkedinU.setImageResource(R.mipmap.linkedin_verify);
            }

            binding.TxtProfileNname.setText(profileData.getUserName());
            PrefsUtil.with(activity).write("UserName", profileData.getUserName());
            binding.TxtProfileNumber.setText(String.format("%s %s", profileData.getCountryCode(), profileData.getContactNumber()));
            binding.TxtProfileMail.setText(profileData.getEmail());

            /*if (profileData.getPaymentMode().equalsIgnoreCase("w")) {
                PrefsUtil.with(activity).write("PaymentMode", "w");
                binding.TxtPaymethodName.setText(R.string.wallet);
                Picasso.get().load(R.mipmap.ic_wallet);
            } else if (profileData.getPaymentMode().equalsIgnoreCase("c")) {
                PrefsUtil.with(activity).write("PaymentMode", "c");
                binding.TxtPaymethodName.setText(R.string.cash);
                Picasso.get().load(R.mipmap.ic_cash);
            }*/

            if (!profileData.getAddress().equals("")) {
                PrefsUtil.with(activity).write("customer_address", profileData.getAddress());
                PrefsUtil.with(activity).write("login_cust_address", profileData.getAddress());
                binding.TxtProfileAddress.setText(profileData.getAddress());
            } else {
                binding.TxtProfileAddress.setText("N/A");
            }
            if (profileData.getProfileImg() != null && profileData.getProfileImg().length() > 0) {
                PrefsUtil.with(activity).write("UserImg", profileData.getProfileImg());
                Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.imgProfile);
                // Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.imgBack);
            } else {
                Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.imgProfile);
                // Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.imgBack);
            }

            if (!profileData.getTaskAssigned().equals("")) {
                binding.txtAssignedTasks.setText(profileData.getTaskAssigned());
            } else {
                binding.txtAssignedTasks.setText("N/A");
            }

            PrefsUtil.with(activity).write("fb_id", profileData.getFbId());
            PrefsUtil.with(activity).write("g_id", profileData.getGmailId());
            PrefsUtil.with(activity).write("ln_id", profileData.getLinkedinId());
//            new ConnectionCheck().showDialogWithMessage(activity, getString(R.string.sync_data_message)).show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(socialSignInAsync);
        Utils.cancelAsyncTask(profileDataAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}