package com.app.bemyrider.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.activity.WebViewActivity;
import com.app.bemyrider.R;
import com.app.bemyrider.databinding.ItemInfoPageBinding;
import com.app.bemyrider.model.InfoPagePojoItem;

import java.util.ArrayList;

/**
 * Created by Hardik Talaviya on 27/12/19.
 */

public class InfoPageAdapter extends RecyclerView.Adapter<InfoPageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<InfoPagePojoItem> arrayList;

    public InfoPageAdapter(Context context, ArrayList<InfoPagePojoItem> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInfoPageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_info_page,
                parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(arrayList.get(position));

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, WebViewActivity.class)
                        .putExtra("webUrl", arrayList.get(position).getUrl())
                        .putExtra("title", arrayList.get(position).getPageTitle()));
            }
        });

        if (position == (arrayList.size() - 1)) {
            holder.binding.view.setVisibility(View.GONE);
        } else {
            holder.binding.view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemInfoPageBinding binding;

        public ViewHolder(ItemInfoPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(InfoPagePojoItem obj) {
            binding.setInfoPage(obj);
            binding.executePendingBindings();
        }
    }
}
