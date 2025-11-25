package com.app.bemyrider.fragment.partner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.R;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

/**
 * Created by nct121 on 12/12/16.
 */

public class Fragment_Partner_ServiceDetail extends Fragment {
    private TextView txt_servicename,txtServiceDays, txtServiceTime, txt_serviceprice, txt_servicebooking,txt_service_description,txtDeliveryType;
    private ProviderServiceDetailsItem serviceDetailData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.partner_servicedetail_fragment, container, false);

        serviceDetailData = (ProviderServiceDetailsItem) getArguments().getSerializable("data");

        initView(view);

        txt_servicename.setText(serviceDetailData.getCategoryName());
        txt_serviceprice.setText(String.format("%s%s", PrefsUtil.with(getActivity()).readString("CurrencySign"), serviceDetailData.getPrice()));
        txt_service_description.setText(Utils.decodeEmoji(serviceDetailData.getDescription()));
        if (serviceDetailData.getTotalService().equals("")) {
            txt_servicebooking.setText("0");
        } else {
            txt_servicebooking.setText(serviceDetailData.getTotalService());
        }


        if (serviceDetailData.getAvailableDaysList() != null && !serviceDetailData.getAvailableDaysList().equals("")) {
            txtServiceDays.setText(serviceDetailData.getAvailableDaysList());
        } else {
            txtServiceDays.setText("-");
        }

        if (serviceDetailData.getAvailableTimeStart() != null

                && serviceDetailData.getAvailableTimeStart().length() > 0
                && serviceDetailData.getAvailableTimeEnd() != null
                && serviceDetailData.getAvailableTimeEnd().length() > 0) {
            txtServiceTime.setText(String.format("%s - %s", serviceDetailData.getAvailableTimeStart(), serviceDetailData.getAvailableTimeEnd()));
        } else {
            txtServiceTime.setText("-");
        }


        String strRequestType = "";

        String strDeliveryType = "";

        if (serviceDetailData.getSmallDelivery().equals("y")) {
            strDeliveryType = getResources().getString(R.string.small);
        }

        if (serviceDetailData.getMediumDelivery().equals("y")) {
            if (strDeliveryType.equals(""))
                strDeliveryType = getResources().getString(R.string.medium);
            else
                strDeliveryType = strDeliveryType + " , " + getResources().getString(R.string.medium);
        }

        if (serviceDetailData.getLargeDelivery().equals("y")) {
            if (strDeliveryType.equals(""))
                strDeliveryType = getResources().getString(R.string.large);
            else
                strDeliveryType = strDeliveryType + " , " + getResources().getString(R.string.large);
        }

        if (serviceDetailData.getRequestType().equals("quick"))
        {
            strRequestType = getString(R.string.quick);
        } else if (serviceDetailData.getRequestType().equals("scheduled")) {
            strRequestType = getString(R.string.scheduled);
        }

        txtDeliveryType.setText(strDeliveryType);
        //txtRequestType.setText(strRequestType);

        return view;

    }

    private void initView(View view) {

        txt_servicename = view.findViewById(R.id.txt_servicename);
        txt_serviceprice = view.findViewById(R.id.txt_serviceprice);
        txt_servicebooking = view.findViewById(R.id.txt_servicebooking);
        txt_service_description = view.findViewById(R.id.txt_service_description);
        txtDeliveryType = view.findViewById(R.id.txt_delivery_type);
        txtServiceDays = view.findViewById(R.id.txt_serviceDays);
        txtServiceTime = view.findViewById(R.id.txt_serviceTime);


    }
}
