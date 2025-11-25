package com.app.bemyrider.fragment.user;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;

import com.app.bemyrider.activity.user.CustomerHomeActivity;
import com.app.bemyrider.databinding.FragmentBookedDetailBinding;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.ExtendServiceListPojoItem;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 12/12/19.
 */

public class BookedDetailFragment extends Fragment {

    private static final String TAG = "BookedDetailFragment";
    private FragmentBookedDetailBinding binding;
    private ProviderServiceDetailsItem serviceDetailData;
    /*private String[] hours_array = {"Select Hours*", "1 Hour", "2 Hours", "3 Hours", "4 Hours",
            "5 Hours", "6 Hours", "7 Hours", "8 Hours", "9 Hours", "10 Hours", "11 Hours",
            "12 Hours", "13 Hours", "14 Hours", "15 Hours", "16 Hours", "17 Hours", "18 Hours",
            "19 Hours", "20 Hours", "21 Hours", "22 Hours", "23 Hours", "24 Hours"};*/
    private AsyncTask acceptProposalAsync, sendProposalAsync;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_booked_detail, container, false);

        serviceDetailData = (ProviderServiceDetailsItem) getArguments().getSerializable("data");

        init();

        if (serviceDetailData.getPaymentPreference().equals("wallet")) {
            binding.txtPaymentMethod.setText(getString(R.string.wallet));
            //  binding.layoutCustomerAmount.setVisibility(View.VISIBLE);
        } else {
            binding.txtPaymentMethod.setText(getString(R.string.cash));
        }

        binding.txtServiceEndTime.setText(serviceDetailData.getEndTime());
        binding.txtStartServiceTime.setText(serviceDetailData.getStartTime());

        binding.txtServiceAddress.setText(serviceDetailData.getServiceAddress());

        setdata();

        binding.imgMap.setOnClickListener(view1 -> {
            String url = "http://maps.google.com/maps?daddr=" + serviceDetailData.getServiceLatitude() + "," + serviceDetailData.getServiceLongitude();
            Log.e(TAG, "onClick Google Map Url :: " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(intent);
        });

        binding.btnAcceptProposal.setOnClickListener(view12 -> serviceCallAcceptProposal(true));

        binding.btnRejectProposal.setOnClickListener(view13 -> {
//            showSendProposalDialog();
            serviceCallAcceptProposal(false);
        });

        return binding.getRoot();
    }

    /*--------------- Send Proposal Dialog -------------------*/
    private void showSendProposalDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.hourly_message_dialog);
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.send_proposal));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(dialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);

        final Spinner spinner = dialog.findViewById(R.id.spinner_select_proposal_hours);
        final EditText editText = dialog.findViewById(R.id.edt_peoposal_msg);

        Button cancel = dialog.findViewById(R.id.btn_proposal_cancle);
        Button send = dialog.findViewById(R.id.btn_proposal_send);

