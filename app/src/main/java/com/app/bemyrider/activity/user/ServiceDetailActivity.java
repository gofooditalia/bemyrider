package com.app.bemyrider.activity.user;

import static com.app.bemyrider.utils.Utils.PROVIDER_ID;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.bemyrider.fragment.user.DetailFragment;
import com.app.bemyrider.fragment.user.ImageFragment;
import com.app.bemyrider.fragment.user.ReviewFragment;
import com.app.bemyrider.fragment.user.UserDetailFragment;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityServiceDetailBinding;
import com.app.bemyrider.model.ProviderServiceDetailPOJO;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.model.ProviderServiceReviewDataItem;
import com.app.bemyrider.model.ServiceDataItem;
import com.app.bemyrider.model.ServiceListPOJO;
import com.app.bemyrider.model.partner.EditProfilePojo;
import com.app.bemyrider.model.partner.SubCategoryItem;
import com.app.bemyrider.model.partner.SubCategoryListPojo;
import com.app.bemyrider.model.user.CategoryDataItem;
import com.app.bemyrider.model.user.CategoryListPOJO;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Modified by Hardik Talaviya on 9/12/19.
 */

public class ServiceDetailActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private ActivityServiceDetailBinding binding;
    private int defaultTab = 0;
    private ProviderServiceDetailsItem serviceDetailData;
    private int[] tabIcons = {
            R.drawable.tabicon_servicedetail_style,
            R.drawable.tabicon_userdetail_style,
            R.drawable.tabicon_review_style,
            R.drawable.tabicon_images_style
    };
    private AsyncTask serviceDetailAsync, actionFavouriteAsync, categoryListAsync, subCategoryAsync, popularServiceAsync;
    ;
    private Context context;
    private ConnectionManager connectionManager;
    private ArrayList<ProviderServiceReviewDataItem> reviewArrayList = new ArrayList<>();
    boolean isFavRefresh = false;
    private String providerServiceId = "";
    private String providerId = "";
    private ArrayList<CategoryDataItem> categoryDataItems = new ArrayList<>();
    private ArrayList<SubCategoryItem> subCategoryItems = new ArrayList<>();
    private ArrayList<ServiceDataItem> serviceDataItems = new ArrayList<>();

    private String categoryId = "";
    private String subCategoryId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(ServiceDetailActivity.this, R.layout.activity_service_detail, null);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        init();

        if (getIntent().getStringExtra("providerServiceId") != null)
            providerServiceId = getIntent().getStringExtra("providerServiceId");

        if (getIntent().getStringExtra("isCallApi") != null && getIntent().getStringExtra("isCallApi").equals("y")) {
            if (getIntent().hasExtra(PROVIDER_ID)) {
                if (getIntent().getStringExtra(PROVIDER_ID) != null
                        && getIntent().getStringExtra(PROVIDER_ID).length() > 0) {
                    providerId = getIntent().getStringExtra(PROVIDER_ID);
                }
            }
            /*getCategory();*/
            getDetails(true);
        } else {
            getDetails(false);
        }
    }

    private void setupTabIcons() {
        binding.tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        binding.tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        binding.tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        binding.tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        binding.pager.setCurrentItem(tab.getPosition());
        int tabIconColor = ContextCompat.getColor(this, R.color.button);
        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private void init() {
        context = ServiceDetailActivity.this;

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.service_name),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle dataBundle = new Bundle();
        dataBundle.putSerializable("data", serviceDetailData);
        Fragment detailFragment = new DetailFragment();
        Fragment userDetailFragment = new UserDetailFragment();
        Fragment reviewFragment = new ReviewFragment(reviewArrayList);
        Fragment imageFragment = new ImageFragment();
        detailFragment.setArguments(dataBundle);
        userDetailFragment.setArguments(dataBundle);
        reviewFragment.setArguments(dataBundle);
        imageFragment.setArguments(dataBundle);
        adapter.addFrag(detailFragment, "ServiceDetail");
        adapter.addFrag(userDetailFragment, "UserDetail");
        adapter.addFrag(reviewFragment, "Review");
        adapter.addFrag(imageFragment, "Images");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
    }

    /*------------ Get Details Api Call --------------*/
    protected void getDetails(boolean isFlag) {
        binding.rlMain.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        String webServiceUrl = "";
        if (isFlag) {
            webServiceUrl = WebServiceUrl.MY_SERVICE_DETAILS_HOME;
            textParams.put("provider_id", providerId);
        } else {
            webServiceUrl = WebServiceUrl.MY_SERVICE_DETAILS;
            textParams.put("provider_service_id", providerServiceId);
        }
        textParams.put("user_id", PrefsUtil.with(ServiceDetailActivity.this).readString("UserId"));
        textParams.put("delivery_type", PrefsUtil.with(context).readString("delivery_type"));
        textParams.put("request_type", PrefsUtil.with(context).readString("request_type"));

        new WebServiceCall(this, webServiceUrl, textParams,
                ProviderServiceDetailPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.rlMain.setVisibility(View.VISIBLE);
                if (status) {
                    ProviderServiceDetailPOJO mainDetail = (ProviderServiceDetailPOJO) obj;
                    serviceDetailData = mainDetail.getData();
                    reviewArrayList.clear();
                    reviewArrayList.addAll(mainDetail.getData().getReviewData());
                    setData();
                } else {
                    Toast.makeText(ServiceDetailActivity.this, (String) obj, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                serviceDetailAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                serviceDetailAsync = null;
            }
        });
    }

    private void setData() {
        if (!Utils.isNullOrEmpty(serviceDetailData.getProviderImage())) {
            Picasso.get().load(serviceDetailData.getProviderImage())
                    .placeholder(R.drawable.loading).into(binding.imgProfile);
        }

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + serviceDetailData.getServiceName(),HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (Integer.parseInt(serviceDetailData.getTotalFavorite()) > 0) {
            binding.imgFav.setImageResource(R.mipmap.ic_heart_fill);
            binding.imgFav.setTag("0");
        } else {
            binding.imgFav.setImageResource(R.mipmap.ic_heart_empty);
            binding.imgFav.setTag("1");
        }

        binding.txtRatingShow.setText(String.valueOf(serviceDetailData.getAvgRating()));
        binding.txtName.setText(String.format("%s %s", serviceDetailData.getFirstName(), serviceDetailData.getLastName()));

        setupViewPager(binding.pager);
        binding.tabLayout.setupWithViewPager(binding.pager);
        setupTabIcons();
        if (defaultTab != 0) {
            binding.pager.setCurrentItem(defaultTab);
            defaultTab = 0;
        }

        binding.llFavourite.setOnClickListener(v -> favouriteToggle());

        binding.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PartnerProfileActivity.class);
                i.putExtra(Utils.PROVIDER_ID, serviceDetailData.getProviderId());
                startActivity(i);
            }
        });

        binding.layoutProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PartnerProfileActivity.class);
                i.putExtra(Utils.PROVIDER_ID, serviceDetailData.getProviderId());
                startActivity(i);
            }
        });
    }

    /*---------------- Favourite Api Call ------------------*/
    protected void favouriteToggle() {
        binding.llFavourite.setClickable(false);
        binding.imgFav.setVisibility(View.GONE);
        binding.pgFavourite.setVisibility(View.VISIBLE);

        String url = WebServiceUrl.URL_FAVOURITETOGGLE;
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        if(providerServiceId.equals("")){
            textParams.put("service_id", serviceDetailData.getId());
        }else{
            textParams.put("service_id", providerServiceId);
        }

        textParams.put("provider_id", serviceDetailData.getProviderId());
        //textParams.put("service_id", serviceDetailData.getServiceId());

        textParams.put("user_id", PrefsUtil.with(ServiceDetailActivity.this).readString("UserId"));
        textParams.put("delivery_type", PrefsUtil.with(context).readString("delivery_type"));
        textParams.put("request_type", PrefsUtil.with(context).readString("request_type"));

        if (binding.imgFav.getTag().equals("1")) {
            textParams.put("fvrt_val", "0");
        } else if (binding.imgFav.getTag().equals("0")) {
            textParams.put("fvrt_val", "1");
        } else {
            return;
        }

        new WebServiceCall(this, url, textParams, EditProfilePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgFavourite.setVisibility(View.GONE);
                binding.imgFav.setVisibility(View.VISIBLE);
                binding.llFavourite.setClickable(true);
                if (status) {
                    isFavRefresh = true;
                    Toast.makeText(context, ((EditProfilePojo) obj).getMessage(), Toast.LENGTH_SHORT).show();
                    if (binding.imgFav.getTag().equals("1")) {
                        Picasso.get().load(R.mipmap.ic_heart_fill).placeholder(R.drawable.loading).into(binding.imgFav);
                        binding.imgFav.setTag("0");
                    } else if (binding.imgFav.getTag().equals("0")) {
                        Picasso.get().load(R.mipmap.ic_heart_empty).placeholder(R.drawable.loading).into(binding.imgFav);
                        binding.imgFav.setTag("1");
                    }
                } else {
                    isFavRefresh = false;
                    Toast.makeText(ServiceDetailActivity.this, (String) obj, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                actionFavouriteAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                actionFavouriteAsync = null;
            }
        });
    }

    /*----------------- Get Category Api Call -------------------*/
    private void getCategory() {
        binding.rlMain.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("provider_id", providerId);

        new WebServiceCall(context, WebServiceUrl.URL_CATEGORYLIST, textParams,
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
                            getSubCategory();
                        } else {
                            Toast.makeText(context,
                                    listPojo.getMessage(), Toast.LENGTH_LONG).show();
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
        binding.rlMain.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        textParams.put("category_id", categoryId);
        textParams.put("provider_id", providerId);

        new WebServiceCall(context, WebServiceUrl.URL_SUBCATEGORYLIST, textParams,
                SubCategoryListPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                try {
                    if (status) {
                        List<String> stringList = new ArrayList<>();
                        SubCategoryListPojo listPojo = (SubCategoryListPojo) obj;
                        subCategoryItems.clear();
                        subCategoryItems.addAll(listPojo.getData());
                        if (subCategoryItems.size() > 0) {
                            for (int i = 0; i < subCategoryItems.size(); i++) {
                                if (subCategoryItems.get(i).getCategoryId() != null) {
                                    stringList.add(String.valueOf(subCategoryItems.get(i).getCategoryId()));
                                    subCategoryId = TextUtils.join(",", stringList);
                                }
                            }
                            getPopularService();
                        } else {
                            Toast.makeText(context,
                                    listPojo.getMessage(), Toast.LENGTH_LONG).show();
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
                subCategoryAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                subCategoryAsync = null;
            }
        });
    }

    /*------------------ Get Popular Service Api Call ---------------------*/
    private void getPopularService() {
        binding.rlMain.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("sub_category_id", subCategoryId);
        textParams.put("provider_id", providerId);

        new WebServiceCall(ServiceDetailActivity.this, WebServiceUrl.URL_POPULARSERVICS, textParams,
                ServiceListPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                try {
                    if (status) {
                        ServiceListPOJO serviceListPojo = (ServiceListPOJO) obj;
                        serviceDataItems.clear();
                        serviceDataItems.addAll(serviceListPojo.getData());
                        if (serviceDataItems.size() > 0) {
                            providerServiceId = serviceDataItems.get(0).getProviderServiceId();
                            PrefsUtil.with(context).write("request_type", serviceDataItems.get(0).getRequestType());
                            //getDetails();
                        } else {
                            Toast.makeText(context,
                                    serviceListPojo.getMessage(), Toast.LENGTH_LONG).show();
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
                popularServiceAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                popularServiceAsync = null;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra("isFavRefresh", isFavRefresh);
        setResult(RESULT_OK, i);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(categoryListAsync);
        Utils.cancelAsyncTask(subCategoryAsync);
        Utils.cancelAsyncTask(popularServiceAsync);
        Utils.cancelAsyncTask(serviceDetailAsync);
        Utils.cancelAsyncTask(actionFavouriteAsync);
        super.onDestroy();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragmentList = new ArrayList<>();
        private List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFrag(Fragment fragment, String title) {

            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //Display Title with Icon
            // return mFragmentTitleList.get(position);
            return null;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
