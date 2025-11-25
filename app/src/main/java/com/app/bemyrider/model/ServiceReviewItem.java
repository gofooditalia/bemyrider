package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class ServiceReviewItem {

    @SerializedName("review_id")
    private String reviewId;

    @SerializedName("address")
    private String address;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("user_image")
    private String userImage;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("provider_email")
    private String providerEmail;

    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("review_rating")
    private String reviewRating;

    @SerializedName("review_date")
    private String reviewDate;

    @SerializedName("provider_service_id")
    private String providerServiceId;

    @SerializedName("sub_category_name")
    private String subCategoryName;

    @SerializedName("service_master_id")
    private String serviceMasterId;
    //	created_user
    @SerializedName("review_desc")
    private String reviewDesc;

    @SerializedName("created_user")
    private String created_user;

    @SerializedName("isactive")
    private String isactive;

    public String getIsActive() {
        return isactive;
    }

    public void setIsActive(String isactive) {
        this.isactive = isactive;
    }

    public String getCreatedUser() {
        return created_user;
    }

    public void setCreatedUser(String created_user) {
        this.created_user = created_user;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProviderEmail() {
        return providerEmail;
    }

    public void setProviderEmail(String providerEmail) {
        this.providerEmail = providerEmail;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getReviewRating() {
        return reviewRating;
    }

    public void setReviewRating(String reviewRating) {
        this.reviewRating = reviewRating;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getProviderServiceId() {
        return providerServiceId;
    }

    public void setProviderServiceId(String providerServiceId) {
        this.providerServiceId = providerServiceId;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getServiceMasterId() {
        return serviceMasterId;
    }

    public void setServiceMasterId(String serviceMasterId) {
        this.serviceMasterId = serviceMasterId;
    }

    public String getReviewDesc() {
        return reviewDesc;
    }

    public void setReviewDesc(String reviewDesc) {
        this.reviewDesc = reviewDesc;
    }

    @Override
    public String toString() {
        return
                "ServiceReviewItem{" +
                        "review_id = '" + reviewId + '\'' +
                        ",address = '" + address + '\'' +
                        ",category_name = '" + categoryName + '\'' +
                        ",user_image = '" + userImage + '\'' +
                        ",user_name = '" + userName + '\'' +
                        ",provider_email = '" + providerEmail + '\'' +
                        ",service_name = '" + serviceName + '\'' +
                        ",review_rating = '" + reviewRating + '\'' +
                        ",review_date = '" + reviewDate + '\'' +
                        ",provider_service_id = '" + providerServiceId + '\'' +
                        ",sub_category_name = '" + subCategoryName + '\'' +
                        ",service_master_id = '" + serviceMasterId + '\'' +
                        ",review_desc = '" + reviewDesc + '\'' +
                        "}";
    }
}