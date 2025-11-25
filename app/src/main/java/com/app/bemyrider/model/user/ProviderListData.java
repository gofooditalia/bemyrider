package com.app.bemyrider.model.user;

import com.app.bemyrider.model.Pagination;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProviderListData {

    @SerializedName("pagination")
    private Pagination pagination;

    @SerializedName("service_list")
    private List<ProviderListItem> serviceList;

    public void setPagination(Pagination pagination){
        this.pagination = pagination;
    }

    public Pagination getPagination(){
        return pagination;
    }

    public void setServiceList(List<ProviderListItem> serviceList){
        this.serviceList = serviceList;
    }

    public List<ProviderListItem> getServiceList(){
        return serviceList;
    }

    @Override
    public String toString(){
        return
                "Data{" +
                        "pagination = '" + pagination + '\'' +
                        ",service_list = '" + serviceList + '\'' +
                        "}";
    }
}
