package com.app.bemyrider.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.bemyrider.Adapter.ImageSliderAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityImagesSliderBinding;
import com.app.bemyrider.model.ProviderServiceMediaDataItem;
import com.app.bemyrider.utils.LocaleManager;

import java.util.ArrayList;
import java.util.List;

public class ImagesSliderActivity extends AppCompatActivity {

    private static final String TAG = "ImageSliderActivity";
    private ActivityImagesSliderBinding binding;

    private Activity mActivity;
    private Context mContext;
    private List<ProviderServiceMediaDataItem> sliderItemList = new ArrayList<>();
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(ImagesSliderActivity.this, R.layout.activity_images_slider, null);
        mActivity = ImagesSliderActivity.this;
        mContext = ImagesSliderActivity.this;

        getIntentData();

        init();

    }

    private void init() {

        setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.uploaded_images),HtmlCompat.FROM_HTML_MODE_LEGACY));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setupCurrentIndicator(position);
            }
        });

        final ImageSliderAdapter adapter = new ImageSliderAdapter(mContext, sliderItemList);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(position, true);
        setupIndicator(adapter);
        setupCurrentIndicator(position);
    }

    private void setupIndicator(ImageSliderAdapter adapter) {
        ImageView[] indicator = new ImageView[adapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(4, 0, 4, 0);
        for (int i = 0; i < indicator.length; i++) {
            indicator[i] = new ImageView(mContext);
            indicator[i].setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.indicator_inactive));
            indicator[i].setLayoutParams(layoutParams);
            binding.linIndicator.addView(indicator[i]);
        }
    }

    private void setupCurrentIndicator(int index) {
        int itemcildcount = binding.linIndicator.getChildCount();
        for (int i = 0; i < itemcildcount; i++) {
            ImageView imageView = (ImageView) binding.linIndicator.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.indicator_inactive));
            }
        }
    }


    private void getIntentData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.get("images") != null)
                sliderItemList = (List<ProviderServiceMediaDataItem>) bundle.getSerializable("images");
            if (bundle.get("position") != null)
                position = bundle.getInt("position");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}