package com.app.bemyrider.Adapter.User;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.app.bemyrider.utils.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.R;
import com.app.bemyrider.model.DisputeDetailPojoItem;
import com.app.bemyrider.utils.Utils;
// Coil Imports
import coil.Coil;
import coil.request.ImageRequest;
// import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nct33 on 9/10/17.
 */

public class DisputeDetailItemAdapter extends RecyclerView.Adapter<DisputeDetailItemAdapter.ViewHolder> {

    private ArrayList<DisputeDetailPojoItem> detailPojoItems;
    private Context context;
    private String customerName, providerName, customerImg, providerImg;

    public DisputeDetailItemAdapter(ArrayList<DisputeDetailPojoItem> detailPojoItems, Context context,
            String customerName, String providerName, String customerImg, String providerImg) {
        this.detailPojoItems = detailPojoItems;
        this.context = context;
        this.customerName = customerName;
        this.providerName = providerName;
        this.customerImg = customerImg;
        this.providerImg = providerImg;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dispute_details_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        if (detailPojoItems.get(position).getCreatedUserType().equals("c")) {
            holder.l_sender.setVisibility(View.VISIBLE);
            holder.txt_sender_name.setText(customerName);
            holder.txt_sender_msg.setText(Utils.decodeEmoji(detailPojoItems.get(position).getDisputeMessage()));
            
            // Coil Migration: Sender Image
            ImageRequest requestSender = new ImageRequest.Builder(context)
                .data(customerImg)
                .placeholder(R.drawable.loading)
                .target(holder.img_sender)
                .build();
            Coil.imageLoader(context).enqueue(requestSender);

            holder.txt_datetime_sender.setText(detailPojoItems.get(position).getCreatedDate());
            holder.txt_receiver_name.setText("");
            holder.txt_receiver_msg.setText("");
            holder.txt_datetime_receiver.setText("");

            // Coil Migration: Null Receiver Image
            ImageRequest requestNullReceiver = new ImageRequest.Builder(context)
                .data(null)
                .placeholder(R.drawable.loading)
                .target(holder.img_receiver)
                .build();
            Coil.imageLoader(context).enqueue(requestNullReceiver);
            
            holder.l_receiver.setVisibility(View.GONE);

            Log.e("Adapter", " onBindViewHolder: isNullOrEmpty receiver "
                    + isNullOrEmpty(detailPojoItems.get(position).getAppAttUrl()));
            if (!isNullOrEmpty(detailPojoItems.get(position).getAppAttUrl())) {
                holder.txt_sender_msg.setVisibility(View.GONE);
                holder.txtAttachment.setVisibility(View.VISIBLE);
                holder.txtAttachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition == RecyclerView.NO_POSITION)
                            return;
                        if (detailPojoItems.get(adapterPosition).getAppAttUrl().contains("https://")) {
                            Intent intentView = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(detailPojoItems.get(adapterPosition).getAppAttUrl()));
                            context.startActivity(intentView);
                        } else {
                            Intent myIntent = new Intent(Intent.ACTION_VIEW);
                            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            myIntent.setData(FileProvider.getUriForFile(context,
                                    context.getApplicationContext().getPackageName() + ".provider",
                                    new File(detailPojoItems.get(adapterPosition).getAppAttUrl())));
                            Intent i = Intent.createChooser(myIntent, "Choose an application to open with:");
                            // i.setDataAndType(Uri.parse(detailPojoItems.get(adapterPosition).getAppAttUrl()),"video/mp4");
                            context.startActivity(i);
                        }
                    }
                });
            } else {
                holder.txt_receiver_msg.setVisibility(View.VISIBLE);
                holder.txtUserAttachment.setVisibility(View.GONE);
            }
        } else {
            holder.l_receiver.setVisibility(View.VISIBLE);
            holder.txt_receiver_name.setText(providerName);
            holder.txt_receiver_msg.setText(Utils.decodeEmoji(detailPojoItems.get(position).getDisputeMessage()));
            
            // Coil Migration: Receiver Image
            ImageRequest requestReceiver = new ImageRequest.Builder(context)
                .data(providerImg)
                .placeholder(R.drawable.loading)
                .target(holder.img_receiver)
                .build();
            Coil.imageLoader(context).enqueue(requestReceiver);

            holder.txt_datetime_receiver.setText(detailPojoItems.get(position).getCreatedDate());
            holder.txt_sender_name.setText("");
            holder.txt_sender_msg.setText("");
            holder.txt_datetime_sender.setText("");
            
            // Coil Migration: Null Sender Image
            ImageRequest requestNullSender = new ImageRequest.Builder(context)
                .data(null)
                .placeholder(R.drawable.loading)
                .target(holder.img_sender)
                .build();
            Coil.imageLoader(context).enqueue(requestNullSender);

            holder.l_sender.setVisibility(View.GONE);

            Log.e("Adapter", "onBindViewHolder: isNullOrEmpty sender "
                    + isNullOrEmpty(detailPojoItems.get(position).getAppAttUrl()));
            if (!isNullOrEmpty(detailPojoItems.get(position).getAppAttUrl())) {
                holder.txt_receiver_msg.setVisibility(View.GONE);
                holder.txtUserAttachment.setVisibility(View.VISIBLE);
                holder.txtUserAttachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition == RecyclerView.NO_POSITION)
                            return;
                        if (detailPojoItems.get(adapterPosition).getAppAttUrl().contains("https://")) {
                            Intent intentView = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(detailPojoItems.get(adapterPosition).getAppAttUrl()));
                            context.startActivity(intentView);
                        } else {
                            Intent myIntent = new Intent(Intent.ACTION_VIEW);
                            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            myIntent.setData(FileProvider.getUriForFile(context,
                                    context.getApplicationContext().getPackageName() + ".provider",
                                    new File(detailPojoItems.get(adapterPosition).getAppAttUrl())));
                            Intent i = Intent.createChooser(myIntent, "Choose an application to open with:");
                            // i.setDataAndType(Uri.parse(detailPojoItems.get(adapterPosition).getAppAttUrl()),"video/mp4");
                            context.startActivity(i);
                        }
                    }
                });
            } else {
                holder.txt_sender_msg.setVisibility(View.VISIBLE);
                holder.txtAttachment.setVisibility(View.GONE);
            }
        }
    }

    public boolean isNullOrEmpty(String str) {
        try {
            if (str.isEmpty() || str.length() <= 0 || str.equalsIgnoreCase("null")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            // e.printStackTrace();
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return detailPojoItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout l_sender, l_receiver;
        private ImageView img_receiver, img_sender;
        private TextView txt_receiver_name, txt_receiver_msg, txt_sender_name, txt_sender_msg, txt_datetime_receiver,
                txt_datetime_sender, txtAttachment, txtUserAttachment;

        public ViewHolder(View itemView) {
            super(itemView);

            l_sender = itemView.findViewById(R.id.l_sender);
            l_receiver = itemView.findViewById(R.id.l_receiver);

            img_receiver = itemView.findViewById(R.id.img_receiver);
            img_sender = itemView.findViewById(R.id.img_sender);

            txt_receiver_name = itemView.findViewById(R.id.txt_receiver_name);
            txt_receiver_msg = itemView.findViewById(R.id.txt_receiver_msg);
            txt_sender_name = itemView.findViewById(R.id.txt_sender_name);
            txt_sender_msg = itemView.findViewById(R.id.txt_sender_msg);
            txt_datetime_receiver = itemView.findViewById(R.id.txt_datetime_receiver);
            txt_datetime_sender = itemView.findViewById(R.id.txt_datetime_sender);
            txtAttachment = itemView.findViewById(R.id.txtAttachment);
            txtUserAttachment = itemView.findViewById(R.id.txtUserAttachment);
        }

    }
}
