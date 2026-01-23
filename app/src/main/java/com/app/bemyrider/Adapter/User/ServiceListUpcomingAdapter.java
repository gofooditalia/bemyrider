package com.app.bemyrider.Adapter.User;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

// Rimosso l'import di ActivityResultLauncher
// import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.user.BookedServiceDetailActivity;
import com.app.bemyrider.R;
import com.app.bemyrider.model.CustomerHistoryPojoItem;
import com.app.bemyrider.utils.PrefsUtil;
// Coil Imports
import coil.Coil;
import coil.request.ImageRequest;
// import com.squareup.picasso.Picasso;

import java.util.List;

public class ServiceListUpcomingAdapter extends RecyclerView.Adapter<ServiceListUpcomingAdapter.MyViewHolder> {

    private List<CustomerHistoryPojoItem> historyList;
    private Activity act;
    // Rimosso il membro myActivityResultLauncher
    // private ActivityResultLauncher<Intent> myActivityResultLauncher;


    // Modificato il costruttore per accettare solo 2 argomenti
    public ServiceListUpcomingAdapter(Activity act, List<CustomerHistoryPojoItem> historyList) {
        this.act = act;
        this.historyList = historyList;
        // this.myActivityResultLauncher = myActivityResultLauncher; // Rimosso
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemrow_service_history, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final CustomerHistoryPojoItem item = historyList.get(position);


        if (item.getProviderFname() != null && item.getProviderFname().length() > 0) {
            if (item.getProviderLname() != null && item.getProviderLname().length() > 0) {
                holder.txtProviderName.setText(item.getProviderFname() + " " + item.getProviderLname());
            } else {
                holder.txtProviderName.setText(item.getProviderFname());
            }
        } else if (item.getProviderLname() != null && item.getProviderLname().length() > 0) {
            holder.txtProviderName.setText(item.getProviderLname());
        } else {
            holder.txtProviderName.setText("");
        }

        if (item.getServiceName() != null && item.getServiceName().length() > 0) {
            holder.txtServiceName.setText(item.getServiceName());
        } else {
            holder.txtServiceName.setText("");
        }

        // Coil Migration from Picasso
        String imageUrl = item.getProviderImage();
        ImageRequest.Builder profileBuilder = new ImageRequest.Builder(act)
                .placeholder(R.drawable.loading)
                .target(holder.imgProfile);

        if (imageUrl != null && imageUrl.length() > 0) {
            profileBuilder.data(imageUrl);
        } else {
            profileBuilder.data(R.mipmap.user);
        }
        Coil.imageLoader(act).enqueue(profileBuilder.build());

        /*----- Set Status text & background color -----*/
        holder.txtStatus.setText(item.getServiceStatusDisplayName());

        switch (item.getServiceStatus()) {
            case "rejected":
                holder.txtStatus.setBackgroundColor(act.getResources().getColor(R.color.status_rejected));
                break;
            case "pending":
                holder.txtStatus.setBackgroundColor(act.getResources().getColor(R.color.status_pending));
                break;
            case "cancelled":
                holder.txtStatus.setBackgroundColor(act.getResources().getColor(R.color.status_cancelled));
                break;
            case "accepted":
                holder.txtStatus.setBackgroundColor(act.getResources().getColor(R.color.status_accepted));
                break;
            case "closed":
                holder.txtStatus.setBackgroundColor(act.getResources().getColor(R.color.status_closed));
                break;
            case "completed":
                holder.txtStatus.setBackgroundColor(act.getResources().getColor(R.color.status_completed));
                break;
            case "dispute":
                holder.txtStatus.setBackgroundColor(act.getResources().getColor(R.color.status_dispute));
                break;
            case "ongoing":
                holder.txtStatus.setBackgroundColor(act.getResources().getColor(R.color.status_ongoing));
                break;
            case "hired":
                holder.txtStatus.setBackgroundColor(act.getResources().getColor(R.color.status_hired));
                break;
            case "expired":
                holder.txtStatus.setBackgroundColor(act.getResources().getColor(R.color.status_expired));
                break;
        }

        if (item.getBookingAmount() != null && item.getBookingAmount().length() > 0) {
            holder.txtCost.setText(String.format("%s%s", PrefsUtil.with(act).readString("CurrencySign"), item.getBookingAmount()));
        } else {
            holder.txtCost.setText(String.format("%s0", PrefsUtil.with(act).readString("CurrencySign")));
        }

        if (item.getBookingStartTime() != null && item.getBookingStartTime().length() > 0) {
            holder.txtDateTime.setText(item.getBookingStartTime());
        }

        holder.rlMain.setOnClickListener(v -> {
            Intent i = new Intent(act, BookedServiceDetailActivity.class);
            i.putExtra("providerServiceId", item.getProviderServiceId());
            i.putExtra("serviceRequestId", item.getServiceRequestId());
            i.putExtra("serviceName", item.getServiceName());
            // Utilizzo startActivity standard
            act.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtProviderName, txtServiceName, txtStatus, txtCost, txtDateTime;
        private ImageView imgProfile;
        private RelativeLayout rlMain;

        public MyViewHolder(View view) {
            super(view);

            rlMain = view.findViewById(R.id.rlMain);
            imgProfile = view.findViewById(R.id.imgProfile);
            txtProviderName = view.findViewById(R.id.txtProviderName);
            txtServiceName = view.findViewById(R.id.txtServiceName);
            txtStatus = view.findViewById(R.id.txtStatus);
            txtCost = view.findViewById(R.id.txtCost);
            txtDateTime = view.findViewById(R.id.txtDateTime);
        }
    }
}
