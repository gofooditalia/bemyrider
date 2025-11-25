package com.app.bemyrider.model.user;

import com.google.gson.annotations.SerializedName;


public class ProviderMainPojo {

@SerializedName("data")
private ProviderData providerData;
@SerializedName("status")

private Boolean status;
@SerializedName("type")

private String type;
@SerializedName("message")

private String message;

public ProviderData getData() {
return providerData;
}

public void setData(ProviderData providerData) {
this.providerData = providerData;
}

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

public String getMessage() {
return message;
}

public void setMessage(String message) {
this.message = message;
}

}