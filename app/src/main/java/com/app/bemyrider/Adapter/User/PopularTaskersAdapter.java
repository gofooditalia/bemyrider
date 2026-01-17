package com.app.bemyrider.Adapter.User;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.user.PartnerProfileActivity;
import com.app.bemyrider.model.user.PopularTaskerItem;
import com.app.bemyrider.R;
import com.app.bemyrider.utils.Utils;
// Coil Imports
import coil.Coil;
import coil.request.ImageRequest;
// import com.squareup.picasso.Picasso;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class PopularTaskersAdapter extends RecyclerView.Adapter<PopularTaskersAdapter.TaskerHolder> {

    private List<PopularTaskerItem> providerList;
    private Activity act;
    private SharedPreferences preferences;

    public PopularTaskersAdapter(Activity act, List<PopularTaskerItem> moviesList) {
        this.act = act;
        this.providerList = moviesList;
        preferences = act.getSharedPreferences("Unique", MODE_PRIVATE);
    }

    @Override
    public TaskerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.popular_tasker_list_row, parent, false);

        return new TaskerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TaskerHolder holder, int position) {
        final PopularTaskerItem item = providerList.get(position);
        holder.txt_description.setText(item.getService());
        holder.txt_serviceName.setText(item.getUsername());
        
        // Coil Migration from Picasso
        String imageUrl = item.getUserimg();
        ImageRequest.Builder profileBuilder = new ImageRequest.Builder(act)
                .placeholder(R.drawable.loading)
                .target(holder.img_profile);

        if (imageUrl != null && imageUrl.length() > 0) {
            profileBuilder.data(imageUrl);
        } else {
            profileBuilder.data(R.mipmap.user);
        }
        Coil.imageLoader(act).enqueue(profileBuilder.build());

        holder.item_main.setOnClickListener(view -> {
            Intent i = new Intent(act, PartnerProfileActivity.class);
            i.putExtra(Utils.PROVIDER_ID, item.getUserid());
            act.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return providerList.size();
    }

    public class TaskerHolder extends RecyclerView.ViewHolder {
        private TextView txt_serviceName, txt_description;
        private ImageView img_profile;
        private LinearLayoutCompat item_main;

        public TaskerHolder(View view) {
            super(view);
            item_main = view.findViewById(R.id.item_main);
            img_profile = view.findViewById(R.id.img_profile);
            txt_description = view.findViewById(R.id.txt_description);
            txt_serviceName = view.findViewById(R.id.txt_serviceName);
        }
    }
}
