package com.app.bemyrider.fragment.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateUtils;

import com.app.bemyrider.utils.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.FragmentDetailBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Modified by Hardik Talaviya on 10/12/19.
 */

public class DetailFragment extends Fragment {

    private static final String TAG = "DetailFragment";
    private FragmentDetailBinding binding;
    private ProviderServiceDetailsItem serviceDetailData;
    /*private String[] hours_array = {"Select Hours*", "1 Hour", "2 Hours", "3 Hours", "4 Hours",
            "5 Hours", "6 Hours", "7 Hours", "8 Hours", "9 Hours", "10 Hours", "11 Hours",
            "12 Hours", "13 Hours", "14 Hours", "15 Hours", "16 Hours", "17 Hours", "18 Hours",
            "19 Hours", "20 Hours", "21 Hours", "22 Hours", "23 Hours", "24 Hours"};*/
    private AsyncTask bookServiceAsync;
    private Context context;
    ActivityResultLauncher<Intent> locationActivityResultLauncher;
    String strDeliveryType = "";

    long enterTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);

        serviceDetailData = (ProviderServiceDetailsItem) getArguments().getSerializable("data");

        init();

        if (!Places.isInitialized()) {
            Places.initialize(context.getApplicationContext(),
                    getResources().getString(R.string.google_api_key));
        }

        binding.edtServiceAddress.setText(PrefsUtil.with(context).readString("search_address"));

        binding.edtServiceAddress.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ID,
                    Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS))
                    .build(context);
            locationActivityResultLauncher.launch(intent);
        });

        if (serviceDetailData != null) {
            setdata();
        }
        binding.edtStarttime.setFocusable(false);
        binding.edtStarttime.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                getDateTime();
            }
        });

        binding.edtStarttime.setOnClickListener(v -> getDateTime());

        binding.btnSendRequest.setOnClickListener(v -> {
            if (checkBookServiceValidation(binding.edtServiceAddress.getText().toString().trim(),
                    binding.edtStarttime.getText().toString().trim(), binding.edtServiceDescription.getText().toString().trim(),
                    binding.spnSelectHours.getSelectedItemPosition())) {
                Utils.hideSoftKeyboard((Activity) context);
                binding.btnSendRequest.setClickable(false);
                bookService();
            }
        });

        binding.edtStarttime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilStarttime.setErrorEnabled(false);
                binding.tilStarttime.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.edtServiceAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.edtServiceAddress.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.edtServiceDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilDescription.setErrorEnabled(false);
                binding.tilDescription.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return binding.getRoot();
    }

    private void init() {
        context = getActivity();
        binding.edtStarttime.setInputType(InputType.TYPE_NULL);

        /*ArrayAdapter hoursAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, hours_array);
        binding.spnSelectHours.setAdapter(hoursAdapter);
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/
        locationActivityResult();

       /* binding.spinnerDeliveryType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (binding.spinnerDeliveryType.getSelectedItemPosition() == 1) {
                    strDeliveryType = "quick";
                }
                if (binding.spinnerDeliveryType.getSelectedItemPosition() == 2) {
                    strDeliveryType = "scheduled";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/
    }

    private void locationActivityResult() {
        locationActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                try {
                    if (result.getResultCode() == RESULT_OK) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        Log.i("AUTO COMPLETE", "Place: " + place.getName() + ", " + place.getId());
                        try {
                            if (place.getLatLng() != null) {
                                PrefsUtil.with(context).write("bookingLat",
                                        String.valueOf(place.getLatLng().latitude));
                                PrefsUtil.with(context).write("bookingLong",
                                        String.valueOf(place.getLatLng().longitude));
                            }
                            PrefsUtil.with(context).write("search_address",
                                    place.getAddress());
                            binding.edtServiceAddress.setText(place.getAddress());
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
            }
        });
    }

    private void setdata() {
        String strDeliveryType = "";
        String strRequestType = "";

        if (PrefsUtil.with(context).readString("delivery_type").equals("small")) {
            strDeliveryType = getString(R.string.small);
        } else if (PrefsUtil.with(context).readString("delivery_type").equals("medium")) {
            strDeliveryType = getString(R.string.medium);
        } else if (PrefsUtil.with(context).readString("delivery_type").equals("large")) {
            strDeliveryType = getString(R.string.large);
        }

        if (PrefsUtil.with(context).readString("request_type").equals("quick")) {
            strRequestType = getString(R.string.quick);
        } else if (PrefsUtil.with(context).readString("request_type").equals("scheduled")) {
            strRequestType = getString(R.string.scheduled);
        }

        if(serviceDetailData.getRequestType() != null && !"".equals(serviceDetailData.getRequestType())) {
            PrefsUtil.with(context).write("request_type", serviceDetailData.getRequestType());
        }

        if (serviceDetailData.getAvailableDaysList() != null && !serviceDetailData.getAvailableDaysList().equals("")) {
            binding.txtServiceDays.setText(serviceDetailData.getAvailableDaysList());
        } else {
            binding.txtServiceDays.setText("-");
        }

        if (serviceDetailData.getAvailableTimeStart() != null
                && serviceDetailData.getAvailableTimeStart().length() > 0
                && serviceDetailData.getAvailableTimeEnd() != null
                && serviceDetailData.getAvailableTimeEnd().length() > 0) {
            binding.txtServiceTime.setText(String.format("%s - %s", serviceDetailData.getAvailableTimeStart(), serviceDetailData.getAvailableTimeEnd()));
        } else {
            binding.txtServiceTime.setText("-");
        }

        binding.txtDeliveryType.setText(strDeliveryType);
        binding.txtRequestType.setText(strRequestType);

        binding.txtServiceName.setText(serviceDetailData.getCategoryName());
        binding.txtServiceDesc.setText(Utils.decodeEmoji(serviceDetailData.getDescription()));
        binding.txtServiceHours.setText(String.format("%s %s", serviceDetailData.getHours(), getString(R.string.hours_with_space)));

        if (serviceDetailData.getServiceMasterType().equalsIgnoreCase("hourly")) {
            binding.layoutHours.setVisibility(View.GONE);
        } else {
            binding.layoutHours.setVisibility(View.VISIBLE);
        }

        Log.e(TAG, "setData Price :: " + serviceDetailData.getPrice());
        if (serviceDetailData.getServiceMasterType().equals("fixed")) {
            binding.txtServicePrice.setText(String.format("%s%s", PrefsUtil.with(context)
                    .readString("CurrencySign"), serviceDetailData.getPrice()));
        } else {
            binding.txtServicePrice.setText(String.format("%s%s%s", PrefsUtil.with(context)
                    .readString("CurrencySign"), serviceDetailData.getPrice(), getString(R.string.per_hours)));
        }

        if (PrefsUtil.with(context).readString("PaymentMode").equals("c")) {
            binding.txtPaymentMethod.setText(getString(R.string.cash));
        } else {
            binding.txtPaymentMethod.setText(getString(R.string.wallet));
        }

        if (serviceDetailData.getServiceMasterType().equalsIgnoreCase("fixed")) {
            binding.spnSelectHours.setVisibility(View.GONE);
            //binding.viewSpHour.setVisibility(View.GONE);
        } else {
            binding.spnSelectHours.setVisibility(View.VISIBLE);
            //binding.viewSpHour.setVisibility(View.VISIBLE);
        }
        binding.tilStarttime.setVisibility(View.VISIBLE);
        binding.tilAddress.setVisibility(View.VISIBLE);
    }

    private void getDateTime() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance((view, year, monthOfYear, dayOfMonth) -> {
            Calendar selectedTime = Calendar.getInstance();
            selectedTime.set(year, monthOfYear, dayOfMonth);

            TimePickerDialog tpd = TimePickerDialog.newInstance((view1, hourOfDay, minute, second) -> {
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
               // SimpleDateFormat format1 = new SimpleDateFormat("hh:mm a", Locale.US);
                SimpleDateFormat format1 = new SimpleDateFormat("HH:mm", Locale.US);
                binding.edtStarttime.setText(String.format("%s %s", format.format(selectedTime.getTime()), format1.format(selectedTime.getTime())));
            }, true);
            tpd.setAccentColor(ContextCompat.getColor(context, R.color.button));
            tpd.setTimeInterval(1, 15);
            if (DateUtils.isToday(selectedTime.getTimeInMillis())) {
                tpd.setMinTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
                        now.get(Calendar.SECOND));
            }
            tpd.show(getFragmentManager(), "TimePickerDialog");

        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dpd.setAccentColor(ContextCompat.getColor(context, R.color.button));
        dpd.setMinDate(now);
        dpd.show(getFragmentManager(), "DatePickerDialog");
    }

    /* ------------------ Book Service Api Call ---------------------- */
    private void bookService() {
        binding.pgSendRequest.setVisibility(View.VISIBLE);

        String url = WebServiceUrl.URL_BOOKSERVICE;

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("provider_service_id", serviceDetailData.getId());
        textParams.put("login_service_id", PrefsUtil.with(context).readString("UserId"));
        textParams.put("service_start_time", binding.edtStarttime.getText().toString());
        if ((serviceDetailData.getServiceMasterType().equalsIgnoreCase("fixed"))) {
            textParams.put("provider_service_hours", serviceDetailData.getHours());
        } else if ((serviceDetailData.getServiceMasterType().equalsIgnoreCase("hourly"))) {
            textParams.put("sel_hours", String.valueOf(binding.spnSelectHours.getSelectedItemPosition()));
        }
        textParams.put("user_id", PrefsUtil.with(context).readString("UserId"));
        textParams.put("service_address", PrefsUtil.with(context).readString("search_address"));
        textParams.put("service_details", Utils.encodeEmoji(binding.edtServiceDescription.getText().toString()));
        textParams.put("bookingLat", PrefsUtil.with(context).readString("bookingLat"));
        textParams.put("bookingLong", PrefsUtil.with(context).readString("bookingLong"));
        textParams.put("delivery_type", PrefsUtil.with(context).readString("delivery_type"));
        textParams.put("request_type", PrefsUtil.with(context).readString("request_type"));

//        Log.e("ZZZ","PARAM::"+textParams);

        new WebServiceCall(context, url, textParams, CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgSendRequest.setVisibility(View.GONE);
                binding.btnSendRequest.setClickable(true);
                if (status) {
                    Toast.makeText(context, ((CommonPojo) obj).getMessage(), Toast.LENGTH_LONG).show();
                    getActivity().finish();
                } else {
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                bookServiceAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                bookServiceAsync = null;
            }
        });
    }

    private boolean checkBookServiceValidation(String address, String startTime, String description,
                                               int selectedHour) {
        /*if (binding.spinnerDeliveryType.getSelectedItemPosition() == 0) {
            Toast.makeText(context, getResources().getString(R.string.please_select_delivery_type), Toast.LENGTH_SHORT).show();
            return false;
        } else */

        try {
           // SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.US);
            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US);
            Date strDate = format.parse(startTime);
            Log.e(TAG, strDate.getTime() + " " + System.currentTimeMillis() + " final_1");
            // 1 min= 60000 milliseconds for buffer
            enterTime = strDate.getTime();
            Log.e(TAG, enterTime + " " + System.currentTimeMillis() + " final_2");

            if (enterTime > System.currentTimeMillis()) {
                Log.e(TAG, enterTime + " true " + System.currentTimeMillis() + " final_if");
            } else {
                Log.e(TAG, enterTime + " false " + System.currentTimeMillis() + " final_else");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (serviceDetailData.getServiceMasterType().equals("fixed")) {
            if (startTime != null && startTime.length() > 0) {
                if (address != null && address.length() > 0) {
                    if (description != null && description.length() > 0) {
                        if (enterTime > System.currentTimeMillis()) {
                            return true;
                        } else {
                            binding.tilStarttime.setError(getString(R.string.past_time_validation));
                            return false;
                        }
                    } else {
                        binding.tilDescription.setError(getString(R.string.please_enter_description));
                        return false;
                    }
                } else {
                    binding.edtServiceAddress.setError(getString(R.string.please_enter_address));
                    return false;
                }

            } else {
                binding.tilStarttime.setError(getString(R.string.please_enter_start_time));
                return false;
            }
        } else {
            if (startTime != null && startTime.length() > 0) {
                if (selectedHour > 0) {
                    if (address != null && address.length() > 0) {
                        if (description != null && description.length() > 0) {
                            if (enterTime > System.currentTimeMillis()) {
                                return true;
                            } else {
                                binding.tilStarttime.setError(getString(R.string.past_time_validation));
                                return false;
                            }
                        } else {
                            binding.tilDescription.setError(getString(R.string.please_enter_description));
                            return false;
                        }
                    } else {
                        binding.edtServiceAddress.setError(getString(R.string.please_enter_address));
                        return false;
                    }
                } else {
                    Toast.makeText(context, getString(R.string.please_select_hours_first),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                binding.tilStarttime.setError(getString(R.string.please_enter_start_time));
                return false;
            }
        }
    }

    @Override
    public void onDestroy() {
        Utils.cancelAsyncTask(bookServiceAsync);
        super.onDestroy();
    }
}
