package com.app.bemyrider.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemyrider.R;
import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.myinterfaces.MenuItemClickListener;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {

    private Context context;
    private Activity activity;
    private MenuItemClickListener menuItemClickListener;
    private ModelForDrawer[] drawerItem;
    private LayoutInflater layoutInflater;

    public MenuItemAdapter(Context context, Activity activity, MenuItemClickListener menuItemClickListener, ModelForDrawer[] drawerItem) {
        this.context = context;
        this.activity = activity;
        this.menuItemClickListener = menuItemClickListener;
        this.drawerItem = drawerItem;
        layoutInflater = LayoutInflater.from(this.context);
    }

    @NonNull
    @Override
    public MenuItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.raw_menu_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemAdapter.ViewHolder holder, int position) {
        if (drawerItem.length > 0) {
            ModelForDrawer modelForDrawer = drawerItem[position];
            holder.imgMenu.setImageResource(modelForDrawer.icon);
            /*if((drawerItem.length - 1) == position) {

            }else{
                holder.imgMenu.setColorFilter(ContextCompat.getColor(context,
                        R.color.button));
            }*/

            holder.txtMenu.setText(modelForDrawer.name);
            holder.relMenuItem.setOnClickListener(v -> menuItemClickListener.onMenuItemClick(modelForDrawer, position));

            if((drawerItem.length - 1) == position) {
                holder.viewDivider.setVisibility(View.GONE);
            }else {
                holder.viewDivider.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return drawerItem.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relMenuItem;
        AppCompatImageView imgMenu, imgArrow;
        View viewDivider;
        AppCompatTextView txtMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMenu = itemView.findViewById(R.id.imgMenu);
            imgArrow = itemView.findViewById(R.id.imgArrow);
            viewDivider = itemView.findViewById(R.id.viewDivider);
            txtMenu = itemView.findViewById(R.id.txtMenu);
            relMenuItem = itemView.findViewById(R.id.relMenuItem);
        }
    }
}
