package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class DisputeDetailPojoItem {

    @SerializedName("dispute_message")
    private String disputeMessage;

    @SerializedName("created_user")
    private String createdUser;

    @SerializedName("message_id")
    private String messageId;

    @SerializedName("createdDate")
    private String createdDate;

    @SerializedName("created_user_type")
    private String createdUserType;

    @SerializedName("appAttUrl")
    private String appAttUrl;

    @SerializedName("downloadUrl")
    private String downloadUrl;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDisputeMessage() {
        return disputeMessage;
    }

    public void setDisputeMessage(String disputeMessage) {
        this.disputeMessage = disputeMessage;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUserType() {
        return createdUserType;
    }

    public void setCreatedUserType(String createdUserType) {
        this.createdUserType = createdUserType;
    }

    public String getAppAttUrl() {
        return appAttUrl;
    }

    public void setAppAttUrl(String appAttUrl) {
        this.appAttUrl = appAttUrl;
    }

    @Override
    public String toString() {
        return "DisputeDetailPojoItem{" +
                "disputeMessage='" + disputeMessage + '\'' +
                ", createdUser='" + createdUser + '\'' +
                ", messageId='" + messageId + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", createdUserType='" + createdUserType + '\'' +
                ", appAttUrl='" + appAttUrl + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
}