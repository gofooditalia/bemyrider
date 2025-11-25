package com.app.bemyrider.activity.user;

import static com.app.bemyrider.utils.Utils.PROVIDER_ID;

import android.content.Context;
import android.os.AsyncTask;
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

import com.app.bemyrider.Adapter.User.SelectSubCategoryAdapter;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivitySubCategoryBinding;
import com.app.bemyrider.model.partner.SubCategoryItem;
import com.app.bemyrider.model.partner.SubCategoryListPojo;
import com.app.bemyrider.model.user.CategoryDataItem;
import com.app.bemyrider.model.user.CategoryListPOJO;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 9/12/19.
 */

public class SubCategoryActivity extends AppCompatActivity {

    private ActivitySubCategoryBinding binding;
    private ArrayList<SubCategoryItem> subCategoryItems = new ArrayList<>();
    private SelectSubCategoryAdapter subCategoryAdapter;
    private String categoryId = "", categoryName = "";
    private AsyncTask subCategoryAsync, categoryListAsync;
    private Context context;
    private ConnectionManager connectionManager;
    private String providerId = "";
    private ArrayList<CategoryDataItem> categoryDataItems = new ArrayList<>();

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

        getCategory();
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

    /*----------------- Get Category Api Call -------------------*/
    private void getCategory() {
        binding.rvSubCategories.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("provider_id", providerId);

        new WebServiceCall(this, WebServiceUrl.URL_CATEGORYLIST, textParams,
                CategoryListPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                try {
                    if (status) {
                        CategoryListPOJO listPojo = (CategoryListPOJO) obj;
                        categoryDataItems.clear();
                        categoryDataItems.addAll(listPojo.getData());
                        if (categoryDataItems.size() > 0) {
                            categoryId = categoryDataItems.get(0).getCategoryId();
                            categoryName = categoryDataItems.get(0).getCategoryName();
                            if (categoryName != null && !"".equals(categoryName))
                                setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + categoryName,HtmlCompat.FROM_HTML_MODE_LEGACY));
                            else
                                setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + "SubCategory",HtmlCompat.FROM_HTML_MODE_LEGACY));
                            getSubCategory();
                        } else {
                            Toast.makeText(SubCategoryActivity.this,
                                    getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                categoryListAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                categoryListAsync = null;
            }
        });
    }

    /*---------------- Get Sub Category Api Call -------------------*/
    private void getSubCategory() {
        binding.rvSubCategories.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("category_id", categoryId);
        textParams.put("provider_id", providerId);

        new WebServiceCall(this, WebServiceUrl.URL_SUBCATEGORYLIST, textParams,
                SubCategoryListPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.rvSubCategories.setVisibility(View.VISIBLE);
                if (status) {
                    SubCategoryListPojo listPojo = (SubCategoryListPojo) obj;
                    subCategoryItems.clear();
                    subCategoryItems.addAll(listPojo.getData());
                    subCategoryAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                subCategoryAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                subCategoryAsync = null;
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
        Utils.cancelAsyncTask(subCategoryAsync);
        Utils.cancelAsyncTask(categoryListAsync);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
