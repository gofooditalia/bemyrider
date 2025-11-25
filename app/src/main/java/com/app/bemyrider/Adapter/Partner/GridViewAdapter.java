package com.app.bemyrider.Adapter.Partner;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import com.app.bemyrider.utils.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.bemyrider.R;
import com.app.bemyrider.model.ProviderServiceMediaDataItem;
import com.app.bemyrider.model.partner.ImageItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<ImageItem> ImgArray;
    private Image_interface imgInterface;
    private List<ProviderServiceMediaDataItem> mediaDataItems;

    public GridViewAdapter(Context context, int layoutResourceId, List<ProviderServiceMediaDataItem> mediaData, ArrayList<ImageItem> ImgArray, Image_interface imgInterface) {

        super(context, layoutResourceId, ImgArray);
        Log.e("GridViewAdapter", "GridViewAdapter: Constructor ");
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.ImgArray = ImgArray;
        this.imgInterface = imgInterface;
        this.mediaDataItems = mediaData;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) row.findViewById(R.id.Img_photos);
            holder.imgRemove = (ImageView) row.findViewById(R.id.Img_remove);
            holder.pgDelete = row.findViewById(R.id.pgDelete);
            //  holder.txt_name = (TextView) row.findViewById(R.id.Txt_imgName);
            holder.relative_imageremove = (RelativeLayout) row.findViewById(R.id.relative_imageremove);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        ImageItem item = ImgArray.get(position);

        Uri uriPath = item.getImageStream();
        String imgName = item.getImageName();

        //holder.txt_name.setText(imgName);
        Picasso.get().load(uriPath).placeholder(R.drawable.loading).into(holder.image);
//        holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        holder.image.setLayoutParams(new GridView.LayoutParams(20, 20));

        ViewHolder finalHolder = holder;
        holder.imgRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isApiCall = false;
                for (int i = 0; i < mediaDataItems.size(); i++) {
                    if ((Uri.parse(mediaDataItems.get(i).getMediaUrl())).equals((ImgArray.get(position)).getImageStream())) {
                        try {
                            isApiCall = true;
                            removeItem(mediaDataItems.get(position).getMediaId(), position, finalHolder.pgDelete, finalHolder.imgRemove);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!isApiCall) {
                    ImgArray.remove(position);
                }
                Log.e("GridViewAdapter", "setOnClickListener: onClick ");
                notifyDataSetChanged();
            }
        });

        return row;
    }

    private void removeItem(String mediaId, int position, ProgressBar pgDelete, ImageView imgRemove) {
        //ImgArray.remove(position);
        imgInterface.deleteimage(mediaId, position, pgDelete, imgRemove);
        notifyDataSetChanged();
    }

    public void removeData(int pos) {
        try {
            ImgArray.remove(pos);
            // notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public interface Image_interface {

        public void deleteimage(String mediaId, int id, ProgressBar pgDelete, ImageView imgRemove);

    }

    static class ViewHolder {
        ImageView image, imgRemove;
        TextView txt_name;
        RelativeLayout relative_imageremove;
        ProgressBar pgDelete;
    }
}