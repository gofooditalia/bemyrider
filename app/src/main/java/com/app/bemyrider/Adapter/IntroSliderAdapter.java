package com.app.bemyrider.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.R;
import com.app.bemyrider.activity.SignupActivity;
import com.app.bemyrider.model.IntroductionModel;
import com.app.bemyrider.utils.SecurePrefsUtil;
// Coil Imports
import coil.Coil;
import coil.request.ImageRequest;
// import com.squareup.picasso.Picasso;

import java.util.List;

public class IntroSliderAdapter extends RecyclerView.Adapter<IntroSliderAdapter.SliderAdapterVH> {

    private final Context context;
    private final Activity mActivity;
    private final List<IntroductionModel> mSliderItems;

    public IntroSliderAdapter(Context context, Activity mActivity, List<IntroductionModel> mSliderItems) {
        this.mActivity = mActivity;
        this.context = context;
        this.mSliderItems = mSliderItems;
    }

    @NonNull
    @Override
    public IntroSliderAdapter.SliderAdapterVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_intro_slider, parent, false);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderAdapterVH viewHolder, int position) {
        if (!mSliderItems.isEmpty()) {
            IntroductionModel sliderItem = mSliderItems.get(position);
            if (sliderItem != null) {
                if (position == 0) {
                    viewHolder.relPageFirst.setVisibility(View.VISIBLE);
                    viewHolder.relPageAnother.setVisibility(View.GONE);

                    // Coil Migration: Slide 1 Image
                    ImageRequest requestImg1 = new ImageRequest.Builder(context)
                            .data(sliderItem.getPageImg())
                            .placeholder(R.drawable.intro_img_1)
                            .target(viewHolder.imgPage1)
                            .build();
                    Coil.imageLoader(context).enqueue(requestImg1);

                    // Coil Migration: Slide 1 Logo
                    ImageRequest requestLogo1 = new ImageRequest.Builder(context)
                            .data(R.drawable.intro_logo)
                            .placeholder(R.drawable.intro_logo)
                            .target(viewHolder.logoPage1)
                            .build();
                    Coil.imageLoader(context).enqueue(requestLogo1);

                    viewHolder.txtPage1.setText(HtmlCompat.fromHtml(sliderItem.getPageText(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                    viewHolder.txtBtn1.setText(HtmlCompat.fromHtml(sliderItem.getBtnText(), HtmlCompat.FROM_HTML_MODE_LEGACY));

                    // Listener per il pulsante "Let's Start" nella prima slide
                    viewHolder.txtBtn1.setOnClickListener(v -> {
                        // Vai alla slide successiva
                        // Il ViewPager gestirà lo scroll
                    });
                } else {
                    viewHolder.relPageFirst.setVisibility(View.GONE);
                    viewHolder.relPageAnother.setVisibility(View.VISIBLE);

                    // Coil Migration: Slide N Image
                    ImageRequest requestImg2 = new ImageRequest.Builder(context)
                            .data(sliderItem.getPageImg())
                            .placeholder(R.drawable.intro_img_2)
                            .target(viewHolder.imgPage2)
                            .build();
                    Coil.imageLoader(context).enqueue(requestImg2);

                    // Coil Migration: Slide N Logo
                    ImageRequest requestLogo2 = new ImageRequest.Builder(context)
                            .data(R.drawable.intro_logo)
                            .placeholder(R.drawable.intro_logo)
                            .target(viewHolder.logoPage2)
                            .build();
                    Coil.imageLoader(context).enqueue(requestLogo2);

                    viewHolder.txtPage2.setText(HtmlCompat.fromHtml(sliderItem.getPageText(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                    if (position == 4) {
                        viewHolder.txtBtn2.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.txtBtn2.setVisibility(View.GONE);
                    }
                    viewHolder.txtBtn2.setText(HtmlCompat.fromHtml(sliderItem.getBtnText(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                }

                viewHolder.txtSkip.setOnClickListener(v -> {
                    // Segna che le slide sono state viste
                    SecurePrefsUtil.with(context).write("hasSeenIntro", true);
                    Intent intent = new Intent(context, SignupActivity.class);
                    /*intent.putExtra("from","intro");*/
                    context.startActivity(intent);
                    mActivity.finish();
                });

                viewHolder.txtBtn2.setOnClickListener(v -> {
                    // Segna che le slide sono state viste
                    SecurePrefsUtil.with(context).write("hasSeenIntro", true);
                    Intent intent = new Intent(context, SignupActivity.class);
                    context.startActivity(intent);
                    mActivity.finish();
                });

            }
        }
    }

    @Override
    public int getItemCount() {
        return mSliderItems.size();
    }

    static class SliderAdapterVH extends RecyclerView.ViewHolder {

        RelativeLayout relMain, relPageFirst, relPageAnother;
        ImageView imgPage1, logoPage1, logoPage2, imgPage2;
        TextView txtSkip, txtPage1, txtBtn1, txtPage2, txtBtn2;


        public SliderAdapterVH(@NonNull View itemView) {
            super(itemView);
            relMain = itemView.findViewById(R.id.relMain);
            relPageFirst = itemView.findViewById(R.id.relPageFirst);
            relPageAnother = itemView.findViewById(R.id.relPageAnother);

            imgPage1 = itemView.findViewById(R.id.imgPage1);
            logoPage1 = itemView.findViewById(R.id.logoPage1);
            logoPage2 = itemView.findViewById(R.id.logoPage2);
            imgPage2 = itemView.findViewById(R.id.imgPage2);

            txtSkip = itemView.findViewById(R.id.txtSkip);
            txtPage1 = itemView.findViewById(R.id.txtPage1);
            txtBtn1 = itemView.findViewById(R.id.txtBtn1);
            txtPage2 = itemView.findViewById(R.id.txtPage2);
            txtBtn2 = itemView.findViewById(R.id.txtBtn2);
        }
    }
}
