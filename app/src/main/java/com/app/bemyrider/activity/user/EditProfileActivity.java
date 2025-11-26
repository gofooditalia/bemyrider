package com.app.bemyrider.activity.user;

import static com.app.bemyrider.utils.Utils.EMOJI_FILTER;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityEditProfileBinding;
import com.app.bemyrider.helper.PermissionUtils;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.model.FileUtilPOJO;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.model.partner.CountryCodePojo;
import com.app.bemyrider.model.partner.CountryCodePojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.FileUtils;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Modified by Hardik Talaviya on 10/12/19.
 */

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfile_User";

    private Context mContext;
    private Activity mActivity;

    private PermissionUtils permissionUtils;

    private ActivityResultLauncher<Uri> actResCamera;
    private ActivityResultLauncher<Intent> actResGallery;
    private ActivityResultLauncher<Intent> actResCropper;
    private ActivityResultLauncher<Intent> actResLocation;

    public static ProfileItem profileData = null;
    private ActivityEditProfileBinding binding;
    private Geocoder geocoder;
    private Uri mCropImageUri, resultUri;
    private String selectedImagePath = "";
    private String countryCodeId = "";
    private ArrayList<CountryCodePojoItem> countrycodeArrayList = new ArrayList<>();
    private ArrayAdapter countrycodeAdapter;
    private AsyncTask countryCodeAsync, updateProfileAsync;
    private ConnectionManager connectionManager;

    private boolean isFromEdit = false;
    private NewLoginPojoItem loginPojoData = null;
    private String lat = "", lng = "";

    /*private ActivityResultLauncher<Intent> actResult;
    private boolean isSignatureSelect = false;
    private String selectedSignatureImagePath = "";*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(EditProfileActivity.this, R.layout.activity_edit_profile, null);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mContext = binding.getRoot().getContext();
        mActivity = EditProfileActivity.this;

        isFromEdit = getIntent().getBooleanExtra("isFromEdit", false);
        if (!isFromEdit) {
            loginPojoData = (NewLoginPojoItem) getIntent().getSerializableExtra("loginPojoData");
        }

        permissionUtils = new PermissionUtils(mActivity, mContext, new PermissionUtils.OnPermissionGrantedListener() {
            @Override
            public void onCameraPermissionGranted() {
                selectedImagePath = Utils.openCamera(mContext, actResCamera);
            }

            @Override
            public void onStoragePermissionGranted() {
                selectedImagePath = "";
                Utils.openImagesDocument(actResGallery);
            }
        });

        init();
        fillData();

        geocoder = new Geocoder(EditProfileActivity.this, Locale.getDefault());
        binding.txtAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ID,
                        Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS))
                        .build(EditProfileActivity.this);
                actResLocation.launch(intent);
            }
        });


        getCountryCode();

        binding.spCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                countryCodeId = countrycodeArrayList.get(position).getId();
                ((TextView) view).setText(countrycodeArrayList.get(position).getCountryCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.ivProfile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                openCameraGalleryDialog();
            }
        });

        binding.rgPaymentMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case 0:
                        binding.rbWallet.setSelected(true);
                        break;
                    case 1:
                        binding.rbCash.setSelected(true);
                        break;
                }
            }
        });

//        binding.imgAddSignature.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                isSignatureSelect = true;
//                checkPermission();  // select signature
//
//                /*Intent i = new Intent(mContext, AddSignatureActivity.class);
//                actResult.launch(i);*/
//            }
//        });

        binding.BtnUpdateprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkValidation()) {
                    binding.BtnUpdateprofile.setClickable(false);
                    updateProfile();
                }
            }

        });

        binding.edtFname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilFname.setError("");
                binding.tilFname.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtLname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilLname.setError("");
                binding.tilLname.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilEmail.setError("");
                binding.tilEmail.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilNumber.setError("");
                binding.tilNumber.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.txtAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilAddress.setError("");
                binding.tilAddress.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtCityOfCompany.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilCityOfCompany.setError("");
                binding.tilCityOfCompany.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

       /* binding.edtCompany.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilCompany.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtVat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilVat.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtTaxIdCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilTaxIdCode.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtInvoiceRecipeCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilInvoiceRecipeCode.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtCertifiedMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilCertifiedMail.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/
    }

    private void fillData() {
        if (isFromEdit) {
            binding.edtFname.setText(profileData.getFirstName());
            binding.edtLname.setText(profileData.getLastName());
            binding.edtNumber.setText(profileData.getContactNumber());
            binding.edtEmail.setText(profileData.getEmail());
            binding.txtAddress.setText(profileData.getAddress());
            binding.edtCityOfCompany.setText(profileData.getCity_of_company());

            lat = profileData.getLatitude();
            lng = profileData.getLongitude();

            binding.edtCompany.setText(profileData.getCompanyName());
            binding.edtVat.setText(profileData.getVat());
            // binding.edtTaxIdCode.setText(profileData.getTaxId());
            binding.edtInvoiceRecipeCode.setText(profileData.getReceiptCode());
            binding.edtCertifiedMail.setText(profileData.getCertifiedEmail());

            if (profileData.getPaymentMode().equalsIgnoreCase("w")) {
                binding.rbWallet.setSelected(true);
            } else {
                binding.rbCash.setSelected(true);
            }

            if (profileData.getProfileImg() != null && profileData.getProfileImg().length() > 0) {
                Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.ivProfile);
            } else {
                Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.ivProfile);
            }

            if (profileData.getPaymentMode().length() > 0) {
                if (profileData.getPaymentMode().equalsIgnoreCase("c")) {
                    binding.rbCash.setChecked(true);
                } else {
                    binding.rbWallet.setChecked(true);
                }
            }

        } else {
            binding.edtFname.setText(loginPojoData.getFirstName());
            binding.edtLname.setText(loginPojoData.getLastName());
            binding.edtNumber.setText(loginPojoData.getContactNumber());
            binding.edtEmail.setText(loginPojoData.getEmailId());
            binding.txtAddress.setText(loginPojoData.getAddress());

            binding.edtCompany.setText(loginPojoData.getCompanyName());
            binding.edtVat.setText(loginPojoData.getVat());
            // binding.edtTaxIdCode.setText(loginPojoData.getTaxId());
            binding.edtInvoiceRecipeCode.setText(loginPojoData.getReceiptCode());
            binding.edtCertifiedMail.setText(loginPojoData.getCertifiedEmail());

            lat = loginPojoData.getLatitude();
            lng = loginPojoData.getLongitude();

            /*if (loginPojoData.getPaymentMode().equalsIgnoreCase("w")) {
                binding.rbWallet.setSelected(true);
            } else {
                binding.rbCash.setSelected(true);
            }*/

            if (loginPojoData.getProfileImg() != null && loginPojoData.getProfileImg().length() > 0) {
                Picasso.get().load(loginPojoData.getProfileImg()).placeholder(R.drawable.loading).into(binding.ivProfile);
            } else {
                Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.ivProfile);
            }

            /*if (loginPojoData.getPaymentMode().length() > 0) {
                if (profileData.getPaymentMode().equalsIgnoreCase("c")) {
                    binding.rbCash.setChecked(true);
                } else {
                    binding.rbWallet.setChecked(true);
                }
            }*/
        }
    }

    protected void init() {
        if (isFromEdit) {
            if (profileData == null) {
                finish();
            }
        } else {
            if (loginPojoData == null) {
                finish();
            }
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        binding.edtFname.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtLname.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtVat.setFilters(new InputFilter[]{EMOJI_FILTER});
        // binding.edtTaxIdCode.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtInvoiceRecipeCode.setFilters(new InputFilter[]{EMOJI_FILTER});

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));
        }

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.edit_profile),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        countrycodeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countrycodeArrayList);
        countrycodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCode.setAdapter(countrycodeAdapter);

        initActivityResult();
    }

    private void initActivityResult() {
        actResCamera = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    Uri uri = Uri.parse(selectedImagePath);
                    openCropActivity(uri, uri);
                }
            }
        });

        actResGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        try {
                            Uri sourceUri = data.getData();
                            File file = Utils.createTempFileInAppPackage(mContext);
                            Uri destinationUri = Uri.fromFile(file);
                            openCropActivity(sourceUri, destinationUri);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: " + e.getMessage());
                        }
                    }
                });

        actResCropper = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent data = result.getData();
                try {
                    Uri uri = UCrop.getOutput(data);
                    showImage(uri);
                } catch (Exception e) {
                    Log.e(TAG, "onActivityResult: " + e.getMessage());
                }
            }
        });

        actResLocation = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                try {
                    if (result.getResultCode() == RESULT_OK) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        Log.i("AUTO COMPLETE", "Place: " + place.getName() + ", " + place.getId());
                        try {
                            binding.txtAddress.setText(place.getAddress());
                            lat = String.valueOf(place.getLatLng().latitude);
                            lng = String.valueOf(place.getLatLng().longitude);
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                        }
                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                        // TODO: Handle the error.
                        Status status = Autocomplete.getStatusFromIntent(result.getData());
                        Log.i("AUTO COMPLETE", status.getStatusMessage());
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        // The user canceled the operation.
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    /*-------------- Get Country Code Api Call ------------------*/
    protected void getCountryCode() {
        binding.spCode.setVisibility(View.GONE);
        binding.pgCountryCode.setVisibility(View.VISIBLE);

        countrycodeArrayList.clear();
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        new WebServiceCall(this, WebServiceUrl.URL_COUNTRY_CODE, textParams,
                CountryCodePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgCountryCode.setVisibility(View.GONE);
                binding.spCode.setVisibility(View.VISIBLE);
                if (status) {
                    CountryCodePojo countryCodePojo = (CountryCodePojo) obj;
                    countrycodeArrayList.addAll(countryCodePojo.getData());
                    countrycodeAdapter.notifyDataSetChanged();

                    if (getIntent().getStringExtra("Edit") != null && getIntent().getStringExtra("Edit").equals("true")) {
                        for (int i = 0; i < countrycodeArrayList.size(); i++) {
                            if (countrycodeArrayList.get(i).getId().equals(PrefsUtil.with(EditProfileActivity.this).readString("countrycodeid"))) {
                                binding.spCode.setSelection(i);
                                break;
                            }

                        }
                    }

                } else {
                    Toast.makeText(EditProfileActivity.this, (String) obj,
                            Toast.LENGTH_SHORT).show();
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

    /*----------------- Check Validation ----------------------*/
    protected boolean checkValidation() {
        if (!(binding.edtFname.getText().toString().length() > 0)) {
            binding.tilFname.setErrorEnabled(true);
            binding.tilFname.setError(getResources().getString(R.string.provide_fname));
            binding.edtFname.requestFocus();
            return false;
        } else if (!(binding.edtLname.getText().toString().length() > 0)) {
            binding.tilLname.setErrorEnabled(true);
            binding.tilLname.setError(getResources().getString(R.string.provide_lname));
            binding.edtLname.requestFocus();
            return false;
        } else if (!(binding.edtEmail.getText().toString().length() > 0)) {
            binding.tilEmail.setErrorEnabled(true);
            binding.tilEmail.setError(getResources().getString(R.string.provide_uname));
            binding.edtEmail.requestFocus();
            return false;
        } else if (!(Utils.isEmailValid(binding.edtEmail.getText().toString()))) {
            binding.tilEmail.setErrorEnabled(true);
            binding.tilEmail.setError(getResources().getString(R.string.provide_valid_uname));
            binding.edtEmail.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtNumber.getText().toString().trim())) {
            binding.edtNumber.setError(getResources().getString(R.string.provide_number));
            binding.edtNumber.requestFocus();
            return false;
        } else if (binding.edtNumber.getText().toString().trim().length() < 10 || binding.edtNumber.getText().toString().trim().length() > 15) {
            binding.edtNumber.setError(getResources().getString(R.string.vali_contact_num));
            binding.edtNumber.requestFocus();
            return false;
        } else if (countryCodeId.equals("")) {
            Toast.makeText(EditProfileActivity.this, R.string.provide_country_code_id, Toast.LENGTH_LONG).show();
            return false;
        }
        /*else if (TextUtils.isEmpty(binding.edtCompany.getText().toString().trim())) {
            binding.tilCompany.setErrorEnabled(true);
            binding.tilCompany.setError(getResources().getString(R.string.err_msg_company));
            binding.tilCompany.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtVat.getText().toString().trim())) {
            binding.tilVat.setErrorEnabled(true);
            binding.tilVat.setError(getResources().getString(R.string.err_msg_vat));
            binding.tilVat.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtTaxIdCode.getText().toString().trim())) {
            binding.tilTaxIdCode.setErrorEnabled(true);
            binding.tilTaxIdCode.setError(getResources().getString(R.string.err_msg_tax_id_code));
            binding.tilTaxIdCode.requestFocus();
            return false;
        }*/
        else if (TextUtils.isEmpty(binding.txtAddress.getText().toString().trim())) {
            binding.tilAddress.setErrorEnabled(true);
            binding.tilAddress.setError(getResources().getString(R.string.provide_address));
            binding.txtAddress.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtCityOfCompany.getText().toString().trim())) {
            binding.edtCityOfCompany.setError(getResources().getString(R.string.err_msg_city_of_company));
            binding.edtCityOfCompany.requestFocus();
            return false;
        }
        /*else if (TextUtils.isEmpty(binding.edtInvoiceRecipeCode.getText().toString().trim())) {
            binding.tilInvoiceRecipeCode.setErrorEnabled(true);
            binding.tilInvoiceRecipeCode.setError(getResources().getString(R.string.err_msg_invoice_recipe));
            binding.tilInvoiceRecipeCode.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtCertifiedMail.getText().toString().trim())) {
            binding.tilCertifiedMail.setErrorEnabled(true);
            binding.tilCertifiedMail.setError(getResources().getString(R.string.err_msg_certified_email));
            binding.tilCertifiedMail.requestFocus();
            return false;
        } else if (!(Utils.isEmailValid(binding.edtCertifiedMail.getText().toString()))) {
            binding.tilCertifiedMail.setErrorEnabled(true);
            binding.tilCertifiedMail.setError(getResources().getString(R.string.provide_valid_uname));
            binding.tilCertifiedMail.requestFocus();
            return false;
        } */
        else {
            return true;
        }
    }

    /*-------------- Update Profile Api Call -----------------*/
    protected void updateProfile() {
        binding.pgUpdateProfile.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        LinkedHashMap<String, File> fileParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(EditProfileActivity.this).readString("UserId"));
        textParams.put("firstName", binding.edtFname.getText().toString().trim());
        textParams.put("lastName", binding.edtLname.getText().toString().trim());

        if (!selectedImagePath.isEmpty()) {
            File imageFile = new File(selectedImagePath);
            if (imageFile.exists()) {
                Log.e(TAG, "Adding profile image: " + selectedImagePath + " (size: " + imageFile.length() + " bytes)");
                fileParams.put("profile_pic", imageFile);
            } else {
                Log.e(TAG, "Profile image file NOT FOUND: " + selectedImagePath);
                Toast.makeText(EditProfileActivity.this, "Immagine non trovata. Riprova a selezionare l'immagine.", Toast.LENGTH_SHORT).show();
            }
        }
        textParams.put("contact_number", binding.edtNumber.getText().toString().trim());
        textParams.put("country_code", countryCodeId);
        textParams.put("city_of_company", binding.edtCityOfCompany.getText().toString().trim());
        textParams.put("address", binding.txtAddress.getText().toString().trim());
        textParams.put("payment_mode", (binding.rgPaymentMethod.getCheckedRadioButtonId() == R.id.rb_cash) ? "c" : "w");
        String str = (binding.rgPaymentMethod.getCheckedRadioButtonId() == R.id.rb_cash) ? "c" : "w";
        Log.e("Edit ", "updateProfile: " + str);
        textParams.put("company_name", binding.edtCompany.getText().toString().trim());
        textParams.put("vat", binding.edtVat.getText().toString().trim());
        //textParams.put("tax_id", binding.edtTaxIdCode.getText().toString().trim());
        textParams.put("certified_email", binding.edtCertifiedMail.getText().toString().trim());
        textParams.put("receipt_code", binding.edtInvoiceRecipeCode.getText().toString().trim());

        textParams.put("latitude", lat);
        textParams.put("longitude", lng);

        new WebServiceCall(EditProfileActivity.this, WebServiceUrl.URL_EDIT_PROFILE,
                textParams, fileParams, ProfilePojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.pgUpdateProfile.setVisibility(View.GONE);
                        binding.BtnUpdateprofile.setClickable(true);

                        if (status) {
                            ProfilePojo data = (ProfilePojo) obj;

                            PrefsUtil.with(mContext).write("isProfileCompleted", true);
                            PrefsUtil.with(mContext).write("UserName", binding.edtFname.getText().toString().trim() + " " + binding.edtLname.getText().toString().trim());
                            PrefsUtil.with(mContext).write("login_cust_address", binding.txtAddress.getText().toString().trim());
                            PrefsUtil.with(mContext).write("UserImg", data.getData().getProfileImg());
                            PrefsUtil.with(mContext).write("countrycodeid", countryCodeId);

                            Intent intent;
                            if (isFromEdit) {
                                intent = new Intent();
                                setResult(RESULT_OK, intent);
                            } else {
                                intent = new Intent(mContext, CustomerHomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                            finish();
                        } else {
                            Toast.makeText(EditProfileActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        updateProfileAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        updateProfileAsync = null;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private void permissionMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setMessage(getString(R.string.cancelling_granted)).show();
    }

    private void openCameraGalleryDialog() {
        final Dialog d = new Dialog(EditProfileActivity.this);
        d.setContentView(getLayoutInflater().inflate(R.layout.dialog_camera_gallery, null));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = d.getWindow();
        lp.copyFrom(window.getAttributes());
        // This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        LinearLayoutCompat linCamera = d.findViewById(R.id.linCamera);
        LinearLayoutCompat linGallery = d.findViewById(R.id.linGallery);

        linCamera.setOnClickListener(view -> {
            d.dismiss();
            permissionUtils.checkCameraPermission();
        });

        linGallery.setOnClickListener(view -> {
            d.dismiss();
            permissionUtils.checkStoragePermission();
        });
        d.show();
    }


    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setActiveControlsWidgetColor(ContextCompat.getColor(mContext, R.color.button));
        options.setToolbarTitle("Edit Photo");
        options.setStatusBarColor(ContextCompat.getColor(mContext, R.color.white));
        options.setToolbarColor(ContextCompat.getColor(mContext, R.color.white));
        options.setToolbarWidgetColor(ContextCompat.getColor(mContext, R.color.button));
        Intent myIntent = UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .withAspectRatio(5f, 5f)
                .getIntent(mContext);
        actResCropper.launch(myIntent);
    }

    private void showImage(Uri imageUri) {
        try {
            File file;
            FileUtilPOJO fileUtils = FileUtils.getPath(mContext, imageUri);
            file = new File(fileUtils.getPath());
            InputStream inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            binding.ivProfile.setImageBitmap(bitmap);
            selectedImagePath = file.getAbsolutePath();
            PrefsUtil.with(EditProfileActivity.this).write("UserImg", selectedImagePath);

        } catch (Exception e) {
            Log.e(TAG, "showImage: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_CODE_CAMERA) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastMaster.showShort(mContext, R.string.err_permission_camera);
            } else {
                permissionUtils.checkCameraPermission();
            }
        } else if (requestCode == PermissionUtils.REQ_CODE_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastMaster.showShort(mContext, R.string.err_permission_storage);
            } else {
                permissionUtils.checkStoragePermission();
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
        Utils.cancelAsyncTask(countryCodeAsync);
        Utils.cancelAsyncTask(updateProfileAsync);

        /** clear cache dir of picture which is taken photo from camera */
        Utils.clearCameraCache(mContext);

        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }

}
