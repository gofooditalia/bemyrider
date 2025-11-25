package com.app.bemyrider.fragment.partner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.app.bemyrider.R;
import com.app.bemyrider.databinding.PartnerServicerequestDetailBinding;
import com.app.bemyrider.model.ExtendServiceListPojoItem;
import com.app.bemyrider.model.ProposalServiceDataItem;
import com.app.bemyrider.model.ProviderHistoryPojoItem;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by nct121 on 12/12/16.
 * Modified by Hardik Talaviya on 5/12/19.
 */

public class Fragment_ServiceRequest_ServiceDetail extends Fragment {

    ////Service request detail tab for completed  ---> Partner_ServiceRequestDetail_Tablayout_Activity

    private static final String TAG = "Fragment_ServiceRequest";
    private List<ProposalServiceDataItem> proposalServiceDataItems = new ArrayList<>();
    private List<ExtendServiceListPojoItem> extendServiceListPojoItems = new ArrayList<>();
    private PartnerServicerequestDetailBinding binding;
    private Context context;
    private ProviderHistoryPojoItem providerHistoryPojoItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.partner_servicerequest_detail, container, false);

        providerHistoryPojoItem = (ProviderHistoryPojoItem) getArguments().getSerializable("data");

        initView();

        proposalServiceDataItems.clear();
        proposalServiceDataItems.addAll(providerHistoryPojoItem.getProposalServiceData());
        extendServiceListPojoItems.clear();
        extendServiceListPojoItems.addAll(providerHistoryPojoItem.getExtendServiceData());

        binding.layoutPropoButtonP.setVisibility(View.GONE);

        if (proposalServiceDataItems.size() > 0) {
            if (providerHistoryPojoItem.getServiceStatus().equals("pending")) {
                binding.layoutMessageProposalP.setVisibility(View.VISIBLE);
            } else {
                binding.layoutMessageProposalP.setVisibility(View.GONE);
            }
            if (proposalServiceDataItems.size() == 1) {
                binding.layoutProposalFirstMessageP.setVisibility(View.VISIBLE);
                binding.txtFmessageHoursP.setText(proposalServiceDataItems.get(0).getHours());
                binding.txtFmessageP.setText(Utils.decodeEmoji(proposalServiceDataItems.get(0).getMessage()));
                if (proposalServiceDataItems.get(0).getStatus().equals("pending")) {
                    binding.txtFmessageStatusP.setBackgroundResource(R.color.status_pending);
                    binding.txtFmessageStatusP.setText(getString(R.string.status_pending));
                } else if (proposalServiceDataItems.get(0).getStatus().equals("accepted")) {
                    binding.txtFmessageStatusP.setBackgroundResource(R.color.status_accepted);
                    binding.txtFmessageStatusP.setText(getString(R.string.status_accepted));
                } else {
                    binding.txtFmessageStatusP.setBackgroundResource(R.color.status_rejected);
                    binding.txtFmessageStatusP.setText(getString(R.string.status_rejected));
                }
            } else {
                binding.layoutProposalSecondMessageP.setVisibility(View.GONE);
            }

            if (proposalServiceDataItems.size() == 2) {
                binding.layoutProposalFirstMessageP.setVisibility(View.VISIBLE);
                binding.txtFmessageHoursP.setText(proposalServiceDataItems.get(0).getHours());
                binding.txtFmessageP.setText(Utils.decodeEmoji(proposalServiceDataItems.get(0).getMessage()));

                if (proposalServiceDataItems.get(0).getStatus().equals("pending")) {
                    binding.txtFmessageStatusP.setBackgroundResource(R.color.status_pending);
                    binding.txtFmessageStatusP.setText(getString(R.string.status_pending));
                } else if (proposalServiceDataItems.get(0).getStatus().equals("rejected")) {
                    binding.txtFmessageStatusP.setBackgroundResource(R.color.status_rejected);
                    binding.txtFmessageStatusP.setText(getString(R.string.status_rejected));
                } else {
                    binding.txtFmessageStatusP.setBackgroundResource(R.color.status_accepted);
                    binding.txtFmessageStatusP.setText(getString(R.string.status_accepted));
                }

                binding.layoutProposalSecondMessageP.setVisibility(View.VISIBLE);
                binding.txtLmessageHoursP.setText(proposalServiceDataItems.get(1).getHours());
                binding.txtLmessageP.setText(Utils.decodeEmoji(proposalServiceDataItems.get(1).getMessage()));
                binding.txtLmessageStatusP.setText(proposalServiceDataItems.get(1).getStatus());

                if (proposalServiceDataItems.get(1).getStatus().equals("accepted")) {
                    binding.txtLmessageStatusP.setBackgroundResource(R.color.status_accepted);
                    binding.txtLmessageStatusP.setText(getString(R.string.status_accepted));
                } else if (proposalServiceDataItems.get(1).getStatus().equals("pending")) {
                    binding.txtLmessageStatusP.setBackgroundResource(R.color.status_pending);
                    binding.txtLmessageStatusP.setText(getString(R.string.status_pending));
                } else {
                    binding.txtLmessageStatusP.setBackgroundResource(R.color.status_rejected);
                    binding.txtLmessageStatusP.setText(getString(R.string.status_rejected));
                }
            }
        }

        if (extendServiceListPojoItems.size() > 0) {
            binding.layoutExtendMessageP.setVisibility(View.VISIBLE);
            ExtendServiceListPojoItem extendedItem = extendServiceListPojoItems.get(0);
            binding.txtExtendDurationP.setText(HtmlCompat.fromHtml(extendedItem.getBookingStartTime()
                    + "<br/>" + extendedItem.getBookingEndTime(),HtmlCompat.FROM_HTML_MODE_LEGACY));
            binding.txtExtendHoursP.setText(extendedItem.getExtendHours());
            binding.txtExtendCostP.setText(extendedItem.getBookingAmt());
            if (extendedItem.getExtendStatus().equals("pending")) {
                binding.txtExtendStatusP.setText(getString(R.string.status_pending));
                binding.txtExtendStatusP.setBackgroundResource(R.color.status_pending);
            } else if (extendedItem.getExtendStatus().equals("accepted")) {
                binding.layoutExtendButton.setVisibility(View.GONE);
                binding.txtExtendStatusP.setText(getString(R.string.status_accepted));
                binding.txtExtendStatusP.setBackgroundResource(R.color.status_accepted);
            } else if (extendedItem.getExtendStatus().equals("rejected")) {
                binding.txtExtendStatusP.setText(getString(R.string.status_rejected));
                binding.txtExtendStatusP.setBackgroundResource(R.color.status_rejected);
            } else {
                binding.layoutExtendButton.setVisibility(View.GONE);
                binding.txtExtendStatusP.setText(getString(R.string.paid));
                binding.txtExtendStatusP.setBackgroundResource(R.color.status_accepted);
            }
        }

        String strDeliveryType = "";
        String strRequestType = "";
        if (providerHistoryPojoItem.getDeliveryType().equals("small")) {
            strDeliveryType = getString(R.string.small);
        } else if (providerHistoryPojoItem.getDeliveryType().equals("medium")) {
            strDeliveryType = getString(R.string.medium);
        } else if (providerHistoryPojoItem.getDeliveryType().equals("large")) {
            strDeliveryType = getString(R.string.large);
        }

        if (providerHistoryPojoItem.getRequestType().equals("quick"))
        {
            strRequestType = getString(R.string.quick);
        } else if (providerHistoryPojoItem.getRequestType().equals("scheduled")) {
            strRequestType = getString(R.string.scheduled);
        }

        binding.txtDeliveryType.setText(strDeliveryType);

        if (providerHistoryPojoItem.getAvailableDaysList() != null && !providerHistoryPojoItem.getAvailableDaysList().equals("")) {
            binding.txtServiceDays.setText(providerHistoryPojoItem.getAvailableDaysList());
        } else {
            binding.txtServiceDays.setText("-");
        }

        if (providerHistoryPojoItem.getAvailableTimeStart() != null

                && providerHistoryPojoItem.getAvailableTimeStart().length() > 0
                && providerHistoryPojoItem.getAvailableTimeEnd() != null
                && providerHistoryPojoItem.getAvailableTimeEnd().length() > 0) {
//            binding.txtServiceTime.setText(String.format("%s - %s", providerHistoryPojoItem.getAvailableTimeStart(), providerHistoryPojoItem.getAvailableTimeEnd()));
        } else {
//            binding.txtServiceTime.setText("-");
        }
       // binding.txtRequestType.setText(strRequestType);

        binding.TxtServiceName.setText(providerHistoryPojoItem.getCategoryName());
        if (providerHistoryPojoItem.getServiceType().equalsIgnoreCase("hourly")) {
            binding.TxtServicePrice.setText(String.format("%s%s (%s%s/hour)", PrefsUtil.with(context).readString("CurrencySign"),
                    providerHistoryPojoItem.getBookingAmount(), PrefsUtil.with(context).readString("CurrencySign"),
                    providerHistoryPojoItem.getServicePrice()));
        } else {
            binding.TxtServicePrice.setText(String.format("%s%s", PrefsUtil.with(context).readString("CurrencySign"),
                    providerHistoryPojoItem.getBookingAmount()));
        }
        binding.TxtServiceTime.setText(providerHistoryPojoItem.getBookingStartTime());
        binding.TxtEndServiceTime.setText(providerHistoryPojoItem.getBookingEndTime());
        binding.TxtServiceAddress.setText(providerHistoryPojoItem.getBookingAddress());
       // binding.TxtProfileNumber.setText(providerHistoryPojoItem.getCustomerContactNumber());
       // binding.TxtProfileMail.setText(providerHistoryPojoItem.getCustomerEmail());
        binding.TxtServiceDesc.setText(Utils.decodeEmoji(providerHistoryPojoItem.getDescription()));
        binding.TxtAdminFeesP.setText(String.format("%s%s", PrefsUtil.with(context).readString("CurrencySign"),
                providerHistoryPojoItem.getProviderCommissionAmount()));
        binding.TxtDesc.setText(Utils.decodeEmoji(providerHistoryPojoItem.getBookingDetails()));

        binding.layoutBookedTime.setVisibility(View.GONE);

        if (providerHistoryPojoItem.getPaymentMode().equalsIgnoreCase("")) {
            binding.TxtPaymentpreference.setText(getString(R.string.none));
        }
        if (providerHistoryPojoItem.getPaymentMode().equalsIgnoreCase("wallet")) {
            binding.TxtPaymentpreference.setText(getString(R.string.wallet));
        }
        if (providerHistoryPojoItem.getPaymentMode().equalsIgnoreCase("cash")) {
            binding.TxtPaymentpreference.setText(getString(R.string.cash));
        }

        if (providerHistoryPojoItem.getIsActive().equalsIgnoreCase("du")) {
            //binding.layoutCustomerdetials.setVisibility(View.GONE);
            binding.llAddress.setVisibility(View.GONE);
        }

        binding.imgMapProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://maps.google.com/maps?daddr=" + providerHistoryPojoItem.getServiceLatitude() + "," +
                        providerHistoryPojoItem.getServiceLongitude();
                Log.e(TAG, "onClick Google Map Url :: " + url);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse(url));
                startActivity(intent);
            }
        });
        return binding.getRoot();
    }

    private void initView() {
        context = getActivity();
    }
}
