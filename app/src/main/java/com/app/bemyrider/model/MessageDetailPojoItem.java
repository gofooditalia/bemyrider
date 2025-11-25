package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MessageDetailPojoItem {

    @SerializedName("my_user_type")
    private String myUserType;

    @SerializedName("to_user_email")
    private String toUserEmail;

    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("to_user_type")
    private String toUserType;

    @SerializedName("my_user_email")
    private String myUserEmail;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("service_id")
    private String serviceId;

    @SerializedName("to_user_name")
    private String toUserName;

    @SerializedName("my_user_name")
    private String myUserName;

    @SerializedName("service_master_id")
    private String serviceMasterId;

    @SerializedName("to_profile_img")
    private String toProfileImg;

    @SerializedName("my_profile_img")
    private String myProfileImg;

    @SerializedName("isactive")
    private String isActive;

    @SerializedName("ser_active")
    private String serActive;

    @SerializedName("pagination")
    private Pagination pagination;

    @SerializedName("message_list")
    private List<MessageListDetailPojoItem> messageList;

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public String getSerActive() {
        return serActive;
    }

    public void setSerActive(String serActive) {
        this.serActive = serActive;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public List<MessageListDetailPojoItem> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<MessageListDetailPojoItem> messageList) {
        this.messageList = messageList;
    }

    public String getMyUserType() {
        return myUserType;
    }

    public void setMyUserType(String myUserType) {
        this.myUserType = myUserType;
    }

    public String getToUserEmail() {
        return toUserEmail;
    }

    public void setToUserEmail(String toUserEmail) {
        this.toUserEmail = toUserEmail;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getToUserType() {
        return toUserType;
    }

    public void setToUserType(String toUserType) {
        this.toUserType = toUserType;
    }

    public String getMyUserEmail() {
        return myUserEmail;
    }

    public void setMyUserEmail(String myUserEmail) {
        this.myUserEmail = myUserEmail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getMyUserName() {
        return myUserName;
    }

    public void setMyUserName(String myUserName) {
        this.myUserName = myUserName;
    }

    public String getServiceMasterId() {
        return serviceMasterId;
    }

    public void setServiceMasterId(String serviceMasterId) {
        this.serviceMasterId = serviceMasterId;
    }

    public String getToProfileImg() {
        return toProfileImg;
    }

    public void setToProfileImg(String toProfileImg) {
        this.toProfileImg = toProfileImg;
    }

    public String getMyProfileImg() {
        return myProfileImg;
    }

    public void setMyProfileImg(String myProfileImg) {
        this.myProfileImg = myProfileImg;
    }

    @Override
    public String toString() {
        return
                "MessageDetailPojoItem{" +
                        ",my_user_type = '" + myUserType + '\'' +
                        ",to_user_email = '" + toUserEmail + '\'' +
                        ",service_name = '" + serviceName + '\'' +
                        ",to_user_type = '" + toUserType + '\'' +
                        ",my_user_email = '" + myUserEmail + '\'' +
                        ",user_id = '" + userId + '\'' +
                        ",service_id = '" + serviceId + '\'' +
                        ",to_user_name = '" + toUserName + '\'' +
                        ",my_user_name = '" + myUserName + '\'' +
                        ",service_master_id = '" + serviceMasterId + '\'' +
                        ",to_profile_img = '" + toProfileImg + '\'' +
                        ",my_profile_img = '" + myProfileImg + '\'' +
                        "}";
    }
}