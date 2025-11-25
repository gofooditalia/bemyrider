package com.app.bemyrider.activity.user;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;

import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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

/**
 * Modified by Hardik Talaviya on 10/12/19.
 */

public class SearchServiceActivity extends AppCompatActivity {

    private static final String TAG = "SearchServiceActivity";
    private ActivitySearchServicesBinding binding;
    private LatLng selectedLatLng = new LatLng(0.0, 0.0);
    private ServiceListPojo serviceListPojowithitem;
    private String selectedCatId = "", actualDate = "", selectedServiceId = "",
            selectedServiceName = "", selectedSubCatId = "";
    private ArrayList<ServiceDataItem> serviceDataItems = new ArrayList<>();
    private ArrayAdapter serviceadapter;
    private AsyncTask getServiceAsync;
    private Context context;
    private ConnectionManager connectionManager;
    ActivityResultLauncher<Intent> locationActivityResultLauncher;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(SearchServiceActivity.this, R.layout.activity_search_services, null);

        init();

        if (!PrefsUtil.with(SearchServiceActivity.this).readString("customer_address").equals("")) {
            binding.txtLocation.setText(PrefsUtil.with(SearchServiceActivity.this).readString("customer_address"));

            Geocoder coder = new Geocoder(this);
            List<Address> address;

            try {
                address = coder.getFromLocationName(PrefsUtil.with(SearchServiceActivity.this).readString("customer_address"), 5);
                if (address != null) {
                    Address location = address.get(0);
                    selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        binding.txtLocation.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ID,
                    Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS))
                    .build(SearchServiceActivity.this);
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
                PrefsUtil.with(SearchServiceActivity.this).write("search_address",
                        binding.txtLocation.getText().toString());
                PrefsUtil.with(SearchServiceActivity.this).write("bookingLat",
                        String.valueOf(selectedLatLng.latitude));
                PrefsUtil.with(SearchServiceActivity.this).write("bookingLong",
                        String.valueOf(selectedLatLng.longitude));

                Intent i = new Intent(SearchServiceActivity.this,
                        ProviderListActivity.class);
                i.putExtra("categoryId", selectedCatId);
                i.putExtra("subCategoryId", selectedSubCatId);
                i.putExtra("serviceId", selectedServiceId);
                i.putExtra("serviceName", selectedServiceName);
                i.putExtra("address", binding.txtLocation.getText().toString());
                i.putExtra("latitude", String.valueOf(selectedLatLng.latitude));
                i.putExtra("longitude", String.valueOf(selectedLatLng.longitude));
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
//                selectedLatLng = new LatLng(0d, 0d);
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
//                    new ConnectionCheck().showDialogWithMessage(context, getString(R.string.sync_data_message)).show();
                    Log.e("HomeActivity", "disconnected");
                    EventBus.getDefault().post(new MessageEvent("connection", "disconnected"));
                }
            }
        };
    }

    private void init() {
        context = SearchServiceActivity.this;

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.search_service),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ServiceListItem dummyItem = new ServiceListItem();
        dummyItem.setServiceId("0");
        dummyItem.setServiceName(getResources().getString(R.string.no_services_found));
        List<ServiceListItem> dummylist = new ArrayList<>();
        dummylist.add(dummyItem);
        ServiceListPojo dummypojo = new ServiceListPojo();
        dummypojo.setData(dummylist);
        serviceListPojowithitem = dummypojo;

        serviceadapter = new ArrayAdapter<>
                (SearchServiceActivity.this,
                        R.layout.spinner_item_inverse, serviceDataItems);
        serviceadapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        serviceDataItems.add(new ServiceDataItem("0", getString(R.string.select_service)));
        binding.spSelectService.setAdapter(serviceadapter);

        locationActivityForResult();
    }

    private void locationActivityForResult() {
        locationActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                try {
                    if (result.getResultCode() == RESULT_OK) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        Log.i("AUTO COMPLETE", "Place: " + place.getName() + ", " + place.getId());
                        Utils.hideSoftKeyboard(SearchServiceActivity.this);
                        try {
                            selectedLatLng = place.getLatLng();
                            binding.txtLocation.setText(place.getName());
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
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
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

        if (selectedServiceId.equals("") || selectedServiceId.equals("0")) {
            Toast.makeText(SearchServiceActivity.this, R.string.provide_service_id, Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(SearchServiceActivity.this);
//        builder.setMessage(R.string.sure_exit_app)
//                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        finish();
//                    }
//                })
//                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.cancel();
//                    }
//                }).show();
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
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(getServiceAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
