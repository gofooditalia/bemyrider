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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.ProviderAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityProviderlistBinding;
import com.app.bemyrider.model.user.ProviderListData;
import com.app.bemyrider.viewmodel.ProviderListViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.model.user.ProviderListItem;
import com.app.bemyrider.model.user.ProviderListPOJO;
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

/**
 * Modified by Hardik Talaviya on 9/12/19.
 */

public class ProviderListActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityProviderlistBinding binding;
    private ProviderAdapter providerAdapter;
    private GoogleMap map;
    private ArrayList<ProviderListItem> arrayList;
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private boolean pendingClear = false;
    private int pastVisibleItems, visibleItemCount, totalItemCount, page = 1, total_page = 1;
    private ProviderListViewModel viewModel;
    private Context context;
    private ConnectionManager connectionManager;
    ActivityResultLauncher<Intent> myIntentActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        binding = DataBindingUtil.setContentView(ProviderListActivity.this, R.layout.activity_providerlist);

        init();

        viewModel = new ViewModelProvider(this).get(ProviderListViewModel.class);
        observeViewModel();

        binding.txtTitle.setText(getIntent().getStringExtra("serviceName"));

        searchProviders(true);

        binding.rvProviders.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ((!isLoading) && page < total_page) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            page++;
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

        layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.rvProviders.setHasFixedSize(true);
        binding.rvProviders.setLayoutManager(layoutManager);
        binding.rvProviders.setItemAnimator(new DefaultItemAnimator());

        arrayList = new ArrayList<>();
        providerAdapter = new ProviderAdapter(ProviderListActivity.this, arrayList,
                this::favouriteToggle);
        binding.rvProviders.setAdapter(providerAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            searchProviders(true);
        });

        myActivityResult();
    }

    private void myActivityResult() {
        myIntentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                binding.rvProviders.setVisibility(View.GONE);
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
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                ProviderListData data = pojo.getData();
                if (pendingClear) {
                    arrayList.clear();
                    if (data.getServiceList().isEmpty()) {
                        binding.txtProviderCount.setText(String.format("%s%s",
                                getResources().getString(R.string.no), getResources().getString(R.string.provider_count)));
                    } else {
                        binding.txtProviderCount.setText(data.getPagination().getTotalRecords() + " "
                                + getResources().getString(R.string.provider_count));
                    }
                    pendingClear = false;
                }
                binding.rvProviders.setVisibility(View.VISIBLE);
                arrayList.addAll(data.getServiceList());
                boolean hasItems = !arrayList.isEmpty();
                binding.rvProviders.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                binding.txtNoRecordFav.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                providerAdapter.notifyDataSetChanged();
                total_page = data.getPagination().getTotalPages();
                page = data.getPagination().getCurrentPage();
            }
            isLoading = false;
        });

        viewModel.getToggleResult().observe(this, result -> {
            // Handled inline in favouriteToggle — no additional action needed here
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                isLoading = false;
            }
        });
    }

    private void searchProviders(boolean isClear) {
        if (isClear) { page = 1; binding.txtNoRecordFav.setVisibility(View.GONE); binding.rvProviders.scrollToPosition(0); }
        isLoading = true;
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("user_id", PrefsUtil.with(this).readString("UserId"));
        params.put("service_id", getIntent().getStringExtra("serviceId"));
        if (getIntent().hasExtra("address")) params.put("search_location", getIntent().getStringExtra("address"));
        params.put("category_id", getIntent().getStringExtra("categoryId"));
        params.put("subcategory_id", getIntent().getStringExtra("subCategoryId"));
        if (getIntent().hasExtra("providerName")) params.put("search_provider_name", getIntent().getStringExtra("providerName"));
        if (getIntent().hasExtra("searchKeyWord")) params.put("search_keyword", getIntent().getStringExtra("searchKeyWord"));
        if (getIntent().hasExtra("rating") && !"0.0".equals(getIntent().getStringExtra("rating")))
            params.put("search_rating", getIntent().getStringExtra("rating"));
        if (getIntent().hasExtra("minRate")) params.put("search_min_rate", getIntent().getStringExtra("minRate"));
        if (getIntent().hasExtra("date")) params.put("search_service_date", getIntent().getStringExtra("date"));
        if (getIntent().hasExtra("maxRate")) params.put("search_max_rate", getIntent().getStringExtra("maxRate"));
        if (getIntent().hasExtra("latitude") && getIntent().hasExtra("longitude")) {
            params.put("search_lat", getIntent().getStringExtra("latitude"));
            params.put("search_long", getIntent().getStringExtra("longitude"));
        }
        params.put("page", String.valueOf(page));
        viewModel.loadProviders(params);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            map = googleMap;
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setInfoWindowAdapter(new MyInfoWindowAdapter());
        }
    }

    protected void favouriteToggle(String providerServiceId, final ImageView img_fav, ProgressBar progress) {
        img_fav.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("service_id", providerServiceId);
        params.put("user_id", PrefsUtil.with(this).readString("UserId"));
        params.put("fvrt_val", "1".equals(img_fav.getTag()) ? "0" : "1");

        viewModel.getToggleResult().observe(this, result -> {
            progress.setVisibility(View.GONE);
            img_fav.setVisibility(View.VISIBLE);
            if (result != null) {
                if (result.isStatus()) {
                    Toast.makeText(context, result.getMessage(), Toast.LENGTH_SHORT).show();
                    if ("1".equals(img_fav.getTag())) {
                        Coil.imageLoader(context).enqueue(new ImageRequest.Builder(context).data(R.mipmap.ic_heart_fill).placeholder(R.drawable.loading).target(img_fav).build());
                        img_fav.setTag("0");
                    } else if ("0".equals(img_fav.getTag())) {
                        Coil.imageLoader(context).enqueue(new ImageRequest.Builder(context).data(R.mipmap.ic_heart_empty).placeholder(R.drawable.loading).target(img_fav).build());
                        img_fav.setTag("1");
                    }
                }
            }
        });
        viewModel.toggleFavourite(params);
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        super.onDestroy();
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View myContentsView;
        MyInfoWindowAdapter() { myContentsView = getLayoutInflater().inflate(R.layout.itemrow_provider, null); }

        @Override
        public View getInfoContents(Marker marker) {
            ImageView img_profile = myContentsView.findViewById(R.id.img_profile);
            TextView txt_rating = myContentsView.findViewById(R.id.txt_rating);
            TextView txt_serviceName = myContentsView.findViewById(R.id.txt_serviceName);
            ProgressBar progress = myContentsView.findViewById(R.id.progress);
            final ImageView img_fav = myContentsView.findViewById(R.id.img_fav);

            final ProviderListItem item = (ProviderListItem) marker.getTag();
            if (item != null) {
                txt_rating.setText(item.getAvgRating() > 0 ? String.valueOf(item.getAvgRating()) : "0.0");
                txt_serviceName.setText(String.format("%s %s", item.getProviderFirstName(), item.getProviderLastName()));
                
                ImageRequest.Builder profileBuilder = new ImageRequest.Builder(context).placeholder(R.drawable.loading).target(img_profile);
                if (item.getProviderImage() != null && item.getProviderImage().length() > 0) profileBuilder.data(item.getProviderImage());
                else profileBuilder.data(R.mipmap.user);
                Coil.imageLoader(context).enqueue(profileBuilder.build());

                int heartResource = item.getFavoriteId() > 0 ? R.mipmap.ic_heart_fill : R.mipmap.ic_heart_empty;
                String tag = item.getFavoriteId() > 0 ? "1" : "2";
                ImageRequest favRequest = new ImageRequest.Builder(context).data(heartResource).placeholder(R.drawable.loading).target(img_fav).build();
                Coil.imageLoader(context).enqueue(favRequest);
                img_fav.setTag(tag);

                myContentsView.setOnClickListener(v -> {
                    Intent i = new Intent(ProviderListActivity.this, ServiceDetailActivity.class);
                    i.putExtra("providerServiceId", item.getProviderServiceId());
                    startActivity(i);
                });
                img_fav.setOnClickListener(v -> favouriteToggle(item.getProviderServiceId(), img_fav, progress));
            }
            return myContentsView;
        }

        @Override public View getInfoWindow(Marker marker) { return null; }
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(LocaleManager.onAttach(newBase)); }
}
