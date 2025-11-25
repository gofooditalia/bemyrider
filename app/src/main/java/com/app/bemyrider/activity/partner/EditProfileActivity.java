package com.app.bemyrider.activity.partner;

import static com.app.bemyrider.utils.Utils.EMOJI_FILTER;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.squareup.picasso.Picasso;
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

/*
 * Modified by Hardik Talaviya on 3/12/19.
 */

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private Context mContext;
    private Activity mActivity;
    private boolean isDestroyed = false; // Flag per evitare callback dopo distruzione

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

    private Uri mCropImageUri;
    private LatLng selectedLatLng = new LatLng(0.0, 0.0);
    private TimePickerDialog timePickerDialog;
    private Uri resultUri;
    private String selectedImagePath = "";
    private List<String> switches;
    private ArrayList<CountryCodePojoItem> countrycodeArrayList = new ArrayList<>();
    private ArrayAdapter countrycodeAdapter;
    private AsyncTask editProfileAsync, countryCodeAsync;

    String timeDiffString = "0:0";
    long timeDiffSecs = 0;

    Date startDate, endDate;

    private boolean isFromEdit = false;
    private NewLoginPojoItem loginPojoData = null;
    private ProfileItem profilePojoData = null;
    private String lat = "", lng = "";

    private boolean isSignatureSelect = false;
    private String selectedSignatureImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.partner_activity_edit_profile, null);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mContext = binding.getRoot().getContext();
        mActivity = EditProfileActivity.this;

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
            }
            strstarttime = profilePojoData.getAvailableTimeStart();
            strendtime = profilePojoData.getAvailableTimeEnd();

            smallDelivery = profilePojoData.getSmallDelivery();
            mediumDelivery = profilePojoData.getMediumDelivery();
            largeDelivery = profilePojoData.getLargeDelivery();

            /*strstarttime = getIntent().getStringExtra("strepoc_avl_start_time");
            strendtime = getIntent().getStringExtra("strepoc_avl_end_time");
            smallDelivery = getIntent().getStringExtra("smallDelivery");
            mediumDelivery = getIntent().getStringExtra("mediumDelivery");
            largeDelivery = getIntent().getStringExtra("largeDelivery");*/
        } else {
            loginPojoData = (NewLoginPojoItem) getIntent().getSerializableExtra("loginPojoData");
            if (loginPojoData == null) {
                finish();
            }
        }

        initView();
        fillData();
        setAvailableDays();
        initActivityResult();

        binding.imgAddSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSignatureSelect = true;
                openCameraGalleryDialog();
                /*Intent i = new Intent(mContext, AddSignatureActivity.class);
                actResult.launch(i);*/
            }
        });

        binding.imgSignaturePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSignatureSelect = true;
                openCameraGalleryDialog();  // select signature
                /*Intent i = new Intent(mContext, AddSignatureActivity.class);
                actResult.launch(i);*/
            }
        });

        binding.imgUserprofile.setOnClickListener(v -> {
            isSignatureSelect = false;
            openCameraGalleryDialog();
        });

        binding.etEditAddress.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ID,
                    Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)).build(mContext);
            actResLocation.launch(intent);
        });

        serviceCallCountryCode();

        binding.spCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strselectedCountryCodeId = countrycodeArrayList.get(position).getId();
                countrycode = countrycodeArrayList.get(position).getCountryCode();
                ((TextView) view).setText(countrycodeArrayList.get(position).getCountryCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.etEditStartTime.setOnClickListener(v -> {

            int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            int min = mCalendar.get(Calendar.MINUTE);

            timePickerDialog = new TimePickerDialog(mContext, (view, hourOfDay, minute) -> {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
                Format formatter;

                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
                mCalendar.set(Calendar.SECOND, 0);
                mCalendar.set(Calendar.MILLISECOND, 0);

                //formatter = new SimpleDateFormat("hh:mm:a");
                formatter = new SimpleDateFormat("HH:mm");
                binding.etEditStartTime.setText(formatter.format(mCalendar.getTime()));

                //formatter = new SimpleDateFormat("HH:mm");
                formatter = new SimpleDateFormat("HH:mm");
                strstarttime = formatter.format(mCalendar.getTime());

//                Log.e("Edit_start-time", strstarttime + " " + mCalendar.getTime());

                startDate = mCalendar.getTime();

            }, hour, min, true);
            timePickerDialog.setTitle(getString(R.string.select_start_time));

            timePickerDialog.show();
        });

        binding.etEditEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
                int min = mCalendar.get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(mContext, (view, hourOfDay, minute) -> {
                    mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    mCalendar.set(Calendar.MINUTE, minute);
                    Format formatter;

                    mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    mCalendar.set(Calendar.MINUTE, minute);
                    mCalendar.set(Calendar.SECOND, 0);
                    mCalendar.set(Calendar.MILLISECOND, 0);

                    //formatter = new SimpleDateFormat("hh:mm:a");
                    formatter = new SimpleDateFormat("HH:mm");
                    binding.etEditEndTime.setText(formatter.format(mCalendar.getTime()));

                    //formatter = new SimpleDateFormat("HH:mm");
                    formatter = new SimpleDateFormat("HH:mm");
                    strendtime = formatter.format(mCalendar.getTime());

                    endDate = mCalendar.getTime();

//                    Log.e("Edit_start-time", strendtime + " " + mCalendar.getTime());

                }, hour, min, true);
                timePickerDialog.setTitle(getString(R.string.select_end_time));
                timePickerDialog.show();

            }
        });

        setEditTextListener();

        binding.switchSunday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!switches.contains(SUN)) {
                    switches.add(SUN);
                }
            } else {
                if (switches.contains(SUN)) {
                    switches.remove(SUN);
                }
            }
        });

        binding.switchMonday.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                if (!switches.contains(MON)) {
                    switches.add(MON);
                }
            } else {
                if (switches.contains(MON)) {
                    switches.remove(MON);
                }
            }
        });

        binding.switchTuesday.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                if (!switches.contains(TUE)) {
                    switches.add(TUE);
                }
            } else {
                if (switches.contains(TUE)) {
                    switches.remove(TUE);
                }
            }
        });

        binding.switchWednwsday.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                if (!switches.contains(WED)) {
                    switches.add(WED);
                }
            } else {
                if (switches.contains(WED)) {
                    switches.remove(WED);
                }
            }
        });

        binding.switchThursday.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                if (!switches.contains(THU)) {
                    switches.add(THU);
                }
            } else {
                if (switches.contains(THU)) {
                    switches.remove(THU);
                }
            }
        });

        binding.switchFriday.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                if (!switches.contains(FRI)) {
                    switches.add(FRI);
                }
            } else {
                if (switches.contains(FRI)) {
                    switches.remove(FRI);
                }
            }
        });

        binding.switchSaturday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if (!switches.contains(SAT)) {
                        switches.add(SAT);
                    }
                } else {
                    if (switches.contains(SAT)) {
                        switches.remove(SAT);
                    }
                }
            }
        });

        binding.btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*StringBuilder builder = new StringBuilder();
                for (int i = 0; i < switches.size(); i++) {
                    builder.append(switches.get(i) + ",");
                }

                if (builder.toString().endsWith(",")) {
                    builder.toString().substring(0, builder.toString().length() - 1);
                }*/

                if (checkValidation()) {
                    callService();
                }
            }
        });

        binding.edtDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance((view, year, monthOfYear, dayOfMonth) -> {
            Calendar selectedTime = Calendar.getInstance();
            selectedTime.set(year, monthOfYear, dayOfMonth);

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            binding.edtDateOfBirth.setText(String.format("%s", format.format(selectedTime.getTime())));

            /*com.wdullaer.materialdatetimepicker.time.TimePickerDialog tpd = com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance((view1, hourOfDay, minute, second) -> {
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                // SimpleDateFormat format1 = new SimpleDateFormat("hh:mm a", Locale.US);
                SimpleDateFormat format1 = new SimpleDateFormat("HH:mm", Locale.US);
                binding.edtStarttime.setText(String.format("%s %s", format.format(selectedTime.getTime()), format1.format(selectedTime.getTime())));
            }, true);
            tpd.setAccentColor(getResources().getColor(R.color.button));
            tpd.setTimeInterval(1, 15);
            if (DateUtils.isToday(selectedTime.getTimeInMillis())) {
                tpd.setMinTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
                        now.get(Calendar.SECOND));
            }
            tpd.show(getFragmentManager(), "TimePickerDialog");*/

        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dpd.setAccentColor(ContextCompat.getColor(mContext, R.color.button));
        dpd.setMaxDate(now);
        dpd.show(getSupportFragmentManager(), "DatePickerDialog");
    }


    private void setAvailableDays() {
        strprofileavldays = PrefsUtil.with(mContext).readString("userAvalDay");

        if (strprofileavldays.contains("0")) {
            binding.switchSunday.setChecked(true);
            switches.add(SUN);
        }
        if (strprofileavldays.contains("1")) {
            binding.switchMonday.setChecked(true);
            switches.add(MON);
        }
        if (strprofileavldays.contains("2")) {
            binding.switchTuesday.setChecked(true);
            switches.add(TUE);
        }
        if (strprofileavldays.contains("3")) {
            binding.switchWednwsday.setChecked(true);
            switches.add(WED);
        }
        if (strprofileavldays.contains("4")) {
            binding.switchThursday.setChecked(true);
            switches.add(THU);
        }
        if (strprofileavldays.contains("5")) {
            binding.switchFriday.setChecked(true);
            switches.add(FRI);
        }
        if (strprofileavldays.contains("6")) {
            binding.switchSaturday.setChecked(true);
            switches.add(SAT);
        }

        if (smallDelivery.equalsIgnoreCase("y")) {
            binding.switchSmall.setChecked(true);
        } else {
            binding.switchSmall.setChecked(false);
        }

        if (mediumDelivery.equalsIgnoreCase("y")) {
            binding.switchMedium.setChecked(true);
        } else {
            binding.switchMedium.setChecked(false);
        }

        if (largeDelivery.equalsIgnoreCase("y")) {
            binding.switchLarge.setChecked(true);
        } else {
            binding.switchLarge.setChecked(false);
        }
    }

    /*----------- Edit Profile Api Call --------------*/
    private void callService() {

        Log.e("ZZZ", "selectedImagePath::" + selectedImagePath);
        Log.e("ZZZ", "selectedSignatureImagePath::" + selectedSignatureImagePath);

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

        //textParams.put("paypal_email", binding.etEditPaypalEmail.getText().toString().trim());
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

        if (!selectedImagePath.isEmpty()) {
            fileParams.put("profile_pic", new File(selectedImagePath));
        }

        if (!selectedSignatureImagePath.isEmpty()) {
            fileParams.put("signature_img", new File(selectedSignatureImagePath));
        }

        for (int i = 0; i < switches.size(); i++) {
            textParams.put("avl_dat[" + i + "]", switches.get(i));
        }

        /*if (selectedLatLng.latitude == 0.0) {
            textParams.put("latitude", PrefsUtil.with(context).readString("lat"));
        } else {
            textParams.put("latitude", String.valueOf(selectedLatLng.latitude));
        }
        if (selectedLatLng.longitude == 0.0) {
            textParams.put("longitude", PrefsUtil.with(context).readString("long"));
        } else {
            textParams.put("longitude", String.valueOf(selectedLatLng.longitude));
        }*/

//        Log.e("ZZZ", "Param::" + textParams);

        new WebServiceCall(mContext, WebServiceUrl.URL_EDIT_PROFILE, textParams, fileParams,
                ProfilePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                // Evita callback se l'activity è stata distrutta
                if (isDestroyed || isFinishing()) {
                    return;
                }
                
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
//                        Toast.makeText(context, pojo.getMessage(), Toast.LENGTH_SHORT).show();
                        EventBus.getDefault().post(new MessageEvent("provider_edit_profile", "refresh"));
                    } else {
                        Intent intentHome = new Intent(mContext, ProviderHomeActivity.class);
                        Intent intentService = new Intent(mContext, Partner_MyServices_Activity.class);
                        intentHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivities(new Intent[]{intentHome, intentService});
                    }
                    finish();
                } else {
                    // Mostra messaggio solo se l'activity è ancora valida
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                editProfileAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                editProfileAsync = null;
            }
        });
    }

    /*-------------- Country Code Api Call ----------------*/
    private void serviceCallCountryCode() {
        binding.progressCountryCode.setVisibility(View.VISIBLE);
        binding.spCode.setVisibility(View.GONE);

        countrycodeArrayList.clear();
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        new WebServiceCall(mContext, WebServiceUrl.URL_COUNTRY_CODE, textParams,
                CountryCodePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progressCountryCode.setVisibility(View.GONE);
                binding.spCode.setVisibility(View.VISIBLE);
                if (status) {
                    CountryCodePojo countryCodePojo = (CountryCodePojo) obj;
                    countrycodeArrayList.addAll(countryCodePojo.getData());
                    countrycodeAdapter.notifyDataSetChanged();

                    if (getIntent().getStringExtra("Edit") != null && getIntent()
                            .getStringExtra("Edit").equals("true")) {
                        for (int i = 0; i < countrycodeArrayList.size(); i++) {
                            if (countrycodeArrayList.get(i).getId().equals(PrefsUtil.with(
                                    mContext).readString("countrycodeid"))) {
                                binding.spCode.setSelection(i);
                                break;
                            }

                        }
                    }
                } else {
                    Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
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

    private boolean checkValidation() {
        /*try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date date1 = format.parse(strstarttime);
            Date date2 = format.parse(strendtime);

            long differenceTime = date1.getTime() - date2.getTime();

            timeDiffSecs = differenceTime / 1000;

            Log.e("Edit_timeDiffSecs", timeDiffSecs + "" + Math.abs(timeDiffSecs));

            timeDiffString = timeDiffSecs / 3600 + ":" +
                    (timeDiffSecs % 3600) / 60 + ":" +
                    (timeDiffSecs % 3600) % 60;

            Log.e("Edit_timeDiffString", timeDiffString);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        if (binding.etEditFname.getText().toString().trim().isEmpty()) {
            binding.tillEditFname.setErrorEnabled(true);
            binding.tillEditFname.setError(getString(R.string.error_required));
            binding.etEditFname.requestFocus();
            return false;
        } else if (binding.etEditLname.getText().toString().trim().isEmpty()) {
            binding.tillEditLname.setErrorEnabled(true);
            binding.tillEditLname.setError(getString(R.string.error_required));
            binding.etEditLname.requestFocus();
            return false;
        } else if (binding.etEditEmail.getText().toString().trim().isEmpty()) {
            binding.tillEditEmail.setErrorEnabled(true);
            binding.tillEditEmail.setError(getString(R.string.error_required));
            binding.etEditEmail.requestFocus();
            return false;
        } else if (!Utils.isEmailValid(binding.etEditEmail.getText().toString().trim())) {
            binding.tillEditEmail.setErrorEnabled(true);
            binding.tillEditEmail.setError(getString(R.string.error_valid_email));
            binding.etEditEmail.requestFocus();
            return false;
        } else if (!(binding.spCode.getSelectedItemPosition() >= 0)) {
            Toast.makeText(mContext, R.string.please_select_country_code, Toast.LENGTH_SHORT).show();
            return false;
        } /*else if (binding.etEditPaypalEmail.getText().toString().trim().isEmpty()) {
            binding.tillEditPaypalEmail.setErrorEnabled(true);
            binding.tillEditPaypalEmail.setError(getString(R.string.error_required));
            binding.etEditPaypalEmail.requestFocus();
            return false;
        } else if (!Utils.isEmailValid(binding.etEditPaypalEmail.getText().toString().trim())) {
            binding.tillEditPaypalEmail.setErrorEnabled(true);
            binding.tillEditPaypalEmail.setError(getString(R.string.error_valid_email));
            binding.etEditPaypalEmail.requestFocus();
            return false;
        } */ else if (binding.etEditContactno.getText().toString().trim().isEmpty()) {
            binding.etEditContactno.setError(getString(R.string.error_required));
            binding.etEditContactno.requestFocus();
            return false;
        } else if (binding.etEditContactno.getText().toString().trim().length() < 10 || binding.etEditContactno.getText().toString().trim().length() > 15) {
            binding.etEditContactno.setError(getResources().getString(R.string.vali_contact_num));
            binding.etEditContactno.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtCityOfBirth.getText().toString().trim())) {
            binding.tilCityOfBirth.setErrorEnabled(true);
            binding.tilCityOfBirth.setError(getString(R.string.error_required));
            binding.tilCityOfBirth.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtDateOfBirth.getText().toString().trim())) {
            binding.tilDataOfBirth.setErrorEnabled(true);
            binding.tilDataOfBirth.setError(getString(R.string.error_required));
            binding.tilDataOfBirth.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtCityOfResidence.getText().toString().trim())) {
            binding.tilCityOfResidence.setErrorEnabled(true);
            binding.tilCityOfResidence.setError(getString(R.string.error_required));
            binding.tilCityOfResidence.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtResidentialAddress.getText().toString().trim())) {
            binding.tilResidentialAddress.setErrorEnabled(true);
            binding.tilResidentialAddress.setError(getString(R.string.error_required));
            binding.tilResidentialAddress.requestFocus();
            return false;
        } /*else if (TextUtils.isEmpty(binding.edtCompany.getText().toString().trim())) {
            binding.tilCompany.setErrorEnabled(true);
            binding.tilCompany.setError(getResources().getString(R.string.err_msg_company));
            binding.tilCompany.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(binding.edtVat.getText().toString().trim())) {
            binding.tilVat.setErrorEnabled(true);
            binding.tilVat.setError(getResources().getString(R.string.err_msg_vat));
            binding.tilVat.requestFocus();
            return false;
        }*/ else if (TextUtils.isEmpty(binding.edtTaxIdCode.getText().toString().trim())) {
            binding.tilTaxIdCode.setErrorEnabled(true);
            binding.tilTaxIdCode.setError(getResources().getString(R.string.err_msg_tax_id_code));
            binding.tilTaxIdCode.requestFocus();
            return false;
        } else if (binding.etEditAddress.getText().toString().trim().isEmpty()
                || binding.etEditAddress.getText().toString().equalsIgnoreCase("n/a")) {
            binding.tillEditAddress.setErrorEnabled(true);
            binding.tillEditAddress.setError(getString(R.string.error_required));
            binding.etEditAddress.requestFocus();
            return false;
        } /*else if (TextUtils.isEmpty(binding.edtInvoiceRecipeCode.getText().toString().trim())) {
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
        } */ else if (binding.etEditAboutme.getText().toString().trim().isEmpty()) {
            binding.tillEditAboutme.setErrorEnabled(true);
            binding.tillEditAboutme.setError(getString(R.string.error_required));
            binding.etEditAboutme.requestFocus();
            return false;
        } else if (binding.etEditStartTime.getText().toString().trim().isEmpty()) {
            binding.tillEditStarttime.setErrorEnabled(true);
            binding.tillEditStarttime.setError(getString(R.string.error_required));
            binding.etEditStartTime.requestFocus();
            return false;
        } else if (binding.etEditEndTime.getText().toString().trim().isEmpty()) {
            binding.tillEditEndtime.setErrorEnabled(true);
            binding.tillEditEndtime.setError(getString(R.string.error_required));
            binding.etEditEndTime.requestFocus();
            return false;
        } /* else if (Math.abs(timeDiffSecs) < 28800) {
            Toast.makeText(context, getResources().getString(R.string.time_validation), Toast.LENGTH_SHORT).show();
            return false;
        }*/
        return true;
    }

    private void fillData() {
        if (isFromEdit) {
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
            // binding.etEditPaypalEmail.setText(profilePojoData.getPaypalEmail());
            binding.etEditStartTime.setText(profilePojoData.getAvailableTimeStart());
            binding.etEditEndTime.setText(profilePojoData.getAvailableTimeEnd());

            Log.e("KKK", "profilePojoData.getSignature_img()" + profilePojoData.getSignature_img());

            if (profilePojoData.getSignature_img() != null && profilePojoData.getSignature_img() != "") {
                binding.imgAddSignature.setVisibility(View.GONE);
                binding.imgSignaturePreview.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(profilePojoData.getSignature_img())
                        .placeholder(R.drawable.loading)
                        .into(binding.imgSignaturePreview);
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

            if (profilePojoData.getProfileImg().equals("")) {
                try {
                    Picasso.get().load(R.mipmap.user).into(binding.imgUserprofile);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Picasso.get().load(profilePojoData.getProfileImg())
                            .placeholder(R.drawable.loading).error(R.mipmap.user).into(binding.imgUserprofile);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            /*binding.etEditFname.setText(PrefsUtil.with(context).readString("userfname"));
            binding.etEditLname.setText(PrefsUtil.with(context).readString("userlname"));
            binding.etEditContactno.setText(PrefsUtil.with(context).readString("userContactno"));
            binding.etEditAboutme.setText(Utils.decodeEmoji(PrefsUtil.with(context).readString("userAbout")));
            binding.etEditAddress.setText(PrefsUtil.with(context).readString("userAddress"));
            binding.etEditEmail.setText(PrefsUtil.with(context).readString("userEmail"));
            binding.etEditPaypalEmail.setText(PrefsUtil.with(context).readString("paypalEmailId"));
            binding.etEditStartTime.setText(PrefsUtil.with(context).readString("start_time"));
            binding.etEditEndTime.setText(PrefsUtil.with(context).readString("end_time"));
            lat = PrefsUtil.with(context).readString("lat");
            lng = PrefsUtil.with(context).readString("long");*/

           /* if (PrefsUtil.with(context).readString("UserImg").equals("")) {
                try {
                    Picasso.get().load(R.mipmap.user).into(binding.imgUserprofile);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Picasso.get().load(PrefsUtil.with(context).readString("UserImg"))
                            .placeholder(R.drawable.loading).error(R.mipmap.user).into(binding.imgUserprofile);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }*/
        } else {
            binding.etEditFname.setText(loginPojoData.getFirstName());
            binding.etEditLname.setText(loginPojoData.getLastName());
            binding.etEditContactno.setText(loginPojoData.getContactNumber());
//            binding.etEditAboutme.setText(Utils.decodeEmoji(PrefsUtil.with(context).readString("userAbout")));
            binding.etEditAddress.setText(loginPojoData.getAddress());
            binding.etEditEmail.setText(loginPojoData.getEmailId());
//            binding.etEditPaypalEmail.setText("");
//            binding.etEditStartTime.setText("");
//            binding.etEditEndTime.setText("");

            binding.edtCompany.setText(loginPojoData.getCompanyName());
            binding.edtVat.setText(loginPojoData.getVat());
            binding.edtTaxIdCode.setText(loginPojoData.getTaxId());
            binding.edtInvoiceRecipeCode.setText(loginPojoData.getReceiptCode());
            binding.edtCertifiedMail.setText(loginPojoData.getCertifiedEmail());

            lat = loginPojoData.getLatitude();
            lng = loginPojoData.getLongitude();

            if (loginPojoData.getProfileImg().equals("")) {
                try {
                    Picasso.get()
                            .load(R.mipmap.user)
                            .into(binding.imgUserprofile);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Picasso.get()
                            .load(loginPojoData.getProfileImg())
                            .placeholder(R.drawable.loading)
                            .error(R.mipmap.user)
                            .into(binding.imgUserprofile);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initView() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));
        }

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.edit_profile),HtmlCompat.FROM_HTML_MODE_LEGACY));

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        switches = new ArrayList<>();

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        /*Init Country Code Spinner*/
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
        actResCamera = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    if (selectedImagePath != "") {
                        Uri uri = Uri.parse(selectedImagePath);
                        openCropActivity(uri, uri);
                    }
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

        actResLocation = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            try {
                if (result.getResultCode() == RESULT_OK) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    Log.i("AUTO COMPLETE", "Place: " + place.getName() + ", " + place.getId());
                    try {
                        selectedLatLng = place.getLatLng();
                        binding.etEditAddress.setText(place.getAddress());
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
                e.printStackTrace();
            }
        });
    }


    private void permissionMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setMessage(getString(R.string.cancelling_granted)).show();
    }


    private void openCameraGalleryDialog() {
        final Dialog d = new Dialog(mActivity);
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
            if (isSignatureSelect) {
                selectedImagePath = "";
                binding.imgAddSignature.setVisibility(View.GONE);
                binding.imgSignaturePreview.setVisibility(View.VISIBLE);
                binding.imgSignaturePreview.setImageBitmap(bitmap);
                selectedSignatureImagePath = file.getAbsolutePath();
            } else {
                selectedSignatureImagePath = "";
                binding.imgUserprofile.setImageBitmap(bitmap);
                selectedImagePath = file.getAbsolutePath();
                PrefsUtil.with(mContext).write("UserImg", selectedImagePath);
            }
        } catch (Exception e) {
            Log.e(TAG, "showImage: " + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }



    private void setEditTextListener() {
       /*binding.etEditPaypalEmail.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tillEditPaypalEmail.setError("");
                binding.tillEditPaypalEmail.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/

        binding.etEditEmail.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tillEditEmail.setError("");
                binding.tillEditEmail.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etEditAddress.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tillEditAddress.setError("");
                binding.tillEditAddress.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etEditContactno.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tillEditContactno.setError("");
                binding.tillEditContactno.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etEditFname.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tillEditFname.setError("");
                binding.tillEditFname.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etEditLname.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tillEditLname.setError("");
                binding.tillEditLname.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtCityOfBirth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilCityOfBirth.setError("");
                binding.tilCityOfBirth.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtDateOfBirth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilDataOfBirth.setError("");
                binding.tilDataOfBirth.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtCityOfResidence.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilCityOfResidence.setError("");
                binding.tilCityOfResidence.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtResidentialAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilResidentialAddress.setError("");
                binding.tilResidentialAddress.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etEditAboutme.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tillEditAboutme.setError("");
                binding.tillEditAboutme.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtCompany.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tilCompany.setError("");
                binding.tilCompany.setErrorEnabled(false);
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
                binding.tilVat.setErrorEnabled(false);
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
                binding.tilTaxIdCode.setErrorEnabled(false);
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
                binding.tilInvoiceRecipeCode.setErrorEnabled(false);
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
                binding.tilCertifiedMail.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etEditStartTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tillEditStarttime.setError("");
                binding.tillEditStarttime.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etEditEndTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tillEditEndtime.setError("");
                binding.tillEditEndtime.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
        isDestroyed = true; // Imposta flag prima di distruggere
        
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Cancella le chiamate API in corso per evitare Toast dopo la distruzione
        Utils.cancelAsyncTask(editProfileAsync);
        Utils.cancelAsyncTask(countryCodeAsync);
        
        // Imposta flag per evitare callback dopo la distruzione
        editProfileAsync = null;
        countryCodeAsync = null;

        /** clear cache dir of picture which is taken photo from camera */
        Utils.clearCameraCache(mContext);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
