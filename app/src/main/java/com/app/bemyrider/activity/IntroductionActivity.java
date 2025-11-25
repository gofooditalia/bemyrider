package com.app.bemyrider.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.bemyrider.Adapter.IntroSliderAdapter;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ActivityIntroductionBinding;
import com.app.bemyrider.model.IntroductionModel;
import com.app.bemyrider.utils.LocaleManager;

import java.util.ArrayList;
import java.util.List;

public class IntroductionActivity extends AppCompatActivity {

    private static final String TAG = "IntroductionActivity";

    ActivityIntroductionBinding binding;

    private List<IntroductionModel> introductionModelList = new ArrayList<>();
    IntroductionModel introductionModel = new IntroductionModel();
    private Activity mActivity;
    private Context mContext;
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DataBindingUtil.setContentView(IntroductionActivity.this, R.layout.activity_introduction, null);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        mActivity = IntroductionActivity.this;
        mContext = IntroductionActivity.this;

        setData();

        init();

    }

    private void init() {
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setupCurrentIndicator(position);
            }
        });

        final IntroSliderAdapter adapter = new IntroSliderAdapter(mContext, mActivity, introductionModelList);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(position, true);
        setupIndicator(adapter);
        setupCurrentIndicator(position);
    }

    private void setupIndicator(IntroSliderAdapter adapter) {
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
            imageView.setPadding(10,0,10,0);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.intro_indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.intro_indicator_inactive));
            }
        }
    }


    private void setData() {
        position = 0;
        introductionModel = new IntroductionModel();
        introductionModel.setPositionId(0);
        introductionModel.setPageText(getResources().getString(R.string.intro_first_page_text));
        introductionModel.setPageImg(R.drawable.intro_img_1);
        introductionModel.setBtnText(getResources().getString(R.string.intro_lets_start));
        introductionModelList.add(introductionModel);

        introductionModel = new IntroductionModel();
        introductionModel.setPositionId(1);
        introductionModel.setPageText(getResources().getString(R.string.intro_second_page_text));
        introductionModel.setPageImg(R.drawable.intro_img_2);
        introductionModel.setBtnText("");
        introductionModelList.add(introductionModel);

        introductionModel = new IntroductionModel();
        introductionModel.setPositionId(2);
        introductionModel.setPageText(getResources().getString(R.string.intro_third_page_text));
        introductionModel.setPageImg(R.drawable.intro_img_3);
        introductionModel.setBtnText("");
        introductionModelList.add(introductionModel);

        introductionModel = new IntroductionModel();
        introductionModel.setPositionId(3);
        introductionModel.setPageText(getResources().getString(R.string.intron_fourth_text));
        introductionModel.setPageImg(R.drawable.intro_img_4);
        introductionModel.setBtnText("");
        introductionModelList.add(introductionModel);

        introductionModel = new IntroductionModel();
        introductionModel.setPositionId(4);
        introductionModel.setPageText(getResources().getString(R.string.intro_fifth_text));
        introductionModel.setPageImg(R.drawable.intro_img_5);
        introductionModel.setBtnText(getResources().getString(R.string.intro_signin));
        introductionModelList.add(introductionModel);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}