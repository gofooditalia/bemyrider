package com.app.bemyrider.Adapter.Partner;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.partner.Partner_ServiceDetail_Activity;
import com.app.bemyrider.model.partner.MyServiceListItem;
import com.app.bemyrider.R;
import com.app.bemyrider.utils.CircleImageView;
import com.app.bemyrider.utils.PrefsUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by nct45 on 26/6/17.
 */

public class RvMyServiceAdapter extends RecyclerView.Adapter<RvMyServiceAdapter.Holder> {

    private Context mContext;
    private ArrayList<MyServiceListItem> arrayList;


    public RvMyServiceAdapter(Context mContext, ArrayList<MyServiceListItem> arrayList) {
        this.mContext = mContext;
        this.arrayList = arrayList;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.partner_row_myservice, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {

        MyServiceListItem item = arrayList.get(position);

        holder.txt_service_name.setText(item.getServiceName());
        holder.txt_sub_categoty.setText(String.format("%s-> %s", item.getCategoryName(), item.getSubcategoryName()));
        holder.txt_service_price.setText(String.format("%s%s", PrefsUtil.with(mContext).readString("CurrencySign"), item.getPrice()));
        holder.txt_dur.setText(item.getAddress());

        if (item.getServiceImage().equals("")) {
            holder.img_service.setImageResource(R.mipmap.user);
        } else {
            try {
                Picasso.get().load(item.getServiceImage()).placeholder(R.mipmap.user).placeholder(R.drawable.loading).into(holder.img_service);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, Partner_ServiceDetail_Activity.class);
            intent.putExtra("providerServiceId", item.getProviderServiceId());
            mContext.startActivity(intent);
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
