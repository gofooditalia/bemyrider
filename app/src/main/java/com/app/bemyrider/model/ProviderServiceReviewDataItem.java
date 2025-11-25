package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProviderServiceReviewDataItem implements Serializable {

    @SerializedName("review_id")
    private String reviewId;

    @SerializedName("review")
    private String review;

    @SerializedName("service_request_id")
    private String serviceRequestId;

    @SerializedName("rating")
    private String rating;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("profile_img")
    private String profile_img;
//    created_user
    @SerializedName("created_date")
    private String created_date;

    @SerializedName("created_user")
    private String created_user;

    public void setCreatedUser(String created_user) {
        this.created_user= created_user;
    }

    public String getCreatedUser() {
        return created_user;
    }

    public void setCreatedDate(String created_date) {
        this.created_date = created_date;
    }

    public String getCreatedDate() {
        return created_date;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setProfileImage(String profile_img) {
        this.profile_img= profile_img;
    }

    public String getProfileImage() {
        return profile_img;
    }

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public String getUserName() {
        return user_name;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getReview() {
        return review;
    }

    public void setServiceRequestId(String serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public String getServiceRequestId() {
        return serviceRequestId;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return
                "ProviderServiceReviewDataItem{" +
                        "review_id = '" + reviewId + '\'' +
                        ",review = '" + review + '\'' +
                        ",service_request_id = '" + serviceRequestId + '\'' +
                        ",rating = '" + rating + '\'' +
                        "}";
    }
}