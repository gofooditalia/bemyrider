package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class ServiceListItem {

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

	public void setServiceType(String serviceType){
		this.serviceType = serviceType;
	}

	public String getServiceType(){
		return serviceType;
	}

	public void setCreatedDate(String createdDate){
		this.createdDate = createdDate;
	}

	public String getCreatedDate(){
		return createdDate;
	}

	public void setCategoryId(String categoryId){
		this.categoryId = categoryId;
	}

	public String getCategoryId(){
		return categoryId;
	}

	public void setSubCategoryId(String subCategoryId){
		this.subCategoryId = subCategoryId;
	}

	public String getSubCategoryId(){
		return subCategoryId;
	}

	public void setServiceName(String serviceName){
		this.serviceName = serviceName;
	}

	public String getServiceName(){
		return serviceName;
	}

	public void setServiceId(String serviceId){
		this.serviceId = serviceId;
	}

	public String getServiceId(){
		return serviceId;
	}

	public void setIsActive(String isActive){
		this.isActive = isActive;
	}

	public String getIsActive(){
		return isActive;
	}

	public void setServiceImg(String serviceImg){
		this.serviceImg = serviceImg;
	}

	public String getServiceImg(){
		return serviceImg;
	}

	public void setCreatedUser(String createdUser){
		this.createdUser = createdUser;
	}

	public String getCreatedUser(){
		return createdUser;
	}

	public void setServiceImgUrl(String serviceImgUrl){
		this.serviceImgUrl = serviceImgUrl;
	}

	public String getServiceImgUrl(){
		return serviceImgUrl;
	}

	@Override
 	public String toString(){
		return 
			"ServiceListItem{" +
			"service_type = '" + serviceType + '\'' + 
			",createdDate = '" + createdDate + '\'' + 
			",category_id = '" + categoryId + '\'' + 
			",sub_category_id = '" + subCategoryId + '\'' + 
			",service_name = '" + serviceName + '\'' + 
			",service_id = '" + serviceId + '\'' + 
			",isActive = '" + isActive + '\'' + 
			",service_img = '" + serviceImg + '\'' + 
			",createdUser = '" + createdUser + '\'' + 
			",service_img_url = '" + serviceImgUrl + '\'' + 
			"}";
		}
}