package com.app.bemyrider.Adapter.Partner;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ItemPartnerPaymentHistoryBinding;
import com.app.bemyrider.model.partner.PartnerPaymentHistoryItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Hardik Talaviya on 29/11/19.
 */

public class PartnerPaymentHistoryAdapter extends RecyclerView.Adapter<PartnerPaymentHistoryAdapter.ViewHolder> {

    private ArrayList<PartnerPaymentHistoryItem> arrayList;
    private Context context;

    public PartnerPaymentHistoryAdapter(Context context, ArrayList<PartnerPaymentHistoryItem> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPartnerPaymentHistoryBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_partner_payment_history, parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(arrayList.get(position));

        /*------------- Load profile image -------------*/
        if (!TextUtils.isEmpty(arrayList.get(position).getProfile_image())) {
            Picasso.get().load(arrayList.get(position).getProfile_image()).placeholder(R.drawable.loading).into(holder.binding.imgProfile);
        } else {
            Picasso.get().load(R.mipmap.user).placeholder(R.drawable.loading).into(holder.binding.imgProfile);
        }

        /*----- Set Status text -----*/
        holder.binding.txtStatus.setText(arrayList.get(position).getStatus());

        switch (arrayList.get(position).getStatus().toLowerCase(Locale.ROOT)) {
            case "rejected":
                holder.binding.txtStatus.setBackgroundColor(context.getResources().getColor(R.color.status_rejected));
                break;
            case "pending":
                holder.binding.txtStatus.setBackgroundColor(context.getResources().getColor(R.color.status_pending));
                break;
            case "cancelled":
                holder.binding.txtStatus.setBackgroundColor(context.getResources().getColor(R.color.status_cancelled));
                break;
            case "accepted":
                holder.binding.txtStatus.setBackgroundColor(context.getResources().getColor(R.color.status_accepted));
                break;
            case "closed":
                holder.binding.txtStatus.setBackgroundColor(context.getResources().getColor(R.color.status_closed));
                break;
            case "completed":
                holder.binding.txtStatus.setBackgroundColor(context.getResources().getColor(R.color.status_completed));
                break;
            case "dispute":
                holder.binding.txtStatus.setBackgroundColor(context.getResources().getColor(R.color.status_dispute));
                break;
            case "ongoing":
                holder.binding.txtStatus.setBackgroundColor(context.getResources().getColor(R.color.status_ongoing));
                break;
            case "hired":
                holder.binding.txtStatus.setBackgroundColor(context.getResources().getColor(R.color.status_hired));
                break;
            case "expired":
                holder.binding.txtStatus.setBackgroundColor(context.getResources().getColor(R.color.status_expired));
                break;
        }

        holder.binding.txtCategory.setText(String.format("%s > %s", arrayList.get(position).getCategory(), arrayList.get(position).getSubcategory()));

        /*------- Hide Address(Deleted User) ------*/
        if (arrayList.get(position).getIsActive().equalsIgnoreCase("du")) {
            holder.binding.llAddress.setVisibility(View.GONE);
        }else{
            holder.binding.llAddress.setVisibility(View.VISIBLE);
        }

        /*--- If hourly service then hide working hours layout ---*/
        if (arrayList.get(position).getPer_hour_class().equalsIgnoreCase("hide")) {
            holder.binding.llTtlWorkingHours.setVisibility(View.GONE);
        } else {
            holder.binding.llTtlWorkingHours.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ItemPartnerPaymentHistoryBinding binding;

        public ViewHolder(ItemPartnerPaymentHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PartnerPaymentHistoryItem obj) {
            binding.setHistory(obj);
            binding.executePendingBindings();
        }
    }
}
