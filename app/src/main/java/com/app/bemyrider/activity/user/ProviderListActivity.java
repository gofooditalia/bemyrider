package com.app.bemyrider.activity.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.ProviderAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityProviderlistBinding;
import com.app.bemyrider.model.Pagination;
import com.app.bemyrider.model.user.ProviderListData;
import com.app.bemyrider.viewmodel.ProviderListViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.model.user.ProviderListItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;

import coil.Coil;
import coil.request.ImageRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ProviderListActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityProviderlistBinding binding;
    private ProviderAdapter providerAdapter;
    private ArrayList<ProviderListItem> arrayList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private boolean isRefreshing = false; 
    private int page = 1, total_page = 1;
    private ProviderListViewModel viewModel;
    private Context context;
    private ConnectionManager connectionManager;
    ActivityResultLauncher<Intent> myIntentActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_providerlist);

        init();

        viewModel = new ViewModelProvider(this).get(ProviderListViewModel.class);
        observeViewModel();

        binding.txtTitle.setText(getIntent().getStringExtra("serviceName"));

        searchProviders(true); 

        binding.rvProviders.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && page < total_page) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 2) {
                            searchProviders(false); 
                        }
                    }
                }
            }
        });

        binding.imgFilter.setOnClickListener(v -> {
            Intent i = new Intent(ProviderListActivity.this, FilterProviderActivity.class);
            i.putExtra("serviceName", getIntent().getStringExtra("serviceName"));
            i.putExtra("address", getIntent().getStringExtra("address"));
            i.putExtra("latitude", getIntent().getStringExtra("latitude"));
            i.putExtra("longitude", getIntent().getStringExtra("longitude"));
            i.putExtra("serviceId", getIntent().getStringExtra("serviceId"));
            myIntentActivityResultLauncher.launch(i);
        });
    }

    private void init() {
        context = ProviderListActivity.this;

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        setupToolBar();

        layoutManager = new LinearLayoutManager(this);
        binding.rvProviders.setLayoutManager(layoutManager);
        binding.rvProviders.setItemAnimator(new DefaultItemAnimator());
        binding.rvProviders.setNestedScrollingEnabled(true);

        providerAdapter = new ProviderAdapter(this, arrayList, this::favouriteToggle);
        binding.rvProviders.setAdapter(providerAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            searchProviders(true);
        });

        myActivityResult();
    }

    private void myActivityResult() {
        myIntentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                searchProviders(true);
            }
        });
    }

    private void setupToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void observeViewModel() {
        viewModel.getProviders().observe(this, pojo -> {
            binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            binding.paginationProgress.setVisibility(View.GONE);

            if (pojo != null && pojo.getData() != null) {
                ProviderListData data = pojo.getData();
                List<ProviderListItem> newItems = data.getServiceList();
                Pagination pagination = data.getPagination();

                if (pagination != null) {
                    total_page = pagination.getTotalPages();
                }

                if (isRefreshing) {
                    isRefreshing = false;
                    arrayList.clear();
                    if (newItems != null) arrayList.addAll(newItems);
                    providerAdapter.notifyDataSetChanged();

                    if (pagination != null) {
                        binding.txtProviderCount.setText(String.format("%d %s",
                                pagination.getTotalRecords(),
                                getResources().getString(R.string.provider_count)));
                    }
                } else {
                    if (newItems != null && !newItems.isEmpty()) {
                        int oldSize = arrayList.size();
                        arrayList.addAll(newItems);
                        providerAdapter.notifyItemRangeInserted(oldSize, newItems.size());
                    }
                }

                boolean hasItems = !arrayList.isEmpty();
                binding.rvProviders.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                binding.txtNoRecordFav.setVisibility(hasItems ? View.GONE : View.VISIBLE);
            }

            // isLoading resettato DOPO le notify: se un layout pass di RecyclerView
            // causato da notifyItemRangeInserted spara onScrolled, trova ancora isLoading=true.
            isLoading = false;
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                binding.paginationProgress.setVisibility(View.GONE);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                isLoading = false;
                isRefreshing = false;
            }
        });
    }

    private void searchProviders(boolean isClear) {
        if (isLoading) {
            return;
        }

        if (isClear) {
            page = 1;
            isRefreshing = true; 
        } else {
            page++;
            isRefreshing = false; 
        }

        isLoading = true;
        
        if (!binding.swipeRefresh.isRefreshing()) {
            if (page == 1) {
                binding.progress.setVisibility(View.VISIBLE);
            } else {
                binding.paginationProgress.setVisibility(View.VISIBLE);
            }
        }

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("user_id", PrefsUtil.with(this).readString("UserId"));
        params.put("service_id", getIntent().getStringExtra("serviceId"));
        params.put("category_id", getIntent().getStringExtra("categoryId"));
        params.put("subcategory_id", getIntent().getStringExtra("subCategoryId"));
        params.put("page", String.valueOf(page));

        if (getIntent().hasExtra("address")) params.put("search_location", getIntent().getStringExtra("address"));
        if (getIntent().hasExtra("latitude") && getIntent().hasExtra("longitude")) {
            params.put("search_lat", getIntent().getStringExtra("latitude"));
            params.put("search_long", getIntent().getStringExtra("longitude"));
        }
        if (getIntent().hasExtra("providerName")) params.put("search_provider_name", getIntent().getStringExtra("providerName"));
        if (getIntent().hasExtra("searchKeyWord")) params.put("search_keyword", getIntent().getStringExtra("searchKeyWord"));
        if (getIntent().hasExtra("rating") && !"0.0".equals(getIntent().getStringExtra("rating")))
            params.put("search_rating", getIntent().getStringExtra("rating"));
        if (getIntent().hasExtra("minRate")) params.put("search_min_rate", getIntent().getStringExtra("minRate"));
        if (getIntent().hasExtra("maxRate")) params.put("search_max_rate", getIntent().getStringExtra("maxRate"));
        if (getIntent().hasExtra("date")) params.put("search_service_date", getIntent().getStringExtra("date"));

        viewModel.loadProviders(params);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
    }

    protected void favouriteToggle(String providerServiceId, final ImageView img_fav, ProgressBar progress) {
        img_fav.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("service_id", providerServiceId);
        params.put("user_id", PrefsUtil.with(this).readString("UserId"));
        params.put("fvrt_val", "1".equals(img_fav.getTag()) ? "1" : "0");

        if ("1".equals(img_fav.getTag())) {
            img_fav.setImageResource(R.mipmap.ic_heart_fill);
            img_fav.setTag("0");
        } else {
            img_fav.setImageResource(R.mipmap.ic_heart_empty);
            img_fav.setTag("1");
        }
        
        progress.setVisibility(View.GONE);
        img_fav.setVisibility(View.VISIBLE);

        viewModel.toggleFavourite(params);
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        super.onDestroy();
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(LocaleManager.onAttach(newBase)); }
}
