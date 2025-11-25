package com.app.bemyrider.activity.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.activity.partner.PartnerReviewsActivity;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerProfileAcitvityBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.ProfileItem;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 4/12/19.
 */

public class PartnerProfileActivity extends AppCompatActivity {

    private PartnerProfileAcitvityBinding binding;
    private Context mContext = PartnerProfileActivity.this;
    private String partnerId = "";
    private AsyncTask getProfileAsync, reportUserAsync;
    private ConnectionManager connectionManager;
    private String isFlag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(PartnerProfileActivity.this, R.layout.partner_profile_acitvity, null);

        if (getIntent().hasExtra(Utils.PROVIDER_ID)) {
            if (getIntent().getStringExtra(Utils.PROVIDER_ID) != null
                    && getIntent().getStringExtra(Utils.PROVIDER_ID).length() > 0) {
                partnerId = getIntent().getStringExtra(Utils.PROVIDER_ID);
            } else {
                finish();
            }
        } else {
            finish();
        }

        initView();

        serviceCall();

        binding.relReportUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceCallReportUser();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*---------------- Get Profile Api Call ------------------*/
    private void serviceCall() {
        binding.progress.setVisibility(View.VISIBLE);
        binding.llMain.setVisibility(View.GONE);
        binding.relReportUser.setVisibility(View.GONE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("loginuser_id", PrefsUtil.with(PartnerProfileActivity.this).readString("UserId"));
        textParams.put("profile_id", partnerId);

        new WebServiceCall(mContext, WebServiceUrl.URL_PROFILE, textParams, ProfilePojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.progress.setVisibility(View.GONE);
                        binding.llMain.setVisibility(View.VISIBLE);
                        binding.relReportUser.setVisibility(View.VISIBLE);
                        if (status) {
                            ProfilePojo response_profile = (ProfilePojo) obj;
                            final ProfileItem pItem = response_profile.getData();

                            if (pItem.getProfileImg().equals("")) {
                                //binding.ppaImgBg.setImageResource(R.mipmap.user);
                                binding.ppaImgProfile.setImageResource(R.mipmap.user);
                            } else {
                                try {
                                    Picasso.get().load(pItem.getProfileImg()).placeholder(R.drawable.loading).into(binding.ppaImgProfile);
                                    // Picasso.get().load(pItem.getProfileImg()).placeholder(R.drawable.loading).into(binding.ppaImgBg);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                }
                            }

                            String strDeliveryType = "";

                            if (response_profile.getData().getSmallDelivery().equals("y")) {
                                strDeliveryType = getResources().getString(R.string.small);
                            }

                            if (response_profile.getData().getMediumDelivery().equals("y")) {
                                if (strDeliveryType.equals(""))
                                    strDeliveryType = getResources().getString(R.string.medium);
                                else
                                    strDeliveryType = strDeliveryType + " , " + getResources().getString(R.string.medium);
                            }

                            if (response_profile.getData().getLargeDelivery().equals("y")) {
                                if (strDeliveryType.equals(""))
                                    strDeliveryType = getResources().getString(R.string.large);
                                else
                                    strDeliveryType = strDeliveryType + " , " + getResources().getString(R.string.large);
                            }

                            binding.txtDeliveryType.setText(strDeliveryType);

                            isFlag = pItem.getIs_flag();
                            if (isFlag.equalsIgnoreCase("y")) {
                                binding.imgReportUser.setImageResource(R.mipmap.ic_report_user_filled);
                            } else {
                                binding.imgReportUser.setImageResource(R.mipmap.ic_report_user);
                            }

                            if (pItem.getUserName() != null && pItem.getUserName().length() > 0) {
                                binding.ppaTxtUsername.setText(pItem.getUserName());
                            } else {
                                binding.ppaTxtUsername.setText("-");
                            }

                           /* if (pItem.getEmailMask() != null && pItem.getEmailMask().length() > 0) {
                                binding.ppaTxtUseremail.setText(pItem.getEmailMask());
                            } else {
                                binding.ppaTxtUseremail.setText("-");
                            }

                            if (pItem.getContactMask() != null && pItem.getContactMask().length() > 0) {
                                binding.ppaTxtUsercontactno.setText(String.format("%s %s", pItem.getCountryCode(), pItem.getContactMask()));
                            } else {
                                binding.ppaTxtUsercontactno.setText("-");
                            }*/

                            if (pItem.getAddress() != null && pItem.getAddress().length() > 0) {
                                binding.ppaTxtAddress.setText(pItem.getAddress());
                            } else {
                                binding.ppaTxtAddress.setText("-");
                            }
                            if (pItem.getPositiveRating() != null
                                    && pItem.getPositiveRating().length() > 0) {
                                binding.ppaTxtPositiveRatings.setText(String.format("%s %s", pItem.getPositiveRating(), getString(R.string.positive_ratings)));
                            } else {
                                binding.ppaTxtPositiveRatings.setText(getString(R.string.no_positive_ratings));
                            }

                            if (pItem.getTaskAssigned() != null && pItem.getTaskAssigned().length() > 0) {
                                binding.ppaTxtWorkedOn.setText(String.format("%s %s %s", getString(R.string.str_worked_on), pItem.getTaskAssigned(), getString(R.string.tasks)));
                            } else {
                                binding.ppaTxtWorkedOn.setText(String.format("%s 0 %s", getString(R.string.str_worked_on), getString(R.string.tasks)));
                            }

                            if (pItem.getDescription() != null && pItem.getDescription().length() > 0) {
                                binding.ppaTxtAboutUser.setText(Utils.decodeEmoji(pItem.getDescription().toString()));
                            } else {
                                binding.ppaTxtAboutUser.setText("-");
                            }

                            if (!pItem.getAvailableDays().equals("")) {
                                binding.ppaTxtAvailableDays.setText(pItem.getAvailableDaysList());
                            } else {
                                binding.ppaTxtAvailableDays.setText("-");
                            }

                            if (pItem.getAvailableTimeStart() != null

                                    && pItem.getAvailableTimeStart().length() > 0
                                    && pItem.getAvailableTimeEnd() != null
                                    && pItem.getAvailableTimeEnd().length() > 0) {
                                binding.ppaTxtServiceTime.setText(String.format("%s - %s", pItem.getAvailableTimeStart(), pItem.getAvailableTimeEnd()));
                            } else {
                                binding.ppaTxtServiceTime.setText("-");
                            }

                            if (pItem.getStartRating() != null && pItem.getStartRating().length() > 0) {
                                binding.ppaTxtRating.setText(String.format("%s ", pItem.getStartRating()));
                            } else {
                                binding.ppaTxtRating.setText("0 ");
                            }

                            if (pItem.getTotalService() != null && pItem.getTotalService().length() > 0) {
                                binding.ppaTxtServiceCount.setText(pItem.getTotalService());
                            } else {
                                binding.ppaTxtServiceCount.setText("-");
                            }

                            binding.ppaTxtViewAllReviews.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, PartnerReviewsActivity.class);
                                    i.putExtra(Utils.PROVIDER_ID, pItem.getId());
                                    startActivity(i);
                                }
                            });

                            binding.ppaTxtViewAllServices.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, UserServicesActivity.class);
                                    i.putExtra(Utils.PROVIDER_ID, pItem.getId());
                                    i.putExtra("providerImage", pItem.getProfileImg());
                                    startActivity(i);
                                }
                            });

                        } else {
                            Toast.makeText(mContext, obj.toString(), Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        getProfileAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        getProfileAsync = null;
                    }
                });
    }

    /*----------------- Report user Api Call --------------------*/
    private void serviceCallReportUser() {
        binding.imgReportUser.setVisibility(View.GONE);
        binding.pgReportUser.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(PartnerProfileActivity.this).readString("UserId"));

        textParams.put("flag_user_id", partnerId);

        new WebServiceCall(mContext, WebServiceUrl.URL_FLAGUSER, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.pgReportUser.setVisibility(View.GONE);
                        binding.imgReportUser.setVisibility(View.VISIBLE);
                        if (status) {
                            CommonPojo commonPojo = (CommonPojo) obj;
                            if (isFlag.equalsIgnoreCase("y")) {
                                binding.imgReportUser.setImageResource(R.mipmap.ic_report_user);
                                isFlag = "n";
                            } else {
                                binding.imgReportUser.setImageResource(R.mipmap.ic_report_user_filled);
                                isFlag = "y";
                            }
                            Toast.makeText(mContext, commonPojo.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        reportUserAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        reportUserAsync = null;
                    }
                });
    }

    private void initView() {
        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // collapsed
                if (!binding.ppaTxtUsername.getText().equals(""))
                    binding.txtHeaderName.setText(binding.ppaTxtUsername.getText());
                else
                    binding.txtHeaderName.setText(getResources().getString(R.string.provider_profile));

                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_bg_color));
                binding.txtHeaderName.setTextColor(getResources().getColor(R.color.white));
                binding.llMain.setPadding(0, 50,0,0);
            } else {
                // expanded
                // binding.txtHeaderName.setText(getResources().getString(R.string.provider_profile));
                binding.txtHeaderName.setText("");
                binding.txtHeaderName.setTextColor(getResources().getColor(R.color.white));
                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                binding.llMain.setPadding(0, 0,0,0);

            }
        });

        getStatusBarHeight();
        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        binding.imgBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void getStatusBarHeight() {
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight= contentViewTop - statusBarHeight;
        binding.linHeader.setPadding(0,titleBarHeight + 50,0,0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (getProfileAsync != null) {
                getProfileAsync.cancel(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (reportUserAsync != null) {
                reportUserAsync.cancel(true);
            }
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
