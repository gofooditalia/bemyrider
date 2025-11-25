package com.app.bemyrider.Adapter.User;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.ImagesSliderActivity;
import com.app.bemyrider.model.ProviderServiceMediaDataItem;
import com.app.bemyrider.R;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.List;

/**
 * Created by nct121 on 13/12/16.
 */

public class DetailImagesAdapter extends RecyclerView.Adapter<DetailImagesAdapter.SolventViewHolders> {
    private List<ProviderServiceMediaDataItem> imageList;
    private Context context;

    public DetailImagesAdapter(Context context, List<ProviderServiceMediaDataItem> imageList) {
        this.imageList = imageList;
        this.context = context;
    }

    @Override
    public SolventViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_simple_itermrow, null);
        SolventViewHolders rcv = new SolventViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(SolventViewHolders holder, int position) {
        /*holder.countryPhoto.setImageResource(imageList.get(position));*/
        if (imageList.size() > 0) {
            /*.transform(new RoundedCornersTransformation(50, 20))*/
            Picasso.get().load(imageList.get(position).getMediaUrl()).placeholder(R.drawable.loading).into(holder.img);
            holder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.img.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400));

            holder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, ImagesSliderActivity.class).putExtra("position", position).putExtra("images", (Serializable) imageList));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class SolventViewHolders extends RecyclerView.ViewHolder {
        public ImageView img;

        public SolventViewHolders(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_photos);
        }
    }
}
