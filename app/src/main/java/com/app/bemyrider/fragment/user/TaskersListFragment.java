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
import com.app.bemyrider.R;
import com.app.bemyrider.viewmodel.TaskersListViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.databinding.FragmentTaskerListBinding;
import com.app.bemyrider.model.user.PopularTaskerItem;
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

public class TaskersListFragment extends Fragment {

    private FragmentTaskerListBinding binding;
    private PopularTaskersAdapter providerAdapter;
    private ArrayList<PopularTaskerItem> arrayList;
    private LinearLayoutManager layoutManager;
    private String categoryId = "";
    private TaskersListViewModel viewModel;
    private Context context;
    private PermissionManager permissionManager;
    private FusedLocationProviderClient client;
    private LocationCallback locationCallback;
    private double curLat = 0.0, curLng = 0.0;
    private static final String TAG = "TaskersListFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tasker_list, container, false);
        if (getArguments() != null) {
            categoryId = getArguments().getString(Utils.CATEGORY_ID);
        }
        init();
        viewModel = new ViewModelProvider(this).get(TaskersListViewModel.class);
        observeViewModel();
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

    private final BroadcastReceiver gpsCheck = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (permissionManager.checkAndRequestPermissions(PermissionManager.ACCESS_FINE_LOCATION, 732, "setupLocation")) {
                    setupLocationService();
                }
            }
        }
    };

    private void setupLocationService() {
        LocationRequest request = LocationRequest.create()
                .setFastestInterval(2000)
                .setInterval(5000)
                .setNumUpdates(1)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener((Activity) context, locationSettingsResponse -> requestLocationUpdates());

        task.addOnFailureListener((Activity) context, e -> {
            int statusCode = ((ApiException) e).getStatusCode();
            if (statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                try {
                    ((ResolvableApiException) e).startResolutionForResult((Activity) context, 555);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    Log.e(TAG, "Unable to start resolution intent");
                }
            }
        });
    }

    private void requestLocationUpdates() {
        LocationRequest request = LocationRequest.create();
        request.setFastestInterval(2000).setInterval(5000).setNumUpdates(1).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client = LocationServices.getFusedLocationProviderClient(context);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        curLat = location.getLatitude();
                        curLng = location.getLongitude();
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

    private void observeViewModel() {
        viewModel.getTaskers().observe(getViewLifecycleOwner(), pojo -> {
            binding.progress.setVisibility(View.GONE);
            binding.rvTaskers.setVisibility(View.VISIBLE);
            if (pojo != null && pojo.getData() != null) {
                arrayList.clear();
                arrayList.addAll(pojo.getData());
                providerAdapter.notifyDataSetChanged();
                if (arrayList.isEmpty()) {
                    binding.rvTaskers.setVisibility(View.GONE);
                    binding.txtNoRecord.setVisibility(View.VISIBLE);
                }
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                binding.progress.setVisibility(View.GONE);
                binding.rvTaskers.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTaskers() {
        binding.txtNoRecord.setVisibility(View.GONE);
        binding.rvTaskers.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);
        viewModel.loadPopularTaskers(categoryId, String.valueOf(curLat), String.valueOf(curLng),
                PrefsUtil.with(context).readString("UserId"));
    }

    @Override
    public void onDestroy() {
        if (context != null) {
            try {
                context.unregisterReceiver(gpsCheck);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
