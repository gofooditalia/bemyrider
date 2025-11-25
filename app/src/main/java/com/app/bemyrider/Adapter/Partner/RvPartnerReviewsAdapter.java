package com.app.bemyrider.Adapter.Partner;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.user.UserProfileActivity;
import com.app.bemyrider.R;
import com.app.bemyrider.model.ServiceReviewItem;
import com.app.bemyrider.utils.CircleImageView;
import com.app.bemyrider.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by nct45 on 30/6/17.
 */

public class RvPartnerReviewsAdapter extends RecyclerView.Adapter<RvPartnerReviewsAdapter.Holder> {

    private Context context;
    private ArrayList<ServiceReviewItem> arrayList;

    public RvPartnerReviewsAdapter(Context context, ArrayList<ServiceReviewItem> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.partner_row_partner_reviews, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {

        final ServiceReviewItem item = arrayList.get(position);

        holder.txt_customername.setText(item.getUserName());
        holder.txt_review.setText(Utils.decodeEmoji(item.getReviewDesc()));
        holder.txtAddress.setText(item.getAddress());
        holder.txt_service_name.setText(item.getServiceName());
        holder.ratignbar.setRating(Float.parseFloat(item.getReviewRating()));
        holder.txtDate.setText(item.getReviewDate());

        if (item.getUserImage().equals("")) {
            holder.img_customerprofile.setImageResource(R.mipmap.user);
        } else {
            try {
                Picasso.get().load(item.getUserImage()).placeholder(R.drawable.loading)
                        .into(holder.img_customerprofile);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        if (arrayList.get(position).getIsActive().equalsIgnoreCase("du")) {
            holder.layout_address.setVisibility(View.GONE);
        } else {
            holder.layout_address.setVisibility(View.VISIBLE);
        }

        holder.layout_provider_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(arrayList.get(position).getIsActive().equalsIgnoreCase("du"))) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("userId", item.getCreatedUser());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private TextView txt_customername, txt_review, txt_service_name, txtDate, txtAddress;
        private CircleImageView img_customerprofile;
        private RatingBar ratignbar;
        private LinearLayout layout_provider_review;
        private RelativeLayout layout_address;

        public Holder(View itemView) {
            super(itemView);

            txt_customername = itemView.findViewById(R.id.txt_customername);
            txt_review = itemView.findViewById(R.id.txt_review);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txt_service_name = itemView.findViewById(R.id.txt_service_name);
            ratignbar = itemView.findViewById(R.id.ratignbar);
            txtDate = itemView.findViewById(R.id.txt_date);
            layout_address = itemView.findViewById(R.id.layout_address);

            img_customerprofile = itemView.findViewById(R.id.img_customerprofile);
            layout_provider_review = itemView.findViewById(R.id.layout_provider_review);
        }
    }
}
