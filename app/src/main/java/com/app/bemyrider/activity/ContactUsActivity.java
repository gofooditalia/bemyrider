package com.app.bemyrider.activity;

import static com.app.bemyrider.utils.Utils.EMOJI_FILTER;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityContactUsBinding;
import com.app.bemyrider.model.partner.CountryCodePojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.ContactUsViewModel;

import java.util.ArrayList;

public class ContactUsActivity extends AppCompatActivity {

    private ActivityContactUsBinding binding;
    private Context context;
    private ConnectionManager connectionManager;
    private ContactUsViewModel viewModel;

    private ArrayList<CountryCodePojoItem> countryArrayList = new ArrayList<>();
    private ArrayAdapter<CountryCodePojoItem> countrycodeAdapter;
    private String selected_country_code = "";
    private String selected_country_code_position = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(ContactUsActivity.this, R.layout.activity_contact_us, null);

        initViews();

        viewModel = new ViewModelProvider(this).get(ContactUsViewModel.class);
        observeViewModel();
        viewModel.loadCountryCodes();

        binding.edtFnameConus.setText(PrefsUtil.with(this).readString("FirstName"));
        binding.edtLnameConus.setText(PrefsUtil.with(this).readString("LastName"));
        binding.edtEmailConus.setText(PrefsUtil.with(this).readString("eMail"));
        binding.edtFnameConus.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtLnameConus.setFilters(new InputFilter[]{EMOJI_FILTER});

        binding.btnSubmitConus.setOnClickListener(v -> {
            if (checkValidation()) {
                Utils.hideSoftKeyboard(ContactUsActivity.this);
                binding.btnSubmitConus.setClickable(false);
                viewModel.sendContactUs(
                        PrefsUtil.with(this).readString("UserId"),
                        binding.edtFnameConus.getText().toString().trim(),
                        binding.edtLnameConus.getText().toString().trim(),
                        binding.edtEmailConus.getText().toString().trim(),
                        binding.etSignupContactno.getText().toString().trim(),
                        selected_country_code_position,
                        Utils.encodeEmoji(binding.edtConus.getText().toString().trim())
                );
            }
        });

        binding.spinnerCountrycode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_country_code = countryArrayList.get(position).getCountryCode();
                selected_country_code_position = countryArrayList.get(position).getId();
                ((TextView) view).setText(countryArrayList.get(position).getCountryCode());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        clearErrorOnType(binding.edtFnameConus, () -> binding.tilFnameConus.setError(""));
        clearErrorOnType(binding.edtLnameConus, () -> binding.tilLnameConus.setError(""));
        clearErrorOnType(binding.edtEmailConus, () -> binding.tilEmailConus.setError(""));
        clearErrorOnType(binding.edtConus, () -> binding.tilConus.setError(""));
        clearErrorOnType(binding.etSignupContactno, () -> {
            binding.tilSignupContact.setErrorEnabled(false);
            binding.tilSignupContact.setError(null);
        });
    }

    private void observeViewModel() {
        viewModel.getCountryCodes().observe(this, codes -> {
            binding.progressCountryCode.setVisibility(View.GONE);
            binding.spinnerCountrycode.setVisibility(View.VISIBLE);
            if (codes != null) {
                countryArrayList.clear();
                countryArrayList.addAll(codes);
                countrycodeAdapter.notifyDataSetChanged();
            }
        });

        viewModel.getSendResult().observe(this, result -> {
            binding.pgSubmit.setVisibility(View.GONE);
            binding.btnSubmitConus.setClickable(true);
            if (result != null && result.isStatus()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                binding.edtConus.setText("");
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                binding.progressCountryCode.setVisibility(View.GONE);
                binding.spinnerCountrycode.setVisibility(View.VISIBLE);
                binding.pgSubmit.setVisibility(View.GONE);
                binding.btnSubmitConus.setClickable(true);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoadingCodes().observe(this, isLoading -> {
            if (isLoading) {
                binding.spinnerCountrycode.setVisibility(View.GONE);
                binding.progressCountryCode.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsSending().observe(this, isSending -> {
            if (isSending) {
                binding.pgSubmit.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initViews() {
        context = ContactUsActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.comtact_us), HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        countrycodeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countryArrayList);
        countrycodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCountrycode.setAdapter(countrycodeAdapter);
    }

    private void clearErrorOnType(android.widget.EditText editText, Runnable clearAction) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { clearAction.run(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private boolean checkValidation() {
        if (binding.edtFnameConus.getText().toString().trim().isEmpty()) {
            binding.tilFnameConus.setError(getString(R.string.error_required));
            binding.edtFnameConus.requestFocus();
            return false;
        } else if (binding.edtLnameConus.getText().toString().trim().isEmpty()) {
            binding.tilLnameConus.setError(getString(R.string.error_required));
            binding.edtLnameConus.requestFocus();
            return false;
        } else if (binding.edtEmailConus.getText().toString().trim().isEmpty()) {
            binding.tilEmailConus.setError(getString(R.string.error_required));
            binding.edtEmailConus.requestFocus();
            return false;
        } else if (!Utils.isEmailValid(binding.edtEmailConus.getText().toString().trim())) {
            binding.tilEmailConus.setError(getString(R.string.error_valid_email));
            binding.edtEmailConus.requestFocus();
            return false;
        } else if (binding.etSignupContactno.getText().toString().trim().isEmpty()) {
            binding.etSignupContactno.setError(getString(R.string.error_required));
            binding.etSignupContactno.requestFocus();
            return false;
        } else if (binding.etSignupContactno.getText().toString().trim().length() < 10
                || binding.etSignupContactno.getText().toString().trim().length() > 15) {
            binding.etSignupContactno.setError(getResources().getString(R.string.vali_contact_num));
            binding.etSignupContactno.requestFocus();
            return false;
        } else if (binding.edtConus.getText().toString().trim().isEmpty()) {
            binding.tilConus.setError(getString(R.string.error_required));
            binding.edtConus.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
