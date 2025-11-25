package com.app.bemyrider.fragment.user;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.bemyrider.Adapter.User.PopularTaskersAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.FragmentTaskerListBinding;
import com.app.bemyrider.model.user.PopularTaskerItem;
import com.app.bemyrider.model.user.PopularTaskerPOJO;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PermissionManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 9/12/19.
 */

public class TaskersListFragment extends Fragment {

    private FragmentTaskerListBinding binding;
    private PopularTaskersAdapter providerAdapter;
    private ArrayList<PopularTaskerItem> arrayList;
    private LinearLayoutManager layoutManager;
    private String categoryId = "";
    private AsyncTask taskerListAsync;
    private Context context;
    /*----------------- Get Location Variable -------------------*/
    private PermissionManager permissionManager;
    private int ACCESS_FINE_LOCATION_SERVICE_CODE = 732;
    private FusedLocationProviderClient client;
    private LocationCallback locationCallback;
    private LocationRequest request;
    private double curLat = 0.0, curLng = 0.0;
    /*-----------------------------------------------------------*/
    private static final String TAG = "TaskersListFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tasker_list, container, false);
        if (getArguments() != null) {
            categoryId = getArguments().getString(Utils.CATEGORY_ID);
        }

        init();

        /*if (permissionManager.checkAndRequestPermissions(PermissionManager.ACCESS_FINE_LOCATION, PermissionManager.ACCESS_FINE_LOCATION_SERVICE_CODE, "setupLocation")) {
            setupLocationService();
        }*/

//        context.registerReceiver(gpsCheck, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        return binding.getRoot();
    }

    private void init() {
        context = getActivity();
        permissionManager = new PermissionManager(context);

        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        binding.rvTaskers.setLayoutManager(layoutManager);
        binding.rvTaskers.setItemAnimator(new DefaultItemAnimator());

        arrayList = new ArrayList<>();
        providerAdapter = new PopularTaskersAdapter(getActivity(), arrayList);
        binding.rvTaskers.setAdapter(providerAdapter);
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 555) {
            Log.e(TAG, "resultCode :: " + resultCode);
            if (resultCode == RESULT_CANCELED) {
                setupLocationService();
            } else {
                requestLocationUpdates();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }*/

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionManager.ACCESS_FINE_LOCATION_SERVICE_CODE) {
            if (permissionManager.onRequestPermissionResult(requestCode, PermissionManager.ACCESS_FINE_LOCATION, grantResults)) {
                if (permissionManager.currentCodePos().equals("setupLocation")) {
                    setupLocationService();
                }
            } else {
                if (permissionManager.currentCodePos().equals("setupLocation")) {
                    if (permissionManager.checkAndRequestPermissions(PermissionManager.ACCESS_FINE_LOCATION, PermissionManager.ACCESS_FINE_LOCATION_SERVICE_CODE, "setupLocation")) {
                        setupLocationService();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/

    /*---------- For Gps Check If User Turn Off Gps -----------*/
    private BroadcastReceiver gpsCheck = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
                Log.e(TAG, "onReceive :: ");
                if (permissionManager.checkAndRequestPermissions(PermissionManager.ACCESS_FINE_LOCATION, PermissionManager.ACCESS_FINE_LOCATION_SERVICE_CODE, "setupLocation")) {
                    setupLocationService();
                }
            }
        }
    };

    /*------------ Allow Gps --------------*/
    private void setupLocationService() {
        LocationRequest request = new LocationRequest()
                .setFastestInterval(2000)
                .setInterval(5000)
                .setNumUpdates(1)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.e(TAG, "Setting up location service");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(((Activity) context), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.e(TAG, "Location settings satisfied");
                requestLocationUpdates();
            }
        });

        task.addOnFailureListener(((Activity) context), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure Exception :: " + e.getMessage());
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        Log.e(TAG, "Location settings not satisfied, attempting resolution intent");
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(((Activity) context), 555);
                            Log.e(TAG, "Resolvable :: " + resolvable);
                        } catch (IntentSender.SendIntentException sendIntentException) {
                            Log.e(TAG, "Unable to start resolution intent");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.e(TAG, "Location settings not satisfied and can't be changed");
                        break;
                }
            }
        });
    }

    /*------------ Get Location -------------*/
    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setFastestInterval(2000)
                .setInterval(5000)
                .setNumUpdates(1)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client = LocationServices.getFusedLocationProviderClient(context);


        final int[] permission = {ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)};
        if (permission[0] == PackageManager.PERMISSION_GRANTED) {

            final Location[] location = {new Location(LocationManager.GPS_PROVIDER)};
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    location[0] = locationResult.getLastLocation();

                    if (location[0] != null) {
                        Log.d(TAG, "location update " + location[0]);
                        Log.d(TAG, "location Latitude " + location[0].getLatitude());
                        Log.d(TAG, "location Longitude " + location[0].getLongitude());
                        curLat = location[0].getLatitude();
                        curLng = location[0].getLongitude();

                        if (client != null) {
                            client.removeLocationUpdates(locationCallback);
                        }
                        getTaskers();
                    }
                }
            };
            client.requestLocationUpdates(request, locationCallback, null);
        }
    }

    /*---------------- Get Taskers Api Call -------------------*/
    private void getTaskers() {
        binding.txtNoRecord.setVisibility(View.GONE);
        binding.rvTaskers.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        String url = WebServiceUrl.URL_POPULARTASKERS;
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("subcategory_id", categoryId);
        textParams.put("latitude", String.valueOf(curLat));
        textParams.put("longitude", String.valueOf(curLng));
        textParams.put("user_id", PrefsUtil.with(context).readString("UserId"));

        new WebServiceCall(getActivity(), url, textParams, PopularTaskerPOJO.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.progress.setVisibility(View.GONE);
                        binding.rvTaskers.setVisibility(View.VISIBLE);
                        if (status) {
                            PopularTaskerPOJO pojo = (PopularTaskerPOJO) obj;
                            arrayList.clear();
                            arrayList.addAll(pojo.getData());
                            if (arrayList.size() > 0) {
                                binding.txtNoRecord.setVisibility(View.GONE);
                                binding.rvTaskers.setVisibility(View.VISIBLE);
                            } else {
                                binding.rvTaskers.setVisibility(View.GONE);
                                binding.txtNoRecord.setVisibility(View.VISIBLE);
                            }
                            providerAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getActivity(), (String) obj,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        taskerListAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        taskerListAsync.cancel(true);
                    }
                });
    }

    @Override
    public void onDestroy() {
        Utils.cancelAsyncTask(taskerListAsync);
        context.unregisterReceiver(gpsCheck);
        super.onDestroy();
    }
}
