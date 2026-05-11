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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.Partner.PartnerPaymentHistoryAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityPartnerPaymentHistoryBinding;
import com.app.bemyrider.model.partner.PartnerPaymentHistoryItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.viewmodel.PaymentHistoryViewModel;

import java.util.ArrayList;

public class PartnerPaymentHistoryActivity extends AppCompatActivity {

    private ActivityPartnerPaymentHistoryBinding binding;
    private Context context;
    private PartnerPaymentHistoryAdapter partnerPaymentHistoryAdapter;
    private ArrayList<PartnerPaymentHistoryItem> transactionArrayList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private PaymentHistoryViewModel viewModel;
    private ConnectionManager connectionManager;
    private boolean loading = true;
    private boolean pendingClear = false;
    private int page = 1, total_records = 0;
    private int pastVisiblesItems = 0, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(PartnerPaymentHistoryActivity.this, R.layout.activity_partner_payment_history);
        initViews();

        viewModel = new ViewModelProvider(this).get(PaymentHistoryViewModel.class);
        observeViewModel();
        serviceCallPaymentHistory(true);

        binding.rvPaymentHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisiblesItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    if (loading && (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;
                        if (transactionArrayList.size() < total_records) { page++; serviceCallPaymentHistory(false); }
                    }
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getPartnerHistory().observe(this, pojo -> {
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                if (pendingClear) { transactionArrayList.clear(); pendingClear = false; }
                transactionArrayList.addAll(pojo.getData().getPartnerPaymentHistoryItem());
                partnerPaymentHistoryAdapter.notifyDataSetChanged();
                loading = true;
                boolean hasItems = !transactionArrayList.isEmpty();
                binding.rvPaymentHistory.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                binding.txtNoRecordFound.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                try { total_records = pojo.getData().getPagination().getTotalRecords(); } catch (Exception ignored) {}
            }
        });
        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void serviceCallPaymentHistory(boolean clearFlag) {
        if (clearFlag) { page = 1; binding.txtNoRecordFound.setVisibility(View.GONE); binding.rvPaymentHistory.scrollToPosition(0); }
        pendingClear = clearFlag;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);
        viewModel.loadPartnerPaymentHistory(PrefsUtil.with(this).readString("UserId"), page);
    }

    private void initViews() {
        context = PartnerPaymentHistoryActivity.this;
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.payment_history), HtmlCompat.FROM_HTML_MODE_LEGACY));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.setHomeButtonEnabled(true); actionBar.setDisplayHomeAsUpEnabled(true); }
        linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        partnerPaymentHistoryAdapter = new PartnerPaymentHistoryAdapter(context, transactionArrayList);
        binding.rvPaymentHistory.setLayoutManager(linearLayoutManager);
        binding.rvPaymentHistory.setAdapter(partnerPaymentHistoryAdapter);
        binding.swipeRefresh.setOnRefreshListener(() -> { binding.swipeRefresh.setRefreshing(true); serviceCallPaymentHistory(true); });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception ignored) {}
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
