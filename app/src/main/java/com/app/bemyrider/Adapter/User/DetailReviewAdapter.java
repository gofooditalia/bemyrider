package com.app.bemyrider.Adapter.User;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.model.ProviderServiceReviewDataItem;
import com.app.bemyrider.R;
import com.app.bemyrider.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by nct58 on 29/6/17.
 */

public class DetailReviewAdapter extends RecyclerView.Adapter<DetailReviewAdapter.MyViewHolder> {

    private Activity mContext;
    private List<ProviderServiceReviewDataItem> data;

    public DetailReviewAdapter(Activity mContext, List<ProviderServiceReviewDataItem> data){
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        View view = inflater.inflate(R.layout.itemrow_service_detail_review, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.txt_name.setText(data.get(position).getUserName());
        holder.txt_date.setText(data.get(position).getCreatedDate());
        holder.txt_subjectName.setText(Utils.decodeEmoji(data.get(position).getReview()));
        holder.txt_rateCount.setText(data.get(position).getRating());
        Picasso.get().load(data.get(position).getProfileImage()).placeholder(R.drawable.loading).into(holder.img_profile);

        /*holder.relative_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, UserProfileActivity.class);
                intent.putExtra("userId",data.get(position).getCreatedUser());
                mContext.startActivity(intent);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txt_name, txt_date, txt_subjectName, txt_rateCount;
        RelativeLayout relative_review;
        ImageView img_profile;
        public MyViewHolder(View itemView) {
            super(itemView);
            txt_name = (TextView) itemView.findViewById(R.id.txt_name);
            txt_date = (TextView) itemView.findViewById(R.id.txt_date);
            txt_subjectName = (TextView) itemView.findViewById(R.id.txt_subjectName);
            txt_rateCount = (TextView) itemView.findViewById(R.id.txt_rateCount);
            img_profile = (ImageView) itemView.findViewById(R.id.img_profile);
            relative_review = (RelativeLayout) itemView.findViewById(R.id.relative_review);
        }
    }
}
