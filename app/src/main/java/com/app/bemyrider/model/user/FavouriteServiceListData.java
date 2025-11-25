package com.app.bemyrider.model.user;

import com.app.bemyrider.model.Pagination;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FavouriteServiceListData {

    @SerializedName("pagination")
    private Pagination pagination;

    @SerializedName("services")
    private List<FavoriteServiceListPojoItem> serviceList;

    public void setPagination(Pagination pagination){
        this.pagination = pagination;
    }

    public Pagination getPagination(){
        return pagination;
    }

    public List<FavoriteServiceListPojoItem> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<FavoriteServiceListPojoItem> serviceList) {
        this.serviceList = serviceList;
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
