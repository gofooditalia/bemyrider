package com.app.bemyrider.activity.partner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.app.bemyrider.Adapter.Partner.DrawerItemCustomAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.AccountSettingActivity;
import com.app.bemyrider.activity.ContactUsActivity;
import com.app.bemyrider.activity.FeedbackActivity;
import com.app.bemyrider.activity.InfoPageActivity;
import com.app.bemyrider.activity.LoginActivity;
import com.app.bemyrider.activity.NotificationListingActivity;
import com.app.bemyrider.activity.SignupActivity;
import com.app.bemyrider.activity.user.WalletActivity;
import com.app.bemyrider.databinding.PartnerActivityProfileBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.SecurePrefsUtil;
import com.app.bemyrider.utils.Utils;
// import com.facebook.*; // REMOVED
/*
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
*/
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

/**
 * Modified by Hardik Talaviya on 2/12/19.
 */

public class DrawerActivity extends AppCompatActivity {

    private static final String TAG = "DrawerActivity";
    private PartnerActivityProfileBinding binding;
    private DrawerItemCustomAdapter adapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private ModelForDrawer[] drawerItem;
    private GoogleApiClient mGoogleApiClient;

    private Context context;
    private int previousSelectedPos = 0;
    private int selectedPos = 0;
    private String strepoc_avl_start_time, strepoc_avl_end_time,
            countrycodeid, userAddress, smallDelivery, mediumDelivery, largeDelivery;
    private String clicktype = "";
    // private CallbackManager callbackmanager; // REMOVED
    private AsyncTask socialSignInAsync, changeStatusAsync, getProfileAsync, logoutAsync;
    private BroadcastReceiver mMessageReceiver;
    private ConnectionManager connectionManager;
    private Toolbar toolbar;
    ActivityResultLauncher<Intent> permissionActivityResultLauncher;

    private Activity activity;
    ActivityResultLauncher<Intent> gmailActivityResult;
    ActivityResultLauncher<Intent> linkedInActivityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        binding = DataBindingUtil.setContentView(DrawerActivity.this, R.layout.partner_activity_profile, null);

