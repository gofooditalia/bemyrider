package com.app.bemyrider.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityAccountSettingBinding;
import com.app.bemyrider.model.LanguagePojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.NotificationTestHelper;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.SecurePrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.AccountSettingViewModel;

import java.util.ArrayList;

/**
 * Modified by Hardik Talaviya on 4/12/19.
 * Modernized by Gemini on 2024.
 */

public class AccountSettingActivity extends AppCompatActivity {

    private ActivityAccountSettingBinding binding;
    private AccountSettingViewModel viewModel;
    private ArrayAdapter<LanguagePojoItem> lanadapter;
    private ArrayList<LanguagePojoItem> languagePojoItems = new ArrayList<>();
    private String lanId = "";
    private Context context;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(AccountSettingActivity.this, R.layout.activity_account_setting, null);
        viewModel = new ViewModelProvider(this).get(AccountSettingViewModel.class);

        initViews();
        setupObservers();

        binding.relLang.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        binding.btnSaveChange.setOnClickListener(view -> {
            if (checkValidation()) {
                Utils.hideSoftKeyboard(AccountSettingActivity.this);
                binding.btnSaveChange.setClickable(false);
                performChangePassword();
            }
        });

        binding.spSelectLanSetting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i >= 0 && i < languagePojoItems.size()) {
                    SecurePrefsUtil.with(AccountSettingActivity.this).write("lanId", languagePojoItems.get(i).getId());
                    lanId = languagePojoItems.get(i).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.imgDown.setOnClickListener(v -> binding.spSelectLanSetting.performClick());

        // Pulsante di test notifiche push
        binding.btnTestNotification.setOnClickListener(v -> {
            NotificationTestHelper.testNotification(AccountSettingActivity.this);
            Toast.makeText(AccountSettingActivity.this, "Notifica di test inviata! Controlla il suono e la notifica.",
                    Toast.LENGTH_LONG).show();
        });

        binding.btnContinueSetting.setOnClickListener(view -> {
            if (lanId.equals("2")) {
                LocaleManager.setLocale(context, "fr");
            } else if (lanId.equals("3")) {
                LocaleManager.setLocale(context, "pt");
            } else if (lanId.equals("4")) {
                LocaleManager.setLocale(context, "it");
            } else {
                LocaleManager.setLocale(context, "en");
            }
            
            Intent intent = new Intent(AccountSettingActivity.this, SplashScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        setupTextWatchers();

        binding.btnDeactivateAccount.setOnClickListener(view -> {
            AlertDialog alertDialog = new AlertDialog.Builder(AccountSettingActivity.this).create();
            alertDialog.setMessage(getString(R.string.confirm_account_deactivate));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.deactivate),
                    (dialog, which) -> {
                        dialog.dismiss();
                        binding.btnDeactivateAccount.setClickable(false);
                        performDeactivateAccount();
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        });
    }

    private void initViews() {
        context = AccountSettingActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.account_settings),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding.etCurrentPass.setTransformationMethod(new PasswordTransformationMethod());
        binding.etNewPass.setTransformationMethod(new PasswordTransformationMethod());
        binding.etCnewPass.setTransformationMethod(new PasswordTransformationMethod());

        /* Init Internet Connection Class For No Internet Banner */
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        /* Init Select Language Spinner */
        lanadapter = new ArrayAdapter<>(AccountSettingActivity.this, android.R.layout.simple_spinner_item,
                languagePojoItems);
        lanadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spSelectLanSetting.setAdapter(lanadapter);
    }

    private void setupObservers() {
        viewModel.getLanguages().observe(this, languagePojo -> {
            binding.progress.setVisibility(View.GONE);
            binding.relLang.setVisibility(View.VISIBLE);
            if (languagePojo != null && languagePojo.isStatus()) {
                languagePojoItems.clear();
                for (int i = 0; i < languagePojo.getData().size(); i++) {
                    String langName = languagePojo.getData().get(i).getLanguageName().trim();
                    Log.e("Splash", "onResult: " + langName);
                    if (langName.equalsIgnoreCase("french") ||
                            langName.equalsIgnoreCase("english") ||
                            langName.equalsIgnoreCase("Portuguese") ||
                            langName.equalsIgnoreCase("Italian")) {
                        languagePojoItems.add(languagePojo.getData().get(i));
                    }
                }
                lanadapter.notifyDataSetChanged();

                for (int i = 0; i < languagePojoItems.size(); i++) {
                    if (languagePojoItems.get(i).getId().equals(SecurePrefsUtil.with(AccountSettingActivity.this).readString("lanId"))) {
                        binding.spSelectLanSetting.setSelection(i);
                        break;
                    }
                }
            } else {
                Toast.makeText(AccountSettingActivity.this, "Error fetching languages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performChangePassword() {
        binding.pgSaveChange.setVisibility(View.VISIBLE);

        String currentPwd = binding.etCurrentPass.getText().toString().trim();
        String newPwd = binding.etNewPass.getText().toString().trim();
        String reNewPwd = binding.etCnewPass.getText().toString().trim();
        String userId = SecurePrefsUtil.with(AccountSettingActivity.this).readString("UserId");

        viewModel.changePassword(currentPwd, newPwd, reNewPwd, userId).observe(this, commonPojo -> {
            binding.pgSaveChange.setVisibility(View.GONE);
            binding.btnSaveChange.setClickable(true);

            if (commonPojo != null) {
                if (commonPojo.isStatus()) {
                    Toast.makeText(AccountSettingActivity.this, commonPojo.getMessage(), Toast.LENGTH_SHORT).show();
                    
                    SecurePrefsUtil.with(AccountSettingActivity.this).write("Pass", newPwd);
                    PrefsUtil.with(AccountSettingActivity.this).write("Pass", newPwd);
                    finish();
                } else {
                    Toast.makeText(AccountSettingActivity.this, commonPojo.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AccountSettingActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performDeactivateAccount() {
        binding.pgDeActiveAccount.setVisibility(View.VISIBLE);

        String userId = SecurePrefsUtil.with(AccountSettingActivity.this).readString("UserId");
        String userType = SecurePrefsUtil.with(AccountSettingActivity.this).readString("UserType");

        viewModel.deactivateAccount(userId, userType).observe(this, commonPojo -> {
            binding.pgDeActiveAccount.setVisibility(View.GONE);
            binding.btnDeactivateAccount.setClickable(true);

            if (commonPojo != null) {
                if (commonPojo.isStatus()) {
                    SecurePrefsUtil.with(AccountSettingActivity.this).clearPrefs();
                    PrefsUtil.with(AccountSettingActivity.this).clearPrefs();
                    finish();
                    Intent i = new Intent(AccountSettingActivity.this, SignupActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                } else {
                    Toast.makeText(AccountSettingActivity.this, commonPojo.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AccountSettingActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTextWatchers() {
        binding.etCurrentPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilCurrentPass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        binding.etNewPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilNewPass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        binding.etCnewPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilCnewPass.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private boolean checkValidation() {
        if (binding.etCurrentPass.getText().toString().trim().equals("")) {
            binding.tilCurrentPass.setError(getString(R.string.please_enter_current_pass));
            return false;
        } else if (binding.etCurrentPass.getText().toString().trim().length() < 6) {
            binding.tilCurrentPass.setError(getString(R.string.please_enter_valid_password));
            return false;
        } else if (binding.etNewPass.getText().toString().trim().equals("")) {
            binding.tilNewPass.setError(getString(R.string.please_enter_new_pass));
            return false;
        } else if (binding.etNewPass.getText().toString().trim().length() < 6) {
            binding.tilNewPass.setError(getString(R.string.please_enter_valid_password));
            return false;
        } else if (binding.etCnewPass.getText().toString().trim().equals("")) {
            binding.tilCnewPass.setError(getString(R.string.please_enter_confirm_pass));
            return false;
        } else if (!binding.etNewPass.getText().toString().trim()
                .equals(binding.etCnewPass.getText().toString().trim())) {
            binding.tilCnewPass.setError(getString(R.string.password_miss_match));
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) { // POST_NOTIFICATIONS permission
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                NotificationTestHelper.testNotification(AccountSettingActivity.this);
                Toast.makeText(AccountSettingActivity.this, "Permesso concesso! Notifica di test inviata.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(AccountSettingActivity.this,
                        "Permesso notifiche negato. Vai alle impostazioni per abilitarlo.", Toast.LENGTH_LONG).show();
            }
        }
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
