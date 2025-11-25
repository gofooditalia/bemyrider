package com.app.bemyrider.activity.partner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;


import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
//import com.app.bemyrider.activity.LinkedInLoginWebViewActivity;
import com.app.bemyrider.activity.SignupActivity;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.Arrays;
import java.util.LinkedHashMap;


public class PartnerProfile_Acitvity extends AppCompatActivity {

    private ImageView  img_profile, img_verify_facebook, img_verify_gmail, img_verify_linkedin;
    private TextView txt_username, txt_usercontactno, txt_useremail, txt_servicetime,
            txt_rating, txt_tota_review, txt_viewall, txt_about_user, txt_available_days, txt_address_pro, txt_worked_on;
    private Context mContext = PartnerProfile_Acitvity.this;
    private String clicktype = "";
    private String strepoc_avl_start_time, strepoc_avl_end_time, countrycodeid, userAddress;
    private Switch switch_available_now;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackmanager;
    private AsyncTask socialSignInAsync, availableStatusAsync, getProfileAsync;
    private ConnectionManager connectionManager;
    private RelativeLayout fab_edit;

    ActivityResultLauncher<Intent> gmailActivityResult;
    ActivityResultLauncher<Intent> linkedInActivityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.partner_activity_profile);

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.provider_profile),HtmlCompat.FROM_HTML_MODE_LEGACY));

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        serviceCall();

        callbackmanager = CallbackManager.Factory.create();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(PartnerProfile_Acitvity.this)
                .enableAutoManage(PartnerProfile_Acitvity.this, connectionResult -> {

                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        switch_available_now.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                serviceCallChangeStatus("y");
            } else {
                serviceCallChangeStatus("n");
            }
        });
        fab_edit.setOnClickListener(v -> {
           /* Intent i = new Intent(mContext, EditProfileActivity.class);
            i.putExtra("isFromEdit", true);
            i.putExtra("strepoc_avl_start_time", strepoc_avl_start_time);
            i.putExtra("strepoc_avl_end_time", strepoc_avl_end_time);
            i.putExtra("userAddress", userAddress);
            i.putExtra("countrycodeId", countrycodeid);
            i.putExtra("Edit", "true");
            startActivity(i);*/
        });

        txt_viewall.setOnClickListener(v -> {
            Intent i = new Intent(mContext, PartnerReviewsActivity.class);
            startActivity(i);
        });

        img_verify_facebook.setOnClickListener(view -> {
            clicktype = "f";
            loginWithFacebook();
        });

        img_verify_gmail.setOnClickListener(view -> {
            clicktype = "g";
            loginWithGooglePlus();
        });

        img_verify_linkedin.setOnClickListener(view -> {
            clicktype = "l";
            loginWithLinkedIn();
        });
    }

    private void loginWithLinkedIn() {
        /*Intent intent = new Intent(PartnerProfile_Acitvity.this, LinkedInLoginWebViewActivity.class);
        linkedInActivityResult.launch(intent);*/
    }

    private void loginWithGooglePlus() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        Log.e("IN GOOGLE PLUS", "TRUE");
        gmailActivityResult.launch(intent);
    }

    private void loginWithFacebook() {

        LoginManager.getInstance().logInWithReadPermissions(PartnerProfile_Acitvity.this, Arrays.asList("email", "public_profile"));
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
                                            AlertDialog.Builder builder = new AlertDialog.Builder(PartnerProfile_Acitvity.this);
                                            builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss()).setMessage(getString(R.string.fb_error)).show();
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
                        Toast.makeText(PartnerProfile_Acitvity.this,
                                R.string.signup_with_email, Toast.LENGTH_LONG).show();
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

    private void socialSignIn(final String email, final String firstName, final String lastName,
                              String profileImageUrl, String logintype, String social_id) {
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
        textParams.put("picture", profileImageUrl);
        textParams.put("device_token", PrefsUtil.with(PartnerProfile_Acitvity.this).readString("device_token"));

        new WebServiceCall(mContext, WebServiceUrl.URL_SOCIAL_LOGIN, textParams, NewLoginPojo.class, true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    NewLoginPojo resultObj = (NewLoginPojo) obj;
                    if (resultObj.isStatus()) {
                        NewLoginPojoItem loginData = resultObj.getData();

                        if (loginData.getUserType().equals("")) {
                            Intent intent = new Intent(PartnerProfile_Acitvity.this, SignupActivity.class);
                            intent.putExtra("SocialFlag", "true");
                            intent.putExtra("Email", email);
                            intent.putExtra("Fname", firstName);
                            intent.putExtra("Lname", lastName);
                            intent.putExtra("uId", loginData.getUserId());
                            startActivity(intent);
                            finish();
                        } else {
                            PrefsUtil.with(PartnerProfile_Acitvity.this).clearPrefs();
                            PrefsUtil.with(PartnerProfile_Acitvity.this).write("UserId", loginData.getUserId());
                            PrefsUtil.with(PartnerProfile_Acitvity.this).write("CurrencySign", loginData.getCurrencySign());
                            PrefsUtil.with(PartnerProfile_Acitvity.this).write("UserName", loginData.getUserName());
                            PrefsUtil.with(PartnerProfile_Acitvity.this).write("UserType", loginData.getUserType());

                            if (PrefsUtil.with(PartnerProfile_Acitvity.this).readString("UserType").equalsIgnoreCase("c")) {
                                startActivity(new Intent(mContext, CustomerHomeActivity.class));
                            } else {
                                startActivity(new Intent(mContext, ProviderHomeActivity.class));
                            }
                        }
                    }
                } else {
                    Toast.makeText(PartnerProfile_Acitvity.this, (String) obj, Toast.LENGTH_SHORT).show();
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

    private void serviceCallChangeStatus(final String switchstatus) {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(mContext).readString("UserId"));
        textParams.put("isAvailable", switchstatus);

        new WebServiceCall(PartnerProfile_Acitvity.this, WebServiceUrl.URL_AVAILABLE_NOW,
                textParams, CommonPojo.class, true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    if (switchstatus.equals("y")) {
                        switch_available_now.setChecked(true);
                    } else {
                        switch_available_now.setChecked(false);
                    }
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                availableStatusAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                availableStatusAsync = null;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    private void serviceCall() {

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("profile_id", PrefsUtil.with(mContext).readString("UserId"));

        new WebServiceCall(mContext, WebServiceUrl.URL_PROFILE, textParams, ProfilePojo.class,
                true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {

                if (status) {
                    ProfilePojo response_profile = (ProfilePojo) obj;

                    if (response_profile.getData().getIsAvailable().equals("y")) {
                        switch_available_now.setChecked(true);
                    } else {
                        switch_available_now.setChecked(false);
                    }

                    if (!response_profile.getData().getFbId().equalsIgnoreCase("")) {
                        img_verify_facebook.setImageResource(R.mipmap.fb_verify);
                        img_verify_facebook.setClickable(false);
                    } else {
                        img_verify_facebook.setClickable(true);
                    }

                    if (!response_profile.getData().getGmailId().equalsIgnoreCase("")) {
                        img_verify_gmail.setImageResource(R.mipmap.google_verify);
                        img_verify_gmail.setClickable(false);
                    } else {
                        img_verify_gmail.setClickable(true);
                    }

                    if (!response_profile.getData().getLinkedinId().equalsIgnoreCase("")) {
                        img_verify_linkedin.setImageResource(R.mipmap.linkedin_verify);
                        img_verify_linkedin.setClickable(false);
                    } else {
                        img_verify_linkedin.setClickable(true);
                    }

                    if (response_profile.getData().getProfileImg().equals("")) {

                        img_profile.setImageResource(R.mipmap.user);

                    } else {
                        try {
                            Picasso.get().load(response_profile.getData().getProfileImg()).placeholder(R.drawable.loading).into(img_profile);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }

                    strepoc_avl_start_time = response_profile.getData().getAvailableTimeStart();
                    strepoc_avl_end_time = response_profile.getData().getAvailableTimeEnd();
                    countrycodeid = response_profile.getData().getCountryCode();
                    userAddress = response_profile.getData().getAddress();
                    PrefsUtil.with(mContext).write("userAddress", userAddress);
                    txt_useremail.setText(response_profile.getData().getEmail());
                    if (response_profile.getData().getTotalReview().equalsIgnoreCase("0")) {
                        txt_viewall.setVisibility(View.GONE);
                    } else {
                        txt_viewall.setVisibility(View.VISIBLE);
                    }
                    if (response_profile.getData().getPositiveRating().equals("")) {
                        txt_rating.setText("0 ");
                    } else {
                        txt_rating.setText(response_profile.getData().getStartRating() + " ");
                    }
                    if (!response_profile.getData().getDescription().toString().equals("")) {
                        txt_about_user.setText(response_profile.getData().getDescription().toString());
                    } else {
                        txt_about_user.setText("N/A");
                    }
                    txt_tota_review.setText(" " + response_profile.getData().getTotalReview());
                    txt_username.setText(response_profile.getData().getUserName());
                    txt_usercontactno.setText(response_profile.getData().getCountryCode() + " " + response_profile.getData().getContactNumber());
                    if (!response_profile.getData().getAvailableTimeStart().equals("")) {
                        txt_servicetime.setText(response_profile.getData().getAvailableTimeStart() + " - " + response_profile.getData().getAvailableTimeEnd());
                    } else {
                        txt_servicetime.setText("N/A");
                    }
                    if (!response_profile.getData().getAvailableDays().equals("")) {
                        txt_available_days.setText(response_profile.getData().getAvailableDaysList());
                    } else {
                        txt_available_days.setText("N/A");
                    }
                    if (!response_profile.getData().getAddress().equals("")) {
                        txt_address_pro.setText(response_profile.getData().getAddress());
                    } else {
                        txt_address_pro.setText("N/A");
                    }
                    txt_worked_on.setText(response_profile.getData().getTaskAssigned());

                    PrefsUtil.with(mContext).write("userContactno", response_profile.getData().getContactNumber());
                    PrefsUtil.with(mContext).write("userEmail", response_profile.getData().getEmail());
                    PrefsUtil.with(mContext).write("total_review", response_profile.getData().getTotalReview());
                    PrefsUtil.with(mContext).write("total_rating", response_profile.getData().getPositiveRating());
                    PrefsUtil.with(mContext).write("userAddress", response_profile.getData().getAddress());
                    PrefsUtil.with(mContext).write("userAbout", response_profile.getData().getDescription());
                    PrefsUtil.with(mContext).write("userfname", response_profile.getData().getFirstName());
                    PrefsUtil.with(mContext).write("userlname", response_profile.getData().getLastName());
                    PrefsUtil.with(mContext).write("start_time", response_profile.getData().getAvailableTimeStart());
                    PrefsUtil.with(mContext).write("end_time", response_profile.getData().getAvailableTimeEnd());
                    PrefsUtil.with(mContext).write("UserImg", response_profile.getData().getProfileImg());
                    PrefsUtil.with(mContext).write("userlatitude", response_profile.getData().getLatitude());
                    PrefsUtil.with(mContext).write("userlongitude", response_profile.getData().getLongitude());
                    PrefsUtil.with(mContext).write("userAvalDay", response_profile.getData().getAvailableDays());
                    PrefsUtil.with(mContext).write("lat", response_profile.getData().getLatitude());
                    PrefsUtil.with(mContext).write("long", response_profile.getData().getLongitude());


                } else {
                    Toast.makeText(mContext, obj.toString(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                getProfileAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                getProfileAsync = null;
            }
        });
    }


    private void initView() {

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        txt_about_user = findViewById(R.id.txt_about_user);
        txt_username = findViewById(R.id.txt_username);
        txt_usercontactno = findViewById(R.id.txt_usercontactno);
        txt_useremail = findViewById(R.id.txt_useremail);
        txt_servicetime = findViewById(R.id.txt_servicetime);
        txt_rating = findViewById(R.id.txt_rating);
        txt_tota_review = findViewById(R.id.txt_tota_review);
        txt_viewall = findViewById(R.id.txt_viewall);
        txt_servicetime = findViewById(R.id.txt_servicetime);
        txt_available_days = findViewById(R.id.txt_available_days);
        txt_address_pro = findViewById(R.id.txt_address_pro);
        txt_worked_on = findViewById(R.id.txt_worked_on);
        img_verify_facebook = findViewById(R.id.img_verify_facebook);
        img_verify_gmail = findViewById(R.id.img_verify_gmail);
        img_verify_linkedin = findViewById(R.id.img_verify_linkedin);
        switch_available_now = findViewById(R.id.switch_available_now);
        fab_edit = findViewById(R.id.fab_edit);

        img_profile = findViewById(R.id.img_profile);

        gmailActivityResult();
        linkedInActivityResult();
    }

    private void linkedInActivityResult() {
        linkedInActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (socialSignInAsync != null) {
            socialSignInAsync.cancel(true);
        }
        if (availableStatusAsync != null) {
            availableStatusAsync.cancel(true);
        }
        if (getProfileAsync != null) {
            getProfileAsync.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
