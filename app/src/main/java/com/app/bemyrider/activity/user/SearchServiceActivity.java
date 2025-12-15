package com.app.bemyrider.activity.user;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivitySearchServicesBinding;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.ServiceDataItem;
import com.app.bemyrider.model.ServiceListPOJO;
import com.app.bemyrider.model.partner.ServiceListItem;
import com.app.bemyrider.model.partner.ServiceListPojo;
import com.app.bemyrider.utils.ConnectionManager;
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

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Modified by Hardik Talaviya on 10/12/19.
 */

public class SearchServiceActivity extends AppCompatActivity {

    private ActivitySearchServicesBinding binding;
    private LatLng selectedLatLng = new LatLng(0.0, 0.0);
    private String selectedCatId = "", actualDate = "", selectedServiceId = "",
            selectedServiceName = "", selectedSubCatId = "";
    private ArrayList<ServiceDataItem> serviceDataItems = new ArrayList<>();
    private ArrayAdapter<ServiceDataItem> serviceadapter;
    private AsyncTask<Void, Void, ServiceListPOJO> getServiceAsync;
    private ConnectionManager connectionManager;
    private ActivityResultLauncher<Intent> locationActivityResultLauncher;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_services);

        init();

        if (!Objects.equals(PrefsUtil.with(this).readString("customer_address"), "")) {
            binding.txtLocation.setText(PrefsUtil.with(this).readString("customer_address"));

            Geocoder coder = new Geocoder(this);
            List<Address> address;

            try {
                address = coder.getFromLocationName(PrefsUtil.with(this).readString("customer_address"), 5);
                if (address != null) {
                    Address location = address.get(0);
                    selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                }

            } catch (IOException e) {
                Log.e("SearchServiceActivity", "Error getting location from address");
            }
        }

        binding.txtLocation.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION, Place.Field.FORMATTED_ADDRESS);
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this);
            locationActivityResultLauncher.launch(intent);
        });

        getService();

        binding.spSelectService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedServiceId = serviceDataItems.get(position).getServiceId();
                selectedServiceName = serviceDataItems.get(position).getServiceName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.btnSearch.setOnClickListener(v -> {
            if (checkValidation()) {
                PrefsUtil.with(this).write("search_address",
                        Objects.requireNonNull(binding.txtLocation.getText()).toString());
                if (selectedLatLng != null) {
                    PrefsUtil.with(this).write("bookingLat",
                            String.valueOf(selectedLatLng.latitude));
                    PrefsUtil.with(this).write("bookingLong",
                            String.valueOf(selectedLatLng.longitude));
                }

                Intent i = new Intent(this,
                        ProviderListActivity.class);
                i.putExtra("categoryId", selectedCatId);
                i.putExtra("subCategoryId", selectedSubCatId);
                i.putExtra("serviceId", selectedServiceId);
                i.putExtra("serviceName", selectedServiceName);
                i.putExtra("address", Objects.requireNonNull(binding.txtLocation.getText()).toString());
                if (selectedLatLng != null) {
                    i.putExtra("latitude", String.valueOf(selectedLatLng.latitude));
                    i.putExtra("longitude", String.valueOf(selectedLatLng.longitude));
                }
                i.putExtra("date", actualDate);
                startActivity(i);
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

        new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (new ConnectionCheck().isNetworkConnected(context)) {
                    Log.e("HomeActivity", "connected");
                    EventBus.getDefault().post(new MessageEvent("connection", "connected"));
                } else {
                    Log.e("HomeActivity", "disconnected");
                    EventBus.getDefault().post(new MessageEvent("connection", "disconnected"));
                }
            }
        };
    }

    private void init() {
        Context context = this;

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.search_service), HtmlCompat.FROM_HTML_MODE_LEGACY));

        ServiceListItem dummyItem = new ServiceListItem();
        dummyItem.setServiceId("0");
        dummyItem.setServiceName(getResources().getString(R.string.no_services_found));
        List<ServiceListItem> dummylist = new ArrayList<>();
        dummylist.add(dummyItem);
        ServiceListPojo dummypojo = new ServiceListPojo();
        dummypojo.setData(dummylist);

        serviceadapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_inverse, serviceDataItems);
        serviceadapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        serviceDataItems.add(new ServiceDataItem("0", getString(R.string.select_service)));
        binding.spSelectService.setAdapter(serviceadapter);

        locationActivityForResult();
    }

    private void locationActivityForResult() {
        locationActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            try {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    Log.i("AUTO COMPLETE", "Place: " + place.getDisplayName() + ", " + place.getId());
                    Utils.hideSoftKeyboard(this);
                    if (place.getLocation() != null) {
                        selectedLatLng = place.getLocation();
                        binding.txtLocation.setText(place.getDisplayName());
                    } else {
                        Log.e("SearchServiceActivity", "Place location is null");
                    }
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    Log.i("AUTO COMPLETE", status.getStatusMessage());
                }
            } catch (Exception e) {
                Log.e("SearchServiceActivity", "Error in locationActivityResult");
            }
        });
    }


    /*--------------- Get Service Api Call ------------------*/
    private void getService() {
        binding.spSelectService.setVisibility(View.GONE);
        binding.pgService.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("user_type", "c");
        textParams.put("subcategory_id", "0");

        new WebServiceCall(this, WebServiceUrl.URL_SERVICELIST, textParams,
                ServiceListPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgService.setVisibility(View.GONE);
                binding.spSelectService.setVisibility(View.VISIBLE);
                if (status) {
                    ServiceListPOJO serviceListPojo = (ServiceListPOJO) obj;
                    serviceDataItems.clear();
                    serviceDataItems.add(new ServiceDataItem("0", getString(R.string.select_service)));
                    serviceDataItems.addAll(serviceListPojo.getData());
                    serviceadapter.notifyDataSetChanged();
                    binding.spSelectService.setSelection(0);
                } else {
                    Toast.makeText(SearchServiceActivity.this, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                getServiceAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                getServiceAsync = null;
            }
        });
    }

    private boolean checkValidation() {

        return !selectedServiceId.equals("") && !selectedServiceId.equals("0");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            Log.e("SearchServiceActivity", "Error unregistering receiver");
        }
        Utils.cancelAsyncTask(getServiceAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