//        ArrayAdapter hoursAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, hours_array);
//        spinner.setAdapter(hoursAdapter);
//        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        cancel.setOnClickListener(v -> dialog.dismiss());

        send.setOnClickListener(v -> {
            if (spinner.getSelectedItemPosition() != 0) {
                if (!editText.getText().toString().trim().equals("")) {
                    String tmp = String.valueOf(spinner.getSelectedItemPosition());
//                    String hour[] = tmp.split(" ");

                    Utils.hideSoftKeyboard((Activity) context);
                    send.setClickable(false);
                    cancel.setClickable(false);
                    serviceCallSendProposal(tmp, editText.getText().toString().trim(), dialog);

                    /*serviceCallSendProposal(spinner.getSelectedItemPosition() + " "
                                    + getString(R.string.hours), editText.getText().toString().trim(),
                            dialog);*/

                } else {
                    editText.setError(getString(R.string.please_enter_message));
                }
            } else {
                Toast.makeText(context, getString(R.string.please_select_hours_first),
                        Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    /*------------------- Accept Proposal Api Call -----------------------*/
    private void serviceCallAcceptProposal(boolean isAccept) {
        binding.btnAcceptProposal.setClickable(false);
        binding.btnRejectProposal.setClickable(false);
        if (isAccept) {
            binding.pgAcceptP.setVisibility(View.VISIBLE);
        } else {
            binding.pgRejectP.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("status_type", isAccept ? "accepted" : "rejected");
        textParams.put("proposal_id", serviceDetailData.getProposalServiceData().get(0).getId());
        textParams.put("user_id", PrefsUtil.with(context).readString("UserId"));

        new WebServiceCall(context, WebServiceUrl.URL_ACCEPT_PROPOSAL, textParams,
                CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (isAccept) {
                    binding.pgAcceptP.setVisibility(View.GONE);
                } else {
                    binding.pgRejectP.setVisibility(View.GONE);
                }
                binding.btnAcceptProposal.setClickable(true);
                binding.btnRejectProposal.setClickable(true);
                if (status) {
                    if (isAccept) {
                        PrefsUtil.with(context).write("service", "true");
                        Intent intent = new Intent(context, CustomerHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        ((Activity) context).finish();
                    } else {
                        PrefsUtil.with(context).write("service", "true");
                        Intent intent = new Intent(context, CustomerHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("service", true);
                        startActivity(intent);
                        ((Activity) context).finish();
                    }

                    /*Intent intent = new Intent(context, ServiceHistoryActivity.class);
                    startActivity(intent);
                    ((Activity) context).finish();*/
                } else {
                    Toast.makeText(context, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                acceptProposalAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                acceptProposalAsync = null;
            }
        });
    }

    /*----------------- Send Proposal Api Call -------------------*/
    private void serviceCallSendProposal(String selectedHours, String message, final Dialog dialog) {
        (dialog.findViewById(R.id.pgDialogSendApproval)).setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("sel_message_hour", selectedHours);
        textParams.put("txt_message", Utils.encodeEmoji(message));
        textParams.put("txt_proposal_id", serviceDetailData.getProposalServiceData().get(0).getId());
        textParams.put("user_id", PrefsUtil.with(context).readString("UserId"));

        new WebServiceCall(context, WebServiceUrl.URL_SEND_PRAPOSAL, textParams,
                CommonPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                (dialog.findViewById(R.id.pgDialogSendApproval)).setVisibility(View.GONE);
                (dialog.findViewById(R.id.btn_proposal_send)).setClickable(true);
                (dialog.findViewById(R.id.btn_proposal_cancle)).setClickable(true);
                if (status) {
                    dialog.dismiss();
                    PrefsUtil.with(context).write("service", "true");
                    Intent intent = new Intent(context, CustomerHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("service", true);
                    startActivity(intent);
                    ((Activity) context).finish();
                   /* Intent intent = new Intent(context, ServiceHistoryActivity.class);
                    startActivity(intent);
                    ((Activity) context).finish();*/
                } else {
                    Toast.makeText(context, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                sendProposalAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                sendProposalAsync = null;
            }
        });
    }

    private void init() {
        context = getActivity();
    }

    private void setdata() {
        String strDeliveryType = "";
        String strRequestType = "";
        if (serviceDetailData.getDeliveryType().equals("small")) {
            strDeliveryType = getString(R.string.small);
        } else if (serviceDetailData.getDeliveryType().equals("medium")) {
            strDeliveryType = getString(R.string.medium);
        } else if (serviceDetailData.getDeliveryType().equals("large")) {
            strDeliveryType = getString(R.string.large);
        }

        if (serviceDetailData.getRequestType().equals("quick")) {
            strRequestType = getString(R.string.quick);
        } else if (serviceDetailData.getRequestType().equals("scheduled")) {
            strRequestType = getString(R.string.scheduled);
        }

        if (serviceDetailData.getAvailableDaysList() != null && !serviceDetailData.getAvailableDaysList().equals("")) {
            binding.txtServiceDays.setText(serviceDetailData.getAvailableDaysList());
        } else {
            binding.txtServiceDays.setText("-");
        }

        if (serviceDetailData.getAvailableTimeStart() != null

                && serviceDetailData.getAvailableTimeStart().length() > 0
                && serviceDetailData.getAvailableTimeEnd() != null
                && serviceDetailData.getAvailableTimeEnd().length() > 0) {
            binding.txtServiceTime.setText(String.format("%s - %s", serviceDetailData.getAvailableTimeStart(), serviceDetailData.getAvailableTimeEnd()));
        } else {
            binding.txtServiceTime.setText("-");
        }


        binding.txtDeliveryType.setText(strDeliveryType);
        // binding.txtRequestType.setText(strRequestType);
        binding.txtServiceName.setText(serviceDetailData.getCategoryName());
        binding.txtServiceFees.setText(serviceDetailData.getTotalFees());
        binding.txtServiceDesc.setText(Utils.decodeEmoji(serviceDetailData.getDescription()));
        binding.txtBookingDesc.setText(Utils.decodeEmoji(serviceDetailData.getServiceDescription()));
        binding.txtCustomerCommissionAmount.setText(String.format("%s%s", PrefsUtil.with(context)
                .readString("CurrencySign"), serviceDetailData.getAdminFees()));

        if (serviceDetailData.getPaymentPreference().equals("wallet")) {
            binding.txtPaymentMethod.setText(getString(R.string.wallet));
            //binding.layoutCustomerCommission.setVisibility(View.VISIBLE);
        } else {
            binding.txtPaymentMethod.setText(getString(R.string.cash));
            //binding.layoutCustomerCommission.setVisibility(View.GONE);
        }

        if (serviceDetailData.getServiceMasterType().equals("fixed")) {
            binding.layoutServiceHours.setVisibility(View.VISIBLE);

            String serviceHours = serviceDetailData.getProviderServiceHours();
            if (serviceHours.equals("1")) {
                binding.txtServiceHours.setText(String.format("%s %s", serviceDetailData.getProviderServiceHours(),
                        getString(R.string.lbl_hour)));
            } else {
                binding.txtServiceHours.setText(String.format("%s %s", serviceDetailData.getProviderServiceHours(),
                        getString(R.string.lbl_hours)));
            }

            binding.txtServicePrice.setText(String.format("%s%s", PrefsUtil.with(context)
                    .readString("CurrencySign"), serviceDetailData.getPrice()));

            String taskHour = serviceDetailData.getHours();
            if (taskHour.equals("1")) {
                binding.txtTaskHours.setText(taskHour + " " + getString(R.string.lbl_hour));
            } else {
                binding.txtTaskHours.setText(taskHour + " " + getString(R.string.lbl_hours));
            }

        } else {
            binding.layoutServiceHours.setVisibility(View.GONE);
            binding.txtServicePrice.setText(String.format("%s%s (%s%s%s)", PrefsUtil.with(context).readString("CurrencySign"), serviceDetailData.getBookingAmt(), PrefsUtil.with(context).readString("CurrencySign"), serviceDetailData.getPrice(), getString(R.string.per_hours)));

            String taskHour = serviceDetailData.getBookingHours();
//            Toast.makeText(getActivity(), "test:" + taskHour, Toast.LENGTH_SHORT).show();
            if (taskHour.equals("1")) {
                binding.txtTaskHours.setText(taskHour + " " + getString(R.string.lbl_hour));
            } else {
                binding.txtTaskHours.setText(taskHour + " " + getString(R.string.lbl_hours));
            }
//            binding.txtTaskHours.setText(String.format("%s %s", serviceDetailData.getBookingHours(), getString(R.string.hours)));
        }

        binding.txtCustomerCommissionCommission.setText(String.format("%s%%", serviceDetailData.getCustomerCommission()));
        Log.e(TAG, "setData Price :: " + serviceDetailData.getPrice());

        if (serviceDetailData.getProposalServiceData().size() > 0) {
            if (serviceDetailData.getServiceStatus().equalsIgnoreCase("pending")) {
                binding.layoutMessageProposal.setVisibility(View.VISIBLE);
            } else {
                binding.layoutMessageProposal.setVisibility(View.GONE);
            }
            if (serviceDetailData.getProposalServiceData().size() == 1) {
                binding.layoutProposalFirstMessage.setVisibility(View.VISIBLE);
                binding.txtFmessageHours.setText(serviceDetailData.getProposalServiceData().get(0).getHours());
                binding.txtFmessage.setText(Utils.decodeEmoji(serviceDetailData.getProposalServiceData().get(0).getMessage()));
                if (serviceDetailData.getProposalServiceData().get(0).getStatus().equals("pending")) {
                    binding.txtFmessageStatus.setBackgroundResource(R.color.status_pending);
                    binding.txtFmessageStatus.setText(getString(R.string.status_pending));
                } else {
                    binding.txtFmessageStatus.setBackgroundResource(R.color.status_accepted);
                    binding.txtFmessageStatus.setText(getString(R.string.status_accepted));
                }

                if (serviceDetailData.getProposalServiceData().get(0).getStatus().equals("pending")) {
                    binding.layoutPropoButton.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutPropoButton.setVisibility(View.GONE);
                }
            } else if (serviceDetailData.getProposalServiceData().size() == 2) {
                binding.layoutProposalFirstMessage.setVisibility(View.VISIBLE);
                binding.txtFmessageHours.setText(serviceDetailData.getProposalServiceData().get(0).getHours());
                binding.txtFmessage.setText(Utils.decodeEmoji(serviceDetailData.getProposalServiceData().get(0).getMessage()));

                if (serviceDetailData.getProposalServiceData().get(0).getStatus().equals("pending")) {
                    binding.txtFmessageStatus.setBackgroundResource(R.color.status_pending);
                    binding.txtFmessageStatus.setText(getString(R.string.status_pending));
                } else if (serviceDetailData.getProposalServiceData().get(0).getStatus().equals("rejected")) {
                    binding.txtFmessageStatus.setBackgroundResource(R.color.status_rejected);
                    binding.txtFmessageStatus.setText(getString(R.string.status_rejected));
                } else {
                    binding.txtFmessageStatus.setBackgroundResource(R.color.status_accepted);
                    binding.txtFmessageStatus.setText(getString(R.string.status_accepted));
                }

                binding.layoutProposalSecondMessage.setVisibility(View.VISIBLE);
                binding.txtLmessageHours.setText(serviceDetailData.getProposalServiceData().get(1).getHours());
                binding.txtLmessage.setText(Utils.decodeEmoji(serviceDetailData.getProposalServiceData().get(1).getMessage()));
                binding.txtLmessageStatus.setText(serviceDetailData.getProposalServiceData().get(1).getStatus());


                if (serviceDetailData.getProposalServiceData().get(1).getStatus().equals("accepted")) {
                    binding.txtLmessageStatus.setBackgroundResource(R.color.status_accepted);
                    binding.txtLmessageStatus.setText(getString(R.string.status_accepted));
                    binding.layoutPropoButton.setVisibility(View.GONE);
                } else if (serviceDetailData.getProposalServiceData().get(1).getStatus().equals("pending")) {
                    binding.txtLmessageStatus.setBackgroundResource(R.color.status_pending);
                    binding.txtLmessageStatus.setText(getString(R.string.status_pending));
                } else {
                    binding.txtLmessageStatus.setBackgroundResource(R.color.status_rejected);
                    binding.txtLmessageStatus.setText(getString(R.string.status_rejected));
                    binding.layoutPropoButton.setVisibility(View.GONE);
                }

            } else {
                binding.layoutProposalSecondMessage.setVisibility(View.GONE);
            }
        }

        try {
            if (serviceDetailData.getExtendServiceData() != null) {
                if (serviceDetailData.getExtendServiceData().size() > 0) {
                    ExtendServiceListPojoItem extendedItem = serviceDetailData.getExtendServiceData().get(0);
                    binding.layoutExtendMessage.setVisibility(View.VISIBLE);
                    binding.txtExtendDuration.setText(HtmlCompat.fromHtml(extendedItem.getBookingStartTime() + " to <br/>" + extendedItem.getBookingEndTime(),HtmlCompat.FROM_HTML_MODE_LEGACY));
                    binding.txtExtendHours.setText(extendedItem.getExtendHours());
                    binding.txtExtendCost.setText(extendedItem.getBookingAmt());

                    if (extendedItem.getServiceStatus().equals("pending")) {
                        binding.txtExtendStatus.setText(getString(R.string.status_pending));
                        binding.txtExtendStatus.setBackgroundResource(R.color.status_pending);
                    } else if (extendedItem.getServiceStatus().equals("accepted")) {
                        binding.txtExtendStatus.setText(getString(R.string.status_accepted));
                        binding.txtExtendStatus.setBackgroundResource(R.color.status_accepted);
                    } else if (extendedItem.getServiceStatus().equals("rejected")) {
                        binding.txtExtendStatus.setText(getString(R.string.status_rejected));
                        binding.txtExtendStatus.setBackgroundResource(R.color.status_rejected);
                    } else {
                        binding.txtExtendStatus.setText(R.string.paid);
                        binding.txtExtendStatus.setBackgroundResource(R.color.status_accepted);
                    }

                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        binding.llAddress.setVisibility(View.VISIBLE);
        binding.llBookingDescription.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        Utils.cancelAsyncTask(acceptProposalAsync);
        Utils.cancelAsyncTask(sendProposalAsync);
        super.onDestroy();
    }
}
