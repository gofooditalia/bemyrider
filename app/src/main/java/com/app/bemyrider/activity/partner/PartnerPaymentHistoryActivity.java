package com.app.bemyrider.activity.partner;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.Partner.PartnerPaymentHistoryAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityPartnerPaymentHistoryBinding;
import com.app.bemyrider.model.partner.PartnerPaymentHistoryItem;
import com.app.bemyrider.model.partner.PartnerPaymentHistoryPojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Hardik Talaviya on 29/11/19.
 */

public class PartnerPaymentHistoryActivity extends AppCompatActivity {

    private static final String TAG = "PartnerPaymentHistory";
    private ActivityPartnerPaymentHistoryBinding binding;
    private Context context;
    private PartnerPaymentHistoryAdapter partnerPaymentHistoryAdapter;
    private ArrayList<PartnerPaymentHistoryItem> transactionArrayList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private WebServiceCall task;
    private ConnectionManager connectionManager;

    /*pagination vars start*/
    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisiblesItems = 0, visibleItemCount, totalItemCount;
    /*pagination vars end*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(PartnerPaymentHistoryActivity.this, R.layout.activity_partner_payment_history);
        initViews();

        serviceCallPaymentHistory(true);

        binding.rvPaymentHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisiblesItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            if (transactionArrayList.size() < total_records) {
                                page++;
                                serviceCallPaymentHistory(false);
                            }
                            //Do pagination.. i.e. fetch new data
                        }
                    }
                }
            }
        });
    }

    private void initViews() {
        context = PartnerPaymentHistoryActivity.this;

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.payment_history),HtmlCompat.FROM_HTML_MODE_LEGACY));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Transaction Recycler View*/
        linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        partnerPaymentHistoryAdapter = new PartnerPaymentHistoryAdapter(context, transactionArrayList);
        binding.rvPaymentHistory.setLayoutManager(linearLayoutManager);
        binding.rvPaymentHistory.setAdapter(partnerPaymentHistoryAdapter);
        partnerPaymentHistoryAdapter.notifyDataSetChanged();

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallPaymentHistory(true);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /*---------------- Payment History Api Call ---------------------*/
    private void serviceCallPaymentHistory(boolean clearFlag) {

        if (clearFlag) {
            page = 1;
            binding.txtNoRecordFound.setVisibility(View.GONE);
            binding.rvPaymentHistory.scrollToPosition(0);
        }
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(PartnerPaymentHistoryActivity.this).readString("UserId"));
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(PartnerPaymentHistoryActivity.this, WebServiceUrl.URL_PROVIDERPAYMENTHISTORY,
                textParams, PartnerPaymentHistoryPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                if (status) {
                    PartnerPaymentHistoryPojo resultObj = (PartnerPaymentHistoryPojo) obj;
                    if (clearFlag) {
                        transactionArrayList.clear();
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.rvPaymentHistory.setVisibility(View.VISIBLE);
                    transactionArrayList.addAll(resultObj.getData().getPartnerPaymentHistoryItem());
                    partnerPaymentHistoryAdapter.notifyDataSetChanged();
                    loading = true;
                    if (transactionArrayList.size() > 0) {
                        binding.rvPaymentHistory.setVisibility(View.VISIBLE);
                        binding.txtNoRecordFound.setVisibility(View.GONE);
                    } else {
                        binding.rvPaymentHistory.setVisibility(View.GONE);
                        binding.txtNoRecordFound.setVisibility(View.VISIBLE);
                    }
                    try {
                        total_records = resultObj.getData().getPagination().getTotalRecords();
                        Log.e(TAG, "onResult Total Record :: " + total_records);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(Object asyncTask) {
                task = null;
            }

            @Override
            public void onCancelled() {
                task = null;
            }
        });
    }

    @Override
    public void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(task);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
