package com.app.bemyrider.Adapter.User;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.user.MessageDetailActivity;
import com.app.bemyrider.model.MessageListPojoItem;
import com.app.bemyrider.R;
import com.app.bemyrider.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by nct33 on 10/10/17.
 */

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private ArrayList<MessageListPojoItem> messageListPojoItems;
    private Context context;

    public MessageListAdapter(ArrayList<MessageListPojoItem> messageListPojoItems, Context context) {
        this.messageListPojoItems = messageListPojoItems;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.txt_provider_name.setText(messageListPojoItems.get(position).getToUserName());
        holder.txt_service_name.setText(messageListPojoItems.get(position).getServiceName());
        holder.txt_message.setText(Utils.decodeEmoji(messageListPojoItems.get(position).getMessageText()));
        Picasso.get().load(messageListPojoItems.get(position).getToProfileImg()).placeholder(R.drawable.loading).into(holder.img_provider_image);
        holder.txt_time.setText(messageListPojoItems.get(position).getCreatedDate());

        holder.layout_message_main.setOnClickListener(v -> {
            Intent intent = new Intent(context, MessageDetailActivity.class);
            intent.putExtra("to_user",messageListPojoItems.get(position).getToUser());
            intent.putExtra("serviceName",messageListPojoItems.get(position).getServiceName());
            intent.putExtra("master_id",messageListPojoItems.get(position).getServiceMasterId());
            intent.putExtra("bookingId",messageListPojoItems.get(position).getServiceId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return messageListPojoItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout layout_message_main;
//        , img_remove
        private ImageView img_provider_image;
        private TextView txt_provider_name, txt_message, txt_time,txt_service_name;

        public ViewHolder(View itemView) {
            super(itemView);

            img_provider_image = itemView.findViewById(R.id.img_provider_image);
//            img_remove = itemView.findViewById(R.id.img_remove);
            txt_provider_name = itemView.findViewById(R.id.txt_provider_name);
            txt_message = itemView.findViewById(R.id.txt_message);
            txt_service_name = itemView.findViewById(R.id.txt_service_name);
            txt_time = itemView.findViewById(R.id.txt_time);
            layout_message_main = itemView.findViewById(R.id.layout_message_main);
        }
    }
}
