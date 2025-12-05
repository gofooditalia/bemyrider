package com.app.bemyrider.Adapter.User;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.R;
import com.app.bemyrider.model.MessageListDetailPojoItem;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nct33 on 9/10/17.
 */

public class MessageDetailItemAdapter extends RecyclerView.Adapter<MessageDetailItemAdapter.ViewHolder> {

    private ArrayList<MessageListDetailPojoItem> detailPojoItems;
    private Context context;
    private String toUserName, toUserImg, myUserName, myUserImg;

    public MessageDetailItemAdapter(ArrayList<MessageListDetailPojoItem> detailPojoItems, Context context,
            String myUserImg, String toUserName, String toUserImg) {
        this.detailPojoItems = detailPojoItems;
        this.context = context;
        this.myUserImg = myUserImg;
        this.toUserName = toUserName;
        this.toUserImg = toUserImg;
        myUserName = PrefsUtil.with(context).readString("UserName");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dispute_details_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        if (detailPojoItems.get(position).getFromUser().equals(PrefsUtil.with(context).readString("UserId"))) {
            holder.l_sender.setVisibility(View.VISIBLE);
            holder.txt_sender_name.setText(myUserName);
            holder.txt_sender_msg.setText(Utils.decodeEmoji(detailPojoItems.get(position).getMessageText()));
            try {
                Picasso.get().load(myUserImg).placeholder(R.drawable.loading).into(holder.img_sender);
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.txt_datetime_sender.setText(detailPojoItems.get(position).getCreatedDate());
            holder.txt_receiver_name.setText("");
            holder.txt_receiver_msg.setText("");
            holder.txt_datetime_receiver.setText("");
            Picasso.get().load((String) null).placeholder(R.drawable.loading).into(holder.img_receiver);
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
            holder.txt_receiver_name.setText(toUserName);
            holder.txt_receiver_msg.setText(Utils.decodeEmoji(detailPojoItems.get(position).getMessageText()));
            try {
                Picasso.get().load(toUserImg).placeholder(R.drawable.loading).into(holder.img_receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.txt_datetime_receiver.setText(detailPojoItems.get(position).getCreatedDate());
            holder.txt_sender_name.setText("");
            holder.txt_sender_msg.setText("");
            holder.txt_datetime_sender.setText("");
            Picasso.get().load((String) null).placeholder(R.drawable.loading).into(holder.img_sender);
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

    @Override
    public int getItemCount() {
        return detailPojoItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
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
