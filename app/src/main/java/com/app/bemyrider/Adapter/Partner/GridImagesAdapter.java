package com.app.bemyrider.Adapter.Partner;

import android.content.Context;
import com.app.bemyrider.utils.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.app.bemyrider.R;
import com.app.bemyrider.model.ProviderServiceMediaDataItem;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by nct45 on 4/7/17.
 */

public class GridImagesAdapter extends BaseAdapter {

    //    for use of remove image
    private List<ProviderServiceMediaDataItem> arrayList;
    private Context context;
    private RemoveImage removeImage;

    public GridImagesAdapter(List<ProviderServiceMediaDataItem> arrayList, Context context, RemoveImage removeImage) {
        Log.e("GridImagesAdapter", "GridImagesAdapter: Constructor ");
        this.arrayList = arrayList;
        this.context = context;
        this.removeImage = removeImage;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.partner_gridlayout_itermrow, parent, false);
        ImageView Img_photos = (ImageView) convertView.findViewById(R.id.Img_photos);
        ImageView Img_remove = (ImageView) convertView.findViewById(R.id.Img_remove);
        ProgressBar pgDelete = convertView.findViewById(R.id.pgDelete);
        RelativeLayout relative_imageremove = (RelativeLayout) convertView.findViewById(R.id.relative_imageremove);

        try {
            Picasso.get().load(arrayList.get(position).getMediaUrl()).placeholder(R.drawable.loading).into(Img_photos);
//            Img_photos.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            Img_photos.setLayoutParams(new GridView.LayoutParams(20, 20));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        Img_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeImage.PassImageId(arrayList.get(position).getMediaId(), position, pgDelete, Img_remove);

                arrayList.remove(position);
                notifyDataSetChanged();
//                arrayList.remove(arrayList.get(position).getMediaId());
//                arrayList.notify();
            }
        });

        return convertView;
    }

    public interface RemoveImage {
        void PassImageId(String mediaId, int position,ProgressBar pgDelete,ImageView imgRemove);
    }


}
