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
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.R;
import com.app.bemyrider.activity.user.ServiceDetailActivity;
import com.app.bemyrider.model.user.ProviderItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DeliveryTypeAdapter extends RecyclerView.Adapter<DeliveryTypeAdapter.MyViewHolder> {

    private List<ProviderItem> providerItemList;
    private Activity act;

    public DeliveryTypeAdapter(List<ProviderItem> providerItemList, Activity act) {
        this.providerItemList = providerItemList;
        this.act = act;
    }

    @NonNull
    @Override
    public DeliveryTypeAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery_provider, parent, false);
        return new DeliveryTypeAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryTypeAdapter.MyViewHolder holder, int position) {
        if (providerItemList.size() > 0) {
            ProviderItem item = providerItemList.get(position);
            holder.txtName.setText(item.getProviderFirstName() + " " + item.getProviderLastName());
            holder.txtAddress.setText(item.getAddress());
            holder.txtRateCount.setText(item.getAvgRating());
            holder.txtRate.setText(item.getHourRate().toString());

            Picasso.get().load(item.getProviderImage())
                    .placeholder(R.drawable.loading).into(holder.imgProvider);

            holder.relDetail.setOnClickListener(v -> {
                if (isUserLogin()) {
                    PrefsUtil.with(act).write("request_type", "scheduled");
                    Intent i = new Intent(act, ServiceDetailActivity.class);
                    i.putExtra("isCallApi", "y");
                    i.putExtra("serviceName", item.getProviderFirstName() + " " + item.getProviderLastName());
                    i.putExtra(PROVIDER_ID, item.getProviderId());
                    act.startActivity(i);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return providerItemList.size();
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
