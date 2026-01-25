package com.app.bemyrider.activity.user;

import static com.app.bemyrider.utils.Utils.PROVIDER_ID;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.ActivityServiceDetailBinding;
import com.app.bemyrider.fragment.user.DetailFragment;
import com.app.bemyrider.fragment.user.ImageFragment;
import com.app.bemyrider.fragment.user.ReviewFragment;
import com.app.bemyrider.fragment.user.UserDetailFragment;
import com.app.bemyrider.model.ProviderServiceDetailPOJO;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.model.ProviderServiceReviewDataItem;
import com.app.bemyrider.model.partner.EditProfilePojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

import coil.Coil;
import coil.request.ImageRequest;


public class ServiceDetailActivity extends AppCompatActivity {

    private ActivityServiceDetailBinding binding;
    private ProviderServiceDetailsItem serviceDetailData;
    private final int[] tabIcons = {
            R.drawable.tabicon_servicedetail_style,
            R.drawable.tabicon_userdetail_style,
            R.drawable.tabicon_review_style,
            R.drawable.tabicon_images_style
    };
    private WebServiceCall serviceDetailAsync, actionFavouriteAsync;
    private final ArrayList<ProviderServiceReviewDataItem> reviewArrayList = new ArrayList<>();
    private boolean isFavRefresh = false;
    private String providerServiceId = "";

    // Variabili per la traduzione dei messaggi lato client
    private static final String MSG_ADDED_EN = "Service has been added in favourite services successfully";
    private static final String MSG_REMOVED_EN = "Service has been removed from favorite services successfully.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_service_detail);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        init();

        providerServiceId = getIntent().getStringExtra("providerServiceId");
        if (providerServiceId == null) {
            providerServiceId = "";
        }
        boolean isCallApiHome = "y".equals(getIntent().getStringExtra("isCallApi"));

