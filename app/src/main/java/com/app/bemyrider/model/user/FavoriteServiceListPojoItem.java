package com.app.bemyrider.model.user;

import com.google.gson.annotations.SerializedName;

public class FavoriteServiceListPojoItem {

	@SerializedName("category_name")
	private String categoryName;

	@SerializedName("address")
	private String address;

	@SerializedName("service_name")
	private String serviceName;

	@SerializedName("description")
	private String description;

	@SerializedName("profile_img")
	private String profileImg;

	@SerializedName("service_type")
	private String serviceType;

	@SerializedName("price")
	private String price;

	@SerializedName("provider_service_id")
	private String providerServiceId;

	@SerializedName("provider_id")
	private String providerId;

	@SerializedName("id")
	private String id;

	@SerializedName("subcategory")
	private String subcategory;

	@SerializedName("provider_name")
	private String providerName;

	@SerializedName("service_master_id")
	private String serviceMasterId;

	@SerializedName("delivery_type")
	private String deliveryType;

	@SerializedName("request_type")
	private String requestType;

	public String getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(String deliveryType) {
		this.deliveryType = deliveryType;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public void setCategoryName(String categoryName){
		this.categoryName = categoryName;
	}

	public String getCategoryName(){
		return categoryName;
	}

	public void setAddress(String address){
		this.address = address;
	}

	public String getAddress(){
		return address;
	}

	public void setServiceName(String serviceName){
		this.serviceName = serviceName;
	}

	public String getServiceName(){
		return serviceName;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}

	public void setProfileImg(String profileImg){
		this.profileImg = profileImg;
	}

	public String getProfileImg(){
		return profileImg;
	}

	public void setServiceType(String serviceType){
		this.serviceType = serviceType;
	}

	public String getServiceType(){
		return serviceType;
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

	public void setProviderId(String providerId){
		this.providerId = providerId;
	}

	public String getProviderId(){
		return providerId;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setSubcategory(String subcategory){
		this.subcategory = subcategory;
	}

	public String getSubcategory(){
		return subcategory;
	}

	public void setProviderName(String providerName){
		this.providerName = providerName;
	}

	public String getProviderName(){
		return providerName;
	}

	public void setServiceMasterId(String serviceMasterId){
		this.serviceMasterId = serviceMasterId;
	}

	public String getServiceMasterId(){
		return serviceMasterId;
	}

	@Override
 	public String toString(){
		return 
			"FavoriteServiceListPojoItem{" +
			"category_name = '" + categoryName + '\'' + 
			",address = '" + address + '\'' + 
			",service_name = '" + serviceName + '\'' + 
			",description = '" + description + '\'' + 
			",profile_img = '" + profileImg + '\'' + 
			",service_type = '" + serviceType + '\'' + 
			",price = '" + price + '\'' + 
			",provider_service_id = '" + providerServiceId + '\'' + 
			",provider_id = '" + providerId + '\'' + 
			",id = '" + id + '\'' + 
			",subcategory = '" + subcategory + '\'' + 
			",provider_name = '" + providerName + '\'' + 
			",service_master_id = '" + serviceMasterId + '\'' + 
			"}";
		}
}