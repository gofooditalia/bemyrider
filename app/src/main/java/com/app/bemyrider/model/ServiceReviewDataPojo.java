package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServiceReviewDataPojo {

    @SerializedName("review_list")
    private List<ServiceReviewItem> reviewList;

    @SerializedName("pagination")
    private Pagination pagination;

    public List<ServiceReviewItem> getReviewList() {
        return reviewList;
    }

    public void setReviewList(List<ServiceReviewItem> reviewList) {
        this.reviewList = reviewList;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}