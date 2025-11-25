package com.app.bemyrider.Adapter.User;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.user.ServiceDetailActivity;
import com.app.bemyrider.model.user.FavoriteServiceListPojoItem;
import com.app.bemyrider.R;
import com.app.bemyrider.utils.PrefsUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by nct33 on 10/10/17.
 */

public class FavoriteServiceAdapter extends RecyclerView.Adapter<FavoriteServiceAdapter.ViewHolder> {

    private ArrayList<FavoriteServiceListPojoItem> favoriteServiceListPojoItems;
    private Context context;
    private OnActionListener listener;
    ActivityResultLauncher<Intent> myIntentActivityResultLauncher;

    public FavoriteServiceAdapter(Context context,
                                  ArrayList<FavoriteServiceListPojoItem> favoriteServiceListPojoItems,
                                  OnActionListener listener,    ActivityResultLauncher<Intent> myIntentActivityResultLauncher
    ) {
        this.context = context;
        this.favoriteServiceListPojoItems = favoriteServiceListPojoItems;
        this.listener = listener;
        this.myIntentActivityResultLauncher = myIntentActivityResultLauncher;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_service_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        FavoriteServiceListPojoItem item = favoriteServiceListPojoItems.get(position);
        holder.txt_provider_name.setText(item.getProviderName());
        holder.txt_service_name.setText(item.getServiceName());
        Picasso.get().load(item.getProfileImg())
                .placeholder(R.drawable.loading).into(holder.img_provider_image);

        holder.img_remove.setOnClickListener(v -> listener.onAction(position,
                item.getProviderServiceId(),holder.img_remove,holder.progress));

        holder.layout_fav.setOnClickListener(v -> {
            PrefsUtil.with(context).write("request_type", item.getRequestType());
            PrefsUtil.with(context).write("delivery_type", item.getDeliveryType());

            Intent intent = new Intent(context, ServiceDetailActivity.class);
            intent.putExtra("providerServiceId", item.getProviderServiceId());
            intent.putExtra("providerId",item.getProviderId());
            myIntentActivityResultLauncher.launch(intent);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteServiceListPojoItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView img_provider_image, img_remove;
        private LinearLayout layout_fav;
        private TextView txt_service_name, txt_provider_name;
        private ProgressBar progress;

        public ViewHolder(View itemView) {
            super(itemView);

            img_provider_image = itemView.findViewById(R.id.img_provider_image);
            img_remove = itemView.findViewById(R.id.img_remove);
            txt_service_name = itemView.findViewById(R.id.txt_service_name);
            txt_provider_name = itemView.findViewById(R.id.txt_provider_name);
            layout_fav = itemView.findViewById(R.id.layout_fav);
            progress = itemView.findViewById(R.id.progress);
        }
    }

    public interface OnActionListener {
        void onAction(int position, String providerServiceId,ImageView imgRemove,ProgressBar progress);
    }
}
