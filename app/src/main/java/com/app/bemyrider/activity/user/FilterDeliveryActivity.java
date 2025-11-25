package com.app.bemyrider.activity.user;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;

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

public class FilterDeliveryActivity extends AppCompatActivity {

    private ActivityFilterDeliveryBinding binding;
    private Context context;
    ActivityResultLauncher<Intent> locationActivityResultLauncher;
    private String address = "", latitude = "", longitude = "", strAsc = "", strDesc = "", strSearch = "", strRating = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(FilterDeliveryActivity.this, R.layout.activity_filter_delivery, null);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));
        }

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.filter_by),HtmlCompat.FROM_HTML_MODE_LEGACY));

        address = getIntent().getStringExtra("address");
        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");
        strAsc = getIntent().getStringExtra("strAsc");
        strDesc = getIntent().getStringExtra("strDesc");
        strSearch = getIntent().getStringExtra("searchKeyWord");
        strRating = getIntent().getStringExtra("rating");

        if (strAsc.equalsIgnoreCase("y")) {
            binding.switchAsc.setChecked(true);
        } else if (strDesc.equalsIgnoreCase("y")) {
            binding.switchDesc.setChecked(true);
        } else {
            binding.switchAsc.setChecked(false);
            binding.switchDesc.setChecked(false);
        }

        init();

        binding.switchAsc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
            }
        });

        binding.switchDesc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
            }
        });

        binding.txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.txtLocation.setText("");
                address = "";
                latitude = "";
                longitude = "";
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ID,
                        Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS))
                        .build(FilterDeliveryActivity.this);
                locationActivityResultLauncher.launch(intent);
            }
        });

        binding.txtLocation.setText(address);
        binding.EdtSearchKeyword.setText(strSearch);

        try {
            if (!strRating.equals(""))
                binding.ratingbarFilter.setRating(Float.parseFloat(strRating));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        binding.BtnApplyFilter.setOnClickListener(v -> {
            Intent i = new Intent();
            i.putExtra("address", binding.txtLocation.getText().toString());
            i.putExtra("latitude", latitude);
            i.putExtra("longitude", longitude);
            i.putExtra("strAsc", strAsc);
            i.putExtra("strDesc", strDesc);
            i.putExtra("searchKeyWord", binding.EdtSearchKeyword.getText().toString());
            i.putExtra("rating", String.valueOf(binding.ratingbarFilter.getRating()));
            setResult(RESULT_OK, i);
            finish();
        });

        binding.BtnResetFilter.setOnClickListener(v -> {
            setResetFilters();
            Intent i = new Intent();
            i.putExtra("address", binding.txtLocation.getText().toString());
            i.putExtra("latitude", latitude);
            i.putExtra("longitude", longitude);
            i.putExtra("strAsc", strAsc);
            i.putExtra("strDesc", strDesc);
            i.putExtra("searchKeyWord", binding.EdtSearchKeyword.getText().toString());
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
        strRating = "0.0";
        binding.ratingbarFilter.setRating(Float.parseFloat(strRating));
    }

    private void init() {
        context = FilterDeliveryActivity.this;
        setupToolBar();
        locationActivityResult();
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
                            latitude = String.valueOf(place.getLatLng().latitude);
                            longitude = String.valueOf(place.getLatLng().longitude);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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