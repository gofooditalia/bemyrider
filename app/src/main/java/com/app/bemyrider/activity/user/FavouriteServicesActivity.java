package com.app.bemyrider.activity.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.FavoriteServiceAdapter;
import com.app.bemyrider.AsyncTask.ConnectionCheck;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityFavouriteServicesBinding;
import com.app.bemyrider.model.MessageEvent;
import com.app.bemyrider.model.user.FavoriteServiceListPojo;
import com.app.bemyrider.viewmodel.FavouriteViewModel;

import androidx.lifecycle.ViewModelProvider;
import com.app.bemyrider.model.user.FavoriteServiceListPojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
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
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by nct121 on 5/12/16.
 * Modified by Hardik Talaviya on 10/12/19.
 */

public class FavouriteServicesActivity extends AppCompatActivity {

    private ActivityFavouriteServicesBinding binding;
    private FavoriteServiceAdapter adapter;
    private ArrayList<FavoriteServiceListPojoItem> favoriteServiceListPojoItems;
    private int visibleItemCount, totalItemCount, pastVisibleItems;
    private int page = 1;
    private int total_records = 0;
    private LinearLayoutManager layoutManager;
    private boolean loading = false;
    private boolean pendingClear = false;
    private String keyWord = "";
    private FavouriteViewModel viewModel;
    private Context context;
    private ConnectionManager connectionManager;
    ActivityResultLauncher<Intent> myIntentActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(FavouriteServicesActivity.this, R.layout.activity_favourite_services, null);

        init();

        viewModel = new ViewModelProvider(this).get(FavouriteViewModel.class);
        observeViewModel();

        if (new ConnectionCheck().isNetworkConnected(this)) {
            serviceCallGetFavoriteList(true);
        } else {
            getOfflineDetails();
        }

        binding.rvFavouriteServices.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                    if (!loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = true;
                            if (favoriteServiceListPojoItems.size() < total_records) {
                                page++;
                                serviceCallGetFavoriteList(false);
                            }
                        }
                    }
                }
            }
        });

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { keyWord = s.toString().trim(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.imgSearch.setOnClickListener(v -> {
            Utils.hideSoftKeyboard(FavouriteServicesActivity.this);
            binding.rvFavouriteServices.setVisibility(View.GONE);
            serviceCallGetFavoriteList(true);
        });

        binding.edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.imgSearch.performClick();
                return true;
            }
            return false;
        });


    }

    private void observeViewModel() {
        viewModel.getFavourites().observe(this, pojo -> {
            if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getData() != null) {
                if (pendingClear) { favoriteServiceListPojoItems.clear(); pendingClear = false; }
                favoriteServiceListPojoItems.addAll(pojo.getData().getServiceList());
                adapter.notifyDataSetChanged();
                boolean hasItems = !favoriteServiceListPojoItems.isEmpty();
                binding.txtNoRecordFav.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.rvFavouriteServices.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                try { total_records = pojo.getData().getPagination().getTotalRecords(); } catch (Exception ignored) {}
                loading = false;
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                binding.rvFavouriteServices.setVisibility(View.GONE);
                binding.txtNoRecordFav.setVisibility(View.VISIBLE);
                loading = false;
            }
        });
    }

    private void serviceCallGetFavoriteList(boolean isClear) {
        if (isClear) {
            page = 1;
            binding.txtNoRecordFav.setVisibility(View.GONE);
            binding.rvFavouriteServices.scrollToPosition(0);
        }
        pendingClear = isClear;
        if (!binding.swipeRefresh.isRefreshing()) binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("user_id", PrefsUtil.with(this).readString("UserId"));
        params.put("txt_search", Utils.encodeEmoji(keyWord));
        params.put("page", String.valueOf(page));
        viewModel.loadFavourites(params);
    }

    private void serviceCallToggleFavorite(final int position, String providerServiceId, ImageView imgRemove, ProgressBar progress) {
        imgRemove.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("service_id", providerServiceId);
        params.put("user_id", PrefsUtil.with(this).readString("UserId"));
        params.put("fvrt_val", "1");
        params.put("delivery_type", favoriteServiceListPojoItems.get(position).getDeliveryType());
        params.put("request_type", favoriteServiceListPojoItems.get(position).getRequestType());
        params.put("provider_id", favoriteServiceListPojoItems.get(position).getProviderId());

        viewModel.getToggleResult().observe(this, result -> {
            progress.setVisibility(View.GONE);
            imgRemove.setVisibility(View.VISIBLE);
            if (result != null && result.isStatus()) {
                Toast.makeText(context, result.getMessage(), Toast.LENGTH_SHORT).show();
                favoriteServiceListPojoItems.remove(position);
                adapter.notifyDataSetChanged();
                boolean hasItems = !favoriteServiceListPojoItems.isEmpty();
                binding.txtNoRecordFav.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                binding.rvFavouriteServices.setVisibility(hasItems ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.toggleFavourite(params);
    }

    private void init() {
        context = FavouriteServicesActivity.this;
        setupToolBar();

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        myActivityResult();

        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        favoriteServiceListPojoItems = new ArrayList<>();
        binding.rvFavouriteServices.setLayoutManager(new LinearLayoutManager(FavouriteServicesActivity.this, RecyclerView.VERTICAL, false));
        adapter = new FavoriteServiceAdapter(FavouriteServicesActivity.this,
                favoriteServiceListPojoItems, this::serviceCallToggleFavorite,myIntentActivityResultLauncher);
        binding.rvFavouriteServices.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            serviceCallGetFavoriteList(true);
        });
    }

    private void myActivityResult() {
        myIntentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                try {
                    if (result.getData() != null) {
                        boolean isFavRefresh = result.getData().getBooleanExtra("isFavRefresh",false);
                        if (isFavRefresh) {
                            binding.rvFavouriteServices.setVisibility(View.GONE);
                            serviceCallGetFavoriteList(true);
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void setupToolBar() {
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.favorite_services),HtmlCompat.FROM_HTML_MODE_LEGACY));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onStart() { super.onStart(); if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this); }
    @Override public void onStop() { super.onStop(); if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this); super.onStop(); }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    getOfflineDetails();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

    }

    private void getOfflineDetails() {
        try {
            binding.txtNoRecordFav.setVisibility(View.GONE);
            binding.progress.setVisibility(View.GONE);
            binding.rvFavouriteServices.setVisibility(View.VISIBLE);
            Log.e("Offline", "onMessageEvent: My Resolution");
            File f = new File(getFilesDir().getPath() + "/" + "offline.json");
            if (!f.exists()) return;
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String s = new String(buffer);
            JSONObject object = new JSONObject(s);
            JSONObject dataObj = object.getJSONObject("data");
            JSONArray serviceList = dataObj.getJSONArray("favoriteServices");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a");
            Gson gson = gsonBuilder.create();
            Type listType = new TypeToken<List<FavoriteServiceListPojoItem>>() {}.getType();
            ArrayList<FavoriteServiceListPojoItem> arrayList = gson.fromJson(serviceList.toString(), listType);

            favoriteServiceListPojoItems.clear();
            favoriteServiceListPojoItems.addAll(arrayList);
            if (arrayList.isEmpty()) {
                binding.txtNoRecordFav.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected void onDestroy() {
        try { connectionManager.unregisterReceiver(); } catch (Exception e) { e.printStackTrace(); }
        super.onDestroy();
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(LocaleManager.onAttach(newBase)); }
}
