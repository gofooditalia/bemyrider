package com.app.bemyrider.activity;

import static com.app.bemyrider.utils.Utils.EMOJI_FILTER;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.partner.ProviderHomeActivity;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.databinding.ActivitySignupBinding;
import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.model.RegistrationPojo;
import com.app.bemyrider.model.partner.CountryCodePojo;
import com.app.bemyrider.model.partner.CountryCodePojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 3/12/19.
 */


public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private ActivitySignupBinding binding;
    private String strfname, strlname, stremail, strpassword, strconfirmpassword, strcontactno, strUserType;
    private Context mContext = SignupActivity.this;
    private String selected_country_code, selected_country_code_position;
    private ConnectionManager connectionManager;
    private ArrayList<CountryCodePojoItem> countryArrayList = new ArrayList<>();
    private ArrayAdapter countrycodeAdapter;
    private AsyncTask socialSignupAsync, emailSignupAsync, countryCodeAsync, offlineDataAsync;
    boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(SignupActivity.this, R.layout.activity_signup, null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (PrefsUtil.with(mContext).readString("UserId") != null && PrefsUtil.with(mContext).readString("UserId").length() > 0) {
            boolean isProfileCompleted = PrefsUtil.with(mContext).readBoolean("isProfileCompleted");
            if (PrefsUtil.with(mContext).readString("UserType").equalsIgnoreCase("c") && isProfileCompleted) {
                Intent i = new Intent(mContext, CustomerHomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            } else if (PrefsUtil.with(mContext).readString("UserType").equalsIgnoreCase("p") && isProfileCompleted) {
                Intent i = new Intent(mContext, ProviderHomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        }

        initView();

        /*Init Country Code Spinner*/
        countrycodeAdapter = new ArrayAdapter<>(SignupActivity.this, android.R.layout.simple_spinner_item, countryArrayList);
        binding.spinnerCountrycode.setAdapter(countrycodeAdapter);
        countrycodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        textchangeListner();

//        serviceCallCountryCode();

        strUserType = "p";
        binding.linlayProvider.setOnClickListener(v -> {
            strUserType = "p";
            binding.linlayProvider.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_user_type_select));
            binding.linlayCustomer.setBackground(null);
        });
        binding.linlayCustomer.setOnClickListener(v -> {
            strUserType = "c";
            binding.linlayCustomer.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_user_type_select));
            binding.linlayProvider.setBackground(null);
        });

        binding.linContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ContactUsActivity.class));
            }
        });

        binding.linInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, InfoPageActivity.class));
            }
        });
        /*binding.spinnerUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (binding.spinnerUserType.getSelectedItemPosition() == 1) {
                    strUserType = "c";
                }
                if (binding.spinnerUserType.getSelectedItemPosition() == 2) {
                    strUserType = "p";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        binding.spinnerCountrycode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_country_code = countryArrayList.get(position).getCountryCode();
                selected_country_code_position = countryArrayList.get(position).getId();
                ((TextView) view).setText(countryArrayList.get(position).getCountryCode());
                //PrefsUtil.with(mContext).write("position",Integer.parseInt(String.valueOf(countrycodeAdapter.getPosition(position))));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.rgUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdCompany) {
                    binding.tilSignupFname.setVisibility(View.GONE);
                    binding.tilSignupLname.setVisibility(View.GONE);
                }
                if (checkedId == R.id.rdCustomer) {
                    binding.tilSignupFname.setVisibility(View.VISIBLE);
                    binding.tilSignupLname.setVisibility(View.VISIBLE);
                }
                if (checkedId == R.id.rdIndividual) {
                    binding.tilSignupFname.setVisibility(View.VISIBLE);
                    binding.tilSignupLname.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.txtSignin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            //finish();
        });

        binding.txtTermsAndConditions.setOnClickListener(v -> {
            startActivity(new Intent(mContext, WebViewActivity.class)
                    .putExtra("isTermsAndConditions", true)
                    .putExtra("webUrl", WebServiceUrl.terms_and_conditions)
                    .putExtra("title", getString(R.string.terms_and_conditions))
            );
        });


        binding.btnSubmit.setOnClickListener(v -> {
            if (checkValidation()) {
                if (binding.checkboxTermsAndConditions.isChecked()) {
                    Utils.hideSoftKeyboard(SignupActivity.this);
                    if (getIntent().getStringExtra("SocialFlag") != null) {
                        if (getIntent().getStringExtra("SocialFlag").equals("true")) {
                            serviceCallsocialSignUp();
                        }
                    } else {
                        serviCall();
                    }
                    PrefsUtil.with(mContext).write("countrycode", selected_country_code);
                    PrefsUtil.with(mContext).write("countrycodeid", selected_country_code_position);
                } else {
                    Toast.makeText(mContext, getString(R.string.msg_terms_and_conditions), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (getIntent().getStringExtra("SocialFlag") != null) {
            if (getIntent().getStringExtra("SocialFlag").equals("true")) {
                binding.etSignupFname.setText(getIntent().getStringExtra("Fname"));
                binding.etSignupLname.setText(getIntent().getStringExtra("Lname"));
                binding.etSignupEmail.setText(getIntent().getStringExtra("Email"));
                binding.etSignupPassword.setVisibility(View.GONE);
                binding.etSignupConfirmpassword.setVisibility(View.GONE);
                binding.tilSignupPassword.setVisibility(View.GONE);
                binding.tilSignupConfirmPassword.setVisibility(View.GONE);
            }
        }
    }

    /*----------------- Country Code Api Call -------------------*/
    private void serviceCallCountryCode() {
        binding.spinnerCountrycode.setVisibility(View.GONE);
        binding.progressCountryCode.setVisibility(View.VISIBLE);

        countryArrayList.clear();
        new WebServiceCall(mContext, WebServiceUrl.URL_COUNTRY_CODE, new LinkedHashMap<>(),
                CountryCodePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progressCountryCode.setVisibility(View.GONE);
                binding.spinnerCountrycode.setVisibility(View.VISIBLE);
                if (status) {
                    countryArrayList.addAll(((CountryCodePojo) obj).getData());
                    countrycodeAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(mContext, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                countryCodeAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                countryCodeAsync = null;
            }
        });

    }

    /*------------- Sign Up Api Call -----------------*/
    private void serviCall() {
        binding.btnSubmit.setClickable(false);
        binding.progressSignUp.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

//        textParams.put("firstName", binding.etSignupFname.getText().toString().trim());
//        textParams.put("lastName", binding.etSignupLname.getText().toString().trim());
        textParams.put("email", binding.etSignupEmail.getText().toString().trim());
        textParams.put("user_type", strUserType);
//        textParams.put("contact_number", binding.etSignupContactno.getText().toString().trim());
//        textParams.put("country_code", selected_country_code_position);
        textParams.put("password", binding.etSignupPassword.getText().toString().trim());
        textParams.put("repassword", binding.etSignupConfirmpassword.getText().toString().trim());

        new WebServiceCall(mContext, WebServiceUrl.URL_SIGNUP, textParams, RegistrationPojo.class,
                false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progressSignUp.setVisibility(View.GONE);
                binding.btnSubmit.setClickable(true);
                if (status) {
                    RegistrationPojo signin_response = (RegistrationPojo) obj;

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setCancelable(false);
                    builder.setMessage(signin_response.getMessage());
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            binding.etSignupEmail.setText("");
                            binding.etSignupPassword.setText("");
                            binding.etSignupConfirmpassword.setText("");
                            strUserType = "p";
                            binding.linlayProvider.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_user_type_select));
                            binding.linlayCustomer.setBackground(null);
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            //finish();
                        }
                    });
                    builder.create().show();
