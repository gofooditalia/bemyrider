package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class NotificationListItem {

    @SerializedName("image")
    private String image;

    @SerializedName("user_type")
    private String userType;

    @SerializedName("notification_date")
    private String notificationDate;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("message")
    private String message;

    @SerializedName("isactive")
    private String isactive;

    @SerializedName("notification_type")
    private String notificationType;

    @SerializedName("notification_constant")
    private String notificationConstant;

    @SerializedName("service_request_id")
    private String serviceRequestId;

    @SerializedName("provider_service_id")
    private String providerServiceId;

    @SerializedName("customer_id")
    private String customerId;

    @SerializedName("provider_id")
    private String providerId;

    @SerializedName("service_id")
    private String serviceId;

    @SerializedName("dispute_id")
    private String disputeId;

    @SerializedName("service_status")
    private String serviceStatus;

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getDisputeId() {
        return disputeId;
    }

    public void setDisputeId(String disputeId) {
        this.disputeId = disputeId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
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

    public String getProviderServiceId() {
        return providerServiceId;
    }

    public void setProviderServiceId(String providerServiceId) {
        this.providerServiceId = providerServiceId;
    }

    public String getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(String serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotificationConstant() {
        return notificationConstant;
    }

    public void setNotificationConstant(String notificationConstant) {
        this.notificationConstant = notificationConstant;
    }

    public String getIsActive() {
        return isactive;
    }

    public void setIsActive(String isactive) {
        this.isactive = isactive;
    }

    public String getImage() {
        return image;
    }

    public String getUserType() {
        return userType;
    }

    public String getNotificationDate() {
        return notificationDate;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }
}