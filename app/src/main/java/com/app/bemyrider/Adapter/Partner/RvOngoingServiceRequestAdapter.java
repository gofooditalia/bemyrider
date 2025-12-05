package com.app.bemyrider.Adapter.Partner;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.partner.Partner_ServiceRequestDetail_Tablayout_Activity;
import com.app.bemyrider.activity.partner.PartnerServiceRequestDetailsActivity;
import com.app.bemyrider.R;
import com.app.bemyrider.model.ProviderHistoryPojoItem;
import com.app.bemyrider.utils.CircleImageView;
import com.app.bemyrider.utils.PrefsUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by nct45 on 28/6/17.
 */

public class RvOngoingServiceRequestAdapter extends RecyclerView.Adapter<RvOngoingServiceRequestAdapter.Holder> {

    private Context context;
    private ArrayList<ProviderHistoryPojoItem> arrayList;

    public RvOngoingServiceRequestAdapter(Context context, ArrayList<ProviderHistoryPojoItem> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.partner_row_upcoming_servicerequest, parent,
                false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {

        holder.txt_uname.setText(String.format("%s %s", arrayList.get(position).getCustomerFname(),
                arrayList.get(position).getCustomerLname()));
        holder.txt_service.setText(arrayList.get(position).getServiceName());
        holder.txt_price.setText(String.format("%s%s", PrefsUtil.with(context).readString("CurrencySign"),
                arrayList.get(position).getBookingAmount()));
        holder.txt_date.setText(arrayList.get(position).getBookingStartTime());

        /*----- Set Status text & background color -----*/
        holder.txt_servicestatus.setText(arrayList.get(position).getServiceStatusDisplayName());

        switch (arrayList.get(position).getServiceStatus()) {
            case "rejected":
                holder.txt_servicestatus.setBackgroundColor(context.getResources().getColor(R.color.status_rejected));
                break;
            case "pending":
                holder.txt_servicestatus.setBackgroundColor(context.getResources().getColor(R.color.status_pending));
                break;
            case "cancelled":
                holder.txt_servicestatus.setBackgroundColor(context.getResources().getColor(R.color.status_cancelled));
                break;
            case "accepted":
                holder.txt_servicestatus.setBackgroundColor(context.getResources().getColor(R.color.status_accepted));
                break;
            case "closed":
                holder.txt_servicestatus.setBackgroundColor(context.getResources().getColor(R.color.status_closed));
                break;
            case "completed":
                holder.txt_servicestatus.setBackgroundColor(context.getResources().getColor(R.color.status_completed));
                break;
            case "dispute":
                holder.txt_servicestatus.setBackgroundColor(context.getResources().getColor(R.color.status_dispute));
                break;
            case "ongoing":
                holder.txt_servicestatus.setBackgroundColor(context.getResources().getColor(R.color.status_ongoing));
                break;
            case "hired":
                holder.txt_servicestatus.setBackgroundColor(context.getResources().getColor(R.color.status_hired));
                break;
            case "expired":
                holder.txt_servicestatus.setBackgroundColor(context.getResources().getColor(R.color.status_expired));
                break;
        }

        if (arrayList.get(position).getCustomerImage().equals("")) {
            holder.img_request_userprofile.setImageResource(R.mipmap.user);
        } else {
            try {
                Picasso.get().load(arrayList.get(position).getCustomerImage()).placeholder(R.drawable.loading)
                        .error(R.mipmap.user).into(holder.img_request_userprofile);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        holder.layout_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return;
                if (arrayList.get(adapterPosition).getServiceStatus().equals("completed")) {
                    Intent intent = new Intent(context, Partner_ServiceRequestDetail_Tablayout_Activity.class);
                    intent.putExtra("serviceRequestId", arrayList.get(adapterPosition).getServiceRequestId());
                    intent.putExtra("serviceName", arrayList.get(adapterPosition).getServiceName());
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, PartnerServiceRequestDetailsActivity.class);
                    intent.putExtra("serviceRequestId", arrayList.get(adapterPosition).getServiceRequestId());
                    intent.putExtra("serviceName", arrayList.get(adapterPosition).getServiceName());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        // return arrayList.size();
        return arrayList.size();

    }

    public class Holder extends RecyclerView.ViewHolder {

        private TextView txt_uname, txt_service, txt_date, txt_price, txt_servicestatus;
        private CircleImageView img_request_userprofile;
        private RelativeLayout layout_item;

        public Holder(View itemView) {
            super(itemView);

            txt_uname = (TextView) itemView.findViewById(R.id.txt_uname);
            txt_service = (TextView) itemView.findViewById(R.id.txt_service);
            txt_date = (TextView) itemView.findViewById(R.id.txt_date);
            txt_price = (TextView) itemView.findViewById(R.id.txt_price);
            txt_servicestatus = (TextView) itemView.findViewById(R.id.txt_servicestatus);

            layout_item = (RelativeLayout) itemView.findViewById(R.id.layout_item);
            img_request_userprofile = (CircleImageView) itemView.findViewById(R.id.img_request_userprofile);

        }
    }
}
