package com.app.bemyrider.activity.partner;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.Partner.RvPartnerReviewsAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.PartnerActivityReviewsBinding;
import com.app.bemyrider.model.ServiceReviewItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.PartnerReviewsViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class PartnerReviewsActivity extends AppCompatActivity {

    private PartnerActivityReviewsBinding binding;
    private RvPartnerReviewsAdapter adapter;
    private ArrayList<ServiceReviewItem> arrayList = new ArrayList<>();
    private Context mContext = PartnerReviewsActivity.this;
    private LinearLayoutManager layoutManager;
    private PartnerReviewsViewModel viewModel;
    private ConnectionManager connectionManager;

    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisibleItems = 0, visibleItemCount, totalItemCount;
    private boolean pendingClear = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(PartnerReviewsActivity.this, R.layout.partner_activity_reviews, null);

        initView();

        viewModel = new ViewModelProvider(this).get(PartnerReviewsViewModel.class);
        observeViewModel();
        serviceCall(true);

        binding.rvPartnerreviews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if (loading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        loading = false;
                        if (arrayList.size() < total_records) {
                            page++;
                            serviceCall(false);
                        }
                    }
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getReviews().observe(this, pojo -> {
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                if (pendingClear) { arrayList.clear(); pendingClear = false; }
                arrayList.addAll(pojo.getData().getReviewList());
                adapter.notifyDataSetChanged();
                loading = true;
                boolean hasItems = !arrayList.isEmpty();
                binding.txtNoRecoedReview.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.rvPartnerreviews.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                try { total_records = pojo.getData().getPagination().getTotalRecords(); } catch (Exception ignored) {}
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void serviceCall(boolean isClear) {
        if (isClear) { page = 1; binding.txtNoRecoedReview.setVisibility(View.GONE); binding.rvPartnerreviews.scrollToPosition(0); }
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);

        Map<String, String> params = new LinkedHashMap<>();
        String userId = (getIntent().hasExtra(Utils.PROVIDER_ID) &&
                getIntent().getStringExtra(Utils.PROVIDER_ID) != null &&
                !getIntent().getStringExtra(Utils.PROVIDER_ID).isEmpty())
                ? getIntent().getStringExtra(Utils.PROVIDER_ID)
                : PrefsUtil.with(mContext).readString("UserId");
        params.put("user_id", userId);
        params.put("user_type", "p");
        params.put("page", String.valueOf(page));
        viewModel.loadReviews(params);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        if (getIntent().hasExtra(Utils.PROVIDER_ID)) {
            setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.ratings_review), HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.myrating), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.setHomeButtonEnabled(true); actionBar.setDisplayHomeAsUpEnabled(true); }
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);
        layoutManager = new LinearLayoutManager(mContext);
        binding.rvPartnerreviews.setLayoutManager(layoutManager);
        adapter = new RvPartnerReviewsAdapter(mContext, arrayList);
        binding.rvPartnerreviews.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(() -> { binding.swipeRefresh.setRefreshing(true); serviceCall(true); });
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
