package com.app.bemyrider.Adapter.User;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.bemyrider.model.partner.CountryCodePojo;
import com.app.bemyrider.model.partner.CountryCodePojoItem;
import com.app.bemyrider.R;

import java.util.List;

/**
 * Created by nct58 on 21/6/17.
 */

public class CountryCodeAdapter extends BaseAdapter {

    private Activity act;
    private List<CountryCodePojoItem> array = null;
    LayoutInflater inflater;


    public CountryCodeAdapter(Activity act, CountryCodePojo pojo){
        this.act = act;
        this.array = pojo.getData();
        inflater = (LayoutInflater)act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return array.size();
    }

    @Override
    public CountryCodePojoItem getItem(int position) {
        return array.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.simple_spinner_item, null);
            holder = new ViewHolder();
            holder.countryCode = (TextView) convertView.findViewById(R.id.countryCode);
            holder.id = (TextView) convertView.findViewById(R.id.id);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.countryCode.setText(array.get(position).getCountryCode());
        holder.id.setText(array.get(position).getId());

        return convertView;
    }

    static class ViewHolder {
        private TextView countryCode;
        private TextView id;
    }
}
