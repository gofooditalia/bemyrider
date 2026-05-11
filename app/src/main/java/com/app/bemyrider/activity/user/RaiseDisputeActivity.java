package com.app.bemyrider.activity.user;

import static com.app.bemyrider.utils.Utils.EMOJI_FILTER;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityRaiseDisputeBinding;
import com.app.bemyrider.viewmodel.RaiseDisputeViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;


/**
 * Modified by Hardik Talaviya on 4/12/19.
 */

public class RaiseDisputeActivity extends AppCompatActivity {

    private ActivityRaiseDisputeBinding binding;
    private String serviceRequestId = "";
    private RaiseDisputeViewModel viewModel;
    private Context context;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(RaiseDisputeActivity.this, R.layout.activity_raise_dispute, null);

        initViews();

        viewModel = new ViewModelProvider(this).get(RaiseDisputeViewModel.class);
        viewModel.getResult().observe(this, result -> {
            binding.pgSubmit.setVisibility(View.GONE);
            binding.btnSubmit.setClickable(true);
            if (result != null && result.isStatus()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, DisputeListActivity.class));
                finish();
            }
        });
        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                binding.pgSubmit.setVisibility(View.GONE);
                binding.btnSubmit.setClickable(true);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        if (getIntent().hasExtra("RequestID")) {
            serviceRequestId = getIntent().getStringExtra("RequestID");
        }

        binding.btnSubmit.setOnClickListener(view -> {
            if (checkValidation()) {
                Utils.hideSoftKeyboard(RaiseDisputeActivity.this);
                binding.btnSubmit.setClickable(false);
                raiseDispute();
            }
        });
    }

    private void initViews() {
        context = RaiseDisputeActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.raise_dispute),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        binding.edtSubject.setFilters(new InputFilter[]{EMOJI_FILTER});
    }

    private void raiseDispute() {
        binding.pgSubmit.setVisibility(View.VISIBLE);
        viewModel.raiseDispute(
            serviceRequestId,
            PrefsUtil.with(this).readString("UserId"),
            binding.edtSubject.getText().toString().trim(),
            Utils.encodeEmoji(binding.edtDesc.getText().toString().trim())
        );
    }

    private boolean checkValidation() {
        String strSubject = binding.edtSubject.getText().toString().trim();
        String strDesc = binding.edtDesc.getText().toString().trim();

        if (strSubject.length() <= 0) {
            binding.tilSubject.setError(getString(R.string.provide_subject));
            binding.edtSubject.requestFocus();
            return false;
        } else if (strDesc.length() <= 0) {
            binding.tilDesc.setError(getString(R.string.provide_desc));
            binding.edtDesc.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
