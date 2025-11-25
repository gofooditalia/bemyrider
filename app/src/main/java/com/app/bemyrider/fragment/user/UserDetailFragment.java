package com.app.bemyrider.fragment.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.R;
import com.app.bemyrider.utils.Utils;

public class UserDetailFragment extends Fragment {

    private TextView txt_about_provider /*,txt_number, txt_mail*/;
   // private LinearLayout layout_contact;
    private ProviderServiceDetailsItem serviceDetailData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userdetail, container, false);

        serviceDetailData = (ProviderServiceDetailsItem) getArguments().getSerializable("data");

        init(view);

        if (serviceDetailData != null) {
            setdata();
        }
        /*if (serviceDetailData.getServiceStatus().equals("")) {
            if (serviceDetailData.getServiceStatus().equals("accepted")
                    || serviceDetailData.getServiceStatus().equals("completed")
                    || serviceDetailData.getServiceStatus().equals("dispute")
                    || serviceDetailData.getServiceStatus().equals("ongoing")
                    || serviceDetailData.getServiceStatus().equals("hired")) {
                layout_contact.setVisibility(View.VISIBLE);
            } else {
                layout_contact.setVisibility(View.GONE);
            }
        } else {
            layout_contact.setVisibility(View.VISIBLE);
        }
        if (serviceDetailData.getServiceRequestId().equals("")) {
            layout_contact.setVisibility(View.GONE);
        }*/




        return view;
    }


    private void init(View v) {
        txt_about_provider = v.findViewById(R.id.txt_about_provider);
        /*layout_contact = v.findViewById(R.id.layout_contact);
        txt_number = v.findViewById(R.id.txt_number);
        txt_mail = v.findViewById(R.id.txt_mail);*/
    }

    private void setdata() {
        txt_about_provider.setText(Utils.decodeEmoji(serviceDetailData.getAboutMe()));
        /*txt_number.setText(serviceDetailData.getCountryCode() + " "
                + serviceDetailData.getContactNumber());
        txt_mail.setText(serviceDetailData.getEmail());*/
    }
}
