package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class BulkInvoicePojoItem {

    @SerializedName("file_name")
    private String fileName;

    @SerializedName("count")
    private int count;

    public String getFileName() { return fileName; }
    public int getCount() { return count; }
}