//                    Toast.makeText(mContext, signin_response.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                emailSignupAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                emailSignupAsync = null;
            }
        });


    }

    /*------------------ Social Sign Up Api Call ---------------------*/
    private void serviceCallsocialSignUp() {
        binding.btnSubmit.setClickable(false);
        binding.progressSignUp.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(getIntent().getStringExtra("uId").getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            textParams.put("id", getIntent().getStringExtra("uId"));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        textParams.put("txt_fname", getIntent().getStringExtra("Fname"));
        textParams.put("txt_lname", getIntent().getStringExtra("Lname"));
        textParams.put("rdb_user_type", strUserType);
        textParams.put("sel_country_code", selected_country_code_position);
        textParams.put("txt_contact_number", binding.etSignupContactno.getText().toString().trim());
        textParams.put("device_token", PrefsUtil.with(SignupActivity.this).readString("device_token"));

        new WebServiceCall(SignupActivity.this, WebServiceUrl.URL_SOCIAL_SIGNUP, textParams,
                NewLoginPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progressSignUp.setVisibility(View.GONE);
                binding.btnSubmit.setClickable(true);
                if (status) {
                    NewLoginPojo pojo = ((NewLoginPojo) obj);
                    NewLoginPojoItem loginData = pojo.getData();

                    /*For DeActive/Delete user redirect to contact us page*/
                    if (pojo.getData().getIsUserActive() != null && pojo.getData().getIsUserActive().equalsIgnoreCase("d")) {
                        PrefsUtil.with(SignupActivity.this).write("eMail", getIntent().getStringExtra("Email"));
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) ->
                                startActivity(new Intent(SignupActivity.this, ContactUsActivity.class))).setMessage(pojo.getMessage()).show();
                    }
                    /*Normal Login Flow*/
                    else {
                        PrefsUtil.with(SignupActivity.this).clearPrefs();
                        if (getIntent().hasExtra("loginType")
                                && getIntent().hasExtra("socialId")) {
                            PrefsUtil.with(SignupActivity.this).write("loginType", getIntent().getStringExtra("loginType"));
                            PrefsUtil.with(SignupActivity.this).write("socialId", getIntent().getStringExtra("socialId"));
                        }
                        PrefsUtil.with(SignupActivity.this).write("UserId", loginData.getUserId());
                        PrefsUtil.with(SignupActivity.this).write("CurrencySign", loginData.getCurrencySign());
                        PrefsUtil.with(SignupActivity.this).write("UserName", loginData.getUserName());
                        PrefsUtil.with(SignupActivity.this).write("FirstName", loginData.getFirstName());
                        PrefsUtil.with(SignupActivity.this).write("LastName", loginData.getLastName());
                        PrefsUtil.with(SignupActivity.this).write("UserType", loginData.getUserType());
                        PrefsUtil.with(SignupActivity.this).write("eMail", loginData.getEmailId());
                        PrefsUtil.with(mContext).write("CurrencySign", getResources().getString(R.string.currency));
                        PrefsUtil.with(mContext).write("UserImg", loginData.getProfileImg());
                        PrefsUtil.with(mContext).write("login_cust_address", loginData.getAddress());


                        if (loginData.getUserType().equalsIgnoreCase("c")) {
                            startActivity(new Intent(SignupActivity.this, CustomerHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        } else {
                            startActivity(new Intent(SignupActivity.this, ProviderHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }
                        saveOfflineData();
//                    finish();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
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
                socialSignupAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                socialSignupAsync = null;
            }
        });
    }

    /*-------------------- Save Offline Data Api Call ----------------------*/
    private void saveOfflineData() {
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(SignupActivity.this).readString("UserId"));

        new WebServiceCall(SignupActivity.this, WebServiceUrl.URL_GETOFFLINEDATA,
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    boolean checkValidation() {

//        strfname = binding.etSignupFname.getText().toString().trim();
//        strlname = binding.etSignupLname.getText().toString().trim();
        stremail = binding.etSignupEmail.getText().toString().trim();
//        strcontactno = binding.etSignupContactno.getText().toString().trim();
        strpassword = binding.etSignupPassword.getText().toString().trim();
        strconfirmpassword = binding.etSignupConfirmpassword.getText().toString().trim();

        if (getIntent().getStringExtra("SocialFlag") != null) {
            if (getIntent().getStringExtra("SocialFlag").equals("true")) {
                if (strfname.isEmpty()) {
                    binding.tilSignupFname.setErrorEnabled(true);
                    binding.tilSignupFname.setError(getString(R.string.error_required));
                    binding.etSignupFname.requestFocus();
                    return false;
                } else if (strlname.isEmpty()) {
                    binding.tilSignupLname.setErrorEnabled(true);
                    binding.tilSignupLname.setError(getString(R.string.error_required));
                    binding.etSignupLname.requestFocus();
                    return false;
                } else if (stremail.isEmpty()) {
                    binding.tilSignupEmail.setErrorEnabled(true);
                    binding.tilSignupEmail.setError(getString(R.string.error_required));
                    binding.etSignupEmail.requestFocus();
                    return false;
                } else if (!Utils.isEmailValid(stremail)) {
                    binding.tilSignupEmail.setErrorEnabled(true);
                    binding.tilSignupEmail.setError(getString(R.string.error_valid_email));
                    binding.etSignupEmail.requestFocus();
                    return false;
                } else if (strcontactno.isEmpty()) {
                    binding.etSignupContactno.setError(getString(R.string.error_required));
                    binding.etSignupContactno.requestFocus();
                    return false;
                } else if (strcontactno.length() < 10 || strcontactno.length() > 15) {
                    binding.etSignupContactno.setError(getResources().getString(R.string.vali_contact_num));
                    binding.etSignupContactno.requestFocus();
                    return false;
                } /*else if (binding.spinnerUserType.getSelectedItemPosition() == 0) {
                    Toast.makeText(mContext, getResources().getString(R.string.please_select_user_type), Toast.LENGTH_SHORT).show();
                    return false;
                }*/
                return true;
            }
        } else {
            /*if (strfname.isEmpty()) {
                binding.tilSignupFname.setErrorEnabled(true);
                binding.tilSignupFname.setError(getString(R.string.error_required));
                binding.etSignupFname.requestFocus();
                return false;
            } else if (strlname.isEmpty()) {
                binding.tilSignupLname.setErrorEnabled(true);
                binding.tilSignupLname.setError(getString(R.string.error_required));
                binding.etSignupLname.requestFocus();
                return false;
            } else*/
            if (stremail.isEmpty()) {
                binding.tilSignupEmail.setErrorEnabled(true);
                binding.tilSignupEmail.setError(getString(R.string.error_required));
                binding.etSignupEmail.requestFocus();
                return false;
            } else if (!Utils.isEmailValid(stremail)) {
                binding.tilSignupEmail.setErrorEnabled(true);
                binding.tilSignupEmail.setError(getString(R.string.error_valid_email));
                binding.etSignupEmail.requestFocus();
                return false;
            } /*else if (strcontactno.isEmpty()) {
                binding.etSignupContactno.setError(getString(R.string.error_required));
                binding.etSignupContactno.requestFocus();
                return false;
            } else if (strcontactno.length() < 10 || strcontactno.length() > 15) {
                binding.etSignupContactno.setError(getResources().getString(R.string.vali_contact_num));
                binding.etSignupContactno.requestFocus();
                return false;
            } */ else if (strpassword.isEmpty()) {
                binding.tilSignupPassword.setErrorEnabled(true);
                binding.tilSignupPassword.setError(getString(R.string.error_required));
                binding.etSignupPassword.requestFocus();
                return false;
            } else if (strpassword.length() < 6) {
                binding.tilSignupPassword.setErrorEnabled(true);
                binding.tilSignupPassword.setError(getString(R.string.error_min_pwd));
                binding.etSignupPassword.requestFocus();
                return false;
            } else if (strconfirmpassword.isEmpty()) {
                binding.tilSignupConfirmPassword.setErrorEnabled(true);
                binding.tilSignupConfirmPassword.setError(getString(R.string.error_required));
                binding.etSignupConfirmpassword.requestFocus();
                return false;
            } else if (!strconfirmpassword.equals(strpassword)) {
                binding.tilSignupConfirmPassword.setErrorEnabled(true);
                binding.tilSignupConfirmPassword.setError(getString(R.string.error_pwd_not_match));
                binding.etSignupConfirmpassword.requestFocus();
                return false;
            } /*else if (binding.spinnerUserType.getSelectedItemPosition() == 0) {
                Toast.makeText(mContext, R.string.please_select_user_type, Toast.LENGTH_SHORT).show();
                return false;
            }*/
            return true;
        }
        return false;
    }

    private void textchangeListner() {

        binding.etSignupFname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilSignupFname.setErrorEnabled(false);
                binding.tilSignupFname.setError(null);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.etSignupLname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilSignupLname.setErrorEnabled(false);
                binding.tilSignupLname.setError(null);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.etSignupEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilSignupEmail.setErrorEnabled(false);
                binding.tilSignupEmail.setError(null);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.etSignupContactno.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilSignupContact.setErrorEnabled(false);
                binding.tilSignupContact.setError(null);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etSignupPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilSignupPassword.setErrorEnabled(false);
                binding.tilSignupPassword.setError(null);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etSignupConfirmpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilSignupConfirmPassword.setErrorEnabled(false);
                binding.tilSignupConfirmPassword.setError(null);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void initView() {
        getSupportActionBar().hide();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.etSignupPassword.setTransformationMethod(new PasswordTransformationMethod());
        binding.etSignupConfirmpassword.setTransformationMethod(new PasswordTransformationMethod());


        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        binding.etSignupFname.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.etSignupLname.setFilters(new InputFilter[]{EMOJI_FILTER});


    }


    private void setUptoolbar() {
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.create_new_account),HtmlCompat.FROM_HTML_MODE_LEGACY));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.back_press_msg), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        /*if (isFirst) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        } else {
            isFirst = true;
            Fragment fragment = new GuestHomeFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            binding.bottomNavigationView.getMenu().findItem(R.id.nav_home_c).setChecked(true);
            ft.commit();
        }*/
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Utils.cancelAsyncTask(offlineDataAsync);
        Utils.cancelAsyncTask(socialSignupAsync);
        Utils.cancelAsyncTask(emailSignupAsync);
        Utils.cancelAsyncTask(countryCodeAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
