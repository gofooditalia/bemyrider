package com.app.bemyrider.model.user;

import com.google.gson.annotations.SerializedName;

public class PopularTaskerItem {

    @SerializedName("userimg")
    private String userimg;

    @SerializedName("category_id")
    private String categoryId;

    @SerializedName("provider")
    private String provider;

    @SerializedName("service")
    private String service;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("userid")
    private String userid;

    @SerializedName("username")
    private String username;

    @SerializedName("longitude")
    private String longitude;

    public String getUserimg() {
        return userimg;
    }

    public void setUserimg(String userimg) {
        this.userimg = userimg;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return
                "DataItem{" +
                        "userimg = '" + userimg + '\'' +
                        ",category_id = '" + categoryId + '\'' +
                        ",provider = '" + provider + '\'' +
                        ",service = '" + service + '\'' +
                        ",latitude = '" + latitude + '\'' +
                        ",userid = '" + userid + '\'' +
                        ",username = '" + username + '\'' +
                        ",longitude = '" + longitude + '\'' +
                        "}";
    }
}