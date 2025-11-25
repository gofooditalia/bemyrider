package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class PartnerPaymentHistoryItem {
    @SerializedName("profile_image")
    private String profile_image;

    @SerializedName("username")
    private String username;

    @SerializedName("servicename")
    private String servicename;

    @SerializedName("category")
    private String category;

    @SerializedName("subcategory")
    private String subcategory;

    @SerializedName("address")
    private String address;

    @SerializedName("transection_id")
    private String transection_id;

    @SerializedName("recived_amount")
    private String recived_amount;

    @SerializedName("completion_date")
    private String completion_date;

    @SerializedName("per_hour")
    private String per_hour;

    @SerializedName("totel_hours")
    private String totel_hours;

    @SerializedName("status")
    private String status;

    @SerializedName("per_hour_title")
    private String per_hour_title;

    @SerializedName("per_hour_class")
    private String per_hour_class;

    @SerializedName("isactive")
    private String isactive;

    public String getIsActive() {
        return isactive;
    }

    public void setIsActive(String isactive) {
        this.isactive = isactive;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTransection_id() {
        return transection_id;
    }

    public void setTransection_id(String transection_id) {
        this.transection_id = transection_id;
    }

    public String getRecived_amount() {
        return recived_amount;
    }

    public void setRecived_amount(String recived_amount) {
        this.recived_amount = recived_amount;
    }

    public String getCompletion_date() {
        return completion_date;
    }

    public void setCompletion_date(String completion_date) {
        this.completion_date = completion_date;
    }

    public String getPer_hour() {
        return per_hour;
    }

    public void setPer_hour(String per_hour) {
        this.per_hour = per_hour;
    }

    public String getTotel_hours() {
        return totel_hours;
    }

    public void setTotel_hours(String totel_hours) {
        this.totel_hours = totel_hours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPer_hour_title() {
        return per_hour_title;
    }

    public void setPer_hour_title(String per_hour_title) {
        this.per_hour_title = per_hour_title;
    }

    public String getPer_hour_class() {
        return per_hour_class;
    }

    public void setPer_hour_class(String per_hour_class) {
        this.per_hour_class = per_hour_class;
    }
}
