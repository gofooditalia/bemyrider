package com.app.bemyrider.model.partner;

import com.app.bemyrider.model.Pagination;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MyServiceListDataPojo {

    @SerializedName("service_list")
    private List<MyServiceListItem> serviceList;

    @SerializedName("pagination")
    private Pagination pagination;

    public List<MyServiceListItem> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<MyServiceListItem> serviceList) {
        this.serviceList = serviceList;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}