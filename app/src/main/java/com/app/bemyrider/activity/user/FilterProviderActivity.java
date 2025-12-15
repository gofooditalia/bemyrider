package com.app.bemyrider.activity.user;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.format.DateFormat;

import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityFilterProviderBinding;
import com.app.bemyrider.model.MinMaxPricePojo;
import com.app.bemyrider.model.ServiceDataItem;
import com.app.bemyrider.model.ServiceListPOJO;
import com.app.bemyrider.model.user.CategoryDataItem;
import com.app.bemyrider.model.user.FilterDataPOJO;
import com.app.bemyrider.model.partner.SubCategoryItem;
import com.app.bemyrider.model.partner.SubCategoryListPojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.slider.RangeSlider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Modified by Hardik Talaviya on 9/12/19.
 */

public class FilterProviderActivity extends AppCompatActivity {

    private ActivityFilterProviderBinding binding;
    //Filter services from search
    //    edt_time,
    private DialogFragment dateFragment, timeFragment;
    //    categoryId, serviceId, subCategoryId,
    private String serviceName, address, latitude, longitude, date, time;
    private ArrayList<SubCategoryItem> subCategoryItems = new ArrayList<>();
    private ArrayList<CategoryDataItem> categoryDataItems = new ArrayList<>();
    private ArrayList<ServiceDataItem> serviceDataItems = new ArrayList<>();
    private ArrayAdapter subCatadapter;
    private ArrayAdapter catadapter;
    private ArrayAdapter serviceadapter;
    private ServiceListPOJO serviceListPojowithitem;
    private String selectedCatId = "", selectedServiceId = "", actualDate = "", selectedServiceName = "", selectedSubCatId = "",
            minprice = "", maxprice = "";
    private Date currentDate;
    private int year, month, day;
    private AsyncTask minMaxPriceAsync, subCatAsync, providerListAsync, serviceListAsync;
    private Context context;
    private ConnectionManager connectionManager;
    ActivityResultLauncher<Intent> locationActivityResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(FilterProviderActivity.this, R.layout.activity_filter_provider, null);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));
        }

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.filter_by),HtmlCompat.FROM_HTML_MODE_LEGACY));

        serviceName = getIntent().getStringExtra("serviceName");
        address = getIntent().getStringExtra("address");
        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");

        init();

        ServiceDataItem dummyItem = new ServiceDataItem("0", getString(R.string.select_service));
        List<ServiceDataItem> dummylist = new ArrayList<>();
        dummylist.add(dummyItem);
        ServiceListPOJO dummypojo = new ServiceListPOJO();
        dummypojo.setData(dummylist);
        serviceListPojowithitem = dummypojo;

        dateFragment = new SelectDateFragment();
        timeFragment = new TimePickerFragment();
        binding.txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ID,
                        Place.Field.DISPLAY_NAME, Place.Field.LOCATION, Place.Field.FORMATTED_ADDRESS))
                        .build(FilterProviderActivity.this);
                locationActivityResultLauncher.launch(intent);

            }
        });

        binding.txtLocation.setText(address);
        binding.EdtProviderName.setText(PrefsUtil.with(FilterProviderActivity.this).readString("search_providerName"));
        binding.EdtSearchKeyword.setText(PrefsUtil.with(FilterProviderActivity.this).readString("search_searchKeyword"));
        binding.EdtDate.setText(PrefsUtil.with(FilterProviderActivity.this).readString("search_displayDate"));
        try {
            binding.ratingbarFilter.setRating(Float.parseFloat(PrefsUtil.with(FilterProviderActivity.this).readString("search_rating")));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        serviceCallMinMaxPrice();

        getList();

        final Calendar calendar = Calendar.getInstance();

        binding.EdtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate = new Date();
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                year = calendar.get(Calendar.YEAR);

                DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        binding.tilDate.setError(null);

                        calendar.set(calendar.YEAR, i);
                        calendar.set(calendar.MONTH, i1);
                        calendar.set(calendar.DAY_OF_MONTH, i2);

                        SimpleDateFormat sdfPassDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        actualDate = sdfPassDate.format(calendar.getTime());

                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                        binding.EdtDate.setText(sdf.format(calendar.getTime()));
                    }
                };

                DatePickerDialog.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        binding.EdtDate.setText("");
                    }
                };

                DatePickerDialog datePickerDialog = new DatePickerDialog(FilterProviderActivity.this, onDateSetListener, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(currentDate.getTime());
                datePickerDialog.setOnCancelListener(onCancelListener);
                datePickerDialog.show();
            }
        });

        binding.rangeSeekbar5.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                minprice = String.valueOf(slider.getValues().get(0));
                maxprice = String.valueOf(slider.getValues().get(1));
                binding.txtShowPrize.setText(String.format("%s%s - %s%s", PrefsUtil.with(FilterProviderActivity.this).readString("CurrencySign"), minprice, PrefsUtil.with(FilterProviderActivity.this).readString("CurrencySign"), maxprice));
            }
        });

        binding.BtnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    Intent i = new Intent(FilterProviderActivity.this, ProviderListActivity.class);
                    Log.e("Intent", "onClick: " + selectedSubCatId);
                    PrefsUtil.with(FilterProviderActivity.this).write("search_catId", selectedCatId);
                    PrefsUtil.with(FilterProviderActivity.this).write("search_subcatId", selectedSubCatId);
                    PrefsUtil.with(FilterProviderActivity.this).write("search_serviceId", selectedServiceId);
                    PrefsUtil.with(FilterProviderActivity.this).write("search_actualDate", actualDate);
                    PrefsUtil.with(FilterProviderActivity.this).write("search_displayDate", binding.EdtDate.getText().toString().trim());
                    PrefsUtil.with(FilterProviderActivity.this).write("search_providerName", binding.EdtProviderName.getText().toString().trim());
                    PrefsUtil.with(FilterProviderActivity.this).write("search_searchKeyword", binding.EdtSearchKeyword.getText().toString().trim());
                    PrefsUtil.with(FilterProviderActivity.this).write("search_rating", String.valueOf(binding.ratingbarFilter.getRating()));
                    i.putExtra("categoryId", selectedCatId);
                    i.putExtra("subCategoryId", selectedSubCatId);
                    i.putExtra("serviceId", selectedServiceId);
                    i.putExtra("serviceName", selectedServiceName);
                    i.putExtra("address", binding.txtLocation.getText().toString());
                    i.putExtra("latitude", latitude);
                    i.putExtra("longitude", longitude);
                    i.putExtra("date", actualDate);
                    i.putExtra("providerName", binding.EdtProviderName.getText().toString());
                    i.putExtra("searchKeyWord", binding.EdtSearchKeyword.getText().toString());
                    i.putExtra("rating", String.valueOf(binding.ratingbarFilter.getRating()));
