package com.app.bemyrider.activity.partner;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.activity.MyStripeConnectActivity;
import com.app.bemyrider.activity.user.MessageDetailActivity;
import com.app.bemyrider.activity.user.RaiseDisputeActivity;
import com.app.bemyrider.activity.user.UserProfileActivity;
import com.app.bemyrider.databinding.PartnerActivityServiceRequestDetailsBinding;
import com.app.bemyrider.model.CheckStripeConnectedPojo;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.ExtendServiceListPojoItem;
import com.app.bemyrider.model.ProposalServiceDataItem;
import com.app.bemyrider.model.ProviderHistoryPojoItem;
import com.app.bemyrider.model.ProviderServiceRequestPojo;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;
// Coil Imports
import coil.Coil;
import coil.request.ImageRequest;
// import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class PartnerServiceRequestDetailsActivity extends AppCompatActivity {

    private static final String TAG = "Partner_ServiceRequestD";
    private final List<ProposalServiceDataItem> proposalServiceDataItems = new ArrayList<>();
    private final List<ExtendServiceListPojoItem> extendServiceListPojoItems = new ArrayList<>();
    private PartnerActivityServiceRequestDetailsBinding binding;
    private ProviderHistoryPojoItem serviceDetailData;
    private String serviceRequestId = "";

    private final Context mContext = PartnerServiceRequestDetailsActivity.this;
    private WebServiceCall actionProposalAsync, acceptExtendAsync, actionRequestAsync, sendProposalAsync,
            cancelServiceAsync, serviceActionAsync, serviceDetailAsync;
    private String providerId = "";

    // Il metodo ConnectionManager.registerInternetCheckReceiver non viene annullato correttamente
    // in onDestroy se connectionManager è null o la unregister throwa un'eccezione
    private ConnectionManager connectionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(PartnerServiceRequestDetailsActivity.this, R.layout.partner_activity_service_request_details, null);

        initView();

        serviceCallGetDetail();

        binding.layoutProfile.setOnClickListener(v -> {
            if (!(serviceDetailData.getIsActive().equalsIgnoreCase("du"))) {
                Intent intent = new Intent(PartnerServiceRequestDetailsActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", serviceDetailData.getCustomerId());
                startActivity(intent);
            }
        });

        binding.layoutName.setOnClickListener(v -> {
            if (!(serviceDetailData.getIsActive().equalsIgnoreCase("du"))) {
                Intent intent = new Intent(PartnerServiceRequestDetailsActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", serviceDetailData.getCustomerId());
                startActivity(intent);
            }
        });

        binding.includeRequestDetail.btnAcceptExtendP.setOnClickListener(view -> {
            binding.includeRequestDetail.btnAcceptExtendP.setClickable(false);
            binding.includeRequestDetail.btnRejectExtendP.setClickable(false);
            serviceCallAcceptExtendRequest("accepted");
        });

        binding.includeRequestDetail.btnRejectExtendP.setOnClickListener(view -> {
            binding.includeRequestDetail.btnAcceptExtendP.setClickable(false);
            binding.includeRequestDetail.btnRejectExtendP.setClickable(false);
            serviceCallAcceptExtendRequest("rejected");
        });

        binding.btnAccept.setOnClickListener(view -> {
            binding.btnSendProposal.setClickable(false);
            binding.btnAccept.setClickable(false);
            binding.btnReject.setClickable(false);
            binding.includeRequestDetail.btnRejectProposalP.setClickable(false);
            binding.includeRequestDetail.btnAcceptProposalP.setClickable(false);
            serviceCallAcceptReject("accepted", null);
        });
        binding.btnReject.setOnClickListener(view -> {
            binding.btnSendProposal.setClickable(false);
            binding.btnAccept.setClickable(false);
            binding.btnReject.setClickable(false);
            binding.includeRequestDetail.btnRejectProposalP.setClickable(false);
            binding.includeRequestDetail.btnAcceptProposalP.setClickable(false);
            serviceCallAcceptReject("rejected", null);
        });

        binding.btnCancel.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(PartnerServiceRequestDetailsActivity.this);
            builder.setMessage(R.string.are_you_cancel)
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        binding.btnCancel.setClickable(false);
                        serviceCallCancelService();
                    })
                    .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();

        });

        binding.layoutSendMessage.setOnClickListener(v -> {
            if (serviceDetailData.getServiceStatus().equalsIgnoreCase("dispute")) {
                Intent intent = new Intent(PartnerServiceRequestDetailsActivity.this, Partner_DisputeDetail_Activity.class);
                intent.putExtra("DisputeId", serviceDetailData.getDisputeId());
                startActivity(intent);
            } else {
                Intent intent = new Intent(PartnerServiceRequestDetailsActivity.this, MessageDetailActivity.class);
                intent.putExtra("to_user", serviceDetailData.getCustomerId());
                intent.putExtra("master_id", serviceDetailData.getServiceId());
                intent.putExtra("service_booking_id", serviceDetailData.getServiceBookingId());
                startActivity(intent);
            }
        });

        binding.layoutRaiseDispute.setOnClickListener(view -> {
            Intent intent = new Intent(PartnerServiceRequestDetailsActivity.this, RaiseDisputeActivity.class);
            intent.putExtra("RequestID", serviceRequestId);
            startActivity(intent);
        });

        binding.btnSendProposal.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(PartnerServiceRequestDetailsActivity.this);
            dialog.setContentView(R.layout.hourly_message_dialog);
            dialog.setCancelable(false);
            dialog.setTitle(R.string.send_proposal);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            if (dialog.getWindow() != null) { 
                params.copyFrom(dialog.getWindow().getAttributes());
            }
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            if (dialog.getWindow() != null) { 
                dialog.getWindow().setAttributes(params);
            }

            Spinner spinner = dialog.findViewById(R.id.spinner_select_proposal_hours);
            EditText editText = dialog.findViewById(R.id.edt_peoposal_msg);

            Button cancel = dialog.findViewById(R.id.btn_proposal_cancle);
            Button send = dialog.findViewById(R.id.btn_proposal_send);

            cancel.setOnClickListener(v1 -> dialog.dismiss());

            send.setOnClickListener(v12 -> {
                if (spinner.getSelectedItemPosition() != 0) {
                    if (!editText.getText().toString().trim().isEmpty()) { 
                        String tmp = String.valueOf(spinner.getSelectedItemPosition());
                        String[] hour = tmp.split(" "); 
                        Utils.hideSoftKeyboard(PartnerServiceRequestDetailsActivity.this);
                        cancel.setClickable(false);
                        send.setClickable(false);
                        serviceCallSendPraposal(hour[0], editText.getText().toString().trim(), dialog);
                    } else {
                        editText.setError(getString(R.string.please_enter_message));
                    }
                } else {
                    Toast.makeText(PartnerServiceRequestDetailsActivity.this, R.string.please_select_hours_first, Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        });

        // Rimosso il blocco binding.txtShowHereProvider.setOnClickListener() - era già commentato

        binding.includeRequestDetail.imgMapProvider.setOnClickListener(view -> {
            String url = "http://maps.google.com/maps?daddr=" + serviceDetailData.getServiceLatitude() + "," + serviceDetailData.getServiceLongitude();
            Log.e(TAG, "onClick Google Map Url :: " + url);
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(intent);
        });

        binding.includeRequestDetail.btnAcceptProposalP.setOnClickListener(view -> {
            binding.includeRequestDetail.btnRejectProposalP.setClickable(false);
            binding.includeRequestDetail.btnAcceptProposalP.setClickable(false);
            binding.btnAccept.setClickable(false);
            binding.btnReject.setClickable(false);
            serviceCallAcceptRejectProposalData("accepted");
        });

        binding.includeRequestDetail.btnRejectProposalP.setOnClickListener(view -> {
            binding.includeRequestDetail.btnRejectProposalP.setClickable(false);
            binding.includeRequestDetail.btnAcceptProposalP.setClickable(false);
            binding.btnAccept.setClickable(false);
            binding.btnReject.setClickable(false);
            serviceCallAcceptRejectProposalData("rejected");
        });
    }

    private void serviceCallAcceptRejectProposalData(String ProposalStatus) {
        if (ProposalStatus.equalsIgnoreCase("accepted")) {
            binding.includeRequestDetail.pgAcceptP.setVisibility(View.VISIBLE);
        } else {
            binding.includeRequestDetail.pgRejectP.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("status_type", ProposalStatus);

        textParams.put("proposal_id", proposalServiceDataItems.get(proposalServiceDataItems.size() - 1).getProposalId());

        textParams.put("user_id", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("UserId"));

        new WebServiceCall(PartnerServiceRequestDetailsActivity.this,
                WebServiceUrl.URL_ACCEPT_PROPOSAL, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.includeRequestDetail.btnRejectProposalP.setClickable(true);
                        binding.includeRequestDetail.btnAcceptProposalP.setClickable(true);
                        binding.btnAccept.setClickable(true);
                        binding.btnReject.setClickable(true);
                        if (ProposalStatus.equalsIgnoreCase("accepted")) {
                            binding.includeRequestDetail.pgAcceptP.setVisibility(View.GONE);
                        } else {
                            binding.includeRequestDetail.pgRejectP.setVisibility(View.GONE);
                        }

                        if (status) {
                            Intent i = new Intent(mContext, ProviderHomeActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(PartnerServiceRequestDetailsActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(Object asyncTask) {
                        actionProposalAsync = null;
                    }

                    @Override
                    public void onCancelled() {
                        actionProposalAsync = null;
                    }
                });
    }

    private void serviceCallAcceptExtendRequest(String serviceStatus) {
        if (serviceStatus.equalsIgnoreCase("accepted")) {
            binding.includeRequestDetail.pgAcceptEs.setVisibility(View.VISIBLE);
        } else {
            binding.includeRequestDetail.pgRejectEs.setVisibility(View.VISIBLE);
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("status_type", serviceStatus);
        textParams.put("extend_id", extendServiceListPojoItems.get(0).getExtendId());

        new WebServiceCall(PartnerServiceRequestDetailsActivity.this,
                WebServiceUrl.URL_ACCEPT_EXTEND_REQUEST, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.includeRequestDetail.btnAcceptExtendP.setClickable(true);
                        binding.includeRequestDetail.btnRejectExtendP.setClickable(true);
                        if (serviceStatus.equalsIgnoreCase("accepted")) {
                            binding.includeRequestDetail.pgAcceptEs.setVisibility(View.GONE);
                        } else {
                            binding.includeRequestDetail.pgRejectEs.setVisibility(View.GONE);
                        }
                        if (status) {
                            Intent i = new Intent(mContext, ProviderHomeActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(PartnerServiceRequestDetailsActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(Object asyncTask) {
                        acceptExtendAsync = null;
                    }

                    @Override
                    public void onCancelled() {
                        acceptExtendAsync = null;
                    }
                });
    }

    private void serviceCallSendPraposal(String selectedHours, String trim, final Dialog dialog) {
        (dialog.findViewById(R.id.pgDialogSendApproval)).setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("sel_message_hour", selectedHours);

        textParams.put("txt_message", Utils.encodeEmoji(trim));

        textParams.put("txt_proposal_id", serviceDetailData.getProposalServiceData().get(0).getId());

        textParams.put("user_id", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("UserId"));

        new WebServiceCall(PartnerServiceRequestDetailsActivity.this,
                WebServiceUrl.URL_SEND_PRAPOSAL, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        (dialog.findViewById(R.id.pgDialogSendApproval)).setVisibility(View.GONE);
                        (dialog.findViewById(R.id.btn_proposal_cancle)).setClickable(true);
                        (dialog.findViewById(R.id.btn_proposal_send)).setClickable(true);
                        if (status) {
                            dialog.dismiss();
                            Intent i = new Intent(mContext, ProviderHomeActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        } else {
                            String msg = (String) obj;
                            if (msg.equals("Please connect with stripe before accepting service") ||
                                    msg.equals("Please connect with stripe before send the proposal.")) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                showAlertForStripeNotConnected(msg);

                            } else {
                                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onAsync(Object asyncTask) {
                        sendProposalAsync = null;
                    }

                    @Override
                    public void onCancelled() {
                        sendProposalAsync = null;
                    }
                });
    }

    private void serviceCallCancelService() {
        // Nessun riferimento a binding.pgCancel.setVisibility(View.VISIBLE) nell'originale, solo al pulsante Cancel
        
        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("service_id", serviceRequestId);
        textParams.put("user_id", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("UserId"));

        new WebServiceCall(PartnerServiceRequestDetailsActivity.this,
                WebServiceUrl.URL_CANCEL_SERVICE, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.btnCancel.setClickable(true);
                        if (status) {
                            Intent i = new Intent(mContext, ProviderHomeActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(Object asyncTask) {
                        cancelServiceAsync = null;
                    }

                    @Override
                    public void onCancelled() {
                        cancelServiceAsync = null;
                    }
                });
    }

    private void serviceCallAcceptReject(String serviceStatus, final Dialog dialog) {
        if (dialog == null) {
            if (serviceStatus.equalsIgnoreCase("accepted")) {
                binding.pgAccept.setVisibility(View.VISIBLE);
            } else {
                binding.pgReject.setVisibility(View.VISIBLE);
            }
        } else {
            if (serviceStatus.equalsIgnoreCase("accepted")) {
                (dialog.findViewById(R.id.pgProposalDialogAccept)).setVisibility(View.VISIBLE);
            } else {
                (dialog.findViewById(R.id.pgProposalDialogReject)).setVisibility(View.VISIBLE);
            }
        }

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("UserId"));

        textParams.put("service_id", serviceRequestId);
        textParams.put("provider_id", providerId);

        textParams.put("status_type", serviceStatus);

        new WebServiceCall(PartnerServiceRequestDetailsActivity.this,
                WebServiceUrl.URL_ACCEPT_SERVICE, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (dialog == null) {
                            binding.btnSendProposal.setClickable(true);
                            binding.btnAccept.setClickable(true);
                            binding.btnReject.setClickable(true);
                            binding.includeRequestDetail.btnRejectProposalP.setClickable(true);
                            binding.includeRequestDetail.btnAcceptProposalP.setClickable(true);
                            if (serviceStatus.equalsIgnoreCase("accepted")) {
                                binding.pgAccept.setVisibility(View.GONE);
                            } else {
                                binding.pgReject.setVisibility(View.GONE);
                            }
                        } else {
                            // Qui c'era un warning 'dialog != null' is always true
                            (dialog.findViewById(R.id.btn_accept)).setClickable(true);
                            (dialog.findViewById(R.id.btn_reject)).setClickable(true);
                            if (serviceStatus.equalsIgnoreCase("accepted")) {
                                (dialog.findViewById(R.id.pgProposalDialogAccept)).setVisibility(View.GONE);
                            } else {
                                (dialog.findViewById(R.id.pgProposalDialogReject)).setVisibility(View.GONE);
                            }
                        }

                        if (status) {
                            CommonPojo pojo = (CommonPojo) obj;
                            if (dialog != null) { // Rimosso un altro warning qui, anche se inutile
                                dialog.dismiss();
                            }
                            Toast.makeText(mContext, pojo.getMessage(), Toast.LENGTH_SHORT).show();
                            
                            // Refresh data on the current screen to show the accepted status
                            serviceCallGetDetail();
                            
                            Intent i = new Intent(mContext, ProviderHomeActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        } else {
                            String msg = (String) obj;
                            if (msg.equals("Please connect with stripe before accepting service") ||
                                    msg.equals("Please connect with stripe before send the proposal.")) {
                                if (dialog != null) { // Rimosso un altro warning qui
                                    dialog.dismiss();
                                }
                                showAlertForStripeNotConnected(msg);

                            } else {
                                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                            }
                        }

                    }

                    @Override
                    public void onAsync(Object asyncTask) {
                        serviceActionAsync = null;
                    }

                    @Override
                    public void onCancelled() {
                        serviceActionAsync = null;
                    }
                });
    }

    private void showAlertForStripeNotConnected(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setNegativeButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(getString(R.string.connect_account), (d, which) -> {
            d.dismiss();
            callAPICheckConnectedStripeAccount();
        });
        builder.create().show();
    }

    private void callAPICheckConnectedStripeAccount() {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("user_id", PrefsUtil.with(mContext).readString("UserId"));

        new WebServiceCall(mContext, WebServiceUrl.URL_STRIPE_CONNECT, params, CheckStripeConnectedPojo.class, true,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        try {
                            if (status) {
                                CheckStripeConnectedPojo checkStripeConnectedResponse = (CheckStripeConnectedPojo) obj;
                                if (checkStripeConnectedResponse.getData().getConnectUrl() != null) {
                                    Intent intent = new Intent(mContext, MyStripeConnectActivity.class);
                                    intent.putExtra("connect_url", checkStripeConnectedResponse.getData().getConnectUrl());
                                    startActivity(intent);
                                }
                            } else {
                                Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error in Stripe Connect API call: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onAsync(Object asyncTask) {

                    }

                    @Override
                    public void onCancelled() {

                    }
                });
    }

    private void serviceCallGetDetail() {
        binding.rlMain.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("user_id", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("UserId"));
        textParams.put("service_request_id", serviceRequestId);

        new WebServiceCall(PartnerServiceRequestDetailsActivity.this,
                WebServiceUrl.URL_SERVICEDETAILS, textParams, ProviderServiceRequestPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        binding.progress.setVisibility(View.GONE);
                        binding.rlMain.setVisibility(View.VISIBLE);
                        if (status) {
                            ProviderServiceRequestPojo result = (ProviderServiceRequestPojo) obj;
                            serviceDetailData = result.getData();
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + result.getData().getServiceName(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                            }

                            proposalServiceDataItems.clear();
                            proposalServiceDataItems.addAll(result.getData().getProposalServiceData());
                            extendServiceListPojoItems.clear();
                            extendServiceListPojoItems.addAll(result.getData().getExtendServiceData());
                            providerId = result.getData().getProviderId();

                            manageData();

                        } else {
                            Toast.makeText(PartnerServiceRequestDetailsActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(Object asyncTask) {
                        serviceDetailAsync = null;
                    }

                    @Override
                    public void onCancelled() {
                        serviceDetailAsync = null;
                    }
                });

    }

    private void manageData() {
        String status = serviceDetailData.getServiceStatus();
        String statusDisplayName = serviceDetailData.getServiceStatusDisplayName();
        String customerFirstName = serviceDetailData.getCustomerFname();
        String customerLastName = serviceDetailData.getCustomerLname();
        String customerImage = serviceDetailData.getCustomerImage();
        String servicePrice = serviceDetailData.getServicePrice();
        String serviceType = serviceDetailData.getServiceType();
        String serviceAddress = serviceDetailData.getBookingAddress();
        String categoryName = serviceDetailData.getCategoryName();
        String bookingAmount = serviceDetailData.getBookingAmount();
        String paymentMode = serviceDetailData.getPaymentMode();
        // String description = Utils.decodeEmoji(serviceDetailData.getDescription()); // Rimosso unused variable
        String providerCommissionAmount = serviceDetailData.getProviderCommissionAmount();
        String bookingStartTime = serviceDetailData.getBookingStartTime();
        String bookingEndTime = serviceDetailData.getBookingEndTime();
        String bookingDetails = Utils.decodeEmoji(serviceDetailData.getBookingDetails());

        String strDeliveryType = "";
        if (serviceDetailData.getDeliveryType() != null) {
            binding.includeRequestDetail.llDeliveryType.setVisibility(View.VISIBLE);
            // Sostituito if/else con switch per pulizia
            switch (serviceDetailData.getDeliveryType()) {
                case "small":
                    strDeliveryType = getString(R.string.small);
                    break;
                case "medium":
                    strDeliveryType = getString(R.string.medium);
                    break;
                case "large":
                    strDeliveryType = getString(R.string.large);
                    break;
            }
            binding.includeRequestDetail.txtDeliveryType.setText(strDeliveryType);
        } else {
            binding.includeRequestDetail.llDeliveryType.setVisibility(View.GONE);
        }

        if (serviceDetailData.getAvailableDaysList() != null && !serviceDetailData.getAvailableDaysList().isEmpty()) { 
            binding.includeRequestDetail.txtServiceDays.setText(serviceDetailData.getAvailableDaysList());
        } else {
            binding.includeRequestDetail.txtServiceDays.setText("-");
        }

        if (serviceDetailData.getAvailableTimeStart() != null
                && !serviceDetailData.getAvailableTimeStart().isEmpty() 
                && serviceDetailData.getAvailableTimeEnd() != null
                && !serviceDetailData.getAvailableTimeEnd().isEmpty()) { 
            binding.includeRequestDetail.txtServiceTime.setText(String.format("%s - %s", serviceDetailData.getAvailableTimeStart(), serviceDetailData.getAvailableTimeEnd()));
        } else {
            binding.includeRequestDetail.txtServiceTime.setText("-");
        }

        if (!proposalServiceDataItems.isEmpty()) { 
            if (status.equalsIgnoreCase("pending")) {
                binding.includeRequestDetail.layoutMessageProposalP.setVisibility(View.VISIBLE);
            } else {
                binding.includeRequestDetail.layoutMessageProposalP.setVisibility(View.GONE);
            }
            if (proposalServiceDataItems.size() == 1) {
                binding.includeRequestDetail.layoutProposalFirstMessageP.setVisibility(View.VISIBLE);
                binding.includeRequestDetail.txtFmessageHoursP.setText(proposalServiceDataItems.get(0).getHours());
                binding.includeRequestDetail.txtFmessageP.setText(Utils.decodeEmoji(proposalServiceDataItems.get(0).getMessage()));
                
                String proposalStatus = proposalServiceDataItems.get(0).getStatus();
                int statusColor;
                String statusText;

                if (proposalStatus.equals("pending")) {
                    statusColor = R.color.status_pending;
                    statusText = getResources().getString(R.string.status_pending);
                } else {
                    statusColor = R.color.status_accepted;
                    statusText = getResources().getString(R.string.status_accepted);
                }
                
                binding.includeRequestDetail.txtFmessageStatusP.setBackgroundResource(statusColor);
                binding.includeRequestDetail.txtFmessageStatusP.setText(statusText);

                if (proposalStatus.equals("pending")) {
                    binding.includeRequestDetail.layoutPropoButtonP.setVisibility(View.VISIBLE);
                } else {
                    binding.includeRequestDetail.layoutPropoButtonP.setVisibility(View.GONE);
                }
            } else {
                binding.includeRequestDetail.layoutProposalSecondMessageP.setVisibility(View.GONE);
            }

            if (proposalServiceDataItems.size() == 2) {
                binding.includeRequestDetail.layoutProposalFirstMessageP.setVisibility(View.VISIBLE);
                binding.includeRequestDetail.txtFmessageHoursP.setText(proposalServiceDataItems.get(0).getHours());
                binding.includeRequestDetail.txtFmessageP.setText(Utils.decodeEmoji(proposalServiceDataItems.get(0).getMessage()));
                
                String status0 = proposalServiceDataItems.get(0).getStatus();
                int statusColor0;
                String statusText0;
                
                if (status0.equals("pending")) {
                    statusColor0 = R.color.status_pending;
                    statusText0 = getResources().getString(R.string.status_pending);
                } else if (status0.equals("rejected")) {
                    statusColor0 = R.color.status_rejected;
                    statusText0 = getResources().getString(R.string.status_rejected);
                } else {
                    statusColor0 = R.color.status_accepted;
                    statusText0 = getResources().getString(R.string.status_accepted);
                }
                
                binding.includeRequestDetail.txtFmessageStatusP.setBackgroundResource(statusColor0);
                binding.includeRequestDetail.txtFmessageStatusP.setText(statusText0);
                
                
                binding.includeRequestDetail.layoutProposalSecondMessageP.setVisibility(View.VISIBLE);
                binding.includeRequestDetail.txtLmessageHoursP.setText(proposalServiceDataItems.get(1).getHours());
                binding.includeRequestDetail.txtLmessageP.setText(Utils.decodeEmoji(proposalServiceDataItems.get(1).getMessage()));
                binding.includeRequestDetail.txtLmessageStatusP.setText(proposalServiceDataItems.get(1).getStatus());

                String status1 = proposalServiceDataItems.get(1).getStatus();
                int statusColor1;
                String statusText1;

                if (status1.equals("accepted")) {
                    statusColor1 = R.color.status_accepted;
                    statusText1 = getString(R.string.status_accepted);
                    binding.includeRequestDetail.layoutPropoButtonP.setVisibility(View.GONE);
                } else if (status1.equals("pending")) {
                    statusColor1 = R.color.status_pending;
                    statusText1 = getString(R.string.status_pending);
                } else {
                    statusColor1 = R.color.status_rejected;
                    statusText1 = getString(R.string.status_rejected);
                    binding.includeRequestDetail.layoutPropoButtonP.setVisibility(View.GONE);
                }
                
                binding.includeRequestDetail.txtLmessageStatusP.setBackgroundResource(statusColor1);
                binding.includeRequestDetail.txtLmessageStatusP.setText(statusText1);
            }
        }

        if (!extendServiceListPojoItems.isEmpty()) { // Corretto size() > 0
            binding.includeRequestDetail.layoutExtendMessageP.setVisibility(View.VISIBLE);
            ExtendServiceListPojoItem extendedItem = extendServiceListPojoItems.get(0);
            binding.includeRequestDetail.txtExtendDurationP.setText(HtmlCompat.fromHtml(extendedItem.getBookingStartTime()
                    + " to <br/>" + extendedItem.getBookingEndTime(),HtmlCompat.FROM_HTML_MODE_LEGACY));
            binding.includeRequestDetail.txtExtendHoursP.setText(extendedItem.getExtendHours());
            binding.includeRequestDetail.txtExtendCostP.setText(extendedItem.getBookingAmt());

            int statusColor;
            String statusText;
            boolean showButton = false;

            // Sostituito if/else con switch per pulizia
            switch (extendedItem.getExtendStatus()) {
                case "pending":
                    statusColor = R.color.status_pending;
                    statusText = getString(R.string.status_pending);
                    if (status.equalsIgnoreCase("ongoing")) {
                        showButton = true;
                    }
                    break;
                case "accepted":
                    statusColor = R.color.status_accepted;
                    statusText = getString(R.string.status_accepted);
                    break;
                case "rejected":
                    statusColor = R.color.status_rejected;
                    statusText = getString(R.string.status_rejected);
                    break;
                default: // assumed paid
                    statusColor = R.color.status_paid;
                    statusText = getString(R.string.paid);
                    break;
            }
            
            binding.includeRequestDetail.layoutExtendButton.setVisibility(showButton ? View.VISIBLE : View.GONE);
            binding.includeRequestDetail.txtExtendStatusP.setText(statusText);
            binding.includeRequestDetail.txtExtendStatusP.setBackgroundResource(statusColor);
        }

        if (status.equalsIgnoreCase("closed")) {
            binding.layoutSendMessage.setVisibility(View.INVISIBLE);
            binding.layoutRaiseDispute.setVisibility(View.INVISIBLE);
            binding.viewLineOne.setVisibility(View.INVISIBLE);
            binding.viewLineTwo.setVisibility(View.INVISIBLE);
            binding.txtNote.setVisibility(View.GONE);
            binding.llBtnAccept.setVisibility(View.GONE);
            binding.btnSendProposal.setVisibility(View.GONE);
            binding.llBtnReject.setVisibility(View.GONE);

            binding.txtServiceStatus.setText(statusDisplayName);
            binding.txtServiceStatus.setBackgroundResource(R.color.status_closed);

            binding.txtUserName.setText(String.format("%s %s", customerFirstName, customerLastName));

            // Inizio logica caricamento immagine semplificata (rimosso codice duplicato)
            ImageRequest.Builder profileBuilder = new ImageRequest.Builder(mContext)
                .placeholder(R.drawable.loading)
                .error(R.mipmap.user)
                .target(binding.imgUserProfile);

            if (customerImage != null && !customerImage.isEmpty()) {
                try {
                    profileBuilder.data(customerImage);
                    Coil.imageLoader(mContext).enqueue(profileBuilder.build());
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error loading customer image via Coil: " + e.getMessage());
                }
            } else {
                binding.imgUserProfile.setImageResource(R.mipmap.user);
            }
            // Fine logica caricamento immagine semplificata

            binding.includeRequestDetail.TxtServiceName.setText(categoryName);
            if (serviceType.equalsIgnoreCase("hourly")) {
                binding.includeRequestDetail.TxtServicePrice.setText(String.format("%s%s (%s%s/hour)", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), bookingAmount, PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), servicePrice));
            } else {
                binding.includeRequestDetail.TxtServicePrice.setText(String.format("%s%s", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), bookingAmount));
            }

            if (paymentMode.equalsIgnoreCase("")) {
                binding.includeRequestDetail.TxtPaymentpreference.setText(getResources().getString(R.string.none));
            }
            if (paymentMode.equalsIgnoreCase("wallet")) {
                binding.includeRequestDetail.TxtPaymentpreference.setText(getResources().getString(R.string.wallet));
            }
            if (paymentMode.equalsIgnoreCase("cash")) {
                binding.includeRequestDetail.TxtPaymentpreference.setText(getResources().getString(R.string.cash));
            }

            binding.txtUserName.setText(String.format("%s %s", customerFirstName, customerLastName));

            if (paymentMode.equalsIgnoreCase("cash")) {
                binding.includeRequestDetail.TxtAdminFeesP.setVisibility(View.GONE);
            } else {
                binding.includeRequestDetail.TxtAdminFeesP.setText(String.format("%s%s", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), providerCommissionAmount));
            }

            binding.includeRequestDetail.TxtServiceTime.setText(bookingStartTime);
            binding.includeRequestDetail.TxtEndServiceTime.setText(bookingEndTime);
            binding.includeRequestDetail.TxtServiceAddress.setText(serviceAddress);
            binding.includeRequestDetail.TxtServiceAddress.setPadding(0, 0, 0, 60);
            binding.includeRequestDetail.TxtDesc.setText(bookingDetails);
        } else if ("accepted".equalsIgnoreCase(status) || "ongoing".equalsIgnoreCase(status) || "dispute".equalsIgnoreCase(status)) {
            // Status is accepted, ongoing, or dispute - Hide initial action buttons
            binding.layoutSendMessage.setVisibility(View.VISIBLE); // Keep message visible
            binding.layoutRaiseDispute.setVisibility(View.VISIBLE); // Keep dispute visible if applicable
            binding.llBtnAccept.setVisibility(View.GONE);
            binding.llBtnReject.setVisibility(View.GONE);
            binding.btnSendProposal.setVisibility(View.GONE);
            binding.btnCancel.setVisibility(View.GONE); // Cancel button might also need to change visibility
            binding.txtNote.setVisibility(View.GONE);
            binding.viewLineOne.setVisibility(View.INVISIBLE);
            binding.viewLineTwo.setVisibility(View.INVISIBLE);


            binding.txtServiceStatus.setText(statusDisplayName);
            if ("accepted".equalsIgnoreCase(status)) {
                 binding.txtServiceStatus.setBackgroundResource(R.color.status_accepted);
            } else if ("ongoing".equalsIgnoreCase(status)) {
                 binding.txtServiceStatus.setBackgroundResource(R.color.status_ongoing); // Assuming a status_ongoing color exists
            } else if ("dispute".equalsIgnoreCase(status)) {
                 binding.txtServiceStatus.setBackgroundResource(R.color.status_dispute); // Assuming a status_dispute color exists
            }

            binding.txtUserName.setText(String.format("%s %s", customerFirstName, customerLastName));

            // Inizio logica caricamento immagine semplificata
            ImageRequest.Builder profileBuilder = new ImageRequest.Builder(mContext)
                .placeholder(R.drawable.loading)
                .error(R.mipmap.user)
                .target(binding.imgUserProfile);

            if (customerImage != null && !customerImage.isEmpty()) {
                try {
                    profileBuilder.data(customerImage);
                    Coil.imageLoader(mContext).enqueue(profileBuilder.build());
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error loading customer image via Coil: " + e.getMessage());
                }
            } else {
                binding.imgUserProfile.setImageResource(R.mipmap.user);
            }
            // Fine logica caricamento immagine semplificata

            binding.includeRequestDetail.TxtServiceName.setText(categoryName);
            if (serviceType.equalsIgnoreCase("hourly")) {
                binding.includeRequestDetail.TxtServicePrice.setText(String.format("%s%s (%s%s/hour)", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), bookingAmount, PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), servicePrice));
            } else {
                binding.includeRequestDetail.TxtServicePrice.setText(String.format("%s%s", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), bookingAmount));
            }

            if (paymentMode.equalsIgnoreCase("")) {
                binding.includeRequestDetail.TxtPaymentpreference.setText(getResources().getString(R.string.none));
            }
            if (paymentMode.equalsIgnoreCase("wallet")) {
                binding.includeRequestDetail.TxtPaymentpreference.setText(getResources().getString(R.string.wallet));
            }
            if (paymentMode.equalsIgnoreCase("cash")) {
                binding.includeRequestDetail.TxtPaymentpreference.setText(getResources().getString(R.string.cash));
            }

            binding.txtUserName.setText(String.format("%s %s", customerFirstName, customerLastName));

            if (paymentMode.equalsIgnoreCase("cash")) {
                binding.includeRequestDetail.TxtAdminFeesP.setVisibility(View.GONE);
            } else {

                binding.includeRequestDetail.TxtAdminFeesP.setText(String.format("%s%s", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), providerCommissionAmount));
            }

            binding.includeRequestDetail.TxtServiceTime.setText(bookingStartTime);
            binding.includeRequestDetail.TxtEndServiceTime.setText(bookingEndTime);
            binding.includeRequestDetail.TxtServiceAddress.setText(serviceAddress);
            binding.includeRequestDetail.TxtServiceAddress.setPadding(0, 0, 0, 60);
            binding.includeRequestDetail.TxtDesc.setText(bookingDetails);

            // If there are proposals, show proposal section logic (proposalServiceDataItems.size() > 0)
            // If there are extensions, show extension section logic (extendServiceListPojoItems.size() > 0)

        }
        else {
            // Default/Fallback for other statuses (like pending or if accepted logic above failed to execute)
            binding.txtServiceStatus.setText(statusDisplayName);
            
            // Re-add visibility for Accept/Reject buttons if status is still pending
            if ("pending".equalsIgnoreCase(status)) {
                binding.llBtnAccept.setVisibility(View.VISIBLE);
                binding.llBtnReject.setVisibility(View.VISIBLE);
                binding.btnSendProposal.setVisibility(View.VISIBLE); 
                binding.layoutSendMessage.setVisibility(View.GONE); 
                binding.layoutRaiseDispute.setVisibility(View.GONE);
            } else {
                 binding.llBtnAccept.setVisibility(View.GONE);
                 binding.llBtnReject.setVisibility(View.GONE);
                 binding.btnSendProposal.setVisibility(View.GONE);
            }
            
            binding.txtUserName.setText(String.format("%s %s", customerFirstName, customerLastName));

            // Inizio logica caricamento immagine semplificata
            ImageRequest.Builder profileBuilder = new ImageRequest.Builder(mContext)
                .placeholder(R.drawable.loading)
                .error(R.mipmap.user)
                .target(binding.imgUserProfile);

            if (customerImage != null && !customerImage.isEmpty()) {
                try {
                    profileBuilder.data(customerImage);
                    Coil.imageLoader(mContext).enqueue(profileBuilder.build());
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error loading customer image via Coil: " + e.getMessage());
                }
            } else {
                binding.imgUserProfile.setImageResource(R.mipmap.user);
            }
            // Fine logica caricamento immagine semplificata

            binding.includeRequestDetail.TxtServiceName.setText(categoryName);
            if (serviceType.equalsIgnoreCase("hourly")) {
                binding.includeRequestDetail.TxtServicePrice.setText(String.format("%s%s (%s%s/hour)", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), bookingAmount, PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), servicePrice));
            } else {
                binding.includeRequestDetail.TxtServicePrice.setText(String.format("%s%s", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), bookingAmount));
            }

            if (paymentMode.equalsIgnoreCase("")) {
                binding.includeRequestDetail.TxtPaymentpreference.setText(getResources().getString(R.string.none));
            }
            if (paymentMode.equalsIgnoreCase("wallet")) {
                binding.includeRequestDetail.TxtPaymentpreference.setText(getResources().getString(R.string.wallet));
            }
            if (paymentMode.equalsIgnoreCase("cash")) {
                binding.includeRequestDetail.TxtPaymentpreference.setText(getResources().getString(R.string.cash));
            }

            binding.txtUserName.setText(String.format("%s %s", customerFirstName, customerLastName));

            if (paymentMode.equalsIgnoreCase("cash")) {
                binding.includeRequestDetail.TxtAdminFeesP.setVisibility(View.GONE);
            } else {

                binding.includeRequestDetail.TxtAdminFeesP.setText(String.format("%s%s", PrefsUtil.with(PartnerServiceRequestDetailsActivity.this).readString("CurrencySign"), providerCommissionAmount));
            }

            binding.includeRequestDetail.TxtServiceTime.setText(bookingStartTime);
            binding.includeRequestDetail.TxtEndServiceTime.setText(bookingEndTime);
            binding.includeRequestDetail.TxtServiceAddress.setText(serviceAddress);
            binding.includeRequestDetail.TxtServiceAddress.setPadding(0, 0, 0, 60);
            binding.includeRequestDetail.TxtDesc.setText(bookingDetails);
        }
    }

    private void initView() {

        serviceRequestId = getIntent().getStringExtra("serviceRequestId");

        try {
            String title = getIntent().getStringExtra("serviceName");
            if (title != null && !title.isEmpty()) { // Corretto equals("")
                setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + title, HtmlCompat.FROM_HTML_MODE_LEGACY));
            } else {
                setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>", HtmlCompat.FROM_HTML_MODE_LEGACY));
            }

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in initView title setup: " + e.getMessage()); // Corretto logging
        }

        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Sostituito onBackPressed() deprecato
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver: " + e.getMessage()); // Corretto logging
        }

        Utils.cancelAsyncTask(actionProposalAsync);
        Utils.cancelAsyncTask(acceptExtendAsync);
        Utils.cancelAsyncTask(actionRequestAsync);
        Utils.cancelAsyncTask(sendProposalAsync);
        Utils.cancelAsyncTask(cancelServiceAsync);
        Utils.cancelAsyncTask(serviceActionAsync);
        Utils.cancelAsyncTask(serviceDetailAsync);

        super.onDestroy();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}