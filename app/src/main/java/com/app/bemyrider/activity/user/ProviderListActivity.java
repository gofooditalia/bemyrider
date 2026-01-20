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
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityProviderlistBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.user.ProviderListData;
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
    private int pastVisibleItems, visibleItemCount, totalItemCount, page = 1, total_page = 1;
    private WebServiceCall searchListAsync, favTask;
    private Context context;
    private ConnectionManager connectionManager;
    ActivityResultLauncher<Intent> myIntentActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        binding = DataBindingUtil.setContentView(ProviderListActivity.this, R.layout.activity_providerlist);

        init();

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

    private void searchProviders(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.txtNoRecordFav.setVisibility(View.GONE);
            binding.rvProviders.scrollToPosition(0);
        }
        isLoading = true;
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        String url = WebServiceUrl.URL_PROVIDERLIST;
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(ProviderListActivity.this).readString("UserId"));
        textParams.put("service_id", getIntent().getStringExtra("serviceId"));
        if (getIntent().hasExtra("address")) {
            textParams.put("search_location", getIntent().getStringExtra("address"));
        }
        textParams.put("category_id", getIntent().getStringExtra("categoryId"));
        textParams.put("subcategory_id", getIntent().getStringExtra("subCategoryId"));
        if (getIntent().hasExtra("providerName")) {
            textParams.put("search_provider_name", getIntent().getStringExtra("providerName"));
        }
        if (getIntent().hasExtra("searchKeyWord")) {
            textParams.put("search_keyword", getIntent().getStringExtra("searchKeyWord"));
        }
        if (getIntent().hasExtra("rating")) {
            if (!getIntent().getStringExtra("rating").equals("0.0")) {
                textParams.put("search_rating", getIntent().getStringExtra("rating"));
            }
        }
        if (getIntent().hasExtra("minRate")) {
            textParams.put("search_min_rate", getIntent().getStringExtra("minRate"));
        }
        if (getIntent().hasExtra("date")) {
            textParams.put("search_service_date", getIntent().getStringExtra("date"));
        }
        if (getIntent().hasExtra("maxRate")) {
            textParams.put("search_max_rate", getIntent().getStringExtra("maxRate"));
        }
        if (getIntent().hasExtra("latitude") && getIntent().hasExtra("longitude")) {
            textParams.put("search_lat", getIntent().getStringExtra("latitude"));
            textParams.put("search_long", getIntent().getStringExtra("longitude"));
        }
        textParams.put("page", String.valueOf(page));

        new WebServiceCall(this, url, textParams, ProviderListPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                if (status) {
                    ProviderListPOJO providerData = (ProviderListPOJO) obj;
                    ProviderListData providerListData = providerData.getData();
                    if (isClear) {
                        arrayList.clear();
                        if (providerListData.getServiceList().isEmpty()) {
                            binding.txtProviderCount.setText(String.format("%s%s", getResources().getString(R.string.no), getResources().getString(R.string.provider_count)));
                        } else {
                            binding.txtProviderCount.setText(providerListData.getPagination().getTotalRecords() + " "
                                    + getResources().getString(R.string.provider_count));
                        }
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.rvProviders.setVisibility(View.VISIBLE);

                    arrayList.addAll(providerListData.getServiceList());

                    if (arrayList.size() > 0) {
                        binding.rvProviders.setVisibility(View.VISIBLE);
                        binding.txtNoRecordFav.setVisibility(View.GONE);
                    } else {
                        binding.rvProviders.setVisibility(View.GONE);
                        binding.txtNoRecordFav.setVisibility(View.VISIBLE);
                    }

                    providerAdapter.notifyDataSetChanged();

                    total_page = providerListData.getPagination().getTotalPages();
                    page = providerListData.getPagination().getCurrentPage();
                } else {
                    Toast.makeText(ProviderListActivity.this, (String) obj,
                            Toast.LENGTH_SHORT).show();
                }
                isLoading = false;
            }

            @Override
            public void onAsync(Object obj) { searchListAsync = null; }
            @Override public void onCancelled() { searchListAsync = null; }
        });
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

        String url = WebServiceUrl.URL_FAVOURITETOGGLE;
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("service_id", getIntent().getStringExtra("providerServiceId"));
        textParams.put("user_id", PrefsUtil.with(ProviderListActivity.this).readString("UserId"));
        if (img_fav.getTag().equals("1")) {
            textParams.put("fvrt_val", "0");
        } else if (img_fav.getTag().equals("0")) {
            textParams.put("fvrt_val", "1");
        }

        new WebServiceCall(ProviderListActivity.this, url, textParams, CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                progress.setVisibility(View.GONE);
                img_fav.setVisibility(View.VISIBLE);
                if (status) {
                    Toast.makeText(context, ((CommonPojo) obj).getMessage(), Toast.LENGTH_SHORT).show();
                    
                    if (img_fav.getTag().equals("1")) {
                        ImageRequest request = new ImageRequest.Builder(context).data(R.mipmap.ic_heart_fill).placeholder(R.drawable.loading).target(img_fav).build();
                        Coil.imageLoader(context).enqueue(request);
                        img_fav.setTag("0");
                    } else if (img_fav.getTag().equals("0")) {
                        ImageRequest request = new ImageRequest.Builder(context).data(R.mipmap.ic_heart_empty).placeholder(R.drawable.loading).target(img_fav).build();
                        Coil.imageLoader(context).enqueue(request);
                        img_fav.setTag("1");
                    }
                } else {
                    Toast.makeText(ProviderListActivity.this, (String) obj, Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onAsync(Object obj) { favTask = null; }
            @Override public void onCancelled() { favTask = null; }
        });
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        Utils.cancelAsyncTask(searchListAsync);
        Utils.cancelAsyncTask(favTask);
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
