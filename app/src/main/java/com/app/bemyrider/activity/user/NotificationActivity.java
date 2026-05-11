package com.app.bemyrider.activity.user;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityNotificationBinding;
import com.app.bemyrider.model.NotificationListPojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.viewmodel.NotificationSettingsViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;
    private ArrayList<String> checked = new ArrayList<>();
    private ArrayList<String> all = new ArrayList<>();
    private int final_size;
    private Context context;
    private ConnectionManager connectionManager;
    private NotificationSettingsViewModel viewModel;
    private String userId;
    private boolean shouldAutoEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(NotificationActivity.this, R.layout.activity_notification, null);

        initViews();

        viewModel = new ViewModelProvider(this).get(NotificationSettingsViewModel.class);
        observeViewModel();

        viewModel.loadSettings(userId);

        binding.btnSaveChange.setOnClickListener(v -> saveSettings(true));
    }

    private void initViews() {
        context = NotificationActivity.this;
        userId = PrefsUtil.with(context).readString("UserId");

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.notification_settings), HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);
    }

    private void observeViewModel() {
        viewModel.getSettings().observe(this, pojo -> {
            binding.progress.setVisibility(View.GONE);
            binding.llMain.setVisibility(View.VISIBLE);
            if (pojo == null || pojo.getData() == null) return;

            final_size = pojo.getData().size();
            String prefKey = "isNotificationConfigured_" + userId;
            shouldAutoEnable = !PrefsUtil.with(context).readBoolean(prefKey);

            binding.layoutNotification.removeAllViews();
            checked.clear();
            all.clear();

            for (int i = 0; i < pojo.getData().size(); i++) {
                NotificationListPojoItem item = pojo.getData().get(i);
                addNotificationRow(item, i < pojo.getData().size() - 1);
            }

            if (shouldAutoEnable) {
                saveSettings(false);
            }
        });

        viewModel.getUpdateResult().observe(this, result -> {
            binding.pgSaveChanges.setVisibility(View.GONE);
            binding.btnSaveChange.setClickable(true);
            if (result != null && result.isStatus()) {
                PrefsUtil.with(context).write("isNotificationConfigured_" + userId, true);
                if (result.getMessage() != null && !result.getMessage().isEmpty()) {
                    Toast.makeText(context, result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                binding.progress.setVisibility(View.GONE);
                binding.pgSaveChanges.setVisibility(View.GONE);
                binding.btnSaveChange.setClickable(true);
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNotificationRow(NotificationListPojoItem item, boolean addDivider) {
        LinearLayout layoutMain = new LinearLayout(context);
        layoutMain.setOrientation(LinearLayout.VERTICAL);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        param.setMargins(50, 0, 25, 50);

        TextView textView = new TextView(context);
        textView.setText(item.getTitle());
        textView.setTextColor(ContextCompat.getColor(context, R.color.text_light));
        textView.setTextAppearance(context, R.style.font_regular);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        textView.setLayoutParams(param);

        final SwitchCompat aSwitch = new SwitchCompat(context);
        aSwitch.setId(Integer.parseInt(item.getId()));
        aSwitch.setThumbDrawable(ContextCompat.getDrawable(context, R.drawable.custom_thumb));
        aSwitch.setTrackDrawable(null);
        all.add(String.valueOf(aSwitch.getId()));

        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String switchId = String.valueOf(aSwitch.getId());
            if (isChecked) {
                if (!checked.contains(switchId)) checked.add(switchId);
            } else {
                checked.remove(switchId);
            }
        });

        if (item.getChecked().equals("true") || shouldAutoEnable) {
            aSwitch.setChecked(true);
            if (!checked.contains(item.getId())) checked.add(item.getId());
        } else {
            aSwitch.setChecked(false);
        }

        layout.addView(textView);
        layout.addView(aSwitch);
        layoutMain.addView(layout);

        if (addDivider) {
            View viewDivider = new View(context);
            viewDivider.setBackgroundColor(ContextCompat.getColor(context, R.color.dash_line));
            int dividerHeight = getResources().getDimensionPixelSize(R.dimen.divider_height);
            LinearLayout.LayoutParams dividerParam = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight);
            dividerParam.setMargins(50, 0, 50, 50);
            viewDivider.setLayoutParams(dividerParam);
            layoutMain.addView(viewDivider);
        }

        binding.layoutNotification.addView(layoutMain);
    }

    private void saveSettings(boolean showFeedback) {
        if (showFeedback) {
            binding.btnSaveChange.setClickable(false);
            binding.pgSaveChanges.setVisibility(View.VISIBLE);
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("user_id", userId);
        for (int i = 0; i < final_size; i++) {
            boolean isChecked = checked.contains(all.get(i));
            params.put(all.get(i), isChecked ? "y" : "n");
        }

        viewModel.updateSettings(params);
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
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
