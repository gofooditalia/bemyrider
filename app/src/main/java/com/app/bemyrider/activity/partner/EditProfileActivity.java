package com.app.bemyrider.activity.partner;

import static com.app.bemyrider.utils.Utils.EMOJI_FILTER;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerActivityEditProfileBinding;
import com.app.bemyrider.helper.PermissionUtils;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.model.FileUtilPOJO;
import com.app.bemyrider.model.MessageEvent;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import coil.Coil;
import coil.request.ImageRequest;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private Context mContext;
    private Activity mActivity;
    private boolean isDestroyed = false;

    private PermissionUtils permissionUtils;
    private ConnectionManager connectionManager;

    private ActivityResultLauncher<Uri> actResCamera;
    private ActivityResultLauncher<Intent> actResGallery;
    private ActivityResultLauncher<Intent> actResCropper;
    private ActivityResultLauncher<Intent> actResLocation;

    public static String SUN = "0", MON = "1", TUE = "2", WED = "3", THU = "4", FRI = "5", SAT = "6";
    final Calendar mCalendar = Calendar.getInstance();
    private PartnerActivityEditProfileBinding binding;
    private String strstarttime = "", strendtime = "", countrycode = "", strselectedCountryCodeId = "",
            strprofileavldays = "", smallDelivery = "", mediumDelivery = "", largeDelivery = "";

    private LatLng selectedLatLng = new LatLng(0.0, 0.0);
    private String selectedImagePath = "";
    private List<String> switches;
    private ArrayList<CountryCodePojoItem> countrycodeArrayList = new ArrayList<>();
    private ArrayAdapter countrycodeAdapter;
    private WebServiceCall editProfileAsync, countryCodeAsync;

    private boolean isFromEdit = false;
    private NewLoginPojoItem loginPojoData = null;
    private ProfileItem profilePojoData = null;
    private String lat = "", lng = "";

    private boolean isSignatureSelect = false;
    private String selectedSignatureImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.partner_activity_edit_profile);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mContext = this;
        mActivity = this;

        isFromEdit = getIntent().getBooleanExtra("isFromEdit", false);

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

        if (isFromEdit) {
            profilePojoData = (ProfileItem) getIntent().getSerializableExtra("profilePojoData");
            if (profilePojoData == null) {
                finish();
                return;
            }
            strstarttime = profilePojoData.getAvailableTimeStart();
            strendtime = profilePojoData.getAvailableTimeEnd();

            smallDelivery = profilePojoData.getSmallDelivery();
            mediumDelivery = profilePojoData.getMediumDelivery();
            largeDelivery = profilePojoData.getLargeDelivery();
        } else {
            loginPojoData = (NewLoginPojoItem) getIntent().getSerializableExtra("loginPojoData");
            if (loginPojoData == null) {
                finish();
                return;
            }
        }

        initView();
        fillData();
        setAvailableDays();
        initActivityResult();

        binding.imgAddSignature.setOnClickListener(view -> {
            isSignatureSelect = true;
            openCameraGalleryDialog();
        });

        binding.imgSignaturePreview.setOnClickListener(view -> {
            isSignatureSelect = true;
            openCameraGalleryDialog();
        });

        binding.imgUserprofile.setOnClickListener(v -> {
            isSignatureSelect = false;
            openCameraGalleryDialog();
        });

        binding.etEditAddress.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION);
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(mContext);
            actResLocation.launch(intent);
        });

        serviceCallCountryCode();

        binding.spCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strselectedCountryCodeId = countrycodeArrayList.get(position).getId();
                countrycode = countrycodeArrayList.get(position).getCountryCode();
                if (view instanceof TextView) {
                    ((TextView) view).setText(countrycodeArrayList.get(position).getCountryCode());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.etEditStartTime.setOnClickListener(v -> {
            int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            int min = mCalendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, (view, hourOfDay, minute) -> {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
                mCalendar.set(Calendar.SECOND, 0);
                mCalendar.set(Calendar.MILLISECOND, 0);
                Format formatter = new SimpleDateFormat("HH:mm", Locale.US);
                binding.etEditStartTime.setText(formatter.format(mCalendar.getTime()));
                strstarttime = formatter.format(mCalendar.getTime());
            }, hour, min, true);
            timePickerDialog.setTitle(getString(R.string.select_start_time));
            timePickerDialog.show();
        });

        binding.etEditEndTime.setOnClickListener(v -> {
            int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            int min = mCalendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, (view, hourOfDay, minute) -> {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
                mCalendar.set(Calendar.SECOND, 0);
                mCalendar.set(Calendar.MILLISECOND, 0);
                Format formatter = new SimpleDateFormat("HH:mm", Locale.US);
                binding.etEditEndTime.setText(formatter.format(mCalendar.getTime()));
                strendtime = formatter.format(mCalendar.getTime());
            }, hour, min, true);
            timePickerDialog.setTitle(getString(R.string.select_end_time));
            timePickerDialog.show();
        });

        setEditTextListener();

        binding.switchSunday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { if (!switches.contains(SUN)) switches.add(SUN); }
            else { switches.remove(SUN); }
        });

        binding.switchMonday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { if (!switches.contains(MON)) switches.add(MON); }
            else { switches.remove(MON); }
        });

        binding.switchTuesday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { if (!switches.contains(TUE)) switches.add(TUE); }
            else { switches.remove(TUE); }
        });

        binding.switchWednwsday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { if (!switches.contains(WED)) switches.add(WED); }
            else { switches.remove(WED); }
        });

        binding.switchThursday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { if (!switches.contains(THU)) switches.add(THU); }
            else { switches.remove(THU); }
        });

        binding.switchFriday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { if (!switches.contains(FRI)) switches.add(FRI); }
            else { switches.remove(FRI); }
        });

        binding.switchSaturday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { if (!switches.contains(SAT)) switches.add(SAT); }
            else { switches.remove(SAT); }
        });

        binding.btnUpdateProfile.setOnClickListener(v -> {
            if (checkValidation()) {
                callService();
            }
        });

        binding.edtDateOfBirth.setOnClickListener(v -> showDatePicker());

    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance((view, year, monthOfYear, dayOfMonth) -> {
            Calendar selectedTime = Calendar.getInstance();
            selectedTime.set(year, monthOfYear, dayOfMonth);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            binding.edtDateOfBirth.setText(format.format(selectedTime.getTime()));
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dpd.setAccentColor(ContextCompat.getColor(mContext, R.color.button));
        dpd.setMaxDate(now);
        dpd.show(getSupportFragmentManager(), "DatePickerDialog");
    }


    private void setAvailableDays() {
        strprofileavldays = PrefsUtil.with(mContext).readString("userAvalDay");
        if (strprofileavldays.contains("0")) { binding.switchSunday.setChecked(true); switches.add(SUN); }
        if (strprofileavldays.contains("1")) { binding.switchMonday.setChecked(true); switches.add(MON); }
        if (strprofileavldays.contains("2")) { binding.switchTuesday.setChecked(true); switches.add(TUE); }
        if (strprofileavldays.contains("3")) { binding.switchWednwsday.setChecked(true); switches.add(WED); }
        if (strprofileavldays.contains("4")) { binding.switchThursday.setChecked(true); switches.add(THU); }
        if (strprofileavldays.contains("5")) { binding.switchFriday.setChecked(true); switches.add(FRI); }
        if (strprofileavldays.contains("6")) { binding.switchSaturday.setChecked(true); switches.add(SAT); }

        binding.switchSmall.setChecked(smallDelivery.equalsIgnoreCase("y"));
        binding.switchMedium.setChecked(mediumDelivery.equalsIgnoreCase("y"));
        binding.switchLarge.setChecked(largeDelivery.equalsIgnoreCase("y"));
    }

    private void callService() {
        binding.progressUpdateProfile.setVisibility(View.VISIBLE);
        binding.btnUpdateProfile.setClickable(false);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        LinkedHashMap<String, File> fileParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(mContext).readString("UserId"));
        textParams.put("firstName", binding.etEditFname.getText().toString().trim());
        textParams.put("lastName", binding.etEditLname.getText().toString().trim());
        textParams.put("contact_number", binding.etEditContactno.getText().toString().trim());
        textParams.put("country_code", strselectedCountryCodeId);
        textParams.put("address", binding.etEditAddress.getText().toString().trim());
        textParams.put("city_of_birth", binding.edtCityOfBirth.getText().toString().trim());
        textParams.put("date_of_birth", binding.edtDateOfBirth.getText().toString().trim());
        textParams.put("city_of_residence", binding.edtCityOfResidence.getText().toString().trim());
        textParams.put("residential_address", binding.edtResidentialAddress.getText().toString().trim());
        textParams.put("description", Utils.encodeEmoji(binding.etEditAboutme.getText().toString().trim()));
        textParams.put("available_time_start", strstarttime);
        textParams.put("available_time_end", strendtime);
        textParams.put("small_delivery", binding.switchSmall.isChecked() ? "y" : "n");
        textParams.put("medium_delivery", binding.switchMedium.isChecked() ? "y" : "n");
        textParams.put("large_delivery", binding.switchLarge.isChecked() ? "y" : "n");
        textParams.put("company_name", binding.edtCompany.getText().toString().trim());
        textParams.put("vat", binding.edtVat.getText().toString().trim());
        textParams.put("tax_id", binding.edtTaxIdCode.getText().toString().trim());
        textParams.put("certified_email", binding.edtCertifiedMail.getText().toString().trim());
        textParams.put("receipt_code", binding.edtInvoiceRecipeCode.getText().toString().trim());
        textParams.put("latitude", lat);
        textParams.put("longitude", lng);

        if (!selectedImagePath.isEmpty()) fileParams.put("profile_pic", new File(selectedImagePath));
        if (!selectedSignatureImagePath.isEmpty()) fileParams.put("signature_img", new File(selectedSignatureImagePath));

        for (int i = 0; i < switches.size(); i++) textParams.put("avl_dat[" + i + "]", switches.get(i));

        new WebServiceCall(mContext, WebServiceUrl.URL_EDIT_PROFILE, textParams, fileParams,
                ProfilePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (isDestroyed || isFinishing()) return;
                binding.progressUpdateProfile.setVisibility(View.GONE);
                binding.btnUpdateProfile.setClickable(true);
                if (status) {
                    ProfilePojo pojo = (ProfilePojo) obj;
                    PrefsUtil.with(mContext).write("isProfileCompleted", true);
                    PrefsUtil.with(mContext).write("userAddress", binding.etEditAddress.getText().toString().trim());
                    PrefsUtil.with(mContext).write("countrycodeid", strselectedCountryCodeId);
                    PrefsUtil.with(mContext).write("countrycode", countrycode);
                    PrefsUtil.with(mActivity).write("UserName", binding.etEditFname.getText().toString().trim() + " " + binding.etEditLname.getText().toString().trim());
                    PrefsUtil.with(mActivity).write("login_cust_address", binding.etEditAddress.getText().toString().trim());
                    PrefsUtil.with(mActivity).write("UserImg", pojo.getData().getProfileImg());

                    if (isFromEdit) {
                        EventBus.getDefault().post(new MessageEvent("provider_edit_profile", "refresh"));
                    } else {
                        Intent intentHome = new Intent(mContext, ProviderHomeActivity.class);
                        Intent intentService = new Intent(mContext, Partner_MyServices_Activity.class);
                        intentHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivities(new Intent[]{intentHome, intentService});
                    }
                    finish();
                } else {
                    Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onAsync(Object obj) { editProfileAsync = null; }
            @Override public void onCancelled() { editProfileAsync = null; }
        });
    }

    private void serviceCallCountryCode() {
        binding.progressCountryCode.setVisibility(View.VISIBLE);
        binding.spCode.setVisibility(View.GONE);
        countrycodeArrayList.clear();
        new WebServiceCall(mContext, WebServiceUrl.URL_COUNTRY_CODE, new LinkedHashMap<>(),
                CountryCodePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progressCountryCode.setVisibility(View.GONE);
                binding.spCode.setVisibility(View.VISIBLE);
                if (status) {
                    CountryCodePojo countryCodePojo = (CountryCodePojo) obj;
                    countrycodeArrayList.addAll(countryCodePojo.getData());
                    countrycodeAdapter.notifyDataSetChanged();
                    if (getIntent().getStringExtra("Edit") != null && getIntent().getStringExtra("Edit").equals("true")) {
                        for (int i = 0; i < countrycodeArrayList.size(); i++) {
                            if (countrycodeArrayList.get(i).getId().equals(PrefsUtil.with(mContext).readString("countrycodeid"))) {
                                binding.spCode.setSelection(i);
                                break;
                            }
                        }
                    }
                } else {
                    Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onAsync(Object obj) { countryCodeAsync = null; }
            @Override public void onCancelled() { countryCodeAsync = null; }
        });
    }

    private boolean checkValidation() {
        if (binding.etEditFname.getText().toString().trim().isEmpty()) {
            binding.tillEditFname.setError(getString(R.string.error_required));
            binding.etEditFname.requestFocus();
            return false;
        } else if (binding.etEditLname.getText().toString().trim().isEmpty()) {
            binding.tillEditLname.setError(getString(R.string.error_required));
            binding.etEditLname.requestFocus();
            return false;
        } else if (binding.etEditEmail.getText().toString().trim().isEmpty()) {
            binding.tillEditEmail.setError(getString(R.string.error_required));
            binding.etEditEmail.requestFocus();
            return false;
        } else if (!Utils.isEmailValid(binding.etEditEmail.getText().toString().trim())) {
            binding.tillEditEmail.setError(getString(R.string.error_valid_email));
            binding.etEditEmail.requestFocus();
            return false;
        } else if (!(binding.spCode.getSelectedItemPosition() >= 0)) {
            Toast.makeText(mContext, R.string.please_select_country_code, Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.etEditContactno.getText().toString().trim().isEmpty()) {
            binding.etEditContactno.setError(getString(R.string.error_required));
            binding.etEditContactno.requestFocus();
            return false;
        } else if (binding.etEditContactno.getText().toString().trim().length() < 10 || binding.etEditContactno.getText().toString().trim().length() > 15) {
            binding.etEditContactno.setError(getResources().getString(R.string.vali_contact_num));
            binding.etEditContactno.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtCityOfBirth.getText().toString().trim())) {
            binding.tilCityOfBirth.setError(getString(R.string.error_required));
            binding.tilCityOfBirth.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtDateOfBirth.getText().toString().trim())) {
            binding.tilDataOfBirth.setError(getString(R.string.error_required));
            binding.tilDataOfBirth.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtCityOfResidence.getText().toString().trim())) {
            binding.tilCityOfResidence.setError(getString(R.string.error_required));
            binding.tilCityOfResidence.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtResidentialAddress.getText().toString().trim())) {
            binding.tilResidentialAddress.setError(getString(R.string.error_required));
            binding.tilResidentialAddress.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtTaxIdCode.getText().toString().trim())) {
            binding.tilTaxIdCode.setError(getResources().getString(R.string.err_msg_tax_id_code));
            binding.tilTaxIdCode.requestFocus();
            return false;
        } else if (binding.etEditAddress.getText().toString().trim().isEmpty() || binding.etEditAddress.getText().toString().equalsIgnoreCase("n/a")) {
            binding.tillEditAddress.setError(getString(R.string.error_required));
            binding.etEditAddress.requestFocus();
            return false;
        } else if (binding.etEditAboutme.getText().toString().trim().isEmpty()) {
            binding.tillEditAboutme.setError(getString(R.string.error_required));
            binding.etEditAboutme.requestFocus();
            return false;
        } else if (binding.etEditStartTime.getText().toString().trim().isEmpty()) {
            binding.tillEditStarttime.setError(getString(R.string.error_required));
            binding.etEditStartTime.requestFocus();
            return false;
        } else if (binding.etEditEndTime.getText().toString().trim().isEmpty()) {
            binding.tillEditEndtime.setError(getString(R.string.error_required));
            binding.etEditEndTime.requestFocus();
            return false;
        }
        return true;
    }

    private void fillData() {
        if (isFromEdit && profilePojoData != null) {
            binding.etEditFname.setText(profilePojoData.getFirstName());
            binding.etEditLname.setText(profilePojoData.getLastName());
            binding.etEditContactno.setText(profilePojoData.getContactNumber());
            binding.edtCityOfBirth.setText(profilePojoData.getCity_of_birth());
            binding.edtDateOfBirth.setText(profilePojoData.getDate_of_birth());
            binding.edtCityOfResidence.setText(profilePojoData.getCity_of_residence());
            binding.edtResidentialAddress.setText(profilePojoData.getResidential_address());
            binding.etEditAboutme.setText(Utils.decodeEmoji(profilePojoData.getDescription()));
            binding.etEditAddress.setText(profilePojoData.getAddress());
            binding.etEditEmail.setText(profilePojoData.getEmail());
            binding.etEditStartTime.setText(profilePojoData.getAvailableTimeStart());
            binding.etEditEndTime.setText(profilePojoData.getAvailableTimeEnd());

            if (profilePojoData.getSignature_img() != null && !profilePojoData.getSignature_img().isEmpty()) {
                binding.imgAddSignature.setVisibility(View.GONE);
                binding.imgSignaturePreview.setVisibility(View.VISIBLE);
                ImageRequest requestSignature = new ImageRequest.Builder(mContext).data(profilePojoData.getSignature_img()).placeholder(R.drawable.loading).target(binding.imgSignaturePreview).build();
                Coil.imageLoader(mContext).enqueue(requestSignature);
            } else {
                binding.imgAddSignature.setVisibility(View.VISIBLE);
                binding.imgSignaturePreview.setVisibility(View.GONE);
            }

            binding.edtCompany.setText(profilePojoData.getCompanyName());
            binding.edtVat.setText(profilePojoData.getVat());
            binding.edtTaxIdCode.setText(profilePojoData.getTaxId());
            binding.edtInvoiceRecipeCode.setText(profilePojoData.getReceiptCode());
            binding.edtCertifiedMail.setText(profilePojoData.getCertifiedEmail());
            lat = profilePojoData.getLatitude();
            lng = profilePojoData.getLongitude();

            ImageRequest.Builder profileBuilder = new ImageRequest.Builder(mContext).placeholder(R.drawable.loading).error(R.mipmap.user).target(binding.imgUserprofile);
            String profileImg = profilePojoData.getProfileImg();
            if (profileImg.equals("")) profileBuilder.data(R.mipmap.user);
            else profileBuilder.data(profileImg);
            Coil.imageLoader(mContext).enqueue(profileBuilder.build());

        } else if (!isFromEdit && loginPojoData != null) {
            binding.etEditFname.setText(loginPojoData.getFirstName());
            binding.etEditLname.setText(loginPojoData.getLastName());
            binding.etEditContactno.setText(loginPojoData.getContactNumber());
            binding.etEditAddress.setText(loginPojoData.getAddress());
            binding.etEditEmail.setText(loginPojoData.getEmailId());
            binding.edtCompany.setText(loginPojoData.getCompanyName());
            binding.edtVat.setText(loginPojoData.getVat());
            binding.edtTaxIdCode.setText(loginPojoData.getTaxId());
            binding.edtInvoiceRecipeCode.setText(loginPojoData.getReceiptCode());
            binding.edtCertifiedMail.setText(loginPojoData.getCertifiedEmail());
            lat = loginPojoData.getLatitude();
            lng = loginPojoData.getLongitude();

            ImageRequest.Builder loginProfileBuilder = new ImageRequest.Builder(mContext).placeholder(R.drawable.loading).error(R.mipmap.user).target(binding.imgUserprofile);
            String loginProfileImg = loginPojoData.getProfileImg();
            if (loginProfileImg.equals("")) loginProfileBuilder.data(R.mipmap.user);
            else loginProfileBuilder.data(loginProfileImg);
            Coil.imageLoader(mContext).enqueue(loginProfileBuilder.build());
        }
    }

    private void initView() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.edit_profile),HtmlCompat.FROM_HTML_MODE_LEGACY));
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        switches = new ArrayList<>();
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        countrycodeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countrycodeArrayList);
        countrycodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCode.setAdapter(countrycodeAdapter);

        binding.etEditFname.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.etEditLname.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtVat.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtTaxIdCode.setFilters(new InputFilter[]{EMOJI_FILTER});
        binding.edtInvoiceRecipeCode.setFilters(new InputFilter[]{EMOJI_FILTER});
    }

    private void initActivityResult() {
        actResCamera = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && !selectedImagePath.isEmpty()) {
                Uri uri = Uri.parse(selectedImagePath);
                openCropActivity(uri, uri);
            }
        });

        actResGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                try {
                    Uri sourceUri = result.getData().getData();
                    File file = Utils.createTempFileInAppPackage(mContext);
                    openCropActivity(sourceUri, Uri.fromFile(file));
                } catch (Exception e) {
                    Log.e(TAG, "onActivityResult gallery: " + e.getMessage());
                }
            }
        });

        actResCropper = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                showImage(UCrop.getOutput(result.getData()));
            }
        });

        actResLocation = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Place place = Autocomplete.getPlaceFromIntent(result.getData());
                selectedLatLng = place.getLocation();
                binding.etEditAddress.setText(place.getFormattedAddress());
                if (selectedLatLng != null) {
                    lat = String.valueOf(selectedLatLng.latitude);
                    lng = String.valueOf(selectedLatLng.longitude);
                }
            }
        });
    }

    private void openCameraGalleryDialog() {
        final Dialog d = new Dialog(mActivity);
        d.setContentView(getLayoutInflater().inflate(R.layout.dialog_camera_gallery, null));
        Window window = d.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
        d.findViewById(R.id.linCamera).setOnClickListener(view -> { d.dismiss(); permissionUtils.checkCameraPermission(); });
        d.findViewById(R.id.linGallery).setOnClickListener(view -> { d.dismiss(); permissionUtils.checkStoragePermission(); });
        d.show();
    }

    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setActiveControlsWidgetColor(ContextCompat.getColor(mContext, R.color.button));
        options.setToolbarTitle("Edit Photo");
        options.setToolbarColor(ContextCompat.getColor(mContext, R.color.white));
        options.setToolbarWidgetColor(ContextCompat.getColor(mContext, R.color.button));
        actResCropper.launch(UCrop.of(sourceUri, destinationUri).withOptions(options).withAspectRatio(1f, 1f).getIntent(mContext));
    }

    private void showImage(Uri imageUri) {
        try {
            FileUtilPOJO fileUtils = FileUtils.getPath(mContext, imageUri);
            if (fileUtils != null) {
                File file = new File(fileUtils.getPath());
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                if (isSignatureSelect) {
                    binding.imgAddSignature.setVisibility(View.GONE);
                    binding.imgSignaturePreview.setVisibility(View.VISIBLE);
                    binding.imgSignaturePreview.setImageBitmap(bitmap);
                    selectedSignatureImagePath = file.getAbsolutePath();
                } else {
                    binding.imgUserprofile.setImageBitmap(bitmap);
                    selectedImagePath = file.getAbsolutePath();
                    PrefsUtil.with(mContext).write("UserImg", selectedImagePath);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "showImage: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_CODE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) permissionUtils.checkCameraPermission();
            else ToastMaster.showShort(mContext, R.string.err_permission_camera);
        } else if (requestCode == PermissionUtils.REQ_CODE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) permissionUtils.checkStoragePermission();
            else ToastMaster.showShort(mContext, R.string.err_permission_storage);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setEditTextListener() {
        binding.etEditEmail.addTextChangedListener(new GenericTextWatcher(binding.tillEditEmail));
        binding.etEditAddress.addTextChangedListener(new GenericTextWatcher(binding.tillEditAddress));
        binding.etEditContactno.addTextChangedListener(new GenericTextWatcher(binding.tillEditContactno));
        binding.etEditFname.addTextChangedListener(new GenericTextWatcher(binding.tillEditFname));
        binding.etEditLname.addTextChangedListener(new GenericTextWatcher(binding.tillEditLname));
        binding.edtCityOfBirth.addTextChangedListener(new GenericTextWatcher(binding.tilCityOfBirth));
        binding.edtDateOfBirth.addTextChangedListener(new GenericTextWatcher(binding.tilDataOfBirth));
        binding.edtCityOfResidence.addTextChangedListener(new GenericTextWatcher(binding.tilCityOfResidence));
        binding.edtResidentialAddress.addTextChangedListener(new GenericTextWatcher(binding.tilResidentialAddress));
        binding.etEditAboutme.addTextChangedListener(new GenericTextWatcher(binding.tillEditAboutme));
        binding.edtCompany.addTextChangedListener(new GenericTextWatcher(binding.tilCompany));
        binding.edtVat.addTextChangedListener(new GenericTextWatcher(binding.tilVat));
        binding.edtTaxIdCode.addTextChangedListener(new GenericTextWatcher(binding.tilTaxIdCode));
        binding.edtInvoiceRecipeCode.addTextChangedListener(new GenericTextWatcher(binding.tilInvoiceRecipeCode));
        binding.edtCertifiedMail.addTextChangedListener(new GenericTextWatcher(binding.tilCertifiedMail));
        binding.etEditStartTime.addTextChangedListener(new GenericTextWatcher(binding.tillEditStarttime));
        binding.etEditEndTime.addTextChangedListener(new GenericTextWatcher(binding.tillEditEndtime));
    }

    private static class GenericTextWatcher implements TextWatcher {
        private final View view;
        private GenericTextWatcher(View view) { this.view = view; }
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (view instanceof com.google.android.material.textfield.TextInputLayout) {
                ((com.google.android.material.textfield.TextInputLayout) view).setError(null);
            }
        }
        public void afterTextChanged(Editable editable) {}
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        Utils.cancelAsyncTask(editProfileAsync);
        Utils.cancelAsyncTask(countryCodeAsync);
        editProfileAsync = null;
        countryCodeAsync = null;
        Utils.clearCameraCache(mContext);
        super.onDestroy();
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(LocaleManager.onAttach(newBase)); }
}
