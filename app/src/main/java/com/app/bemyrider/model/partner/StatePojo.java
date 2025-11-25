package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StatePojo {

    @SerializedName("data")
    private List<StatepojoItem> data;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private boolean status;

    public List<StatepojoItem> getData() {
        return data;
    }

    public void setData(List<StatepojoItem> data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
                "StatePojo{" +
                        "data = '" + data + '\'' +
                        ",type = '" + type + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}