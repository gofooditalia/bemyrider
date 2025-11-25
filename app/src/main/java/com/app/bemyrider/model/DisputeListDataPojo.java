package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DisputeListDataPojo {

    @SerializedName("dispute_list")
    private List<DisputeListPojoItem> disputeList;

    @SerializedName("pagination")
    private Pagination pagination;

    public List<DisputeListPojoItem> getDisputeList() {
        return disputeList;
    }

    public void setDisputeList(List<DisputeListPojoItem> disputeList) {
        this.disputeList = disputeList;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    @Override
    public String toString() {
        return "DisputeListDataPojo{" +
                "disputeList=" + disputeList +
                ", pagination=" + pagination +
                '}';
    }
}