package com.app.bemyrider.Adapter.User;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.user.ServiceDetailActivity;
import com.app.bemyrider.model.partner.MyServiceListItem;
import com.app.bemyrider.R;
import com.app.bemyrider.utils.CircleImageView;
import com.app.bemyrider.utils.PrefsUtil;
// Coil Imports
import coil.Coil;
import coil.request.ImageRequest;
// import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by nct45 on 26/6/17.
 */

public class ProviderServicesAdapter extends RecyclerView.Adapter<ProviderServicesAdapter.Holder> {

    private Context mContext;
    private ArrayList<MyServiceListItem> arrayList;
    private String profileImage;


    public ProviderServicesAdapter(Context mContext, ArrayList<MyServiceListItem> arrayList,
                                   String profileImage) {
        this.mContext = mContext;
        this.arrayList = arrayList;
        this.profileImage = profileImage;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.partner_row_myservice, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {
        final MyServiceListItem item = arrayList.get(position);
        holder.txt_service_name.setText(item.getServiceName());
        holder.txt_sub_categoty.setText(item.getCategoryName() + "-> " + item.getSubcategoryName());
        holder.txt_service_price.setText(PrefsUtil.with(mContext).readString("CurrencySign") + item.getPrice());
        holder.txt_dur.setText(item.getAddress());

        // Coil Migration from Picasso
        String imageUrl = item.getServiceImage();
        ImageRequest.Builder builder = new ImageRequest.Builder(mContext)
            .placeholder(R.drawable.loading) // Usiamo R.drawable.loading come placeholder
            .error(R.mipmap.user) // Assumo che R.mipmap.user sia l'errore inteso
            .target(holder.img_service);
            
        if (imageUrl.equals("")) {
            holder.img_service.setImageResource(R.mipmap.user);
        } else {
            try {
                builder.data(imageUrl);
                Coil.imageLoader(mContext).enqueue(builder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        holder.rowdata.setOnClickListener(v -> {
            Intent i = new Intent(mContext, ServiceDetailActivity.class);
            i.putExtra("providerServiceId", item.getProviderServiceId());
            mContext.startActivity(i);
        });
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private TextView txt_dur, txt_sub_categoty, txt_service_price, txt_service_name;
        private RelativeLayout rowdata;
        private CircleImageView img_service;


        public Holder(View itemView) {
            super(itemView);

            rowdata = (RelativeLayout) itemView.findViewById(R.id.rowdata);

            txt_dur = (TextView) itemView.findViewById(R.id.txt_dur);
            txt_service_name = (TextView) itemView.findViewById(R.id.txt_service_name);
            txt_service_price = (TextView) itemView.findViewById(R.id.txt_service_price);
            txt_sub_categoty = (TextView) itemView.findViewById(R.id.txt_sub_categoty);

            img_service = (CircleImageView) itemView.findViewById(R.id.img_service);
        }
    }
}
