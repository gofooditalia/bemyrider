package com.app.bemyrider.Adapter.User;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.user.ServiceDetailActivity;
import com.app.bemyrider.R;
import com.app.bemyrider.model.user.ProviderListItem;
// Coil Imports
import coil.Coil;
import coil.request.ImageRequest;
// import com.squareup.picasso.Picasso;

import java.util.List;

public class ProviderAdapter extends RecyclerView.Adapter<ProviderAdapter.MyViewHolder> {

    private List<ProviderListItem> providerList;
    private Activity act;
    private OnFavouriteChangeListener listener;

    public ProviderAdapter(Activity act, List<ProviderListItem> moviesList,
                           OnFavouriteChangeListener listener) {
        this.act = act;
        this.providerList = moviesList;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemrow_provider, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final ProviderListItem item = providerList.get(position);
        if (item.getAvgRating() != 0 && item.getAvgRating() > 0) {
            holder.txt_rating.setText(String.valueOf(item.getAvgRating()));
        } else {
            holder.txt_rating.setText("0.0");
        }
        holder.txt_address_provider.setText(item.getAddress());
        holder.txt_description.setText(item.getServiceDescription());
        holder.txt_serviceName.setText(String.format("%s %s", item.getProviderFirstName(), item.getProviderLastName()));

        // Coil Migration: Profile Image
        String profileImageUrl = item.getProviderImage();
        ImageRequest.Builder profileBuilder = new ImageRequest.Builder(act)
                .placeholder(R.drawable.loading)
                .target(holder.img_profile);

        if (profileImageUrl != null && profileImageUrl.length() > 0) {
            profileBuilder.data(profileImageUrl);
        } else {
            profileBuilder.data(R.mipmap.user);
        }
        Coil.imageLoader(act).enqueue(profileBuilder.build());

        // Coil Migration: Heart Image
        int heartResource;
        String tag;
        if (item.getFavoriteId() > 0) {
            heartResource = R.mipmap.ic_heart_fill;
            tag = "0"; // Tag 0 per favorito
        } else {
            heartResource = R.mipmap.ic_heart_empty;
            tag = "1"; // Tag 1 per non favorito
        }

        ImageRequest favRequest = new ImageRequest.Builder(act)
                .data(heartResource)
                .placeholder(R.drawable.loading)
                .target(holder.img_fav)
                .build();
        Coil.imageLoader(act).enqueue(favRequest);
        holder.img_fav.setTag(tag);


        holder.item_main.setOnClickListener(v -> {
            Intent i = new Intent(act, ServiceDetailActivity.class);
            i.putExtra("providerServiceId", item.getProviderServiceId());
            act.startActivity(i);
        });

        holder.img_fav.setOnClickListener(v -> listener.favouriteToogle(item.getProviderServiceId(),
                holder.img_fav, holder.progress));

    }

    @Override
    public int getItemCount() {
        return providerList.size();
    }

    public interface OnFavouriteChangeListener {
        void favouriteToogle(String providerServiceId, ImageView ivFav, ProgressBar progress);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_rating, txt_serviceName, txt_address_provider, txt_description;
        private ImageView img_profile, img_fav;
        private RelativeLayout item_main;
        private ProgressBar progress;

        MyViewHolder(View view) {
            super(view);
            item_main = view.findViewById(R.id.item_main);
            progress = view.findViewById(R.id.progress);
            img_profile = view.findViewById(R.id.img_profile);
            txt_rating = view.findViewById(R.id.txt_rating);
            txt_address_provider = view.findViewById(R.id.txt_address_provider);
            txt_description = view.findViewById(R.id.txt_description);
            txt_serviceName = view.findViewById(R.id.txt_serviceName);
            img_fav = view.findViewById(R.id.img_fav);
        }
    }
}
