package com.app.bemyrider.Adapter.Partner;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.R;
import com.app.bemyrider.activity.user.UserProfileActivity;
import com.app.bemyrider.model.ServiceReviewItem;
import com.app.bemyrider.utils.CircleImageView;
import com.app.bemyrider.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by nct45 on 28/6/17.
 */

public class RvMyServiceDetailsReviewAdapter extends RecyclerView.Adapter<RvMyServiceDetailsReviewAdapter.Holder> {

    private Context mContext;
    private ArrayList<ServiceReviewItem> arrayLis = new ArrayList<>();

    public RvMyServiceDetailsReviewAdapter(Context mContext, ArrayList<ServiceReviewItem> arrayLis) {
        this.mContext = mContext;
        this.arrayLis = arrayLis;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.partner_row_partnerdetailsreview, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {
        if (arrayLis.size() > 0) {
            holder.txt_username.setText(arrayLis.get(position).getUserName());
            holder.txt_reviewmessage.setText(Utils.decodeEmoji(arrayLis.get(position).getReviewDesc()));
            holder.txt_reviewdate.setText(arrayLis.get(position).getReviewDate());
            holder.txt_reviewrate.setText(arrayLis.get(position).getReviewRating());

            if (arrayLis.get(position).getUserImage().equals("")) {
                holder.Img_profile.setImageResource(R.mipmap.user);
            } else {
                try {
                    Picasso.get().load(arrayLis.get(position).getUserImage()).placeholder(R.drawable.loading)
                            .into(holder.Img_profile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            holder.layout_rating_review.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION)
                        return;
                    if (!(arrayLis.get(adapterPosition).getIsActive().equalsIgnoreCase("du"))) {
                        Intent intent = new Intent(mContext, UserProfileActivity.class);
                        intent.putExtra("userId", arrayLis.get(adapterPosition).getCreatedUser());
                        mContext.startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return arrayLis.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private CircleImageView Img_profile;
        private LinearLayout layout_rating_review;
        private TextView txt_username, txt_reviewdate, txt_reviewmessage, txt_reviewrate;

        public Holder(View itemView) {
            super(itemView);

            Img_profile = itemView.findViewById(R.id.Img_profile);
            txt_username = itemView.findViewById(R.id.txt_username);
            txt_reviewdate = itemView.findViewById(R.id.txt_reviewdate);
            txt_reviewmessage = itemView.findViewById(R.id.txt_reviewmessage);
            txt_reviewrate = itemView.findViewById(R.id.txt_reviewrate);
            layout_rating_review = itemView.findViewById(R.id.layout_rating_review);

        }
    }
}
