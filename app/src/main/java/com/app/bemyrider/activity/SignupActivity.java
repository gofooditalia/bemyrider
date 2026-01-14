package com.app.bemyrider.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.partner.ProviderHomeActivity;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.activity.ContactUsActivity;
import com.app.bemyrider.activity.WebViewActivity;

import com.app.bemyrider.databinding.ActivitySignupBinding;
import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.model.partner.CountryCodePojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.AppSignupViewModel;

import java.util.ArrayList;

/**
 * Modified by Hardik Talaviya on 3/12/19.
 * Modernized by Gemini on 2024.
 */


public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private ActivitySignupBinding binding;
    private String strUserType = "p"; // Default to provider
    private String selectedCountryCode, selectedCountryCodePosition;
    private ConnectionManager connectionManager;
    private ArrayList<CountryCodePojoItem> countryArrayList = new ArrayList<>();
    private ArrayAdapter<CountryCodePojoItem> countrycodeAdapter;
    private AppSignupViewModel viewModel;
    private boolean doubleBackToExitPressedOnce = false;
    private final Context mContext = SignupActivity.this; 


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(SignupActivity.this, R.layout.activity_signup, null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        viewModel = new ViewModelProvider(this).get(AppSignupViewModel.class);
        
        initView();

        countrycodeAdapter = new ArrayAdapter<>(SignupActivity.this, android.R.layout.simple_spinner_item, countryArrayList);
        binding.spinnerCountrycode.setAdapter(countrycodeAdapter);
        countrycodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        textchangeListner();

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

        binding.linContactUs.setOnClickListener(v -> startActivity(new Intent(mContext, ContactUsActivity.class)));
        binding.linInfo.setOnClickListener(v -> startActivity(new Intent(mContext, InfoPageActivity.class)));


        binding.spinnerCountrycode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CountryCodePojoItem item = countryArrayList.get(position);
                selectedCountryCode = item.getCountryCode();
                selectedCountryCodePosition = item.getId();
                ((TextView) view).setText(item.getCountryCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.rgUserType.setOnCheckedChangeListener((group, checkedId) -> {
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
        });

        binding.txtSignin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
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
                    
                    if (getIntent().getStringExtra("SocialFlag") != null && getIntent().getStringExtra("SocialFlag").equals("true")) {
                        performSocialSignup();
                    } else {
                        performEmailSignup();
                    }
                    
                    PrefsUtil.with(mContext).write("countrycode", selectedCountryCode);
                    PrefsUtil.with(mContext).write("countrycodeid", selectedCountryCodePosition);
                } else {
                    Toast.makeText(mContext, getString(R.string.msg_terms_and_conditions), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Gestione dati pre-riempiti da Social Login
        if (getIntent().getStringExtra("SocialFlag") != null && getIntent().getStringExtra("SocialFlag").equals("true")) {
            binding.etSignupFname.setText(getIntent().getStringExtra("Fname"));
            binding.etSignupLname.setText(getIntent().getStringExtra("Lname"));
            binding.etSignupEmail.setText(getIntent().getStringExtra("Email"));
            binding.etSignupPassword.setVisibility(View.GONE);
            binding.etSignupConfirmpassword.setVisibility(View.GONE);
            binding.tilSignupPassword.setVisibility(View.GONE);
            binding.tilSignupConfirmPassword.setVisibility(View.GONE);
        }
        
        if(countryArrayList.isEmpty()){
            binding.spinnerCountrycode.setVisibility(View.GONE);
            binding.progressCountryCode.setVisibility(View.GONE);
        }
    }
    
    private void performEmailSignup() {
        binding.btnSubmit.setClickable(false);
        binding.progressSignUp.setVisibility(View.VISIBLE);

        String firstName = binding.etSignupFname.getText().toString().trim();
        String lastName = binding.etSignupLname.getText().toString().trim();
        String email = binding.etSignupEmail.getText().toString().trim();
        String contactNumber = binding.etSignupContactno.getText().toString().trim();
        String password = binding.etSignupPassword.getText().toString().trim();
        String rePassword = binding.etSignupConfirmpassword.getText().toString().trim();
        String deviceToken = PrefsUtil.with(SignupActivity.this).readString("device_token");

        // Use NewLoginPojo instead of RegistrationPojo as AppRepository unifies response types
        viewModel.signup(firstName, lastName, email, strUserType, contactNumber, password, rePassword, selectedCountryCodePosition, deviceToken)
            .observe(this, newLoginPojo -> {
                binding.progressSignUp.setVisibility(View.GONE);
                binding.btnSubmit.setClickable(true);
                
                if (newLoginPojo != null && newLoginPojo.isStatus()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setCancelable(false);
                    builder.setMessage(newLoginPojo.getMessage());
                    builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                    builder.create().show();
                } else {
                    String msg = newLoginPojo != null ? newLoginPojo.getMessage() : getString(R.string.server_error);
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void performSocialSignup() {
        binding.btnSubmit.setClickable(false);
        binding.progressSignUp.setVisibility(View.VISIBLE);

        String socialId = getIntent().getStringExtra("uId");
        String firstName = getIntent().getStringExtra("Fname");
        String lastName = getIntent().getStringExtra("Lname");
        String email = getIntent().getStringExtra("Email");
        String loginType = getIntent().getStringExtra("loginType");
        String contactNumber = binding.etSignupContactno.getText().toString().trim();
        String deviceToken = PrefsUtil.with(SignupActivity.this).readString("device_token");

        viewModel.socialLogin(firstName, lastName, email, loginType, socialId, null, null, "", deviceToken)
            .observe(this, newLoginPojo -> {
                binding.progressSignUp.setVisibility(View.GONE);
                binding.btnSubmit.setClickable(true);
                
                if (newLoginPojo != null && newLoginPojo.isStatus()) {
                    NewLoginPojoItem loginData = newLoginPojo.getData();

                    if (loginData.getIsUserActive() != null && loginData.getIsUserActive().equalsIgnoreCase("d")) {
                        PrefsUtil.with(SignupActivity.this).write("eMail", email);
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) ->
                                startActivity(new Intent(SignupActivity.this, ContactUsActivity.class))).setMessage(newLoginPojo.getMessage()).show();
                    } else {
                        PrefsUtil.with(SignupActivity.this).clearPrefs();
                        PrefsUtil.with(SignupActivity.this).write("UserId", loginData.getUserId());
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
                    }
                } else {
                    String msg = newLoginPojo != null ? newLoginPojo.getMessage() : getString(R.string.server_error);
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void textchangeListner() {
        binding.etSignupEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilSignupEmail.setErrorEnabled(false);
                binding.tilSignupEmail.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        binding.etSignupPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilSignupPassword.setErrorEnabled(false);
                binding.tilSignupPassword.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        binding.etSignupConfirmpassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilSignupConfirmPassword.setErrorEnabled(false);
                binding.tilSignupConfirmPassword.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private boolean checkValidation() {
        String email = binding.etSignupEmail.getText().toString().trim();
        String password = binding.etSignupPassword.getText().toString().trim();
        String confirmPassword = binding.etSignupConfirmpassword.getText().toString().trim();

        if (getIntent().getStringExtra("SocialFlag") != null && getIntent().getStringExtra("SocialFlag").equals("true")) {
            if (email.isEmpty()) {
                binding.tilSignupEmail.setErrorEnabled(true);
                binding.tilSignupEmail.setError(getString(R.string.error_required));
                binding.etSignupEmail.requestFocus();
                return false;
            } else if (!Utils.isEmailValid(email)) {
                binding.tilSignupEmail.setErrorEnabled(true);
                binding.tilSignupEmail.setError(getString(R.string.error_valid_email));
                binding.etSignupEmail.requestFocus();
                return false;
            }
            return true;
        } else {
            if (email.isEmpty()) {
                binding.tilSignupEmail.setErrorEnabled(true);
                binding.tilSignupEmail.setError(getString(R.string.error_required));
                binding.etSignupEmail.requestFocus();
                return false;
            } else if (!Utils.isEmailValid(email)) {
                binding.tilSignupEmail.setErrorEnabled(true);
                binding.tilSignupEmail.setError(getString(R.string.error_valid_email));
                binding.etSignupEmail.requestFocus();
                return false;
            } else if (password.isEmpty()) {
                binding.tilSignupPassword.setErrorEnabled(true);
                binding.tilSignupPassword.setError(getString(R.string.error_required));
                binding.etSignupPassword.requestFocus();
                return false;
            } else if (password.length() < 6) {
                binding.tilSignupPassword.setErrorEnabled(true);
                binding.tilSignupPassword.setError(getString(R.string.error_min_pwd));
                binding.etSignupPassword.requestFocus();
                return false;
            } else if (confirmPassword.isEmpty()) {
                binding.tilSignupConfirmPassword.setErrorEnabled(true);
                binding.tilSignupConfirmPassword.setError(getString(R.string.error_required));
                binding.etSignupConfirmpassword.requestFocus();
                return false;
            } else if (!confirmPassword.equals(password)) {
                binding.tilSignupConfirmPassword.setErrorEnabled(true);
                binding.tilSignupConfirmPassword.setError(getString(R.string.error_pwd_not_match));
                binding.etSignupConfirmpassword.requestFocus();
                return false;
            }
            return true;
        }
    }

    private void initView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        binding.etSignupPassword.setTransformationMethod(new PasswordTransformationMethod());
        binding.etSignupConfirmpassword.setTransformationMethod(new PasswordTransformationMethod());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
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
