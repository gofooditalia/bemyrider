package com.app.bemyrider.activity.user;

import static com.app.bemyrider.utils.Utils.EMOJI_FILTER;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityEditProfileBinding;
import com.app.bemyrider.helper.PermissionUtils;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.model.FileUtilPOJO;
import com.app.bemyrider.model.NewLoginPojoItem;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.partner.CountryCodePojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.FileUtils;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.EditProfileViewModel;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String selectedImagePath = "";
    private String countryCodeId = "";
    private ArrayList<CountryCodePojoItem> countrycodeArrayList = new ArrayList<>();
    private ArrayAdapter<CountryCodePojoItem> countrycodeAdapter;
    private ConnectionManager connectionManager;
    private boolean isFromEdit = false;
    private NewLoginPojoItem loginPojoData = null;
    private String lat = "", lng = "";
    private EditProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);
        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);
        
        mContext = this;
        mActivity = this;

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
        setupObservers();
        setupListeners();
        setupTextWatchers();
    }

    private void setupObservers() {
        viewModel.getCountryCodes().observe(this, countryCodePojo -> {
            binding.pgCountryCode.setVisibility(View.GONE);
            binding.spCode.setVisibility(View.VISIBLE);
            if (countryCodePojo != null && countryCodePojo.isStatus()) {
                countrycodeArrayList.clear();
                countrycodeArrayList.addAll(countryCodePojo.getData());
                countrycodeAdapter.notifyDataSetChanged();

                String targetCode = null;
                if (isFromEdit && profileData != null) {
                    targetCode = profileData.getCountryCode();
                } else if (!isFromEdit && loginPojoData != null) {
                    targetCode = loginPojoData.getCountryCode();
                }

                if (targetCode != null) {
                    for (int i = 0; i < countrycodeArrayList.size(); i++) {
                        if (countrycodeArrayList.get(i).getCountryCode().equals(targetCode)) {
                            binding.spCode.setSelection(i);
                            break;
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Errore caricamento prefissi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.txtAddress.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION, Place.Field.FORMATTED_ADDRESS);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this);
            actResLocation.launch(intent);
        });

        binding.spCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < countrycodeArrayList.size()) {
                    countryCodeId = countrycodeArrayList.get(position).getId();
                    ((TextView) view).setText(countrycodeArrayList.get(position).getCountryCode());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.ivProfile.setOnClickListener(view -> openCameraGalleryDialog());
        binding.BtnUpdateprofile.setOnClickListener(view -> {
            if (checkValidation()) {
                binding.BtnUpdateprofile.setClickable(false);
                performUpdateProfile();
            }
        });
    }

    private void performUpdateProfile() {
        binding.pgUpdateProfile.setVisibility(View.VISIBLE);
        Map<String, String> textParams = new HashMap<>();
        textParams.put("user_id", PrefsUtil.with(this).readString("UserId"));
        textParams.put("firstName", binding.edtFname.getText().toString().trim());
        textParams.put("lastName", binding.edtLname.getText().toString().trim());
        textParams.put("contact_number", binding.edtNumber.getText().toString().trim());
        textParams.put("country_code", countryCodeId);
        textParams.put("city_of_company", binding.edtCityOfCompany.getText().toString().trim());
        textParams.put("address", binding.txtAddress.getText().toString().trim());
        textParams.put("payment_mode", (binding.rgPaymentMethod.getCheckedRadioButtonId() == R.id.rb_cash) ? "c" : "w");
        textParams.put("company_name", binding.edtCompany.getText().toString().trim());
        textParams.put("vat", binding.edtVat.getText().toString().trim());
        textParams.put("certified_email", binding.edtCertifiedMail.getText().toString().trim());
        textParams.put("receipt_code", binding.edtInvoiceRecipeCode.getText().toString().trim());
        textParams.put("latitude", lat);
        textParams.put("longitude", lng);

        viewModel.updateProfile(textParams, selectedImagePath).observe(this, profilePojo -> {
            binding.pgUpdateProfile.setVisibility(View.GONE);
            binding.BtnUpdateprofile.setClickable(true);

            if (profilePojo != null && profilePojo.isStatus()) {
                PrefsUtil.with(mContext).write("isProfileCompleted", true);
                PrefsUtil.with(mContext).write("UserName", textParams.get("firstName") + " " + textParams.get("lastName"));
                PrefsUtil.with(mContext).write("login_cust_address", textParams.get("address"));
                PrefsUtil.with(mContext).write("UserImg", profilePojo.getData().getProfileImg());
                PrefsUtil.with(mContext).write("countrycodeid", countryCodeId);

                if (isFromEdit) {
                    setResult(RESULT_OK, new Intent());
                } else {
                    Intent intent = new Intent(mContext, CustomerHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                finish();
            } else {
                Toast.makeText(this, profilePojo != null ? profilePojo.getMessage() : getString(R.string.server_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillData() {
        if (isFromEdit && profileData != null) {
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
            binding.edtInvoiceRecipeCode.setText(profileData.getReceiptCode());
            binding.edtCertifiedMail.setText(profileData.getCertifiedEmail());

            if ("w".equalsIgnoreCase(profileData.getPaymentMode())) {
                binding.rbWallet.setChecked(true);
            } else {
                binding.rbCash.setChecked(true);
            }

            if (profileData.getProfileImg() != null && !profileData.getProfileImg().isEmpty()) {
                Picasso.get().load(profileData.getProfileImg()).placeholder(R.drawable.loading).into(binding.ivProfile);
            } else {
                Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.ivProfile);
            }
        } else if (!isFromEdit && loginPojoData != null) {
            binding.edtFname.setText(loginPojoData.getFirstName());
            binding.edtLname.setText(loginPojoData.getLastName());
            binding.edtNumber.setText(loginPojoData.getContactNumber());
            binding.edtEmail.setText(loginPojoData.getEmailId());
            binding.txtAddress.setText(loginPojoData.getAddress());
            binding.edtCompany.setText(loginPojoData.getCompanyName());
            binding.edtVat.setText(loginPojoData.getVat());
            binding.edtInvoiceRecipeCode.setText(loginPojoData.getReceiptCode());
            binding.edtCertifiedMail.setText(loginPojoData.getCertifiedEmail());
            lat = loginPojoData.getLatitude();
            lng = loginPojoData.getLongitude();

            if (loginPojoData.getProfileImg() != null && !loginPojoData.getProfileImg().isEmpty()) {
                Picasso.get().load(loginPojoData.getProfileImg()).placeholder(R.drawable.loading).into(binding.ivProfile);
            } else {
                Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(binding.ivProfile);
            }
        }
    }

    protected void init() {
        if ((isFromEdit && profileData == null) || (!isFromEdit && loginPojoData == null)) {
            Toast.makeText(this, "Dati non disponibili.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        binding.edtFname.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtLname.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtVat.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtInvoiceRecipeCode.setFilters(new InputFilter[]{EMOJI_FILTER});

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));
        }

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.edit_profile), HtmlCompat.FROM_HTML_MODE_LEGACY));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        countrycodeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countrycodeArrayList);
        countrycodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCode.setAdapter(countrycodeAdapter);

        initActivityResult();
        viewModel.getCountryCodes(); // Chiamata per popolare lo spinner
    }

    private void initActivityResult() {
        actResCamera = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                Uri uri = Uri.parse(selectedImagePath);
                openCropActivity(uri, uri);
            }
        });

        actResGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            try {
                if (data != null && data.getData() != null) {
                    Uri sourceUri = data.getData();
                    File file = Utils.createTempFileInAppPackage(mContext);
                    Uri destinationUri = Uri.fromFile(file);
                    openCropActivity(sourceUri, destinationUri);
                }
            } catch (Exception e) {
                Log.e(TAG, "onActivityResult (Gallery): " + e.getMessage());
            }
        });

        actResCropper = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            if (result.getResultCode() == RESULT_OK && data != null) {
                try {
                    Uri uri = UCrop.getOutput(data);
                    showImage(uri);
                } catch (Exception e) {
                    Log.e(TAG, "onActivityResult (Cropper): " + e.getMessage());
                }
            }
        });

        actResLocation = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Place place = Autocomplete.getPlaceFromIntent(result.getData());
                try {
                    if (place.getLocation() != null) {
                        lat = String.valueOf(place.getLocation().latitude);
                        lng = String.valueOf(place.getLocation().longitude);
                    }
                    binding.txtAddress.setText(place.getFormattedAddress());
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR && result.getData() != null) {
                Status status = Autocomplete.getStatusFromIntent(result.getData());
                Log.i("AUTO COMPLETE", status.getStatusMessage());
            }
        });
    }

    private void openCameraGalleryDialog() {
        final Dialog d = new Dialog(this);
        d.setContentView(getLayoutInflater().inflate(R.layout.dialog_camera_gallery, null));
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = d.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        d.findViewById(R.id.linCamera).setOnClickListener(view -> {
            d.dismiss();
            permissionUtils.checkCameraPermission();
        });

        d.findViewById(R.id.linGallery).setOnClickListener(view -> {
            d.dismiss();
            permissionUtils.checkStoragePermission();
        });
        d.show();
    }

    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setActiveControlsWidgetColor(ContextCompat.getColor(mContext, R.color.button));
        options.setToolbarTitle("Modifica Foto");
        options.setStatusBarColor(ContextCompat.getColor(mContext, R.color.white));
        options.setToolbarColor(ContextCompat.getColor(mContext, R.color.white));
        options.setToolbarWidgetColor(ContextCompat.getColor(mContext, R.color.button));
        Intent myIntent = UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .withAspectRatio(1f, 1f)
                .getIntent(mContext);
        actResCropper.launch(myIntent);
    }

    private void showImage(Uri imageUri) {
        try {
            FileUtilPOJO fileUtils = FileUtils.getPath(mContext, imageUri);
            if (fileUtils != null) {
                File file = new File(fileUtils.getPath());
                InputStream inputStream = new FileInputStream(file);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                binding.ivProfile.setImageBitmap(bitmap);
                selectedImagePath = file.getAbsolutePath();
                PrefsUtil.with(this).write("UserImg", selectedImagePath);
            }
        } catch (Exception e) {
            Log.e(TAG, "showImage: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_CODE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionUtils.checkCameraPermission();
            } else {
                ToastMaster.showShort(mContext, R.string.err_permission_camera);
            }
        } else if (requestCode == PermissionUtils.REQ_CODE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionUtils.checkStoragePermission();
            } else {
                ToastMaster.showShort(mContext, R.string.err_permission_storage);
            }
        }
    }

    protected boolean checkValidation() {
        if (TextUtils.isEmpty(binding.edtFname.getText().toString().trim())) {
            binding.tilFname.setError(getResources().getString(R.string.provide_fname));
            return false;
        }
        if (TextUtils.isEmpty(binding.edtLname.getText().toString().trim())) {
            binding.tilLname.setError(getResources().getString(R.string.provide_lname));
            return false;
        }
        if (TextUtils.isEmpty(binding.edtEmail.getText().toString().trim())) {
            binding.tilEmail.setError(getResources().getString(R.string.provide_uname));
            return false;
        }
        if (!Utils.isEmailValid(binding.edtEmail.getText().toString())) {
            binding.tilEmail.setError(getResources().getString(R.string.provide_valid_uname));
            return false;
        }
        if (TextUtils.isEmpty(binding.edtNumber.getText().toString().trim())) {
            binding.edtNumber.setError(getResources().getString(R.string.provide_number));
            return false;
        }
        if (binding.edtNumber.getText().toString().trim().length() < 10 || binding.edtNumber.getText().toString().trim().length() > 15) {
            binding.edtNumber.setError(getResources().getString(R.string.vali_contact_num));
            return false;
        }
        if (countryCodeId.isEmpty()) {
            Toast.makeText(this, R.string.provide_country_code_id, Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(binding.txtAddress.getText().toString().trim())) {
            binding.tilAddress.setError(getResources().getString(R.string.provide_address));
            return false;
        }
        if (TextUtils.isEmpty(binding.edtCityOfCompany.getText().toString().trim())) {
            binding.edtCityOfCompany.setError(getResources().getString(R.string.err_msg_city_of_company));
            return false;
        }
        return true;
    }

    private void setupTextWatchers() {
        binding.edtFname.addTextChangedListener(new GenericTextWatcher(binding.tilFname));
        binding.edtLname.addTextChangedListener(new GenericTextWatcher(binding.tilLname));
        binding.edtEmail.addTextChangedListener(new GenericTextWatcher(binding.tilEmail));
        binding.edtNumber.addTextChangedListener(new GenericTextWatcher(binding.tilNumber));
        binding.txtAddress.addTextChangedListener(new GenericTextWatcher(binding.tilAddress));
        binding.edtCityOfCompany.addTextChangedListener(new GenericTextWatcher(binding.tilCityOfCompany));
    }

    private static class GenericTextWatcher implements TextWatcher {
        private final View view;
        private GenericTextWatcher(View view) {
            this.view = view;
        }
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (view instanceof com.google.android.material.textfield.TextInputLayout) {
                ((com.google.android.material.textfield.TextInputLayout) view).setError(null);
            }
        }
        public void afterTextChanged(Editable editable) {}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try {
            if (connectionManager != null) {
                connectionManager.unregisterReceiver();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.clearCameraCache(mContext);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
