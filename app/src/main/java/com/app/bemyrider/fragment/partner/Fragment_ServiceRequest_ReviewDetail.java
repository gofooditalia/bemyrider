package com.app.bemyrider.fragment.partner;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.PartnerServicerequestReviewBinding;
import com.app.bemyrider.model.ProviderHistoryPojoItem;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;


/**
 * Created by nct121 on 12/12/16.
 * Modified by Hardik Talaviya on 5/12/19.
 */

public class Fragment_ServiceRequest_ReviewDetail extends Fragment {

    //Service request review tab for completed  ---> Partner_ServiceRequestDetail_Tablayout_Activity

    private PartnerServicerequestReviewBinding binding;
    private Context context;
    private ProviderHistoryPojoItem providerHistoryPojoItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        binding = DataBindingUtil.inflate(inflater, R.layout.partner_servicerequest_review, container, false);

        providerHistoryPojoItem = (ProviderHistoryPojoItem) getArguments().getSerializable("data");

        initView();

        binding.TxtCompletedServiceName.setText(providerHistoryPojoItem.getCategoryName());
        binding.TxtCompletedServicePrice.setText(String.format("%s%s", PrefsUtil.with(context).readString("CurrencySign"),
                providerHistoryPojoItem.getServicePrice()));

        if (providerHistoryPojoItem.getPaymentMode().equalsIgnoreCase("")) {
            binding.TxtCompletedPaymentpref.setText(getString(R.string.none));
        } else if (providerHistoryPojoItem.getPaymentMode().equalsIgnoreCase("w")) {
            binding.TxtCompletedPaymentpref.setText(getString(R.string.wallet));
        } else if (providerHistoryPojoItem.getPaymentMode().equalsIgnoreCase("c")) {
            binding.TxtCompletedPaymentpref.setText(getString(R.string.cash));
        }else{
            binding.TxtCompletedPaymentpref.setText(providerHistoryPojoItem.getPaymentMode());
        }

        if (providerHistoryPojoItem.getReview().equalsIgnoreCase("")) {
            binding.TxtCompletedReviewMsg.setText(R.string.no_reviews);
        } else if (!providerHistoryPojoItem.getReview().equalsIgnoreCase("")) {
            binding.TxtCompletedReviewMsg.setText(Utils.decodeEmoji(providerHistoryPojoItem.getReview()));
        }

        if (String.valueOf(providerHistoryPojoItem.getRating()).equalsIgnoreCase("")) {
            binding.TxtCompletedRatting.setText("0");
        } else if (!String.valueOf(providerHistoryPojoItem.getRating()).equalsIgnoreCase("")) {
            binding.TxtCompletedRatting.setText(String.valueOf(providerHistoryPojoItem.getRating()));
        }

        return binding.getRoot();
    }

    private void initView() {
        context = getActivity();
    }
}
