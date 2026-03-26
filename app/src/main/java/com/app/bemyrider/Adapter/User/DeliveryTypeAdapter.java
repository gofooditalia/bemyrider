package com.app.bemyrider.Adapter.User;

import static com.app.bemyrider.utils.PrefsUtil.isUserLogin;
import static com.app.bemyrider.utils.Utils.PROVIDER_ID;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.R;
import com.app.bemyrider.activity.user.ServiceDetailActivity;
import com.app.bemyrider.model.user.ProviderItem;
import com.app.bemyrider.utils.PrefsUtil;
import coil.Coil;
import coil.request.ImageRequest;

public class DeliveryTypeAdapter extends ListAdapter<ProviderItem, DeliveryTypeAdapter.MyViewHolder> {

    private Activity act;
    private String deliveryType = "small";

    public DeliveryTypeAdapter(Activity act) {
        super(new DeliveryTypeDiffCallback());
        this.act = act;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    @NonNull
    @Override
    public DeliveryTypeAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery_provider, parent, false);
        return new DeliveryTypeAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryTypeAdapter.MyViewHolder holder, int position) {
        ProviderItem item = getItem(position);
        if (item != null) {
            String fullName = item.getProviderFirstName() + " " + item.getProviderLastName();
            holder.txtName.setText(fullName);
            holder.txtAddress.setText(item.getAddress());
            holder.txtRateCount.setText(item.getAvgRating());
            holder.txtRate.setText(item.getHourRate().toString());

            String imageUrl = item.getProviderImage();
            
            // Check if it is literally the server default placeholder
            boolean isExplicitlyEmpty = imageUrl == null || imageUrl.isEmpty() || 
                                       imageUrl.toLowerCase().contains("no_user_image.png");

            ImageRequest.Builder requestBuilder = new ImageRequest.Builder(act)
                .placeholder(R.drawable.account_circle_24)
                .error(R.drawable.account_circle_24)
                .target(holder.imgProvider);

            if (!isExplicitlyEmpty) {
                // Load the actual image.
                // Do not use crossfade with custom CircleImageView to avoid blank bitmap conversions!
                requestBuilder.data(imageUrl);
            } else {
                requestBuilder.data(R.drawable.account_circle_24);
            }

            Coil.imageLoader(act).enqueue(requestBuilder.build());

            holder.relDetail.setOnClickListener(v -> {
                if (isUserLogin()) {
                    PrefsUtil.with(act).write("delivery_type", deliveryType);
                    PrefsUtil.with(act).write("request_type", "scheduled");
                    Intent i = new Intent(act, ServiceDetailActivity.class);
                    i.putExtra("isCallApi", "y");
                    i.putExtra("serviceName", fullName);
                    i.putExtra(PROVIDER_ID, item.getProviderId());
                    act.startActivity(i);
                }
            });
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relDetail;
        ImageView imgProvider;
        AppCompatTextView txtName, txtAddress;
        TextView txtRateCount, txtRate;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProvider = itemView.findViewById(R.id.imgProvider);
            txtName = itemView.findViewById(R.id.txtName);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtRateCount = itemView.findViewById(R.id.txtRateCount);
            relDetail = itemView.findViewById(R.id.relDetail);
            txtRate = itemView.findViewById(R.id.txtRate);
        }
    }
}
