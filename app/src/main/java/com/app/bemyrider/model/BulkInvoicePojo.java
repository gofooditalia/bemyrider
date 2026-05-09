package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class BulkInvoicePojo {

    @SerializedName("data")
    private BulkInvoicePojoItem data;

    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private boolean status;

    public BulkInvoicePojoItem getData() { return data; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isStatus() { return status; }
}
