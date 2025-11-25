package com.app.bemyrider.model.user;

import com.google.gson.annotations.SerializedName;

public class ProviderListItem {

	@SerializedName("service_description")
	private String serviceDescription;

	@SerializedName("address")
	private String address;

	@SerializedName("provider_last_name")
	private String providerLastName;

	@SerializedName("provider_image")
	private String providerImage;

	@SerializedName("duration")
	private String duration;

	@SerializedName("subcategory_id")
	private String subcategoryId;

	@SerializedName("provider_first_name")
	private String providerFirstName;

	@SerializedName("service_type")
	private String serviceType;

	@SerializedName("user_type")
	private String userType;

	@SerializedName("category_id")
	private String categoryId;

	@SerializedName("price")
	private String price;

	@SerializedName("provider_service_id")
	private String providerServiceId;

	@SerializedName("service_id")
	private String serviceId;

	@SerializedName("avg_rating")
	private float avgRating;

	@SerializedName("provider_id")
	private String providerId;

	@SerializedName("total_records")
	private String total_records;

	@SerializedName("favorite_id")
	private int favoriteId;

	public void setTotalRecords(String total_records){
		this.total_records= total_records;
	}

	public String getTotalRecords(){
		return total_records;
	}

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

	public void setProviderLastName(String providerLastName){
		this.providerLastName = providerLastName;
	}

	public String getProviderLastName(){
		return providerLastName;
	}

	public void setProviderImage(String providerImage){
		this.providerImage = providerImage;
	}

	public String getProviderImage(){
		return providerImage;
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

	public void setProviderFirstName(String providerFirstName){
		this.providerFirstName = providerFirstName;
	}

	public String getProviderFirstName(){
		return providerFirstName;
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

	public void setAvgRating(float avgRating){
		this.avgRating = avgRating;
	}

	public float getAvgRating(){
		return avgRating;
	}

	public void setProviderId(String providerId){
		this.providerId = providerId;
	}

	public String getProviderId(){
		return providerId;
	}

	public void setFavoriteId(int favoriteId){
		this.favoriteId = favoriteId;
	}

	public int getFavoriteId(){
		return favoriteId;
	}

	@Override
 	public String toString(){
		return 
			"ProviderListItem{" +
			"service_description = '" + serviceDescription + '\'' + 
			",address = '" + address + '\'' + 
			",provider_last_name = '" + providerLastName + '\'' + 
			",provider_image = '" + providerImage + '\'' + 
			",duration = '" + duration + '\'' + 
			",subcategory_id = '" + subcategoryId + '\'' + 
			",provider_first_name = '" + providerFirstName + '\'' + 
			",service_type = '" + serviceType + '\'' + 
			",user_type = '" + userType + '\'' + 
			",category_id = '" + categoryId + '\'' + 
			",price = '" + price + '\'' + 
			",provider_service_id = '" + providerServiceId + '\'' + 
			",service_id = '" + serviceId + '\'' + 
			",avg_rating = '" + avgRating + '\'' + 
			",provider_id = '" + providerId + '\'' + 
			",favorite_id = '" + favoriteId + '\'' + 
			"}";
		}
}