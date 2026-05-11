package com.app.bemyrider.activity.user;

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

import com.app.bemyrider.Adapter.User.PaymentHistoryAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityPaymentHistoryBinding;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.PaymentHistoryPojoItem;
import com.app.bemyrider.model.TransectionListItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.PaymentHistoryViewModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PaymentHistoryActivity extends AppCompatActivity {

    private ActivityPaymentHistoryBinding binding;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private ArrayList<TransectionListItem> paymentHistoryPojoItems;
    private PaymentHistoryViewModel viewModel;
    private Context context;
    private ConnectionManager connectionManager;
    private LinearLayoutManager linearLayoutManager;

    private boolean loading = true;
    private boolean pendingClear = false;
    private int page = 1, total_records = 0;
    private int pastVisiblesItems = 0, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_history, null);

        initViews();

        viewModel = new ViewModelProvider(this).get(PaymentHistoryViewModel.class);
        observeViewModel();

        binding.recyclerPaymentHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                visibleItemCount = linearLayoutManager.getChildCount();
                totalItemCount = linearLayoutManager.getItemCount();
                pastVisiblesItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (loading && (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                    loading = false;
                    if (paymentHistoryPojoItems.size() < total_records) { page++; serviceCallGetPaymentHistory(false); }
                }
            }
        });

        if (new ConnectionCheck().isNetworkConnected(this)) {
            serviceCallGetPaymentHistory(true);
        } else {
            getOfflineDetails();
        }
    }

    private void observeViewModel() {
        viewModel.getHistory().observe(this, pojo -> {
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                if (pendingClear) { paymentHistoryPojoItems.clear(); pendingClear = false; }
                if (pojo.getData().getTransectionList() != null) {
                    paymentHistoryPojoItems.addAll(pojo.getData().getTransectionList());
                    paymentHistoryAdapter.notifyDataSetChanged();
                    loading = true;
                    boolean hasItems = !paymentHistoryPojoItems.isEmpty();
                    binding.txtNoRecordFav.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                    binding.recyclerPaymentHistory.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                } else {
                    binding.recyclerPaymentHistory.setVisibility(View.GONE);
                    binding.txtNoRecordFav.setVisibility(View.VISIBLE);
                }
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

    private void serviceCallGetPaymentHistory(boolean isClear) {
        if (isClear) { page = 1; binding.txtNoRecordFav.setVisibility(View.GONE); binding.recyclerPaymentHistory.scrollToPosition(0); }
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);
        viewModel.loadPaymentHistory(PrefsUtil.with(this).readString("UserId"), page);
    }

    private void initViews() {
        context = PaymentHistoryActivity.this;
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.payment_history), HtmlCompat.FROM_HTML_MODE_LEGACY));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.setHomeButtonEnabled(true); actionBar.setDisplayHomeAsUpEnabled(true); }
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);
        paymentHistoryPojoItems = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        binding.recyclerPaymentHistory.setLayoutManager(linearLayoutManager);
        paymentHistoryAdapter = new PaymentHistoryAdapter(paymentHistoryPojoItems, this);
        binding.recyclerPaymentHistory.setAdapter(paymentHistoryAdapter);
        binding.swipeRefresh.setOnRefreshListener(() -> { binding.swipeRefresh.setRefreshing(true); serviceCallGetPaymentHistory(true); });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    @Override public void onStart() { super.onStart(); EventBus.getDefault().register(this); }
    @Override public void onStop() { EventBus.getDefault().unregister(this); super.onStop(); }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection") && event.getMessage().equalsIgnoreCase("disconnected")) {
                paymentHistoryPojoItems.clear();
                getOfflineDetails();
            }
        } catch (Exception ignored) {}
    }

    private void getOfflineDetails() {
        try {
            binding.progress.setVisibility(View.GONE);
            binding.txtNoRecordFav.setVisibility(View.GONE);
            binding.recyclerPaymentHistory.setVisibility(View.VISIBLE);
            File f = new File(getFilesDir().getPath() + "/offline.json");
            FileInputStream is = new FileInputStream(f);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONObject object = new JSONObject(new String(buffer));
            JSONArray serviceList = object.getJSONObject("data").getJSONArray("transection_list");
            Gson gson = new GsonBuilder().setDateFormat("M/d/yy hh:mm a").create();
            Type listType = new TypeToken<List<PaymentHistoryPojoItem>>() {}.getType();
            ArrayList<TransectionListItem> arrayList = gson.fromJson(serviceList.toString(), listType);
            paymentHistoryPojoItems.clear();
            paymentHistoryPojoItems.addAll(arrayList);
            if (arrayList.isEmpty()) binding.txtNoRecordFav.setVisibility(View.VISIBLE);
            paymentHistoryAdapter.notifyDataSetChanged();
        } catch (Exception e) { e.printStackTrace(); }
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
