package com.app.bemyrider.Adapter.Partner;

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
// Coil Imports
import coil.Coil;
import coil.request.ImageRequest;
// import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.List;

/**
 * Created by nct121 on 13/12/16.
 */

public class SolventRecyclerViewAdapter extends RecyclerView.Adapter<SolventRecyclerViewAdapter.SolventViewHolders> {
    private List<ProviderServiceMediaDataItem> gaggeredList;
    private Context context;

    public SolventRecyclerViewAdapter(Context context, List<ProviderServiceMediaDataItem> gaggeredList) {
        this.gaggeredList = gaggeredList;
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
        // holder.countryPhoto.setImageResource(gaggeredList.get(position).getPhoto());
        
        // Coil Migration from Picasso
        ImageRequest request = new ImageRequest.Builder(context)
            .data(gaggeredList.get(position).getMediaUrl())
            .placeholder(R.drawable.loading)
            .target(holder.countryPhoto)
            .build();
        Coil.imageLoader(context).enqueue(request);

        holder.countryPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.countryPhoto.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400));
        holder.countryPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return;
                context.startActivity(new Intent(context, ImagesSliderActivity.class)
                        .putExtra("position", adapterPosition).putExtra("images", (Serializable) gaggeredList));
            }
        });
    }

    @Override
    public int getItemCount() {
        return gaggeredList.size();
    }

    public class SolventViewHolders extends RecyclerView.ViewHolder {
        public ImageView countryPhoto;

        public SolventViewHolders(View itemView) {
            super(itemView);
            countryPhoto = (ImageView) itemView.findViewById(R.id.img_photos);
        }
    }
}
