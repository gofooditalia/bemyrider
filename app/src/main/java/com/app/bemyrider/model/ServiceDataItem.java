package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class ServiceDataItem {

    @SerializedName("service_type")
    private String serviceType;

    @SerializedName("createdDate")
    private String createdDate;

    @SerializedName("category_id")
    private String categoryId;

    @SerializedName("sub_category_id")
    private String subCategoryId;

    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("service_id")
    private String serviceId;

    @SerializedName("isActive")
    private String isActive;

    @SerializedName("service_img")
    private String serviceImg;

    @SerializedName("createdUser")
    private String createdUser;

    @SerializedName("service_img_url")
    private String serviceImgUrl;

    @SerializedName("selected")
    private String selected;

    @SerializedName("total")
    private String total;

    @SerializedName("provider_service_id")
    private String providerServiceId;

    @SerializedName("provider_id")
    private String providerId;

    @SerializedName("request_type")
    private String requestType;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public ServiceDataItem(String s, String s1) {
        this.serviceId = s;
        this.serviceName = s1;
    }

    public String getProviderServiceId() {
        return providerServiceId;
    }

    public void setProviderServiceId(String providerServiceId) {
        this.providerServiceId = providerServiceId;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(String subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getServiceImg() {
        return serviceImg;
    }

    public void setServiceImg(String serviceImg) {
        this.serviceImg = serviceImg;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public String getServiceImgUrl() {
        return serviceImgUrl;
    }

    public void setServiceImgUrl(String serviceImgUrl) {
        this.serviceImgUrl = serviceImgUrl;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return serviceName;
    }

    public String getSelected() {
        return selected;
    }
}