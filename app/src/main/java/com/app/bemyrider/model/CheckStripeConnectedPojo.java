package com.app.bemyrider.model;


import com.google.gson.annotations.SerializedName;

public class CheckStripeConnectedPojo {

    @SerializedName("status")

    private Boolean status;
    @SerializedName("type")

    private String type;
    @SerializedName("data")

    private CheckStripeConnectedItem data;
    @SerializedName("message")

    private String message;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CheckStripeConnectedItem getData() {
        return data;
    }

    public void setData(CheckStripeConnectedItem data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}