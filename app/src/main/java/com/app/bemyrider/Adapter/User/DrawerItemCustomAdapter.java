package com.app.bemyrider.Adapter.User;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.app.bemyrider.model.ModelForDrawer;
import com.app.bemyrider.R;

/**
 * Created by nct121 on 3/12/16.
 */

public class DrawerItemCustomAdapter extends ArrayAdapter<ModelForDrawer>
{
    Context mContext;
    int layoutResourceId;
    ModelForDrawer data[] = null;
    private int selectePos;

    public DrawerItemCustomAdapter(Context mContext, int layoutResourceId, ModelForDrawer[] data, int selectePos)
    {
        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
        this.selectePos = selectePos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItem = convertView;

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        listItem = inflater.inflate(layoutResourceId, parent, false);

        ImageView imageViewIcon = (ImageView) listItem.findViewById(R.id.imageViewIcon);
        TextView textViewName = (TextView) listItem.findViewById(R.id.textViewName);

        ModelForDrawer folder = data[position];

        imageViewIcon.setImageResource(folder.icon);
        textViewName.setText(folder.name);

        if(selectePos == position) {
            listItem.setBackgroundResource(R.drawable.list_item_bg_pressed);
            textViewName.setTextColor(mContext.getResources().getColor(R.color.white));
            imageViewIcon.setColorFilter(ContextCompat.getColor(mContext,
                    R.color.white));
        } else {
            listItem.setBackgroundResource(R.drawable.list_item_bg_normal);
            textViewName.setTextColor(mContext.getResources().getColor(R.color.text));
            imageViewIcon.setColorFilter(ContextCompat.getColor(mContext,
                    R.color.text));
        }

        return listItem;
    }

    public void selectedItem(int position){
        this.selectePos = position;
    }




}



