package com.app.bemyrider.activity.user;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;

import com.app.bemyrider.R;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.viewmodel.RedeemViewModel;

public class RedeemActivity extends AppCompatActivity {

    private Button Btn_submit;
    private RedeemViewModel viewModel;
    private Context context;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reedem);

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.redeem_request), HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        viewModel = new ViewModelProvider(this).get(RedeemViewModel.class);
        viewModel.getResult().observe(this, result -> {
            if (result != null)
                Toast.makeText(this, result.isStatus() ? result.getMessage() : result.getMessage(), Toast.LENGTH_SHORT).show();
        });
        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        });

        Btn_submit.setOnClickListener(view ->
                viewModel.sendRedeemRequest(PrefsUtil.with(this).readString("UserId")));
    }

    private void initViews() {
        context = RedeemActivity.this;
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);
        Btn_submit = findViewById(R.id.Btn_submit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception ignored) {}
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
