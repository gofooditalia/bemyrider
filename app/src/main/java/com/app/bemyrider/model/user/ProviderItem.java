package com.app.bemyrider.model.user;


import com.google.gson.annotations.SerializedName;


public class ProviderItem {

    @SerializedName("total_records")
    private Integer totalRecords;

    @SerializedName("avg_rating")
    private String avgRating;

    @SerializedName("hour_rate")
    private String hourRate;

    public String getHourRate() {
        return hourRate;
    }

    public void setHourRate(String hourRate) {
        this.hourRate = hourRate;
    }

    @SerializedName("total_ratting")
    private String totalRatting;

    @SerializedName("total_reviews")
    private String totalReviews;

    @SerializedName("user_type")
    private String userType;

    @SerializedName("provider_first_name")
    private String providerFirstName;

    @SerializedName("provider_last_name")
    private String providerLastName;

    @SerializedName("address")
    private String address;

    @SerializedName("provider_image")
    private String providerImage;

    @SerializedName("provider_id")
    private String providerId;

    @SerializedName("small_delivery")
    private String smallDelivery;

    @SerializedName("medium_delivery")
    private String mediumDelivery;

    @SerializedName("large_delivery")
    private String largeDelivery;

    @SerializedName("request_type")
    private String requestType;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public String getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(String avgRating) {
        this.avgRating = avgRating;
    }

    public String getTotalRatting() {
        return totalRatting;
    }

    public void setTotalRatting(String totalRatting) {
        this.totalRatting = totalRatting;
    }

    public String getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(String totalReviews) {
        this.totalReviews = totalReviews;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getProviderFirstName() {
        return providerFirstName;
    }

    public void setProviderFirstName(String providerFirstName) {
        this.providerFirstName = providerFirstName;
    }

    public String getProviderLastName() {
        return providerLastName;
    }

    public void setProviderLastName(String providerLastName) {
        this.providerLastName = providerLastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProviderImage() {
        return providerImage;
    }

    public void setProviderImage(String providerImage) {
        this.providerImage = providerImage;
    }

    public String getSmallDelivery() {
        return smallDelivery;
    }

    public void setSmallDelivery(String smallDelivery) {
        this.smallDelivery = smallDelivery;
    }

    public String getMediumDelivery() {
        return mediumDelivery;
    }

    public void setMediumDelivery(String mediumDelivery) {
        this.mediumDelivery = mediumDelivery;
    }

    public String getLargeDelivery() {
        return largeDelivery;
    }

    public void setLargeDelivery(String largeDelivery) {
        this.largeDelivery = largeDelivery;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}