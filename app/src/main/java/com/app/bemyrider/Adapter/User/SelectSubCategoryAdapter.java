package com.app.bemyrider.Adapter.User;

import static com.app.bemyrider.utils.Utils.PROVIDER_ID;

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

import com.app.bemyrider.activity.user.PopularCategoryActivity;
import com.app.bemyrider.model.partner.SubCategoryItem;
import com.app.bemyrider.R;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
// Coil Imports
import coil.Coil;
import coil.request.ImageRequest;
// import com.squareup.picasso.Picasso;

import java.util.List;

public class SelectSubCategoryAdapter extends RecyclerView.Adapter<SelectSubCategoryAdapter.ServiceHolder> {

    private Context mContext;
    private String providerId;
    private List<SubCategoryItem> list;

    public SelectSubCategoryAdapter(Context mContext, List<SubCategoryItem> list, String providerId) {
        this.mContext = mContext;
        this.list = list;
        this.providerId = providerId;
    }

    @NonNull
    @Override
    public ServiceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_list_row, parent, false);
        return new ServiceHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceHolder holder, int position) {
        final SubCategoryItem item = list.get(position);
        
        // Coil Migration from Picasso
        ImageRequest request = new ImageRequest.Builder(mContext)
            .data(item.getBannerUrl())
            .target(holder.ivCategoryImage)
            .build();
        Coil.imageLoader(mContext).enqueue(request);

        holder.txtCategoryName.setText(item.getCategoryName());

        holder.rlCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrefsUtil.with(mContext).write("request_type", item.getRequestType());
                Intent i = new Intent(mContext,
                        PopularCategoryActivity.class);
                i.putExtra(Utils.CATEGORY_ID, item.getCategoryId());
                i.putExtra("serviceName", item.getCategoryName());
                i.putExtra(PROVIDER_ID, providerId);
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ServiceHolder extends RecyclerView.ViewHolder {
        RelativeLayout rlCategory;
        TextView txtCategoryName;
        ImageView ivCategoryImage;

        public ServiceHolder(View itemView) {
            super(itemView);
            rlCategory = itemView.findViewById(R.id.rlCategory);
            ivCategoryImage = itemView.findViewById(R.id.ivCategoryImage);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
        }
    }
}
