package com.app.bemyrider.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityFilterDeliveryBinding;
import com.app.bemyrider.utils.Log;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FilterDeliveryActivity extends AppCompatActivity {

    private ActivityFilterDeliveryBinding binding;
    private ActivityResultLauncher<Intent> locationActivityResultLauncher;
    private String latitude = "", longitude = "", strAsc = "", strDesc = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_filter_delivery);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.filter_by), HtmlCompat.FROM_HTML_MODE_LEGACY));

        String address = getIntent().getStringExtra("address");
        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");
        strAsc = getIntent().getStringExtra("strAsc");
        strDesc = getIntent().getStringExtra("strDesc");
        String strSearch = getIntent().getStringExtra("searchKeyWord");
        String strRating = getIntent().getStringExtra("rating");

        if ("y".equalsIgnoreCase(strAsc)) {
            binding.switchAsc.setChecked(true);
        } else if ("y".equalsIgnoreCase(strDesc)) {
            binding.switchDesc.setChecked(true);
        } else {
            binding.switchAsc.setChecked(false);
            binding.switchDesc.setChecked(false);
        }

        init();

        binding.switchAsc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.switchDesc.isChecked()) {
                binding.switchDesc.setChecked(false);
            }
            if (isChecked) {
                binding.switchAsc.setChecked(true);
                strAsc = "y";
            } else {
                binding.switchAsc.setChecked(false);
                strAsc = "n";
            }
        });

        binding.switchDesc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.switchAsc.isChecked()) {
                binding.switchAsc.setChecked(false);
            }
            if (isChecked) {
                binding.switchDesc.setChecked(true);
                strDesc = "y";
            } else {
                binding.switchDesc.setChecked(false);
                strDesc = "n";
            }
        });

        binding.txtLocation.setOnClickListener(v -> {
            binding.txtLocation.setText("");
            latitude = "";
            longitude = "";
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this);
            locationActivityResultLauncher.launch(intent);
        });

        binding.txtLocation.setText(address);
        binding.EdtSearchKeyword.setText(strSearch);

        try {
            if (strRating != null && !strRating.isEmpty())
                binding.ratingbarFilter.setRating(Float.parseFloat(strRating));
        } catch (NumberFormatException e) {
            Log.e("FilterDeliveryActivity", "Error parsing rating");
        }

        binding.BtnApplyFilter.setOnClickListener(v -> {
            Intent i = new Intent();
            i.putExtra("address", Objects.requireNonNull(binding.txtLocation.getText()).toString());
            i.putExtra("latitude", latitude);
            i.putExtra("longitude", longitude);
            i.putExtra("strAsc", strAsc);
            i.putExtra("strDesc", strDesc);
            i.putExtra("searchKeyWord", Objects.requireNonNull(binding.EdtSearchKeyword.getText()).toString());
            i.putExtra("rating", String.valueOf(binding.ratingbarFilter.getRating()));
            setResult(RESULT_OK, i);
            finish();
        });

        binding.BtnResetFilter.setOnClickListener(v -> {
            setResetFilters();
            Intent i = new Intent();
            i.putExtra("address", Objects.requireNonNull(binding.txtLocation.getText()).toString());
            i.putExtra("latitude", latitude);
            i.putExtra("longitude", longitude);
            i.putExtra("strAsc", strAsc);
            i.putExtra("strDesc", strDesc);
            i.putExtra("searchKeyWord", Objects.requireNonNull(binding.EdtSearchKeyword.getText()).toString());
            i.putExtra("rating", String.valueOf(binding.ratingbarFilter.getRating()));
            setResult(RESULT_OK, i);
            finish();
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
    }

    private void setResetFilters() {
        binding.txtLocation.setText("");
        latitude = "";
        longitude = "";
        binding.switchAsc.setChecked(false);
        binding.switchDesc.setChecked(false);
        strAsc = "";
        strDesc = "";
        binding.EdtSearchKeyword.setText("");
        String strRating = "0.0";
        binding.ratingbarFilter.setRating(Float.parseFloat(strRating));
    }

    private void init() {
        setupToolBar();
        locationActivityResult();
    }

    private void locationActivityResult() {
        locationActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            try {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    Log.i("AUTO COMPLETE", "Place: " + place.getName() + ", " + place.getId());
                    if (place.getLatLng() != null) {
                        latitude = String.valueOf(place.getLatLng().latitude);
                        longitude = String.valueOf(place.getLatLng().longitude);
                        binding.txtLocation.setText(place.getName());
                    } else {
                        Log.e("FilterDeliveryActivity", "Place location is null");
                    }
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    Log.i("AUTO COMPLETE", status.getStatusMessage());
                }
            } catch (Exception e) {
                Log.e("FilterDeliveryActivity", "Error in locationActivityResult");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private void setupToolBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_remove_resize);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

}