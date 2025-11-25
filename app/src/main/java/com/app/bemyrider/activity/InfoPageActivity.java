package com.app.bemyrider.activity;

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

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.Adapter.InfoPageAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityInfoPageBinding;
import com.app.bemyrider.model.InfoPagePojo;
import com.app.bemyrider.model.InfoPagePojoItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Hardik Talaviya on 27/12/19.
 */

public class InfoPageActivity extends AppCompatActivity {

    private ActivityInfoPageBinding binding;
    private Context context;
    private LinearLayoutManager layoutManager;
    private InfoPageAdapter infoPageAdapter;
    private ArrayList<InfoPagePojoItem> arrayList = new ArrayList<>();
    private AsyncTask infoPageListAsync;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(InfoPageActivity.this, R.layout.activity_info_page, null);

        initViews();

        getInfoPageList();
    }

    /*-------------- Info Page List Api Call -----------------*/
    private void getInfoPageList() {
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.rvInfoPage.setVisibility(View.GONE);
            binding.progress.setVisibility(View.VISIBLE);
        }

        String url = WebServiceUrl.URL_GET_CMS_INFO;
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        new WebServiceCall(this, url, textParams, InfoPagePojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (binding.swipeRefresh.isRefreshing()) {
                            binding.swipeRefresh.setRefreshing(false);
                        }
                        binding.progress.setVisibility(View.GONE);
                        binding.rvInfoPage.setVisibility(View.VISIBLE);
                        if (status) {
                            InfoPagePojo infoPagePojo = (InfoPagePojo) obj;
                            arrayList.clear();
                            arrayList.addAll(infoPagePojo.getInfoPageList());
                            infoPageAdapter.notifyDataSetChanged();

                            if (arrayList.size() > 0) {
                                binding.rvInfoPage.setVisibility(View.VISIBLE);
                                binding.imgNoRecord.setVisibility(View.GONE);
                            } else {
                                binding.imgNoRecord.setVisibility(View.VISIBLE);
                                binding.rvInfoPage.setVisibility(View.GONE);
                            }

                        } else {
                            binding.imgNoRecord.setVisibility(View.VISIBLE);
                            binding.rvInfoPage.setVisibility(View.GONE);
                            Toast.makeText(InfoPageActivity.this, (String) obj,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        infoPageListAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        infoPageListAsync = null;
                    }
                });
    }

    private void initViews() {
        context = InfoPageActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + context.getResources().getString(R.string.info),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        /*Init Recycler View*/
        layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        binding.rvInfoPage.setLayoutManager(layoutManager);
        infoPageAdapter = new InfoPageAdapter(InfoPageActivity.this, arrayList);
        binding.rvInfoPage.setAdapter(infoPageAdapter);
        infoPageAdapter.notifyDataSetChanged();

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(true);
            getInfoPageList();
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
        Utils.cancelAsyncTask(infoPageListAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
