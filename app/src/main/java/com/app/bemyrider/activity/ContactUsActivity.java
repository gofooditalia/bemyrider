package com.app.bemyrider.activity;

import static com.app.bemyrider.utils.Utils.EMOJI_FILTER;

import android.content.Context;
import android.os.AsyncTask;
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

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityContactUsBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.partner.CountryCodePojo;
import com.app.bemyrider.model.partner.CountryCodePojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 7/12/19.
 */

public class ContactUsActivity extends AppCompatActivity {

    private ActivityContactUsBinding binding;
    private WebServiceCall contactUsAsync, countryCodeAsync;
    private Context context;
    private ConnectionManager connectionManager;

    private ArrayList<CountryCodePojoItem> countryArrayList = new ArrayList<>();
    private ArrayAdapter countrycodeAdapter;
    private String selected_country_code, selected_country_code_position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(ContactUsActivity.this, R.layout.activity_contact_us, null);

        initViews();

        serviceCallCountryCode();

        binding.edtFnameConus.setText(PrefsUtil.with(ContactUsActivity.this).readString("FirstName"));
        binding.edtLnameConus.setText(PrefsUtil.with(ContactUsActivity.this).readString("LastName"));
        binding.edtEmailConus.setText(PrefsUtil.with(ContactUsActivity.this).readString("eMail"));

        binding.edtFnameConus.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtFnameConus.setFilters(new InputFilter[]{EMOJI_FILTER});

        binding.btnSubmitConus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkValidation()) {
                    Utils.hideSoftKeyboard(ContactUsActivity.this);
                    binding.btnSubmitConus.setClickable(false);
                    serviceCallSendContactUs();
                }
            }
        });

        binding.edtFnameConus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilFnameConus.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtLnameConus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilLnameConus.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtEmailConus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilEmailConus.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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

        binding.edtConus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilConus.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
    }

    /*---------------- Send Contact Us Api Call -----------------*/
    private void serviceCallSendContactUs() {
        binding.pgSubmit.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("user_id", PrefsUtil.with(ContactUsActivity.this).readString("UserId"));
        textParams.put("email", binding.edtEmailConus.getText().toString().trim());
        textParams.put("message", Utils.encodeEmoji(binding.edtConus.getText().toString().trim()));
        textParams.put("firstName", binding.edtFnameConus.getText().toString().trim());
        textParams.put("lastName", binding.edtLnameConus.getText().toString().trim());
        textParams.put("contact_number", binding.etSignupContactno.getText().toString().trim());
        textParams.put("country_code", selected_country_code_position);

        new WebServiceCall(ContactUsActivity.this, WebServiceUrl.URL_SEND_CONTACTUS,
                textParams, CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgSubmit.setVisibility(View.GONE);
                binding.btnSubmitConus.setClickable(true);
                if (status) {
                    Toast.makeText(ContactUsActivity.this, ((CommonPojo) obj).getMessage(), Toast.LENGTH_SHORT).show();
                    binding.edtConus.setText("");
                } else {
                    Toast.makeText(ContactUsActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(Object obj) {
                // Added implementation for the missing abstract method onAsync(Object)
            }

            @Override
            public void onCancelled() {
                contactUsAsync = null;
            }
        });
    }

    /*----------------- Country Code Api Call -------------------*/
    private void serviceCallCountryCode() {
        binding.spinnerCountrycode.setVisibility(View.GONE);
        binding.progressCountryCode.setVisibility(View.VISIBLE);

        countryArrayList.clear();
        new WebServiceCall(context, WebServiceUrl.URL_COUNTRY_CODE, new LinkedHashMap<>(),
                CountryCodePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progressCountryCode.setVisibility(View.GONE);
                binding.spinnerCountrycode.setVisibility(View.VISIBLE);
                if (status) {
                    countryArrayList.addAll(((CountryCodePojo) obj).getData());
                    countrycodeAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(Object obj) {
                // Added implementation for the missing abstract method onAsync(Object)
            }

            @Override
            public void onCancelled() {
                countryCodeAsync = null;
            }
        });

    }

    private boolean checkValidation() {
        if (binding.edtFnameConus.getText().toString().trim().equals("")) {
            binding.tilFnameConus.setError(getString(R.string.error_required));
            binding.edtFnameConus.requestFocus();
            return false;
        } else if (binding.edtLnameConus.getText().toString().trim().equals("")) {
            binding.tilLnameConus.setError(getString(R.string.error_required));
            binding.edtLnameConus.requestFocus();
            return false;
        } else if (binding.edtEmailConus.getText().toString().trim().equals("")) {
            binding.tilEmailConus.setError(getString(R.string.error_required));
            binding.edtEmailConus.requestFocus();
            return false;
        } else if (!Utils.isEmailValid(binding.edtEmailConus.getText().toString().trim())) {
            binding.tilEmailConus.setError(getString(R.string.error_valid_email));
            binding.edtEmailConus.requestFocus();
            return false;
        }  else if (binding.etSignupContactno.getText().toString().trim().isEmpty()) {
            binding.etSignupContactno.setError(getString(R.string.error_required));
            binding.etSignupContactno.requestFocus();
            return false;
        } else if (binding.etSignupContactno.getText().toString().trim().length() < 10 || binding.etSignupContactno.getText().toString().trim().length() > 15) {
            binding.etSignupContactno.setError(getResources().getString(R.string.vali_contact_num));
            binding.etSignupContactno.requestFocus();
            return false;
        } else if (binding.edtConus.getText().toString().trim().equals("")) {
            binding.tilConus.setError(getString(R.string.error_required));
            binding.edtConus.requestFocus();
            return false;
        }
        return true;
    }

    private void initViews() {
        context = ContactUsActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.comtact_us),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        /*Init Country Code Spinner*/
        countrycodeAdapter = new ArrayAdapter<>(ContactUsActivity.this, android.R.layout.simple_spinner_item, countryArrayList);
        binding.spinnerCountrycode.setAdapter(countrycodeAdapter);
        countrycodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        Utils.cancelAsyncTask(contactUsAsync);
        Utils.cancelAsyncTask(countryCodeAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}