        getDetails(isCallApiHome);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent i = new Intent();
                i.putExtra("isFavRefresh", isFavRefresh);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    private void init() {
        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.service_name), HtmlCompat.FROM_HTML_MODE_LEGACY));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ConnectionManager connectionManager = new ConnectionManager(this);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(this);
    }

    private void setupViewPager() {
        binding.pager.setAdapter(new ViewPagerAdapter(this));
        new TabLayoutMediator(binding.tabLayout, binding.pager, (tab, position) -> tab.setIcon(tabIcons[position])).attach();
        
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
             @Override
            public void onTabSelected(TabLayout.Tab tab) {
                 if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(ServiceDetailActivity.this, R.color.button), PorterDuff.Mode.SRC_IN));
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {
                if(tab.getIcon() != null) tab.getIcon().clearColorFilter();
            }
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        TabLayout.Tab selectedTab = binding.tabLayout.getTabAt(binding.tabLayout.getSelectedTabPosition());
        if(selectedTab != null && selectedTab.getIcon() != null) {
             selectedTab.getIcon().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.button), PorterDuff.Mode.SRC_IN));
        }
    }

    protected void getDetails(boolean isFromHome) {
        binding.rlMain.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        String webServiceUrl = isFromHome ? WebServiceUrl.MY_SERVICE_DETAILS_HOME : WebServiceUrl.MY_SERVICE_DETAILS;
        
        if (isFromHome) {
             textParams.put("provider_id", getIntent().getStringExtra(PROVIDER_ID));
        } else {
            textParams.put("provider_service_id", providerServiceId);
        }
        textParams.put("user_id", PrefsUtil.with(this).readString("UserId"));
        textParams.put("delivery_type", PrefsUtil.with(this).readString("delivery_type"));
        textParams.put("request_type", PrefsUtil.with(this).readString("request_type"));

        serviceDetailAsync = new WebServiceCall(this, webServiceUrl, textParams, ProviderServiceDetailPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.progress.setVisibility(View.GONE);
                binding.rlMain.setVisibility(View.VISIBLE);
                if (status) {
                    ProviderServiceDetailPOJO mainDetail = (ProviderServiceDetailPOJO) obj;
                    if (mainDetail.getData() != null) {
                        serviceDetailData = mainDetail.getData();
                        reviewArrayList.clear();
                        reviewArrayList.addAll(mainDetail.getData().getReviewData());
                        setData();
                    }
                } else {
                    Toast.makeText(ServiceDetailActivity.this, Objects.toString(obj, getString(R.string.server_error)), Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onAsync(Object asyncTask) { serviceDetailAsync = null; }
            @Override public void onCancelled() { serviceDetailAsync = null; }
        });
    }

    private void setData() {
        if (!Utils.isNullOrEmpty(serviceDetailData.getProviderImage())) {
            ImageRequest request = new ImageRequest.Builder(this)
                .data(serviceDetailData.getProviderImage())
                .placeholder(R.drawable.loading).error(R.mipmap.user)
                .target(binding.imgProfile).build();
            Coil.imageLoader(this).enqueue(request);
        } else {
            binding.imgProfile.setImageResource(R.mipmap.user);
        }
        
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + serviceDetailData.getServiceName(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

        // Recuperiamo lo stato del preferito. isFavorite "y" = Sì, "n" = No.
        boolean isFavorite = "y".equalsIgnoreCase(serviceDetailData.getIsFavorite());
        binding.imgFav.setImageResource(isFavorite ? R.mipmap.ic_heart_fill : R.mipmap.ic_heart_empty);
        
        // LOGICA TAG: 0 = È già preferito, 1 = NON è preferito
        binding.imgFav.setTag(isFavorite ? "0" : "1");

        binding.txtRatingShow.setText(String.valueOf(serviceDetailData.getAvgRating()));
        binding.txtName.setText(String.format("%s %s", serviceDetailData.getFirstName(), serviceDetailData.getLastName()));

        setupViewPager();
        
        binding.llFavourite.setOnClickListener(v -> favouriteToggle());
        
        View.OnClickListener profileClickListener = v -> {
            Intent i = new Intent(this, PartnerProfileActivity.class);
            i.putExtra(Utils.PROVIDER_ID, serviceDetailData.getProviderId());
            startActivity(i);
        };
        binding.txtName.setOnClickListener(profileClickListener);
        binding.layoutProfile.setOnClickListener(profileClickListener);
    }

    // Metodo per tradurre il messaggio dal server
    private String getTranslatedMessage(String message) {
        if (message == null) {
            return getString(R.string.server_error);
        }
        
        return switch (message) {
            case MSG_ADDED_EN -> getString(R.string.favorite_added_success);
            case MSG_REMOVED_EN -> getString(R.string.favorite_removed_success);
            default -> message;
        };
    }


    protected void favouriteToggle() {
        if (serviceDetailData == null || serviceDetailData.getProviderId() == null || serviceDetailData.getId() == null) {
            Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
            return; 
        }

        binding.llFavourite.setClickable(false);
        binding.imgFav.setVisibility(View.GONE);
        binding.pgFavourite.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        
        // CORREZIONE FONDAMENTALE: Usiamo l'ID specifico del servizio (es. 855 o 782) invece della categoria (67)
        textParams.put("service_id", serviceDetailData.getId());
        
        textParams.put("provider_id", serviceDetailData.getProviderId());
        textParams.put("user_id", PrefsUtil.with(this).readString("UserId"));
        
        textParams.put("delivery_type", PrefsUtil.with(this).readString("delivery_type"));
        textParams.put("request_type", PrefsUtil.with(this).readString("request_type"));
        
        // RIPRISTINO LOGICA SERVER: 0 = AGGIUNGI, 1 = RIMUOVI
        // Se il tag è "1" (non preferito), inviamo "0" per aggiungere.
        textParams.put("fvrt_val", binding.imgFav.getTag().equals("1") ? "0" : "1");

        actionFavouriteAsync = new WebServiceCall(this, WebServiceUrl.URL_FAVOURITETOGGLE, textParams, EditProfilePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgFavourite.setVisibility(View.GONE);
                binding.imgFav.setVisibility(View.VISIBLE);
                binding.llFavourite.setClickable(true);
                
                if (status) {
                    isFavRefresh = true;
                    String messageToShow = getTranslatedMessage(((EditProfilePojo) obj).getMessage());
                    Toast.makeText(ServiceDetailActivity.this, messageToShow, Toast.LENGTH_SHORT).show();
                    
                    // Invertiamo il tag locale per riflettere il nuovo stato
                    boolean wasFavorite = "0".equals(binding.imgFav.getTag());
                    boolean isNowFavorite = !wasFavorite;
                    
                    binding.imgFav.setImageResource(isNowFavorite ? R.mipmap.ic_heart_fill : R.mipmap.ic_heart_empty);
                    binding.imgFav.setTag(isNowFavorite ? "0" : "1");

                } else {
                    String messageToShow = (String) obj;
                    Toast.makeText(ServiceDetailActivity.this, Objects.toString(messageToShow, getString(R.string.server_error)), Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onAsync(Object asyncTask) { actionFavouriteAsync = null; }
            @Override public void onCancelled() { actionFavouriteAsync = null; }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.cancelAsyncTask(serviceDetailAsync);
        Utils.cancelAsyncTask(actionFavouriteAsync);
    }
    
    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Bundle dataBundle = new Bundle();
            dataBundle.putSerializable("data", serviceDetailData);
            
            Fragment fragment = switch (position) {
                case 1 -> new UserDetailFragment();
                case 2 -> new ReviewFragment(reviewArrayList);
                case 3 -> new ImageFragment();
                default -> new DetailFragment();
            };

            fragment.setArguments(dataBundle);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}