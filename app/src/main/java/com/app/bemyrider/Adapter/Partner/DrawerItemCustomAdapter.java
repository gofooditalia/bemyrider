package com.app.bemyrider.Adapter.Partner;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.app.bemyrider.R;
import com.app.bemyrider.model.ModelForDrawer;


/**
 * Created by nct121 on 3/12/16.
 */

public class DrawerItemCustomAdapter extends ArrayAdapter<ModelForDrawer>
{
    Context mContext;
    int layoutResourceId;
    int selectedPos;
    ModelForDrawer data[] = null;

    public DrawerItemCustomAdapter(Context mContext, int layoutResourceId, ModelForDrawer[] data, int selectedPos)
    {
        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
        this.selectedPos=selectedPos;

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


        if(selectedPos == position){
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
        this.selectedPos = position;
    }
}



