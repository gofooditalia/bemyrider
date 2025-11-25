package com.app.bemyrider.Adapter.User;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.user.ServiceDetailActivity;
import com.app.bemyrider.model.ServiceDataItem;
import com.app.bemyrider.R;
import com.app.bemyrider.utils.PrefsUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PopularServiceAdapter extends RecyclerView.Adapter<PopularServiceAdapter.ServiceHolder> {

    int[] androidColors;
    private Context mContext;
    private List<ServiceDataItem> list;
    private int colorCounter = 0;
    private String providerId;

    public PopularServiceAdapter(Context mContext, List<ServiceDataItem> list, String providerId) {
        this.mContext = mContext;
        this.list = list;
        this.providerId = providerId;
        androidColors = mContext.getResources().getIntArray(R.array.overlay_colors);
        colorCounter = 0;
    }

    @NonNull
    @Override
    public ServiceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.popular_category_list_row, parent, false);
        return new ServiceHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceHolder holder, int position) {
        final ServiceDataItem item = list.get(position);
        Picasso.get().load(item.getServiceImgUrl()).into(holder.ivPopularCategoryImage);
        holder.txtPopularCategoryName.setText(item.getServiceName());

        if(colorCounter == (androidColors.length - 1)) {
            colorCounter = 0;
        }

        holder.overlay.setBackgroundColor(androidColors[colorCounter++]);

        holder.rlPopularCategory.setOnClickListener(view -> {
            PrefsUtil.with(mContext).write("request_type", item.getRequestType());
            Intent i = new Intent(mContext, ServiceDetailActivity.class);
            i.putExtra("providerServiceId",item.getProviderServiceId());
            i.putExtra("providerId",item.getProviderId());
            mContext.startActivity(i);
            /*Intent i = new Intent(mContext, ProviderListActivity.class);
            i.putExtra("categoryId", item.getCategoryId());
            i.putExtra("subCategoryId", item.getSubCategoryId());
            i.putExtra("serviceId", item.getServiceId());
            i.putExtra("serviceName", item.getServiceName());
            i.putExtra("address", "");
            i.putExtra("latitude", "0.0");
            i.putExtra("longitude", "0.0");
            mContext.startActivity(i);*/
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ServiceHolder extends RecyclerView.ViewHolder {
        RelativeLayout rlPopularCategory;
        TextView txtPopularCategoryName;
        ImageView ivPopularCategoryImage;
        View overlay;

        public ServiceHolder(View itemView) {
            super(itemView);
            rlPopularCategory = itemView.findViewById(R.id.rlPopularCategory);
            ivPopularCategoryImage = itemView.findViewById(R.id.ivPopularCategoryImage);
            txtPopularCategoryName = itemView.findViewById(R.id.txtPopularCategoryName);
            overlay = itemView.findViewById(R.id.overlay);
        }
    }
}
