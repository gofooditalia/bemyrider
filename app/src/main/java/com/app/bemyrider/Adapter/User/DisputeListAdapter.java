package com.app.bemyrider.Adapter.User;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.partner.Partner_DisputeDetail_Activity;
import com.app.bemyrider.R;
import com.app.bemyrider.model.DisputeListPojoItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DisputeListAdapter extends RecyclerView.Adapter<DisputeListAdapter.MyViewHolder> {

    private Context act;
    private List<DisputeListPojoItem> disputeList= new ArrayList<>();
    private String userType;

    public DisputeListAdapter(Context act, List<DisputeListPojoItem> disputeList) {
        this.act = act;
        this.disputeList = disputeList;
        userType = PrefsUtil.with(act).readString("UserType");
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.disputelist_itemrow, parent, false);
        return new DisputeListAdapter.MyViewHolder(view1);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if(disputeList.size() >0 ){
            final DisputeListPojoItem item = disputeList.get(position);
            if (item != null) {
                String profileImg, firstName, lastName;
                if (userType.equalsIgnoreCase("p")) {
                    profileImg = item.getCustomerImg();
                    firstName = item.getCustomerFirstname();
                    lastName = item.getCustomerLastname();
                } else {
                    profileImg = item.getProviderImg();
                    firstName = item.getProviderFirstname();
                    lastName = item.getProviderLastname();
                }
                if (profileImg != null
                        && profileImg.length() > 0) {
                    Picasso.get()
                            .load(profileImg).placeholder(R.drawable.loading)
                            .into(holder.iv_profile);
                } else {
                    Picasso.get()
                            .load(R.mipmap.user)
                            .placeholder(R.drawable.loading)
                            .into(holder.iv_profile);
                }

                if (firstName != null
                        && firstName.length() > 0
                        && lastName != null
                        && lastName.length() > 0) {
                    holder.txt_name.setText(String.format("%s %s", firstName, lastName));
                } else if (firstName != null
                        && firstName.length() > 0) {
                    holder.txt_name.setText(firstName);
                } else if (lastName != null
                        && lastName.length() > 0) {
                    holder.txt_name.setText(lastName);
                } else {
                    holder.txt_name.setText("");
                }
            }

            if (item.getServiceName() != null && item.getServiceName().length() > 0) {
                holder.txt_sName.setText(item.getServiceName());
            } else {
                holder.txt_sName.setText("");
            }

            if (item.getDisputeTitle() != null && item.getDisputeTitle().length() > 0) {
                holder.txt_subjectName.setText(item.getDisputeTitle());
            } else {
                holder.txt_subjectName.setText("");
            }

            if (item.getDisputeMessage() != null) {
                if (item.getDisputeMessage() != null
                        && item.getDisputeMessage().length() > 0) {
                    holder.txt_desc.setText(Utils.decodeEmoji(item.getDisputeMessage()));
                } else {
                    holder.txt_desc.setText("");
                }

                if (item.getCreatedDate() != null
                        && item.getCreatedDate().length() > 0) {
                    holder.txt_date.setText(item.getCreatedDate());
                } else {
                    holder.txt_date.setText("");
                }
            }

            holder.layout_dispute_list.setOnClickListener(view -> {
                Intent intent = new Intent(act, Partner_DisputeDetail_Activity.class);
                intent.putExtra("DisputeId", item.getDisputeId());
                intent.putExtra("createdID", item.getCreatedUser());

                if (item.getCreatedUser().equals(item.getCustomerId())) {
                    intent.putExtra("CreatedUser", item.getCustomerFirstname() + " " + item.getCustomerLastname());
                } else {
                    intent.putExtra("CreatedUser", item.getProviderFirstname() + " " + item.getProviderLastname());
                }
                act.startActivity(intent);
            });
        }

    }

    @Override
    public int getItemCount() {
        return disputeList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_profile;
        RelativeLayout layout_dispute_list;
        TextView txt_name, txt_sName, txt_date, txt_subjectName, txt_desc;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv_profile = itemView.findViewById(R.id.iv_profile);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_sName = itemView.findViewById(R.id.txt_sName);
            txt_date = itemView.findViewById(R.id.txt_date);
            txt_subjectName = itemView.findViewById(R.id.txt_subjectName);
            txt_desc = itemView.findViewById(R.id.txt_desc);
            layout_dispute_list = itemView.findViewById(R.id.layout_dispute_list);

        }
    }
}
