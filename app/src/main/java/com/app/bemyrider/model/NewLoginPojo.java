package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class NewLoginPojo {

    @SerializedName("redirect")
    private String redirect;

    @SerializedName("data")
    private NewLoginPojoItem data;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public NewLoginPojoItem getData() {
        return data;
    }

    public void setData(NewLoginPojoItem data) {
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
                "NewLoginPojo{" +
                        "redirect = '" + redirect + '\'' +
                        ",data = '" + data + '\'' +
                        ",type = '" + type + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}