package com.app.bemyrider.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.R;
import com.app.bemyrider.model.ProviderServiceMediaDataItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderAdapterVH> {

    private Context context;
    private List<ProviderServiceMediaDataItem> mSliderItems = new ArrayList<>();

    public ImageSliderAdapter(Context context, List<ProviderServiceMediaDataItem> mSliderItems) {
        this.context = context;
        this.mSliderItems = mSliderItems;
    }

    @NonNull
    @Override
    public ImageSliderAdapter.SliderAdapterVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, parent, false);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSliderAdapter.SliderAdapterVH viewHolder, int position) {
        if (mSliderItems.size() > 0) {
            ProviderServiceMediaDataItem sliderItem = mSliderItems.get(position);
            if (sliderItem != null) {
                if (sliderItem.getMediaUrl() != null && !"".equals(sliderItem.getMediaUrl())) {
                    Picasso.get().load(sliderItem.getMediaUrl()).placeholder(R.drawable.not_found).into(viewHolder.imgSliderItemDetail);
                } else {
                    Picasso.get().load(R.drawable.not_found).placeholder(R.drawable.not_found).into(viewHolder.imgSliderItemDetail);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mSliderItems.size();
    }

    public class SliderAdapterVH extends RecyclerView.ViewHolder {

        View itemView;
        ImageView imgSliderItemDetail;

        public SliderAdapterVH(@NonNull View itemView) {
            super(itemView);
            imgSliderItemDetail = itemView.findViewById(R.id.imgSliderItemDetail);
            this.itemView = itemView;
        }
    }
}
