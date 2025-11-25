package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DisputeDetailDataPojo {

    @SerializedName("dispute_title")
    private String disputeTitle;

    @SerializedName("customer_lastname")
    private String customerLastname;

    @SerializedName("provider_firstname")
    private String providerFirstname;

    @SerializedName("customer_firstname")
    private String customerFirstname;

    @SerializedName("provider_lastname")
    private String providerLastname;

    @SerializedName("provider_image")
    private String providerImage;

    @SerializedName("dispute_id")
    private String disputeId;

    @SerializedName("provider_id")
    private String providerId;

    @SerializedName("customer_id")
    private String customerId;

    @SerializedName("customer_image")
    private String customerImage;

    @SerializedName("service_status")
    private String serviceStatus;

    @SerializedName("escalate_admin")
    private String escalateAdmin;

    @SerializedName("status")
    private String status;

    @SerializedName("cust_active")
    private String custActive;

    @SerializedName("pro_active")
    private String proActive;

    @SerializedName("service_request_id")
    private String serviceRequestId;

    @SerializedName("dispute_create_userid")
    private String disputeCreateUserId;

    @SerializedName("message_list")
    private List<DisputeDetailPojoItem> messageList;

    @SerializedName("pagination")
    private Pagination pagination;

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public String getDisputeCreateUserId() {
        return disputeCreateUserId;
    }

    public void setDisputeCreateUserId(String disputeCreateUserId) {
        this.disputeCreateUserId = disputeCreateUserId;
    }

    public String getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(String serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public String getDisputeTitle() {
        return disputeTitle;
    }

    public void setDisputeTitle(String disputeTitle) {
        this.disputeTitle = disputeTitle;
    }

    public String getCustomerLastname() {
        return customerLastname;
    }

    public void setCustomerLastname(String customerLastname) {
        this.customerLastname = customerLastname;
    }

    public String getProviderFirstname() {
        return providerFirstname;
    }

    public void setProviderFirstname(String providerFirstname) {
        this.providerFirstname = providerFirstname;
    }

    public String getCustomerFirstname() {
        return customerFirstname;
    }

    public void setCustomerFirstname(String customerFirstname) {
        this.customerFirstname = customerFirstname;
    }

    public String getProviderLastname() {
        return providerLastname;
    }

    public void setProviderLastname(String providerLastname) {
        this.providerLastname = providerLastname;
    }

    public String getProviderImage() {
        return providerImage;
    }

    public void setProviderImage(String providerImage) {
        this.providerImage = providerImage;
    }

    public String getDisputeId() {
        return disputeId;
    }

    public void setDisputeId(String disputeId) {
        this.disputeId = disputeId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerImage() {
        return customerImage;
    }

    public void setCustomerImage(String customerImage) {
        this.customerImage = customerImage;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getEscalateAdmin() {
        return escalateAdmin;
    }

    public void setEscalateAdmin(String escalateAdmin) {
        this.escalateAdmin = escalateAdmin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustActive() {
        return custActive;
    }

    public void setCustActive(String custActive) {
        this.custActive = custActive;
    }

    public String getProActive() {
        return proActive;
    }

    public void setProActive(String proActive) {
        this.proActive = proActive;
    }

    public List<DisputeDetailPojoItem> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<DisputeDetailPojoItem> messageList) {
        this.messageList = messageList;
    }

    @Override
    public String toString() {
        return "DisputeDetailDataPojo{" +
                "disputeTitle='" + disputeTitle + '\'' +
                ", customerLastname='" + customerLastname + '\'' +
                ", providerFirstname='" + providerFirstname + '\'' +
                ", customerFirstname='" + customerFirstname + '\'' +
                ", providerLastname='" + providerLastname + '\'' +
                ", providerImage='" + providerImage + '\'' +
                ", serviceRequestId='" + serviceRequestId + '\'' +
                ", disputeCreateUserId='" + disputeCreateUserId + '\'' +
                ", disputeId='" + disputeId + '\'' +
                ", providerId='" + providerId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", customerImage='" + customerImage + '\'' +
                ", service_status='" + serviceStatus + '\'' +
                ", escalate_admin='" + escalateAdmin + '\'' +
                ", status='" + status + '\'' +
                ", custActive='" + custActive + '\'' +
                ", proActive='" + proActive + '\'' +
                ", messageList=" + messageList +
                '}';
    }
}