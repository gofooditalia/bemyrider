package com.app.bemyrider.fragment.partner;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
//import com.app.bemyrider.activity.LinkedInLoginWebViewActivity;
import com.app.bemyrider.activity.SignupActivity;
import com.app.bemyrider.activity.partner.EditProfileActivity;
import com.app.bemyrider.activity.partner.PartnerReviewsActivity;
import com.app.bemyrider.databinding.FragmentProviderProfileBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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

public class ProviderProfileFragment extends Fragment {

    FragmentProviderProfileBinding binding;
    private GoogleApiClient mGoogleApiClient;
    private String strepoc_avl_start_time, strepoc_avl_end_time,
            countrycodeid, userAddress, smallDelivery, mediumDelivery, largeDelivery;
    private String clicktype = "";
    private CallbackManager callbackmanager;
    private AsyncTask socialSignInAsync, changeStatusAsync, getProfileAsync, logoutAsync;
    private ConnectionManager connectionManager;
    ActivityResultLauncher<Intent> gmailActivityResult;
    ActivityResultLauncher<Intent> linkedInActivityResult;
    private Context context;
    private AppCompatActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_provider_profile, container, false);

        context = getContext();
        activity = (AppCompatActivity) getActivity();

        callbackmanager = CallbackManager.Factory.create();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(activity, connectionResult -> {

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

        binding.fabEdit.setOnClickListener(v -> {
            if (profilePojoData != null) {
                Intent i = new Intent(context, EditProfileActivity.class);
                i.putExtra("isFromEdit", true);
                i.putExtra("profilePojoData", profilePojoData);
                i.putExtra("smallDelivery", smallDelivery);
                i.putExtra("mediumDelivery", mediumDelivery);
                i.putExtra("largeDelivery", largeDelivery);
                i.putExtra("strepoc_avl_start_time", strepoc_avl_start_time);
                i.putExtra("strepoc_avl_end_time", strepoc_avl_end_time);
                i.putExtra("userAddress", userAddress);
                i.putExtra("countrycodeId", countrycodeid);
                i.putExtra("Edit", "true");
                startActivity(i);
            }
        });

        binding.txtViewall.setOnClickListener(v -> {
            Intent i = new Intent(context, PartnerReviewsActivity.class);
            startActivity(i);
        });

        binding.imgVerifyFacebook.setOnClickListener(view -> {
            clicktype = "f";
            loginWithFacebook();
        });

        binding.imgVerifyGmail.setOnClickListener(view -> {
            clicktype = "g";
            loginWithGooglePlus();
        });

        binding.imgVerifyLinkedin.setOnClickListener(view -> {
            clicktype = "l";
            loginWithLinkedIn();
        });


        return binding.getRoot();
    }

    private void loginWithLinkedIn() {
        /*Intent intent = new Intent(activity, LinkedInLoginWebViewActivity.class);
        linkedInActivityResult.launch(intent);*/
    }

    private void loginWithGooglePlus() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        Log.e("IN GOOGLE PLUS", "TRUE");
        gmailActivityResult.launch(intent);
    }

    private void loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("email", "public_profile"));
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
                                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
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

    /*------------- Social Sign in Api Call -------------------*/
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


        new WebServiceCall(context, WebServiceUrl.URL_SOCIAL_LOGIN, textParams, NewLoginPojo.class,
                true, new WebServiceCall.OnResultListener() {
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
                            activity.finish();
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
                            PrefsUtil.with(context).write("UserImg", loginData.getProfileImg());
                            PrefsUtil.with(activity).write("login_cust_address", loginData.getAddress());
                        }
                    }
                } else {
                    androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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

    /*---------------- Change Available Status Api Call -----------------*/
    private void serviceCallChangeStatus(final String switchstatus) {
        binding.progressAvailable.setVisibility(View.VISIBLE);
        binding.switchAvailableNow.setVisibility(View.GONE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(context).readString("UserId"));
        textParams.put("isAvailable", switchstatus);

        new WebServiceCall(context, WebServiceUrl.URL_AVAILABLE_NOW, textParams,
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
    private ProfileItem profilePojoData;

    private void serviceCall() {
        binding.progressProfileDetail.setVisibility(View.VISIBLE);
        binding.llPartnerProfileDetail.setVisibility(View.GONE);
        binding.fabEdit.setVisibility(View.GONE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("profile_id", PrefsUtil.with(context).readString("UserId"));

        new WebServiceCall(context, WebServiceUrl.URL_PROFILE, textParams, ProfilePojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.progressProfileDetail.setVisibility(View.GONE);
                        binding.llPartnerProfileDetail.setVisibility(View.VISIBLE);
                        binding.fabEdit.setVisibility(View.VISIBLE);

                        if (status) {
                            ProfilePojo response_profile = (ProfilePojo) obj;
                            profilePojoData = response_profile.getData();
                            Log.e("KKK","profilePojoData.getSignature_img()"+profilePojoData.getSignature_img());

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
                                    PrefsUtil.with(activity).write("UserImg", response_profile.getData().getProfileImg());
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

                            /*if (response_profile.getData().getPaypalEmail().equals("")) {
                                binding.txtPuseremail.setText("N/A");
                            } else {
                                binding.txtPuseremail.setText(response_profile.getData().getPaypalEmail());
                            }*/
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
                                binding.txtAboutUser.setText(Utils.decodeEmoji(response_profile.getData().getDescription().toString()));
                            } else {
                                binding.txtAboutUser.setText("N/A");
                            }
                            if (!response_profile.getData().getTotalReview().equals("")) {
                                binding.txtTotaReview.setText(" " + response_profile.getData().getTotalReview());
                            } else {
                                binding.txtTotaReview.setText(" N/A");
                            }
                            if (!response_profile.getData().getUserName().equals("")) {
                                PrefsUtil.with(activity).write("UserName", response_profile.getData().getUserName());
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

                            if (!response_profile.getData().getCity_of_birth().equals("")) {
                                binding.txtCityOfBirth.setText(response_profile.getData().getCity_of_birth());
                            } else {
                                binding.txtCityOfBirth.setText("N/A");
                            }
                            if (!response_profile.getData().getDate_of_birth().equals("")) {
                                binding.txtDateOfBirth.setText(response_profile.getData().getDate_of_birth());
                            } else {
                                binding.txtDateOfBirth.setText("N/A");
                            }
                            if (!response_profile.getData().getCity_of_residence().equals("")) {
                                binding.txtCityOfResidence.setText(response_profile.getData().getCity_of_residence());
                            } else {
                                binding.txtCityOfResidence.setText("N/A");
                            }
                            if (!response_profile.getData().getResidential_address().equals("")) {
                                binding.txtResidentialAddress.setText(response_profile.getData().getResidential_address());
                            } else {
                                binding.txtResidentialAddress.setText("N/A");
                            }


                            if (!response_profile.getData().getAddress().equals("")) {
                                PrefsUtil.with(activity).write("login_cust_address", response_profile.getData().getAddress());
                                binding.txtAddressPro.setText(response_profile.getData().getAddress());
                            } else {
                                binding.txtAddressPro.setText("N/A");
                            }
                            if (!response_profile.getData().getTaskAssigned().equals("")) {
                                binding.txtWorkedOn.setText(response_profile.getData().getTaskAssigned());
                            } else {
                                binding.txtWorkedOn.setText("N/A");
                            }

                            if (!response_profile.getData().getCompanyName().equals("")) {
                                binding.txtCompany.setText(response_profile.getData().getCompanyName());
                            } else {
                                binding.txtCompany.setText("N/A");
                            }
                            if (!response_profile.getData().getVat().equals("")) {
                                binding.txtVat.setText(response_profile.getData().getVat());
                            } else {
                                binding.txtVat.setText("N/A");
                            }
                            /*if (!response_profile.getData().getTaxId().equals("")) {
                                binding.txtTaxIdCode.setText(response_profile.getData().getTaxId());
                            } else {
                                binding.txtTaxIdCode.setText("N/A");
                            }*/
                            if (!response_profile.getData().getReceiptCode().equals("")) {
                                binding.txtInvoiceRecipient.setText(response_profile.getData().getReceiptCode());
                            } else {
                                binding.txtInvoiceRecipient.setText("N/A");
                            }
                            if (!response_profile.getData().getCertifiedEmail().equals("")) {
                                binding.txtCertifiedEmail.setText(response_profile.getData().getCertifiedEmail());
                            } else {
                                binding.txtCertifiedEmail.setText("N/A");
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
                            PrefsUtil.with(context).write("userlatitude", response_profile.getData().getLatitude());
                            PrefsUtil.with(context).write("userlongitude", response_profile.getData().getLongitude());
                            PrefsUtil.with(context).write("userAvalDay", response_profile.getData().getAvailableDays());
                            //PrefsUtil.with(context).write("paypalEmailId", response_profile.getData().getPaypalEmail());
                            PrefsUtil.with(context).write("lat", response_profile.getData().getLatitude());
                            PrefsUtil.with(context).write("long", response_profile.getData().getLongitude());
                            PrefsUtil.with(context).write("UserImg", response_profile.getData().getProfileImg());


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
        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // collapsed
                if (!binding.txtUsername.getText().equals(""))
                    binding.txtHeaderName.setText(binding.txtUsername.getText());
                else
                    binding.txtHeaderName.setText(getResources().getString(R.string.user_profile));

                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_bg_color));
                binding.txtHeaderName.setTextColor(getResources().getColor(R.color.white));
            } else {
                // expanded
                binding.txtHeaderName.setText("");
                binding.txtHeaderName.setTextColor(getResources().getColor(R.color.white));
                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
            }
        });


        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

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

    private void getOfflineUserDetails() {
        try {
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
                    PrefsUtil.with(context).write("UserImg", response_profile.getData().getProfileImg());
                    Picasso.get().load(response_profile.getData().getProfileImg()).placeholder(
                            R.drawable.loading).error(R.mipmap.user).into(binding.imgProfile);
                   /* Picasso.get().load(response_profile.getData().getProfileImg()).placeholder(
                            R.drawable.loading).error(R.mipmap.user).into(binding.imgBg);*/
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            /*if (response_profile.getData().getPaypalEmail().equals("")) {
                binding.txtPuseremail.setText("N/A");
            } else {
                binding.txtPuseremail.setText(response_profile.getData().getPaypalEmail());
            }*/
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
                binding.txtAboutUser.setText(Utils.decodeEmoji(response_profile.getData().getDescription().toString()));
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
                PrefsUtil.with(activity).write("UserName", response_profile.getData().getUserName());
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
                PrefsUtil.with(activity).write("login_cust_address", response_profile.getData().getAddress());
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


    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(activity);
        mGoogleApiClient.disconnect();
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
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
