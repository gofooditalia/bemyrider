package com.app.bemyrider.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.WebServices.WebServiceUrl;

import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;

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

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityAccountSettingBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.LanguagePojo;
import com.app.bemyrider.model.LanguagePojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.NotificationTestHelper;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;


import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 4/12/19.
 */

public class AccountSettingActivity extends AppCompatActivity {

    private ActivityAccountSettingBinding binding;
    private ArrayAdapter lanadapter;
    private ArrayList<LanguagePojoItem> languagePojoItems = new ArrayList<>();
    private String lanId = "";
    private AsyncTask getLanguageAsync, setPasswordAsync;
    private Context context;
    private ConnectionManager connectionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(AccountSettingActivity.this, R.layout.activity_account_setting, null);

        initViews();

        serviceCallGetLanguage();

        binding.btnSaveChange.setOnClickListener(view -> {
            if (checkValidation()) {
                Utils.hideSoftKeyboard(AccountSettingActivity.this);
                binding.btnSaveChange.setClickable(false);
                serviceCallChangePass();
            }

        });

        binding.spSelectLanSetting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PrefsUtil.with(AccountSettingActivity.this).write("lanId", languagePojoItems.get(i).getId());
                lanId = languagePojoItems.get(i).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.imgDown.setOnClickListener(v -> binding.spSelectLanSetting.performClick());

        // Pulsante di test notifiche push
        binding.btnTestNotification.setOnClickListener(v -> {
            NotificationTestHelper.testNotification(AccountSettingActivity.this);
            Toast.makeText(AccountSettingActivity.this, "Notifica di test inviata! Controlla il suono e la notifica.", Toast.LENGTH_LONG).show();
        });

        binding.btnContinueSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lanId.equals("2")) {
                    LocaleManager.setLocale(context, "fr");
                    Intent intent = new Intent(AccountSettingActivity.this, SplashScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else if (lanId.equals("3")) {
                    LocaleManager.setLocale(context, "pt");
                    Intent intent = new Intent(AccountSettingActivity.this, SplashScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else if (lanId.equals("4")) {
                    LocaleManager.setLocale(context, "it");
                    Intent intent = new Intent(AccountSettingActivity.this, SplashScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    LocaleManager.setLocale(context, "en");
                    Intent intent = new Intent(AccountSettingActivity.this, SplashScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });

        binding.etCurrentPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilCurrentPass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etNewPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilNewPass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etCnewPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilCnewPass.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.btnDeactivateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(AccountSettingActivity.this).create();
                alertDialog.setMessage(getString(R.string.confirm_account_deactivate));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.deactivate),
                        (dialog, which) -> {
                            dialog.dismiss();
                            binding.btnDeactivateAccount.setClickable(false);
                            serviceCallDeactivateAccount();
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
            }
        });
    }

    private void initViews() {
        context = AccountSettingActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.account_settings), HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding.etCurrentPass.setTransformationMethod(new PasswordTransformationMethod());
        binding.etNewPass.setTransformationMethod(new PasswordTransformationMethod());
        binding.etCnewPass.setTransformationMethod(new PasswordTransformationMethod());


        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        /*Init Select Language Spinner*/
        lanadapter = new ArrayAdapter<>(AccountSettingActivity.this, android.R.layout.simple_spinner_item, languagePojoItems);
        lanadapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        binding.spSelectLanSetting.setAdapter(lanadapter);


    }



    /*-------------- Get Language Api Call ------------------*/
    private void serviceCallGetLanguage() {
        binding.relLang.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        new WebServiceCall(AccountSettingActivity.this, WebServiceUrl.URL_GET_LANGUAGE,
                textParams, LanguagePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.relLang.setVisibility(View.VISIBLE);
                if (status) {
                    LanguagePojo languagePojo = (LanguagePojo) obj;
                    for (int i = 0; i < languagePojo.getData().size(); i++) {
                        Log.e("Splash", "onResult: " + languagePojo.getData().get(i).getLanguageName());
                        if (languagePojo.getData().get(i).getLanguageName().trim().equalsIgnoreCase("french") ||
                                languagePojo.getData().get(i).getLanguageName().trim().equalsIgnoreCase("english") ||
                                languagePojo.getData().get(i).getLanguageName().trim().equalsIgnoreCase("Portuguese")||
                                languagePojo.getData().get(i).getLanguageName().trim().equalsIgnoreCase("Italian")) {
                            languagePojoItems.add(languagePojo.getData().get(i));
                            lanadapter.notifyDataSetChanged();
                        }
                    }

                    for (int i = 0; i < languagePojoItems.size(); i++) {
                        if (languagePojoItems.get(i).getId().equals(PrefsUtil
                                .with(AccountSettingActivity.this).readString("lanId"))) {
                            binding.spSelectLanSetting.setSelection(i);
                            break;
                        }
                    }
                } else {
                    Toast.makeText(AccountSettingActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
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

    /*------------ DeActive Account Api Call ---------------*/
    private void serviceCallDeactivateAccount() {
        binding.pgDeActiveAccount.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(AccountSettingActivity.this).readString("UserId"));
        textParams.put("user_type", PrefsUtil.with(AccountSettingActivity.this).readString("UserType"));

        new WebServiceCall(AccountSettingActivity.this, WebServiceUrl.URL_DEACTIVATE_USER,
                textParams, CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgDeActiveAccount.setVisibility(View.GONE);
                binding.btnDeactivateAccount.setClickable(true);
                if (status) {
                    CommonPojo resultPojo = (CommonPojo) obj;
                    if (resultPojo.isStatus()) {
                        PrefsUtil.with(AccountSettingActivity.this).clearPrefs();
                        finish();
                        Intent i = new Intent(AccountSettingActivity.this,
                                SignupActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                } else {
                    Toast.makeText(AccountSettingActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
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

    /*--------------- Change Password Api Call --------------------*/
    private void serviceCallChangePass() {
        binding.pgSaveChange.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("currentpwd", binding.etCurrentPass.getText().toString().trim());
        textParams.put("newpwd", binding.etNewPass.getText().toString().trim());
        textParams.put("renewpwd", binding.etCnewPass.getText().toString().trim());
        textParams.put("user_id", PrefsUtil.with(AccountSettingActivity.this).readString("UserId"));

        new WebServiceCall(AccountSettingActivity.this, WebServiceUrl.URL_CHANGE_PASS,
                textParams, CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgSaveChange.setVisibility(View.GONE);
                binding.btnSaveChange.setClickable(true);
                if (status) {
                    Toast.makeText(AccountSettingActivity.this, ((CommonPojo) obj).getMessage(), Toast.LENGTH_SHORT).show();
                    PrefsUtil.with(AccountSettingActivity.this).write("Pass", binding.etNewPass.getText().toString().trim());
                    finish();
                } else {
                    Toast.makeText(AccountSettingActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                setPasswordAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                setPasswordAsync = null;
            }
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
        } else if (!binding.etNewPass.getText().toString().trim().equals(binding.etCnewPass.getText().toString().trim())) {
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
                // Permesso concesso, mostra la notifica
                NotificationTestHelper.testNotification(AccountSettingActivity.this);
                Toast.makeText(AccountSettingActivity.this, "Permesso concesso! Notifica di test inviata.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(AccountSettingActivity.this, "Permesso notifiche negato. Vai alle impostazioni per abilitarlo.", Toast.LENGTH_LONG).show();
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
        Utils.cancelAsyncTask(getLanguageAsync);
        Utils.cancelAsyncTask(setPasswordAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
