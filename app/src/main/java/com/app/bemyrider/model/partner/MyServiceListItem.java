package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class MyServiceListItem {

	@SerializedName("service_description")
	private String serviceDescription;

	@SerializedName("address")
	private String address;

	@SerializedName("category_name")
	private String categoryName;

	@SerializedName("service_name")
	private String serviceName;

	@SerializedName("service_image")
	private String serviceImage;

	@SerializedName("description")
	private String description;

	@SerializedName("subcategory_name")
	private String subcategoryName;

	@SerializedName("duration")
	private String duration;

	@SerializedName("subcategory_id")
	private String subcategoryId;

	@SerializedName("service_type")
	private String serviceType;

	@SerializedName("user_type")
	private String userType;

	@SerializedName("category_id")
	private String categoryId;

	@SerializedName("user_id")
	private String userId;

	@SerializedName("price")
	private String price;

	@SerializedName("provider_service_id")
	private String providerServiceId;

	@SerializedName("service_id")
	private String serviceId;

	public void setServiceDescription(String serviceDescription){
		this.serviceDescription = serviceDescription;
	}

	public String getServiceDescription(){
		return serviceDescription;
	}

	public void setAddress(String address){
		this.address = address;
	}

	public String getAddress(){
		return address;
	}

	public void setCategoryName(String categoryName){
		this.categoryName = categoryName;
	}

	public String getCategoryName(){
		return categoryName;
	}

	public void setServiceName(String serviceName){
		this.serviceName = serviceName;
	}

	public String getServiceName(){
		return serviceName;
	}

	public void setServiceImage(String serviceImage){
		this.serviceImage = serviceImage;
	}

	public String getServiceImage(){
		return serviceImage;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}

	public void setSubcategoryName(String subcategoryName){
		this.subcategoryName = subcategoryName;
	}

	public String getSubcategoryName(){
		return subcategoryName;
	}

	public void setDuration(String duration){
		this.duration = duration;
	}

	public String getDuration(){
		return duration;
	}

	public void setSubcategoryId(String subcategoryId){
		this.subcategoryId = subcategoryId;
	}

	public String getSubcategoryId(){
		return subcategoryId;
	}

	public void setServiceType(String serviceType){
		this.serviceType = serviceType;
	}

	public String getServiceType(){
		return serviceType;
	}

	public void setUserType(String userType){
		this.userType = userType;
	}

	public String getUserType(){
		return userType;
	}

	public void setCategoryId(String categoryId){
		this.categoryId = categoryId;
	}

	public String getCategoryId(){
		return categoryId;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return userId;
	}

	public void setPrice(String price){
		this.price = price;
	}

	public String getPrice(){
		return price;
	}

	public void setProviderServiceId(String providerServiceId){
		this.providerServiceId = providerServiceId;
	}

	public String getProviderServiceId(){
		return providerServiceId;
	}

	public void setServiceId(String serviceId){
		this.serviceId = serviceId;
	}

	public String getServiceId(){
		return serviceId;
	}

	@Override
 	public String toString(){
		return 
			"MyServiceListItem{" +
			"service_description = '" + serviceDescription + '\'' + 
			",address = '" + address + '\'' + 
			",category_name = '" + categoryName + '\'' + 
			",service_name = '" + serviceName + '\'' + 
			",service_image = '" + serviceImage + '\'' + 
			",description = '" + description + '\'' + 
			",subcategory_name = '" + subcategoryName + '\'' + 
			",duration = '" + duration + '\'' + 
			",subcategory_id = '" + subcategoryId + '\'' + 
			",service_type = '" + serviceType + '\'' + 
			",user_type = '" + userType + '\'' + 
			",category_id = '" + categoryId + '\'' + 
			",user_id = '" + userId + '\'' + 
			",price = '" + price + '\'' + 
			",provider_service_id = '" + providerServiceId + '\'' + 
			",service_id = '" + serviceId + '\'' + 
			"}";
		}
}