//                    i.putExtra("time", edt_time.getText().toString());
                    i.putExtra("minRate", minprice);
                    i.putExtra("maxRate", maxprice);
                    startActivity(i);
                    finish();
                }
            }
        });

        binding.txtLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilLocation.setError("");
                binding.tilLocation.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.EdtDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilDate.setError("");
                binding.tilDate.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void init() {
        context = FilterProviderActivity.this;
        setupToolBar();

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        catadapter = new ArrayAdapter<>
                (FilterProviderActivity.this,
                        android.R.layout.simple_spinner_item,
                        categoryDataItems);
        catadapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        categoryDataItems.add(new CategoryDataItem("0", getString(R.string.select_category)));
        binding.spSelectCat.setAdapter(catadapter);


        subCatadapter = new ArrayAdapter<>(FilterProviderActivity.this,
                android.R.layout.simple_spinner_item, subCategoryItems);
        subCatadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subCategoryItems.add(new SubCategoryItem("0", getString(R.string.select_subcategory)));
        binding.spSelectSubcat.setAdapter(subCatadapter);

        serviceadapter = new ArrayAdapter<>
                (FilterProviderActivity.this,
                        android.R.layout.simple_spinner_item, serviceDataItems);
        serviceadapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        serviceDataItems.add(new ServiceDataItem("0", getString(R.string.select_service)));
        binding.spSelectService.setAdapter(serviceadapter);
        locationActivityResult();
    }

    private void locationActivityResult() {
        locationActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                try {
                    if (result.getResultCode() == RESULT_OK) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        Log.i("AUTO COMPLETE", "Place: " + place.getDisplayName() + ", " + place.getId());
                        try {
                            latitude = String.valueOf(place.getLocation().latitude);
                            longitude = String.valueOf(place.getLocation().longitude);
                            binding.txtLocation.setText(place.getDisplayName());
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

    private void setupToolBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_remove_resize);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private boolean checkValidation() {
        if (binding.spSelectCat.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getString(R.string.please_select_category), Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.spSelectSubcat.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getString(R.string.please_select_subcategory), Toast.LENGTH_SHORT).show();
            //Toast.makeText(MainActivity.this, R.string.select_valid_address, Toast.LENGTH_LONG).show();
            return false;
        } else if (binding.spSelectService.getSelectedItemPosition() == 0) {
            Toast.makeText(FilterProviderActivity.this, R.string.provide_service_id, Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    /*----------------- Min Max Price Api Call -------------------*/
    private void serviceCallMinMaxPrice() {
        binding.rangeSeekbar5.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        new WebServiceCall(FilterProviderActivity.this,
                WebServiceUrl.URL_SERVICE_MIN_MAX_PRICE, textParams, MinMaxPricePojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.progress.setVisibility(View.GONE);
                        binding.rangeSeekbar5.setVisibility(View.VISIBLE);
                        if (status) {
                            MinMaxPricePojo minMaxPricePojo = (MinMaxPricePojo) obj;
                            minprice = String.valueOf((Float.parseFloat(minMaxPricePojo.getData().getMinPrice()) - 10.0f));
                            if ((Float.parseFloat(minMaxPricePojo.getData().getMinPrice()) - 10.0f) < 0) {
                                minprice = "0";
                            }
                            maxprice = String.valueOf((Float.parseFloat(minMaxPricePojo.getData().getMaxPrice()) + 10.0f));
                            binding.txtShowPrize.setText(String.format("%s%s - %s%s", PrefsUtil.with(FilterProviderActivity.this).readString("CurrencySign"), minprice, PrefsUtil.with(FilterProviderActivity.this).readString("CurrencySign"), maxprice));
                            binding.rangeSeekbar5.setValueFrom(Float.parseFloat(minprice));
                            binding.rangeSeekbar5.setValueTo(Float.parseFloat(maxprice));
                            if ((Float.parseFloat(minMaxPricePojo.getData().getMinPrice()) - 10.0f) < 0) {
                                binding.rangeSeekbar5.setValueFrom(Float.parseFloat(minprice));
                            }

                        } else {
                            Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        minMaxPriceAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        minMaxPriceAsync = null;
                    }
                });
    }

    /*----------------- Get Sub category Api Call --------------------*/
    private void getSubCatId() {
        binding.spSelectSubcat.setVisibility(View.GONE);
        binding.pgSubCat.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("category_id", selectedCatId);

        new WebServiceCall(FilterProviderActivity.this, WebServiceUrl.URL_SUBCATEGORYLIST,
                textParams, SubCategoryListPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgSubCat.setVisibility(View.GONE);
                binding.spSelectSubcat.setVisibility(View.VISIBLE);
                if (status) {
                    subCategoryItems.clear();
                    SubCategoryListPojo subCategoryListPojo = (SubCategoryListPojo) obj;
                    subCategoryItems.add(new SubCategoryItem("0", getString(R.string.select_subcategory)));
                    subCategoryItems.addAll(subCategoryListPojo.getData());
                    subCatadapter.notifyDataSetChanged();
                    binding.spSelectSubcat.setSelection(0);

                    for (int i = 0; i < subCategoryListPojo.getData().size(); i++) {
                        if (subCategoryListPojo.getData().get(i).getCategoryId().equalsIgnoreCase(PrefsUtil.with(FilterProviderActivity.this).readString("search_subcatId"))) {
                            binding.spSelectSubcat.setSelection(i + 1);
                            break;
                        }
                    }
                } else {
                    Toast.makeText(FilterProviderActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                subCatAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                subCatAsync = null;
            }
        });

    }

    /*--------- Get List(Cat,Sub Cat,Service Id) Api Call ----------*/
    private void getList() {
        binding.spSelectCat.setVisibility(View.GONE);
        binding.spSelectSubcat.setVisibility(View.GONE);
        binding.spSelectService.setVisibility(View.GONE);
        binding.pgCat.setVisibility(View.VISIBLE);
        binding.pgSubCat.setVisibility(View.VISIBLE);
        binding.pgService.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("serviceId", getIntent().getStringExtra("serviceId"));

        new WebServiceCall(this, WebServiceUrl.URL_GETLIST, textParams, FilterDataPOJO.class,
                false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgCat.setVisibility(View.GONE);
                binding.pgSubCat.setVisibility(View.GONE);
                binding.pgService.setVisibility(View.GONE);
                binding.spSelectCat.setVisibility(View.VISIBLE);
                binding.spSelectSubcat.setVisibility(View.VISIBLE);
                binding.spSelectService.setVisibility(View.VISIBLE);
                if (status) {
                    FilterDataPOJO filterDataPOJO = (FilterDataPOJO) obj;
                    categoryDataItems.addAll(filterDataPOJO.getFilterData().getCategoryList());
                    catadapter.notifyDataSetChanged();

                    for (int i = 0; i < filterDataPOJO.getFilterData().getCategoryList().size(); i++) {
                        if (filterDataPOJO.getFilterData().getCategoryList().get(i).getSelected().equalsIgnoreCase("y")) {
                            selectedCatId = filterDataPOJO.getFilterData().getCategoryList().get(i).getCategoryId();
                            Log.e("Filter ", filterDataPOJO.getFilterData().getCategoryList().get(i).getCategoryId());
                            binding.spSelectCat.setSelection(i + 1, false);
                            break;
                        }
                    }

                    subCategoryItems.clear();
                    subCategoryItems.add(new SubCategoryItem("0", getString(R.string.select_subcategory)));
                    subCategoryItems.addAll(filterDataPOJO.getFilterData().getSubCategoryList());
                    subCatadapter.notifyDataSetChanged();
                    binding.spSelectSubcat.setSelection(0, false);

                    for (int i = 0; i < filterDataPOJO.getFilterData().getSubCategoryList().size(); i++) {
                        if (filterDataPOJO.getFilterData().getSubCategoryList().get(i).getSelected().equalsIgnoreCase("y")) {
                            selectedSubCatId = filterDataPOJO.getFilterData().getSubCategoryList().get(i).getCategoryId();
                            binding.spSelectSubcat.setSelection(i + 1, false);
                            break;
                        }
                    }


                    serviceDataItems.clear();
                    serviceDataItems.add(new ServiceDataItem("0", "Select Service*"));
                    serviceDataItems.addAll(filterDataPOJO.getFilterData().getServicesList());
                    serviceadapter.notifyDataSetChanged();
                    binding.spSelectService.setSelection(0, false);

                    for (int i = 0; i < filterDataPOJO.getFilterData().getServicesList().size(); i++) {
                        if (filterDataPOJO.getFilterData().getServicesList().get(i).getSelected().equalsIgnoreCase("y")) {
                            selectedServiceId = filterDataPOJO.getFilterData().getServicesList().get(i).getServiceId();
                            selectedServiceName = filterDataPOJO.getFilterData().getServicesList().get(i).getServiceName();
                            binding.spSelectService.setSelection(i + 1, false);
                            break;
                        }
                    }

                    binding.spSelectCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCatId = categoryDataItems.get(position).getCategoryId();

                            if (categoryDataItems.get(position).getCategoryId().equals("0")) {
                                subCategoryItems.clear();
                                subCategoryItems.add(new SubCategoryItem("0", getString(R.string.select_subcategory)));
                                subCatadapter.notifyDataSetChanged();
                            } else {
                                getSubCatId();

                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    binding.spSelectSubcat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            selectedSubCatId = subCategoryItems.get(position).getCategoryId();
                            if (subCategoryItems.get(position).getCategoryId().equals("0")) {
                                serviceDataItems.clear();
                                serviceDataItems.add(new ServiceDataItem("0", getString(R.string.select_service)));
                                serviceadapter.notifyDataSetChanged();
                            } else {
                                getService();
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    binding.spSelectService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (binding.spSelectService.getSelectedItemPosition() != 0) {
                                selectedServiceId = serviceDataItems.get(position).getServiceId();
                                selectedServiceName = serviceDataItems.get(position).getServiceName();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                } else {
                    Toast.makeText(FilterProviderActivity.this, (String) obj,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                providerListAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                providerListAsync = null;
            }
        });
    }

    /*-------------- Get Service Api Call ----------------*/
    private void getService() {
        binding.spSelectService.setVisibility(View.GONE);
        binding.pgService.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("subcategory_id", selectedSubCatId);

        new WebServiceCall(this, WebServiceUrl.URL_SERVICELIST, textParams,
                ServiceListPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgService.setVisibility(View.GONE);
                binding.spSelectService.setVisibility(View.VISIBLE);
                if (status) {
                    ServiceListPOJO serviceListPojo = (ServiceListPOJO) obj;
                    serviceDataItems.clear();
                    serviceDataItems.add(new ServiceDataItem("0", "Select Service*"));
                    serviceDataItems.addAll(serviceListPojo.getData());
                    serviceadapter.notifyDataSetChanged();
                    binding.spSelectService.setSelection(0);

                    for (int i = 0; i < serviceListPojo.getData().size(); i++) {
                        if (serviceListPojo.getData().get(i).getServiceId().equalsIgnoreCase(PrefsUtil.with(FilterProviderActivity.this).readString("search_serviceId"))) {
                            binding.spSelectService.setSelection(i + 1);
                            break;
                        }
                    }
                } else {
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                serviceListAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                serviceListAsync = null;
            }
        });
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
        Utils.cancelAsyncTask(minMaxPriceAsync);
        Utils.cancelAsyncTask(subCatAsync);
        Utils.cancelAsyncTask(providerListAsync);
        Utils.cancelAsyncTask(serviceListAsync);
        super.onDestroy();
    }

    @SuppressLint("ValidFragment")
    public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm + 1, dd);
        }

        public void populateSetDate(int year, int month, int day) {
            binding.EdtDate.setText(month + "/" + day + "/" + year);
        }

    }

    @SuppressLint("ValidFragment")
    public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //Use the current time as the default values for the time picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            //Create and return a new instance of TimePickerDialog
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
