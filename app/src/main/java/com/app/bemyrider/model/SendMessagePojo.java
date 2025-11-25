package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class SendMessagePojo {

    @SerializedName("data")
    private MessageListDetailPojoItem data;

    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private boolean status;

    public MessageListDetailPojoItem getData() {
        return data;
    }

    public void setData(MessageListDetailPojoItem data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
                "CommonPojo{" +
                        /*"data = '" + data + '\'' + */
                        ",message = '" + message + '\'' +
                        ",type = '" + type + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}