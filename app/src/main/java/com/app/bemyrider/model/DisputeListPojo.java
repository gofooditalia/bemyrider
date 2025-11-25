package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class DisputeListPojo {

    @SerializedName("data")
    private DisputeListDataPojo data;

    @SerializedName("type")
    private String type;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public DisputeListDataPojo getData() {
        return data;
    }

    public void setData(DisputeListDataPojo data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return
                "DisputeListPojo{" +
                        "data = '" + data + '\'' +
                        ",type = '" + type + '\'' +
                        ",message = '" + message + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}