        // callbackmanager = CallbackManager.Factory.create(); // REMOVED

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(DrawerActivity.this)
                .enableAutoManage(DrawerActivity.this, connectionResult -> {

                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        initView();

        serviceCall();

        binding.switchAvailableNow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    serviceCallChangeStatus("y");
                } else {
                    serviceCallChangeStatus("n");
                }
            }
        });

        /*binding.fabEdit.setOnClickListener(v -> {
            Intent i = new Intent(context, EditProfileActivity.class);
            i.putExtra("isFromEdit", true);
            i.putExtra("smallDelivery", smallDelivery);
            i.putExtra("mediumDelivery", mediumDelivery);
            i.putExtra("largeDelivery", largeDelivery);
            i.putExtra("strepoc_avl_start_time", strepoc_avl_start_time);
            i.putExtra("strepoc_avl_end_time", strepoc_avl_end_time);
            i.putExtra("userAddress", userAddress);
            i.putExtra("countrycodeId", countrycodeid);
            i.putExtra("Edit", "true");
            startActivity(i);
        });*/

        binding.txtViewall.setOnClickListener(v -> {
            Intent i = new Intent(context, PartnerReviewsActivity.class);
            startActivity(i);
        });

        binding.imgVerifyFacebook.setOnClickListener(view -> {
            clicktype = "f";
            // loginWithFacebook(); // REMOVED
            Toast.makeText(DrawerActivity.this, "Facebook verification is disabled", Toast.LENGTH_SHORT).show();
        });

        binding.imgVerifyGmail.setOnClickListener(view -> {
            clicktype = "g";
            loginWithGooglePlus();
        });

        binding.imgVerifyLinkedin.setOnClickListener(view -> {
            clicktype = "l";
            loginWithLinkedIn();
        });

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (new ConnectionCheck().isNetworkConnected(context)) {
                    Log.e("HomeActivity", "connected");
                    EventBus.getDefault().post(new MessageEvent("connection", "connected"));
                } else {

                    Log.e("HomeActivity", "disconnected");
                    EventBus.getDefault().post(new MessageEvent("connection", "disconnected"));
                }
            }
        };


    }

    private void loginWithLinkedIn() {
        /*Intent intent = new Intent(DrawerActivity.this, LinkedInLoginWebViewActivity.class);
        linkedInActivityResult.launch(intent);*/
    }

    private void loginWithGooglePlus() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        Log.e("IN GOOGLE PLUS", "TRUE");
        gmailActivityResult.launch(intent);
    }

    /*
    private void loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(DrawerActivity.this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackmanager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request1 = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject json, GraphResponse response) {
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
                                                AlertDialog.Builder builder = new AlertDialog.Builder(DrawerActivity.this);
                                                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                }).setMessage(getString(R.string.fb_error)).show();
                                                e.printStackTrace();
                                            }
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
                        Toast.makeText(DrawerActivity.this, R.string.signup_with_email, Toast.LENGTH_LONG).show();
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

    /*------------- Social Sign in Api Call -------------------*/
    private void socialSignIn(final String email, final String firstName, final String lastName, String profileImageUrl, String logintype, String social_id) {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("first_name", firstName);
        textParams.put("last_name", lastName);
        textParams.put("user_id", PrefsUtil.with(DrawerActivity.this).readString("UserId"));
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
        textParams.put("device_token", PrefsUtil.with(DrawerActivity.this).readString("device_token"));


        new WebServiceCall(context, WebServiceUrl.URL_SOCIAL_LOGIN, textParams, NewLoginPojo.class,
                true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    NewLoginPojo resultObj = (NewLoginPojo) obj;
                    if (resultObj.isStatus()) {
                        NewLoginPojoItem loginData = resultObj.getData();

                        if (loginData.getUserType().equals("")) {
                            Intent intent = new Intent(DrawerActivity.this, SignupActivity.class);
                            intent.putExtra("SocialFlag", "true");
                            intent.putExtra("Email", email);
                            intent.putExtra("Fname", firstName);
                            intent.putExtra("Lname", lastName);
                            intent.putExtra("uId", loginData.getUserId());
                            startActivity(intent);
                            finish();
                        } else {
                            PrefsUtil.with(DrawerActivity.this).clearPrefs();
                            PrefsUtil.with(DrawerActivity.this).write("UserId", loginData.getUserId());
                            PrefsUtil.with(DrawerActivity.this).write("CurrencySign", loginData.getCurrencySign());
                            PrefsUtil.with(DrawerActivity.this).write("UserName", loginData.getUserName());
                            PrefsUtil.with(DrawerActivity.this).write("FirstName", loginData.getFirstName());
                            PrefsUtil.with(DrawerActivity.this).write("LastName", loginData.getLastName());
                            PrefsUtil.with(DrawerActivity.this).write("UserType", loginData.getUserType());
                            PrefsUtil.with(DrawerActivity.this).write("eMail", loginData.getEmailId());
                            PrefsUtil.with(DrawerActivity.this).write("loginType", logintype);
                            PrefsUtil.with(DrawerActivity.this).write("socialId", social_id);
                            PrefsUtil.with(context).write("UserImg", loginData.getProfileImg());
                            PrefsUtil.with(context).write("login_cust_address", loginData.getAddress());

                        }
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DrawerActivity.this);
                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).setMessage((String) obj).show();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    /*---------------- Change Available Status Api Call -----------------*/
    private void serviceCallChangeStatus(final String switchstatus) {
        binding.progressAvailable.setVisibility(View.VISIBLE);
        binding.switchAvailableNow.setVisibility(View.GONE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(context).readString("UserId"));
        textParams.put("isAvailable", switchstatus);

        new WebServiceCall(DrawerActivity.this, WebServiceUrl.URL_AVAILABLE_NOW, textParams,
                CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progressAvailable.setVisibility(View.GONE);
                binding.switchAvailableNow.setVisibility(View.VISIBLE);
                if (status) {
                    if (switchstatus.equals("y")) {
                        binding.switchAvailableNow.setChecked(true);
                    } else {
                        binding.switchAvailableNow.setChecked(false);
                    }
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                changeStatusAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                changeStatusAsync = null;
            }
        });
    }

    /*---------------- Get Profile Data Api Call ---------------------*/
    private void serviceCall() {
        binding.progressProfileDetail.setVisibility(View.VISIBLE);
        binding.frameMain.setVisibility(View.GONE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("profile_id", PrefsUtil.with(context).readString("UserId"));

        new WebServiceCall(context, WebServiceUrl.URL_PROFILE, textParams, ProfilePojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.progressProfileDetail.setVisibility(View.GONE);
                        binding.frameMain.setVisibility(View.VISIBLE);
                        if (status) {
                            ProfilePojo response_profile = (ProfilePojo) obj;

                            if (response_profile.getData().getIsAvailable().equals("y")) {
                                binding.switchAvailableNow.setChecked(true);
                            } else {
                                binding.switchAvailableNow.setChecked(false);
                            }

                            String strDeliveryType = "";

                            if (response_profile.getData().getSmallDelivery().equals("y")) {
                                strDeliveryType = getResources().getString(R.string.small);
                            }

                            if (response_profile.getData().getMediumDelivery().equals("y")) {
                                if (strDeliveryType.equals(""))
                                    strDeliveryType = getResources().getString(R.string.medium);
                                else
                                    strDeliveryType = strDeliveryType + " , " + getResources().getString(R.string.medium);
                            }

                            if (response_profile.getData().getLargeDelivery().equals("y")) {
                                if (strDeliveryType.equals(""))
                                    strDeliveryType = getResources().getString(R.string.large);
                                else
                                    strDeliveryType = strDeliveryType + " , " + getResources().getString(R.string.large);
                            }

                            binding.txtDeliveryType.setText(strDeliveryType);

                            if (!response_profile.getData().getFbId().equals("")) {
                                binding.imgVerifyFacebook.setImageResource(R.mipmap.fb_verify);
                                binding.imgVerifyFacebook.setClickable(false);
                            } else {
                                binding.imgVerifyFacebook.setClickable(true);
                            }

                            if (!response_profile.getData().getGmailId().equals("")) {
                                binding.imgVerifyGmail.setImageResource(R.mipmap.google_verify);
                                binding.imgVerifyGmail.setClickable(false);
                            } else {
                                binding.imgVerifyGmail.setClickable(true);
                            }

                            if (!response_profile.getData().getLinkedinId().equals("")) {
                                binding.imgVerifyLinkedin.setImageResource(R.mipmap.linkedin_verify);
                                binding.imgVerifyLinkedin.setClickable(false);
                            } else {
                                binding.imgVerifyLinkedin.setClickable(true);
                            }

                            if (response_profile.getData().getProfileImg().equals("")) {
                                // binding.imgBg.setImageResource(R.mipmap.user);
                                binding.imgProfile.setImageResource(R.mipmap.user);
                            } else {
                                try {
                                    Picasso.get().load(response_profile.getData().getProfileImg())
                                            .placeholder(R.drawable.loading).error(R.mipmap.user)
                                            .into(binding.imgProfile);
                                   /* Picasso.get().load(response_profile.getData().getProfileImg())
                                            .placeholder(R.drawable.loading).error(R.mipmap.user)
                                            .into(binding.imgBg);*/
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (response_profile.getData().getPaypalEmail().equals("")) {
                                binding.txtPuseremail.setText("N/A");
                            } else {
                                binding.txtPuseremail.setText(response_profile.getData().getPaypalEmail());
                            }
                            strepoc_avl_start_time = response_profile.getData().getAvailableTimeStart();
                            strepoc_avl_end_time = response_profile.getData().getAvailableTimeEnd();
                            countrycodeid = response_profile.getData().getCountryCode();
                            userAddress = response_profile.getData().getAddress();
                            smallDelivery = response_profile.getData().getSmallDelivery();
                            mediumDelivery = response_profile.getData().getMediumDelivery();
                            largeDelivery = response_profile.getData().getLargeDelivery();
                            PrefsUtil.with(context).write("userAddress", userAddress);
                            binding.txtUseremail.setText(response_profile.getData().getEmail());
                            if (response_profile.getData().getTotalReview().equalsIgnoreCase("0")) {
                                binding.txtViewall.setVisibility(View.GONE);
                            } else {
                                binding.txtViewall.setVisibility(View.VISIBLE);
                            }
                            if (response_profile.getData().getPositiveRating().equals("")) {
                                binding.txtRating.setText("0 ");
                            } else {
                                binding.txtRating.setText(response_profile.getData().getStartRating() + " ");
                            }
                            if (!response_profile.getData().getDescription().toString().equals("")) {
                                binding.txtAboutUser.setText(response_profile.getData().getDescription().toString());
                            } else {
                                binding.txtAboutUser.setText("N/A");
                            }
                            if (!response_profile.getData().getTotalReview().equals("")) {
                                binding.txtTotaReview.setText(" " + response_profile.getData().getTotalReview());
                            } else {
                                binding.txtTotaReview.setText(" N/A");
                            }
                            if (!response_profile.getData().getUserName().equals("")) {
                                binding.txtUsername.setText(response_profile.getData().getUserName());
                            } else {
                                binding.txtUsername.setText("N/A");
                            }
                            binding.txtUsercontactno.setText(response_profile.getData().getCountryCode() + " " + response_profile.getData().getContactNumber());
                            if (!response_profile.getData().getAvailableTimeStart().equals("")) {
                                binding.txtServicetime.setText(response_profile.getData().getAvailableTimeStart() + " - " + response_profile.getData().getAvailableTimeEnd());
                            } else {
                                binding.txtServicetime.setText("N/A");
                            }
                            if (!response_profile.getData().getAvailableDaysList().equals("")) {
                                binding.txtAvailableDays.setText(response_profile.getData().getAvailableDaysList());
                            } else {
                                binding.txtAvailableDays.setText("N/A");
                            }
                            if (!response_profile.getData().getAddress().equals("")) {
                                binding.txtAddressPro.setText(response_profile.getData().getAddress());
                            } else {
                                binding.txtAddressPro.setText("N/A");
                            }
                            if (!response_profile.getData().getTaskAssigned().equals("")) {
                                binding.txtWorkedOn.setText(response_profile.getData().getTaskAssigned());
                            } else {
                                binding.txtWorkedOn.setText("N/A");
                            }

                            PrefsUtil.with(context).write("userContactno", response_profile.getData().getContactNumber());
                            PrefsUtil.with(context).write("userEmail", response_profile.getData().getEmail());
                            PrefsUtil.with(context).write("total_review", response_profile.getData().getTotalReview());
                            PrefsUtil.with(context).write("total_rating", response_profile.getData().getPositiveRating());
                            PrefsUtil.with(context).write("userAddress", response_profile.getData().getAddress());
                            PrefsUtil.with(context).write("userAbout", response_profile.getData().getDescription());
                            PrefsUtil.with(context).write("userfname", response_profile.getData().getFirstName());
                            PrefsUtil.with(context).write("userlname", response_profile.getData().getLastName());
                            PrefsUtil.with(context).write("start_time", response_profile.getData().getAvailableTimeStart());
                            PrefsUtil.with(context).write("end_time", response_profile.getData().getAvailableTimeEnd());
                            PrefsUtil.with(context).write("UserImg", response_profile.getData().getProfileImg());
                            PrefsUtil.with(context).write("userlatitude", response_profile.getData().getLatitude());
                            PrefsUtil.with(context).write("userlongitude", response_profile.getData().getLongitude());
                            PrefsUtil.with(context).write("userAvalDay", response_profile.getData().getAvailableDays());
                            PrefsUtil.with(context).write("paypalEmailId", response_profile.getData().getPaypalEmail());
                            PrefsUtil.with(context).write("lat", response_profile.getData().getLatitude());
                            PrefsUtil.with(context).write("long", response_profile.getData().getLongitude());


                        } else {
                            Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
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
        context = DrawerActivity.this;
        activity = DrawerActivity.this;
        setUpToolBar();

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // collapsed
                if (!binding.txtUsername.getText().equals(""))
                    binding.txtHeaderName.setText(binding.txtUsername.getText());
                else
                    binding.txtHeaderName.setText(getResources().getString(R.string.provider_profile));

                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.white));
                /*binding.imgBack.setColorFilter(ContextCompat.getColor(mContext,
                        R.color.text));*/
                binding.txtHeaderName.setTextColor(getResources().getColor(R.color.text));
                mDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.text));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(Color.WHITE);
                }
            } else {
                // expanded
                binding.txtHeaderName.setText("");
                binding.txtHeaderName.setTextColor(getResources().getColor(R.color.white));
                mDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                /*binding.imgBack.setColorFilter(ContextCompat.getColor(mContext,
                        R.color.white));*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(Color.TRANSPARENT);
                }
            }
        });

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        setUpDrawer();

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

    private void setUpToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.provider_profile);
    }

    private void permissionMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.denine_permission);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    private void setUpDrawer() {
        drawerItem = new ModelForDrawer[14];
        drawerItem[0] = new ModelForDrawer(R.mipmap.ic_profile_drawer, getString(R.string.profile));
        drawerItem[1] = new ModelForDrawer(R.mipmap.ic_service_request, getString(R.string.service_request));
        drawerItem[2] = new ModelForDrawer(R.mipmap.ic_financial_information, getString(R.string.financial_info));
        drawerItem[3] = new ModelForDrawer(R.mipmap.ic_my_services, getString(R.string.my_services));
        drawerItem[4] = new ModelForDrawer(R.mipmap.ic_messages_drawer, getString(R.string.message));
        drawerItem[5] = new ModelForDrawer(R.mipmap.ic_notification_drawer, getString(R.string.notifications));
        drawerItem[6] = new ModelForDrawer(R.mipmap.ic_resolution_center_drawer, getString(R.string.dispute_list));
        drawerItem[7] = new ModelForDrawer(R.mipmap.ic_payment_history, getString(R.string.payment_history));
        drawerItem[8] = new ModelForDrawer(R.mipmap.ic_wallet_drawer, getString(R.string.wallet));
        drawerItem[9] = new ModelForDrawer(R.mipmap.ic_account_settings, getString(R.string.account_settings));
        drawerItem[10] = new ModelForDrawer(R.mipmap.ic_feedback, getString(R.string.feedback));
        drawerItem[11] = new ModelForDrawer(R.mipmap.ic_contact_us, getString(R.string.comtact_us));
        drawerItem[12] = new ModelForDrawer(R.mipmap.ic_info_drawer, getString(R.string.info));
        drawerItem[13] = new ModelForDrawer(R.mipmap.ic_logout, getString(R.string.logout));

        adapter = new DrawerItemCustomAdapter(this, R.layout.drawerlist_rowitem, drawerItem, selectedPos);
        binding.leftDrawer.setAdapter(adapter);
        binding.leftDrawer.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, toolbar, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (binding.contentFramePartner != null) {
                    binding.contentFramePartner.setTranslationX(slideOffset * drawerView.getWidth());
                }
                binding.drawerLayout.bringChildToFront(drawerView);
                binding.drawerLayout.setScrimColor(Color.TRANSPARENT);
                binding.drawerLayout.requestLayout();
            }
        };

        binding.drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    private void selectItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                break;
            case 1:
                startActivity(new Intent(DrawerActivity.this, Partner_ServiceRequest_TabLayout_Activity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 2:
                startActivity(new Intent(DrawerActivity.this, Partner_FinancialInfo_Activity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 3:
                startActivity(new Intent(DrawerActivity.this, Partner_MyServices_Activity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 4:
                startActivity(new Intent(DrawerActivity.this, Messages_Activity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 5:
                startActivity(new Intent(DrawerActivity.this, NotificationListingActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 6:
                startActivity(new Intent(DrawerActivity.this, ResolutionActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 7:
                startActivity(new Intent(DrawerActivity.this, PartnerPaymentHistoryActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 8:
                startActivity(new Intent(DrawerActivity.this, WalletActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 9:
                startActivity(new Intent(DrawerActivity.this, AccountSettingActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 10:
                startActivity(new Intent(DrawerActivity.this, FeedbackActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 11:
                startActivity(new Intent(DrawerActivity.this, ContactUsActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 12:
                startActivity(new Intent(DrawerActivity.this, InfoPageActivity.class));
                binding.leftDrawer.setSelection(position);
                break;
            case 13:
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DrawerActivity.this);
                builder.setMessage(R.string.sure_logout)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                serviceCallLogout();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).show();
                break;
            default:
                break;
        }


        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            previousSelectedPos = selectedPos;
            selectedPos = position;
            binding.drawerLayout.closeDrawer(binding.leftDrawer);
        } else {
        }
        adapter.notifyDataSetChanged();
    }

    /*------------------ Logout Api Call ----------------------*/
    private void serviceCallLogout() {

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        SecurePrefsUtil securePrefs = SecurePrefsUtil.with(context);
        PrefsUtil prefsUtil = PrefsUtil.with(context);
        
        // Fallback a PrefsUtil se SecurePrefsUtil non ha UserId
        String userId = securePrefs.readString("UserId");
        if (userId == null || userId.isEmpty()) {
            userId = prefsUtil.readString("UserId");
        }
        
        String deviceToken = securePrefs.readString("device_token");
        if (deviceToken == null || deviceToken.isEmpty()) {
            deviceToken = prefsUtil.readString("device_token");
        }
        
        textParams.put("user_id", userId != null ? userId : "");
        textParams.put("device_token", deviceToken != null ? deviceToken : "");

        new WebServiceCall(DrawerActivity.this, WebServiceUrl.URL_LOGOUT, textParams,
                CommonPojo.class, true, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                // Anche se l'API fallisce, procediamo con il logout locale
                File offlineFile = new File(getFilesDir().getPath(), "/offline.json");
                if (offlineFile.exists()) {
                    offlineFile.delete();
                    Log.e(TAG, "Delete Offline File :: ");
                }
                
                // Pulisci sia SecurePrefsUtil che PrefsUtil per sicurezza
                SecurePrefsUtil.with(DrawerActivity.this).clearPrefs();
                PrefsUtil.with(DrawerActivity.this).clearPrefs();
                
                // Vai a LoginActivity con flag per pulire lo stack
                Intent intent = new Intent(DrawerActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                
                if (!status) {
                    // Mostra messaggio solo se necessario, ma procedi comunque
                    Toast.makeText(DrawerActivity.this, getString(R.string.logout),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                logoutAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                logoutAsync = null;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DrawerActivity.this);
        builder.setMessage("Are you sure you want to exit app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
    }

    @Override
    protected void onResume() {
        adapter.selectedItem(0);
        adapter.notifyDataSetChanged();
        registerReceiver(mMessageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        super.onResume();
    }

    @Override
    public void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(socialSignInAsync);
        Utils.cancelAsyncTask(changeStatusAsync);
        Utils.cancelAsyncTask(getProfileAsync);
        Utils.cancelAsyncTask(logoutAsync);
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    getOfflineUserDetails();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getOfflineUserDetails() {
        try {
            File f = new File(getFilesDir().getPath() + "/" + "offline.json");
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String s = new String(buffer);
            JSONObject object = new JSONObject(s);
            JSONObject dataObj = object.getJSONObject("data");
            JSONObject profileObj = dataObj.getJSONObject("profileData");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
            Gson gson = gsonBuilder.create();
            ProfileItem item = new ProfileItem();
            item = gson.fromJson(profileObj.toString(), ProfileItem.class);
            Log.e("Offline", "getOfflineUserDetails: " + item.getEmail());

            ProfilePojo response_profile = new ProfilePojo();
            response_profile.setData(item);

            if (response_profile.getData().getIsAvailable().equals("y")) {
                binding.switchAvailableNow.setChecked(true);
            } else {
                binding.switchAvailableNow.setChecked(false);
            }

            if (!response_profile.getData().getFbId().equals("")) {
                binding.imgVerifyFacebook.setImageResource(R.mipmap.fb_verify);
            }

            if (!response_profile.getData().getGmailId().equals("")) {
                binding.imgVerifyGmail.setImageResource(R.mipmap.google_verify);
            }

            if (!response_profile.getData().getLinkedinId().equals("")) {
                binding.imgVerifyLinkedin.setImageResource(R.mipmap.linkedin_verify);
            }

            if (response_profile.getData().getProfileImg().equals("")) {
                // binding.imgBg.setImageResource(R.mipmap.user);
                binding.imgProfile.setImageResource(R.mipmap.user);
            } else {
                try {
                    Picasso.get().load(response_profile.getData().getProfileImg()).placeholder(
                            R.drawable.loading).error(R.mipmap.user).into(binding.imgProfile);
                   /* Picasso.get().load(response_profile.getData().getProfileImg()).placeholder(
                            R.drawable.loading).error(R.mipmap.user).into(binding.imgBg);*/
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            if (response_profile.getData().getPaypalEmail().equals("")) {
                binding.txtPuseremail.setText("N/A");
            } else {
                binding.txtPuseremail.setText(response_profile.getData().getPaypalEmail());
            }
            strepoc_avl_start_time = response_profile.getData().getAvailableTimeStart();
            strepoc_avl_end_time = response_profile.getData().getAvailableTimeEnd();
            countrycodeid = response_profile.getData().getCountryCode();
            userAddress = response_profile.getData().getAddress();
            PrefsUtil.with(context).write("userAddress", userAddress);
            binding.txtUseremail.setText(response_profile.getData().getEmail());
            if (response_profile.getData().getStartRating().equals("")) {
                binding.txtRating.setText("0 ");
            } else {
                binding.txtRating.setText(String.format("%s ", response_profile.getData().getStartRating()));
            }
            if (!response_profile.getData().getDescription().toString().equals("")) {
                binding.txtAboutUser.setText(response_profile.getData().getDescription().toString());
            } else {
                binding.txtAboutUser.setText("N/A");
            }
            if (!response_profile.getData().getTotalReview().equals("")) {
                if (response_profile.getData().getTotalReview().equals("0")) {
                    binding.txtTotaReview.setText(" N/A");
                    binding.txtViewall.setVisibility(View.GONE);
                }
                binding.txtTotaReview.setText(String.format(" %s", response_profile.getData().getTotalReview()));
                binding.txtViewall.setVisibility(View.VISIBLE);
            } else {
                binding.txtTotaReview.setText(" N/A");
                binding.txtViewall.setVisibility(View.GONE);
            }
            if (!response_profile.getData().getUserName().equals("")) {
                binding.txtUsername.setText(response_profile.getData().getUserName());
            } else {
                binding.txtUsername.setText("N/A");
            }
            binding.txtUsercontactno.setText(String.format("%s %s", response_profile.getData().getCountryCode(), response_profile.getData().getContactNumber()));
            if (!response_profile.getData().getAvailableTimeStart().equals("")) {
                binding.txtServicetime.setText(String.format("%s - %s", response_profile.getData().getAvailableTimeStart(), response_profile.getData().getAvailableTimeEnd()));
            } else {
                binding.txtServicetime.setText("N/A");
            }
            if (!response_profile.getData().getAvailableDaysList().equals("")) {
                binding.txtAvailableDays.setText(response_profile.getData().getAvailableDaysList());
            } else {
                binding.txtAvailableDays.setText("N/A");
            }
            if (!response_profile.getData().getAddress().equals("")) {
                binding.txtAddressPro.setText(response_profile.getData().getAddress());
            } else {
                binding.txtAddressPro.setText("N/A");
            }
            if (!response_profile.getData().getTaskAssigned().equals("")) {
                binding.txtWorkedOn.setText(response_profile.getData().getTaskAssigned());
            } else {
                binding.txtWorkedOn.setText("N/A");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
            adapter.selectedItem(position);
            binding.drawerLayout.closeDrawers();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
