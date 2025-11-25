package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class SubCategoryItem {

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("category_id")
    private String categoryId;

    @SerializedName("category_small_banner")
    private String categorySmallBanner;

    @SerializedName("category_banner_img")
    private String categoryBannerImg;

    @SerializedName("description")
    private String description;

    @SerializedName("banner_url")
    private String bannerUrl;

    @SerializedName("isFeatured")
    private String isFeatured;

    @SerializedName("small_banner_url")
    private String smallBannerUrl;

    @SerializedName("selected")
    private String selected;

    @SerializedName("request_type")
    private String requestType;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public SubCategoryItem(String s, String s1) {
        this.categoryId = s;
        this.categoryName = s1;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategorySmallBanner() {
        return categorySmallBanner;
    }

    public void setCategorySmallBanner(String categorySmallBanner) {
        this.categorySmallBanner = categorySmallBanner;
    }

    public String getCategoryBannerImg() {
        return categoryBannerImg;
    }

    public void setCategoryBannerImg(String categoryBannerImg) {
        this.categoryBannerImg = categoryBannerImg;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(String isFeatured) {
        this.isFeatured = isFeatured;
    }

    public String getSmallBannerUrl() {
        return smallBannerUrl;
    }

    public void setSmallBannerUrl(String smallBannerUrl) {
        this.smallBannerUrl = smallBannerUrl;
    }

    public String getSelected() {
        return selected;
    }

    @Override
    public String toString() {
        return
                categoryName;
    }
}