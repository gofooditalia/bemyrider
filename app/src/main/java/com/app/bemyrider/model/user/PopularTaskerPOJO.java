package com.app.bemyrider.model.user;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PopularTaskerPOJO {

    @SerializedName("data")
    private List<PopularTaskerItem> data;

    @SerializedName("type")
    private String type;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<PopularTaskerItem> getData() {
        return data;
    }

    public void setData(List<PopularTaskerItem> data) {
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
                "PopularTaskerPOJO{" +
                        "data = '" + data + '\'' +
                        ",type = '" + type + '\'' +
                        ",message = '" + message + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}