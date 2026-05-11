package com.app.bemyrider.activity;

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

import com.app.bemyrider.Adapter.InfoPageAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityInfoPageBinding;
import com.app.bemyrider.model.InfoPagePojoItem;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.viewmodel.InfoPageViewModel;

import java.util.ArrayList;

public class InfoPageActivity extends AppCompatActivity {

    private ActivityInfoPageBinding binding;
    private Context context;
    private InfoPageAdapter infoPageAdapter;
    private ArrayList<InfoPagePojoItem> arrayList = new ArrayList<>();
    private ConnectionManager connectionManager;
    private InfoPageViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(InfoPageActivity.this, R.layout.activity_info_page, null);

        initViews();

        viewModel = new ViewModelProvider(this).get(InfoPageViewModel.class);
        observeViewModel();
        viewModel.loadInfoList();
    }

    private void observeViewModel() {
        viewModel.getInfoList().observe(this, pojo -> {
            if (binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }
            binding.progress.setVisibility(View.GONE);
            if (pojo != null && pojo.getInfoPageList() != null) {
                arrayList.clear();
                arrayList.addAll(pojo.getInfoPageList());
                infoPageAdapter.notifyDataSetChanged();
                boolean hasItems = !arrayList.isEmpty();
                binding.rvInfoPage.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                binding.imgNoRecord.setVisibility(hasItems ? View.GONE : View.VISIBLE);
            } else {
                binding.imgNoRecord.setVisibility(View.VISIBLE);
                binding.rvInfoPage.setVisibility(View.GONE);
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                binding.progress.setVisibility(View.GONE);
                binding.imgNoRecord.setVisibility(View.VISIBLE);
                binding.rvInfoPage.setVisibility(View.GONE);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        context = InfoPageActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + context.getResources().getString(R.string.info), HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        binding.rvInfoPage.setLayoutManager(layoutManager);
        infoPageAdapter = new InfoPageAdapter(InfoPageActivity.this, arrayList);
        binding.rvInfoPage.setAdapter(infoPageAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            viewModel.loadInfoList();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
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
