package com.app.bemyrider.model;


import com.google.gson.annotations.SerializedName;

public class CheckStripeConnectedItem {

    @SerializedName("connect_url")

    private String connectUrl;

    public String getConnectUrl() {
        return connectUrl;
    }

    public void setConnectUrl(String connectUrl) {
        this.connectUrl = connectUrl;
    }

}