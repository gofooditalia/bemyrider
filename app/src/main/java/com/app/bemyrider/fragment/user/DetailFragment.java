package com.app.bemyrider.fragment.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.FragmentDetailBinding;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.DetailViewModel;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Modernized by Gemini on 2024.
 */
public class DetailFragment extends Fragment {

    private static final String TAG = "DetailFragment";
    private FragmentDetailBinding binding;
    private ProviderServiceDetailsItem serviceDetailData;
    private Context context;
    private ActivityResultLauncher<Intent> locationActivityResultLauncher;
    private DetailViewModel viewModel;
    
    long enterTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        if (getArguments() != null) {
            serviceDetailData = (ProviderServiceDetailsItem) getArguments().getSerializable("data");
        }

        init();

        if (!Places.isInitialized()) {
            Places.initialize(context.getApplicationContext(),
                    getResources().getString(R.string.google_api_key));
        }

        binding.edtServiceAddress.setText(PrefsUtil.with(context).readString("search_address"));

        binding.edtServiceAddress.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION, Place.Field.FORMATTED_ADDRESS);
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
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
                performBookService();
            }
        });

        setupTextWatchers();

        return binding.getRoot();
    }

    private void init() {
        context = getActivity();
        binding.edtStarttime.setInputType(InputType.TYPE_NULL);
        locationActivityResult();
    }

    private void locationActivityResult() {
        locationActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            try {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    Log.i("AUTO COMPLETE", "Place: " + place.getDisplayName() + ", " + place.getId());
                    if (place.getLocation() != null) {
                        PrefsUtil.with(context).write("bookingLat", String.valueOf(place.getLocation().latitude));
                        PrefsUtil.with(context).write("bookingLong", String.valueOf(place.getLocation().longitude));
                    }
                    PrefsUtil.with(context).write("search_address", place.getFormattedAddress());
                    binding.edtServiceAddress.setText(place.getFormattedAddress());
                } else if (result.getResultCode() != RESULT_CANCELED) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    Log.i("AUTO COMPLETE", status.getStatusMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        } else {
            binding.spnSelectHours.setVisibility(View.VISIBLE);
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
                SimpleDateFormat format1 = new SimpleDateFormat("HH:mm", Locale.US);
                binding.edtStarttime.setText(String.format("%s %s", format.format(selectedTime.getTime()), format1.format(selectedTime.getTime())));
            }, true);
            tpd.setAccentColor(ContextCompat.getColor(context, R.color.button));
            tpd.setTimeInterval(1, 15);
            if (DateUtils.isToday(selectedTime.getTimeInMillis())) {
                tpd.setMinTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
                        now.get(Calendar.SECOND));
            }
            tpd.show(getChildFragmentManager(), "TimePickerDialog"); // Updated getFragmentManager -> getChildFragmentManager

        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dpd.setAccentColor(ContextCompat.getColor(context, R.color.button));
        dpd.setMinDate(now);
        dpd.show(getChildFragmentManager(), "DatePickerDialog"); // Updated
    }

    private void performBookService() {
        binding.pgSendRequest.setVisibility(View.VISIBLE);

        String providerServiceId = serviceDetailData.getId();
        String loginServiceId = PrefsUtil.with(context).readString("UserId");
        String serviceStartTime = binding.edtStarttime.getText().toString();
        
        String providerServiceHours = "";
        String selHours = "";
        if ((serviceDetailData.getServiceMasterType().equalsIgnoreCase("fixed"))) {
            providerServiceHours = serviceDetailData.getHours();
        } else if ((serviceDetailData.getServiceMasterType().equalsIgnoreCase("hourly"))) {
            selHours = String.valueOf(binding.spnSelectHours.getSelectedItemPosition());
        }
        
        String userId = PrefsUtil.with(context).readString("UserId");
        String serviceAddress = PrefsUtil.with(context).readString("search_address");
        String serviceDetails = Utils.encodeEmoji(binding.edtServiceDescription.getText().toString());
        String bookingLat = PrefsUtil.with(context).readString("bookingLat");
        String bookingLong = PrefsUtil.with(context).readString("bookingLong");
        String deliveryType = PrefsUtil.with(context).readString("delivery_type");
        String requestType = PrefsUtil.with(context).readString("request_type");

        viewModel.bookService(providerServiceId, loginServiceId, serviceStartTime, providerServiceHours, selHours, userId, serviceAddress, serviceDetails, bookingLat, bookingLong, deliveryType, requestType)
            .observe(getViewLifecycleOwner(), commonPojo -> {
                binding.pgSendRequest.setVisibility(View.GONE);
                binding.btnSendRequest.setClickable(true);
                
                if (commonPojo != null) {
                    if (commonPojo.isStatus()) {
                        Toast.makeText(context, commonPojo.getMessage(), Toast.LENGTH_LONG).show();
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    } else {
                        Toast.makeText(context, commonPojo.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private boolean checkBookServiceValidation(String address, String startTime, String description,
                                               int selectedHour) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US);
            Date strDate = format.parse(startTime);
            if (strDate != null) {
                enterTime = strDate.getTime();
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
    
    private void setupTextWatchers() {
        binding.edtStarttime.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilStarttime.setErrorEnabled(false);
                binding.tilStarttime.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.edtServiceAddress.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.edtServiceAddress.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.edtServiceDescription.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilDescription.setErrorEnabled(false);
                binding.tilDescription.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
