package com.app.bemyrider.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.app.bemyrider.R;
import com.app.bemyrider.activity.partner.ProviderHomeActivity;
import com.app.bemyrider.activity.ContactUsActivity;
import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.activity.user.EditProfileActivity;
import com.app.bemyrider.databinding.ActivityLoginBinding;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.LoginViewModel;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    
    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private Context context = LoginActivity.this;
    private Activity activity = LoginActivity.this;
    private ConnectionManager connectionManager;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(activity, R.layout.activity_login, null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

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
        textchangeListner();

        binding.txtSignup.setOnClickListener(v -> {
            startActivity(new Intent(context, SignupActivity.class));
            finish();
        });

        binding.btnLogin.setOnClickListener(v -> {
            if (checkValidation()) {
                Utils.hideSoftKeyboard(activity);
                performLogin();
            }
        });

        binding.imgForgot.setOnClickListener(v -> showForgotPasswordDialog());
        binding.txtResendActivationMail.setOnClickListener(view -> showResendActivationDialog());

        binding.imgFb.setOnClickListener(v -> Toast.makeText(context, "Facebook login è disabilitato.", Toast.LENGTH_SHORT).show());
        binding.imgGoogle.setOnClickListener(v -> Toast.makeText(context, "Google login sarà modernizzato in seguito.", Toast.LENGTH_SHORT).show());
        binding.imgLinkdin.setOnClickListener(view -> Toast.makeText(context, "LinkedIn login sarà modernizzato in seguito.", Toast.LENGTH_SHORT).show());
    }

    private void showForgotPasswordDialog() {
        final Dialog d = new Dialog(context);
        d.setContentView(getLayoutInflater().inflate(R.layout.dialog_forgot_password, null));
        setupDialogWindow(d);

        ProgressBar progressBar = d.findViewById(R.id.progressBar);
        Button btn_resetPass = d.findViewById(R.id.btn_resetPass);
        final TextInputLayout til_uname = d.findViewById(R.id.til_uname);
        final EditText edt_uname = d.findViewById(R.id.edt_uname);

        setupDialogTextWatcher(edt_uname, til_uname);

        btn_resetPass.setOnClickListener(v1 -> {
            String email = edt_uname.getText().toString().trim();
            if (email.length() > 0) {
                if (Utils.isEmailValid(email)) {
                    Utils.hideSoftKeyboard(activity);
                    btn_resetPass.setClickable(false);
                    progressBar.setVisibility(View.VISIBLE);
                    
                    viewModel.forgotPassword(email).observe(this, response -> {
                        progressBar.setVisibility(View.GONE);
                        btn_resetPass.setClickable(true);
                        if (response != null) {
                            if (response.isStatus()) {
                                d.dismiss();
                                showSuccessDialog(response.getMessage());
                            } else {
                                if (response.getData() != null && "d".equalsIgnoreCase(response.getData().getIsUserActive())) {
                                    handleDeactivatedUser(email, response.getMessage());
                                } else {
                                    showErrorDialog(response.getMessage());
                                }
                            }
                        } else {
                            showErrorDialog(getString(R.string.server_error));
                        }
                    });
                } else {
                    til_uname.setErrorEnabled(true);
                    til_uname.setError(getString(R.string.error_valid_email));
                }
            } else {
                til_uname.setErrorEnabled(true);
                til_uname.setError(getString(R.string.error_required));
            }
        });
        d.show();
    }

    private void showResendActivationDialog() {
        final Dialog d = new Dialog(context);
        d.setContentView(getLayoutInflater().inflate(R.layout.dialog_forgot_password, null));
        setupDialogWindow(d);

        ProgressBar progressBar = d.findViewById(R.id.progressBar);
        Button btn_resetPass = d.findViewById(R.id.btn_resetPass);
        btn_resetPass.setText(R.string.resend_activation_mail);
        final TextInputLayout til_uname = d.findViewById(R.id.til_uname);
        til_uname.setHint(getString(R.string.email_id));
        final EditText edt_uname = d.findViewById(R.id.edt_uname);

        setupDialogTextWatcher(edt_uname, til_uname);

        btn_resetPass.setOnClickListener(v -> {
            String email = edt_uname.getText().toString().trim();
            if (email.length() > 0) {
                if (Utils.isEmailValid(email)) {
                    Utils.hideSoftKeyboard(activity);
                    btn_resetPass.setClickable(false);
                    progressBar.setVisibility(View.VISIBLE);
                    
                    viewModel.resendActivationMail(email).observe(this, response -> {
                        progressBar.setVisibility(View.GONE);
                        btn_resetPass.setClickable(true);
                        if (response != null) {
                            if (response.isStatus()) {
                                d.dismiss();
                                showSuccessDialog(response.getMessage());
                            } else {
                                if (response.getData() != null && "d".equalsIgnoreCase(response.getData().getIsUserActive())) {
                                    handleDeactivatedUser(email, response.getMessage());
                                } else {
                                    showErrorDialog(response.getMessage());
                                }
                            }
                        } else {
                            showErrorDialog(getString(R.string.server_error));
                        }
                    });
                } else {
                    til_uname.setErrorEnabled(true);
                    til_uname.setError(getString(R.string.error_valid_email));
                }
            } else {
                til_uname.setErrorEnabled(true);
                til_uname.setError(getString(R.string.error_required));
            }
        });
        d.show();
    }

    private void setupDialogWindow(Dialog d) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = d.getWindow();
        if (window != null) {
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private void setupDialogTextWatcher(EditText editText, TextInputLayout textInputLayout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayout.setError("");
                textInputLayout.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void handleDeactivatedUser(String email, String message) {
        PrefsUtil.with(activity).write("eMail", email);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) ->
                startActivity(new Intent(activity, ContactUsActivity.class))).setMessage(message).show();
    }

    private void showSuccessDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    private void performLogin() {
        binding.progressLogin.setVisibility(View.VISIBLE);
        binding.btnLogin.setClickable(false);

        String email = binding.etLoginUsername.getText().toString().trim();
        String password = binding.etLoginPassword.getText().toString().trim();
        String deviceToken = PrefsUtil.with(activity).readString("device_token");

        viewModel.login(email, password, deviceToken).observe(this, newLoginPojo -> {
            binding.progressLogin.setVisibility(View.GONE);
            binding.btnLogin.setClickable(true);
            
            if (newLoginPojo != null && newLoginPojo.isStatus()) {
                NewLoginPojoItem item = newLoginPojo.getData();

                if (item.getIsUserActive() != null && item.getIsUserActive().equalsIgnoreCase("d")) {
                    handleDeactivatedUser(email, newLoginPojo.getMessage());
                }
                else {
                    PrefsUtil.with(context).clearPrefs(); // manual login
                    PrefsUtil.with(context).write("isProfileCompleted", false);
                    PrefsUtil.with(context).write("UserId", item.getUserId());
                    PrefsUtil.with(context).write("countrycodeid", item.getCountryCodeId());
                    PrefsUtil.with(context).write("UserName", item.getUserName());
                    PrefsUtil.with(context).write("FirstName", item.getFirstName());
                    PrefsUtil.with(context).write("LastName", item.getLastName());
                    PrefsUtil.with(context).write("UserType", item.getUserType());
                    PrefsUtil.with(context).write("eMail", item.getEmailId());
                    PrefsUtil.with(context).write("Pass", binding.etLoginPassword.getText().toString().trim());
                    PrefsUtil.with(context).write("CurrencySign", getResources().getString(R.string.currency));
                    PrefsUtil.with(context).write("UserImg", item.getProfileImg());
                    PrefsUtil.with(activity).write("login_cust_address", item.getAddress());

                    if (item.getUserType().equals("c")) {
                        if (item.getFirstName().equals("") || item.getLastName().equals("") || item.getContactNumber().equals("")) {
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
                                item.getCompanyName().equals("") || item.getVat().equals("")) {
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
                }
            } else {
                showErrorDialog(newLoginPojo != null ? newLoginPojo.getMessage() : getString(R.string.server_error));
            }
        });
    }
    
    private void textchangeListner() {
        binding.etLoginUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilLoginUsername.setErrorEnabled(false);
                binding.tilLoginUsername.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        binding.etLoginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilLoginPassword.setErrorEnabled(false);
                binding.tilLoginPassword.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean checkValidation() {
        String strUsername = binding.etLoginUsername.getText().toString().trim();
        String strPassword = binding.etLoginPassword.getText().toString().trim();

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

    private void initView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        binding.etLoginPassword.setTransformationMethod(new PasswordTransformationMethod());
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
