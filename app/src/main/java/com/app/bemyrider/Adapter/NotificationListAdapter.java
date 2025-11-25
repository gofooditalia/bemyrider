package com.app.bemyrider.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.partner.Partner_DisputeDetail_Activity;
import com.app.bemyrider.activity.partner.Partner_ServiceRequestDetail_Tablayout_Activity;
import com.app.bemyrider.activity.partner.PartnerServiceRequestDetailsActivity;
import com.app.bemyrider.activity.user.BookedServiceDetailActivity;
import com.app.bemyrider.activity.user.MessageDetailActivity;
import com.app.bemyrider.R;
import com.app.bemyrider.model.NotificationListItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.MyViewHolder> {

    private List<NotificationListItem> historyList = new ArrayList<>();
    private Activity act;
    private String userType;

    public NotificationListAdapter(Activity act, List<NotificationListItem> historyList) {
        this.act = act;
        this.historyList = historyList;
        userType = PrefsUtil.with(act).readString("UserType");
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemrow_notification_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if (historyList.size() > 0) {
            final NotificationListItem item = historyList.get(position);

            holder.txtName.setText(item.getUserName());

            if (item.getNotificationDate() != null && item.getNotificationDate().length() > 0) {
                holder.txtDateTime.setText(item.getNotificationDate());
            } else {
                holder.txtDateTime.setText("");
            }

            if (item.getImage() != null && item.getImage().length() > 0) {
                Picasso.get().load(item.getImage()).placeholder(R.drawable.loading).into(holder.imgProfile);
            } else {
                Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(holder.imgProfile);
            }

            /*---------- Notification item click redirection ------------*/
            holder.itemView.getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Check if user is deleted(Don't redirect)*/
                    if (!(item.getIsActive().equalsIgnoreCase("du"))) {
                        /*---------- Customer side message notification ----------*/
                        if (item.getNotificationType().equalsIgnoreCase("m") && userType.equalsIgnoreCase("c")) {
                            Intent intent = new Intent(act, MessageDetailActivity.class);
                            intent.putExtra("to_user", item.getProviderId());
                            intent.putExtra("master_id", item.getServiceId());
                            act.startActivity(intent);
                        }
                        /*---------- Customer side service notification ----------*/
                        else if (item.getNotificationType().equalsIgnoreCase("s") && userType.equalsIgnoreCase("c")) {
                            Intent intent = new Intent(act, BookedServiceDetailActivity.class);
                            intent.putExtra("serviceRequestId", item.getServiceRequestId());
                            intent.putExtra("providerServiceId", item.getProviderServiceId());
                            act.startActivity(intent);
                        }
                        /*---------- Provider side message notification ----------*/
                        else if (item.getNotificationType().equalsIgnoreCase("m") && userType.equalsIgnoreCase("p")) {
                            Intent intent = new Intent(act, MessageDetailActivity.class);
                            intent.putExtra("to_user", item.getCustomerId());
                            intent.putExtra("master_id", item.getServiceId());
                            act.startActivity(intent);
                        }
                        /*---------- Provider side service notification ----------*/
                        else if (item.getNotificationType().equalsIgnoreCase("s") && userType.equalsIgnoreCase("p")) {
                            /*check If service completed then goto completed service detail page*/
                            if (item.getServiceStatus().equalsIgnoreCase("completed")) {
                                Intent intent = new Intent(act, Partner_ServiceRequestDetail_Tablayout_Activity.class);
                                intent.putExtra("serviceRequestId", item.getServiceRequestId());
                                act.startActivity(intent);
                            } else {
                                Intent intent = new Intent(act, PartnerServiceRequestDetailsActivity.class);
                                intent.putExtra("serviceRequestId", item.getServiceRequestId());
                                act.startActivity(intent);
                            }
                        }
                        /*---------- Customer and Provider side dispute notification ----------*/
                        else if (item.getNotificationType().equalsIgnoreCase("d")) {
                            /*Check If service closed then goto service detail page Else dispute detail page*/
                            if (item.getServiceStatus().equalsIgnoreCase("closed")) {
                                if (userType.equalsIgnoreCase("p")) {
                                    Intent intent = new Intent(act, PartnerServiceRequestDetailsActivity.class);
                                    intent.putExtra("serviceRequestId", item.getServiceRequestId());
                                    act.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(act, BookedServiceDetailActivity.class);
                                    intent.putExtra("serviceRequestId", item.getServiceRequestId());
                                    intent.putExtra("providerServiceId", item.getProviderServiceId());
                                    act.startActivity(intent);
                                }
                            } else {
                                Intent intent = new Intent(act, Partner_DisputeDetail_Activity.class);
                                intent.putExtra("DisputeId", item.getDisputeId());
                                act.startActivity(intent);
                            }
                        }
                        /*---------- Provider side review notification ----------*/
                        else if (item.getNotificationType().equalsIgnoreCase("r") && userType.equalsIgnoreCase("p")) {
                            Intent intent = new Intent(act, Partner_ServiceRequestDetail_Tablayout_Activity.class);
                            intent.putExtra("serviceRequestId", item.getServiceRequestId());
                            intent.putExtra("fromReviewNotification", true);
                            act.startActivity(intent);
                        }
                    }
                }
            });


            holder.txtMsg.setText(item.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName, txtDateTime, txtMsg;
        private ImageView imgProfile;

        public MyViewHolder(View view) {
            super(view);
            imgProfile = (ImageView) view.findViewById(R.id.imgProfile);
            txtDateTime = (TextView) view.findViewById(R.id.txtDateTime);
            txtName = (TextView) view.findViewById(R.id.txtName);
            txtMsg = (TextView) view.findViewById(R.id.txtMsg);
        }
    }
}
