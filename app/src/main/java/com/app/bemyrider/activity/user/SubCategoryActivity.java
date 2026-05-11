package com.app.bemyrider.activity.user;

import static com.app.bemyrider.utils.Utils.PROVIDER_ID;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.Adapter.User.SelectSubCategoryAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivitySubCategoryBinding;
import com.app.bemyrider.model.partner.SubCategoryItem;
import com.app.bemyrider.model.partner.SubCategoryListPojo;
import com.app.bemyrider.model.user.CategoryDataItem;
import com.app.bemyrider.model.user.CategoryListPOJO;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Utils;
import com.app.bemyrider.viewmodel.SubCategoryViewModel;

import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

/**
 * Modified by Hardik Talaviya on 9/12/19.
 */

public class SubCategoryActivity extends AppCompatActivity {

    private ActivitySubCategoryBinding binding;
    private ArrayList<SubCategoryItem> subCategoryItems = new ArrayList<>();
    private SelectSubCategoryAdapter subCategoryAdapter;
    private String categoryId = "", categoryName = "";
    private SubCategoryViewModel viewModel;
    private Context context;
    private ConnectionManager connectionManager;
    private String providerId = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(SubCategoryActivity.this, R.layout.activity_sub_category, null);

        if (getIntent().hasExtra(PROVIDER_ID)) {
            if (getIntent().getStringExtra(PROVIDER_ID) != null
                    && getIntent().getStringExtra(PROVIDER_ID).length() > 0) {
                providerId = getIntent().getStringExtra(PROVIDER_ID);
            }
        }

        /*if (getIntent().hasExtra("CategoryId")) {
            if (getIntent().getStringExtra("CategoryId") != null
                    && getIntent().getStringExtra("CategoryId").length() > 0) {
                categoryId = getIntent().getStringExtra("CategoryId");
            } else {
                finish();
                Toast.makeText(SubCategoryActivity.this,
                        getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            }
        } else {
            finish();
            Toast.makeText(SubCategoryActivity.this,
                    getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
        }*/


        if (getIntent().hasExtra("CategoryName")) {
            if (getIntent().getStringExtra("CategoryName") != null
                    && getIntent().getStringExtra("CategoryName").length() > 0) {
                categoryName = getIntent().getStringExtra("CategoryName");
            } else {
                categoryName = "SubCategory";
            }
        } else {
            categoryName = "SubCategory";
        }

        initViews();

        viewModel = new ViewModelProvider(this).get(SubCategoryViewModel.class);
        observeViewModel();
        viewModel.loadSubcategories(providerId);
    }

    private void initViews() {
        context = SubCategoryActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>",HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        subCategoryAdapter = new SelectSubCategoryAdapter(SubCategoryActivity.this, subCategoryItems, providerId);
        binding.rvSubCategories.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.rvSubCategories.setItemAnimator(new DefaultItemAnimator());
        binding.rvSubCategories.setAdapter(subCategoryAdapter);
    }

    private void observeViewModel() {
        viewModel.getCategoryName().observe(this, name -> {
            if (name != null && !name.isEmpty()) {
                setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + name, HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
        });

        viewModel.getSubcategories().observe(this, pojo -> {
            binding.progress.setVisibility(View.GONE);
            binding.rvSubCategories.setVisibility(View.VISIBLE);
            if (pojo != null && pojo.getData() != null) {
                subCategoryItems.clear();
                subCategoryItems.addAll(pojo.getData());
                subCategoryAdapter.notifyDataSetChanged();
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
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
