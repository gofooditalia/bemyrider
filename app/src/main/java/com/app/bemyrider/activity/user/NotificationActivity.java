package com.app.bemyrider.activity.user;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
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

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityNotificationBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.NotificationListPojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 7/12/19.
 */


public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;
    private ArrayList<String> checked;
    private ArrayList<String> all;
    private int final_size;
    private AsyncTask updateSettingAsync, getNotificationAsync;
    private Context context;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(NotificationActivity.this, R.layout.activity_notification, null);

        initViews();

        serviceCallGetNotification();

        binding.btnSaveChange.setOnClickListener(v -> serviceCallUpdateSetting());
    }

    private void initViews() {
        context = NotificationActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.notification_settings),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        checked = new ArrayList<>();
        all = new ArrayList<>();

    }

    /*----------------- Update Setting Api Call ---------------------*/
    private void serviceCallUpdateSetting() {
        binding.btnSaveChange.setClickable(false);
        binding.pgSaveChanges.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(NotificationActivity.this).readString("UserId"));

        for (int i = 0; i < final_size; i++) {
            boolean flag = false;
            for (int j = 0; j < checked.size(); j++) {
                if (all.get(i).equals(checked.get(j))) {
                    textParams.put(all.get(i), "y");
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                textParams.put(all.get(i), "n");
            }
        }

        new WebServiceCall(NotificationActivity.this,
                WebServiceUrl.URL_UPDATE_NOTIFICATION, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.pgSaveChanges.setVisibility(View.GONE);
                        binding.btnSaveChange.setClickable(true);
                        if (status) {
                            Toast.makeText(NotificationActivity.this,
                                    ((CommonPojo) obj).getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NotificationActivity.this,
                                    (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        updateSettingAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        updateSettingAsync = null;
                    }
                });
    }

    /*-------------------- Get Notification List Api Call ------------------------*/
    private void serviceCallGetNotification() {
        binding.llMain.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(NotificationActivity.this).readString("UserId"));

        new WebServiceCall(NotificationActivity.this, WebServiceUrl.URL_GET_NOTIFICATION,
                textParams, NotificationListPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.llMain.setVisibility(View.VISIBLE);
                if (status) {
                    NotificationListPojo notificationListPojo = (NotificationListPojo) obj;
                    final_size = notificationListPojo.getData().size();

                    for (int i = 0; i < notificationListPojo.getData().size(); i++) {

                        LinearLayout layoutMain = new LinearLayout(NotificationActivity.this);
                        layoutMain.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout layout = new LinearLayout(NotificationActivity.this);
                        layout.setOrientation(LinearLayout.HORIZONTAL);

                        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                        param.setMargins(50, 0, 25, 50);

                        TextView textView = new TextView(NotificationActivity.this);
                        textView.setText(notificationListPojo.getData().get(i).getTitle());
                        textView.setTextColor(ContextCompat.getColor(context, R.color.text_light));
                        textView.setTextAppearance(NotificationActivity.this, R.style.font_regular);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        textView.setLayoutParams(param);

                        final SwitchCompat aSwitch = new SwitchCompat(NotificationActivity.this);
                        aSwitch.setId(Integer.parseInt(notificationListPojo.getData().get(i).getId()));
                        aSwitch.setThumbDrawable(ContextCompat.getDrawable(context,R.drawable.custom_thumb));
                        aSwitch.setTrackDrawable(null);
                        all.add(String.valueOf(aSwitch.getId()));

                        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                checked.add(String.valueOf(aSwitch.getId()));
                            } else {
                                try {
                                    checked.remove(String.valueOf(aSwitch.getId()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        if (notificationListPojo.getData().get(i).getChecked().equals("true")) {
//                        if (notificationListPojo.getData().get(i).getChecked().equals("false")) {
                            aSwitch.setChecked(true);
                        } else {
                            aSwitch.setChecked(false);
                        }

                        View viewDivider = new View(NotificationActivity.this);
                        viewDivider.setBackgroundColor(ContextCompat.getColor(context, R.color.dash_line));
                        int dividerHeight = getResources().getDimensionPixelSize(R.dimen.divider_height);
                        LinearLayout.LayoutParams dividerParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight);
                        dividerParam.setMargins(50, 0, 50, 50);
                        viewDivider.setLayoutParams(dividerParam);

                        layout.addView(textView);
                        layout.addView(aSwitch);

                        layoutMain.addView(layout);
                        if (i != notificationListPojo.getData().size()-1)
                            layoutMain.addView(viewDivider);

                        binding.layoutNotification.addView(layoutMain);
                    }
                } else {
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                getNotificationAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                getNotificationAsync = null;
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
        Utils.cancelAsyncTask(updateSettingAsync);
        Utils.cancelAsyncTask(getNotificationAsync);

        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
