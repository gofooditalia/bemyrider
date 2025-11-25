package com.app.bemyrider.activity.partner;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.Partner.RvPartnerReviewsAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerActivityReviewsBinding;
import com.app.bemyrider.model.ServiceReviewItem;
import com.app.bemyrider.model.ServiceReviewPojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 2/12/19.
 */

public class PartnerReviewsActivity extends AppCompatActivity {

    private PartnerActivityReviewsBinding binding;
    private RvPartnerReviewsAdapter adapter;
    private ArrayList<ServiceReviewItem> arrayList = new ArrayList<>();
    private Context mContext = PartnerReviewsActivity.this;
    private LinearLayoutManager layoutManager;
    private AsyncTask reviewListAsync;
    private ConnectionManager connectionManager;

    /*pagination vars start*/
    private boolean loading = true;
    private int page = 1;
    private int total_records = 0;
    private int pastVisibleItems = 0, visibleItemCount, totalItemCount;
    /*pagination vars end*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(PartnerReviewsActivity.this, R.layout.partner_activity_reviews, null);

        initView();

        serviceCall(true);

        binding.rvPartnerreviews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            if (arrayList.size() < total_records) {
                                page++;
                                serviceCall(false);
                            }
                        }
                    }
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

    /*-------------- Reviews List Api Call -----------------*/
    private void serviceCall(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.txtNoRecoedReview.setVisibility(View.GONE);
            binding.rvPartnerreviews.scrollToPosition(0);
        }
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        //{user_id=2, user_type=p, page=1}

        if (getIntent().hasExtra(Utils.PROVIDER_ID)) {
            if (getIntent().getStringExtra(Utils.PROVIDER_ID) != null && getIntent().getStringExtra(Utils.PROVIDER_ID).length() > 0) {
                textParams.put("user_id", getIntent().getStringExtra(Utils.PROVIDER_ID));
            } else {
                textParams.put("user_id", PrefsUtil.with(mContext).readString("UserId"));
            }
        } else {
            textParams.put("user_id", PrefsUtil.with(mContext).readString("UserId"));
        }
        textParams.put("user_type", "p");
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(mContext, WebServiceUrl.URL_PROVIDER_REVIEWS, textParams,
                ServiceReviewPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                if (status) {
                    ServiceReviewPojo pojo = (ServiceReviewPojo) obj;
                    if (isClear) {
                        arrayList.clear();
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.rvPartnerreviews.setVisibility(View.VISIBLE);

                    arrayList.addAll(pojo.getData().getReviewList());
                    adapter.notifyDataSetChanged();
                    loading = true;

                    if (!(arrayList.size() > 0)) {
                        binding.txtNoRecoedReview.setVisibility(View.VISIBLE);
                        binding.rvPartnerreviews.setVisibility(View.GONE);
                    } else {
                        binding.txtNoRecoedReview.setVisibility(View.GONE);
                        binding.rvPartnerreviews.setVisibility(View.VISIBLE);
                    }
                    try {
                        total_records = pojo.getData().getPagination().getTotalRecords();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(PartnerReviewsActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                reviewListAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                reviewListAsync = null;
            }
        });
    }

    private void initView() {

        if (getIntent().hasExtra(Utils.PROVIDER_ID)){
            setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.ratings_review),HtmlCompat.FROM_HTML_MODE_LEGACY));
        }else{
            setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.myrating),HtmlCompat.FROM_HTML_MODE_LEGACY));
        }


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        layoutManager = new LinearLayoutManager(mContext);
        binding.rvPartnerreviews.setLayoutManager(layoutManager);
        adapter = new RvPartnerReviewsAdapter(mContext, arrayList);
        binding.rvPartnerreviews.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCall(true);
        });
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(reviewListAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
