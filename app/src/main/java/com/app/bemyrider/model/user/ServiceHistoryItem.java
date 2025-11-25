package com.app.bemyrider.model.user;

import com.google.gson.annotations.SerializedName;

public class ServiceHistoryItem{

	@SerializedName("payment_mode")
	private String paymentMode;

	@SerializedName("category_name")
	private String categoryName;

	@SerializedName("service_name")
	private String serviceName;

	@SerializedName("service_request_id")
	private String serviceRequestId;

	@SerializedName("description")
	private String description;

	@SerializedName("provider_lnamem")
	private String providerLnamem;

	@SerializedName("bookingStartTime")
	private String bookingStartTime;

	@SerializedName("bookingEndTime")
	private String bookingEndTime;

	@SerializedName("duration")
	private String duration;

	@SerializedName("profile_img")
	private String profileImg;

	@SerializedName("createdDate")
	private String createdDate;

	@SerializedName("provider_fname")
	private String providerFname;

	@SerializedName("price")
	private String price;

	@SerializedName("serviceStatus")
	private String serviceStatus;

	@SerializedName("provider_service_id")
	private String providerServiceId;

	@SerializedName("service_id")
	private String serviceId;

	@SerializedName("sub_category_name")
	private String subCategoryName;

	@SerializedName("bookingAddress")
	private String bookingAddress;

	@SerializedName("date_required")
	private String dateRequired;

	public void setPaymentMode(String paymentMode){
		this.paymentMode = paymentMode;
	}

	public String getPaymentMode(){
		return paymentMode;
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

	public void setServiceRequestId(String serviceRequestId){
		this.serviceRequestId = serviceRequestId;
	}

	public String getServiceRequestId(){
		return serviceRequestId;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}

	public void setProviderLnamem(String providerLnamem){
		this.providerLnamem = providerLnamem;
	}

	public String getProviderLnamem(){
		return providerLnamem;
	}

	public void setBookingStartTime(String bookingStartTime){
		this.bookingStartTime = bookingStartTime;
	}

	public String getBookingStartTime(){
		return bookingStartTime;
	}

	public void setBookingEndTime(String bookingEndTime){
		this.bookingEndTime = bookingEndTime;
	}

	public String getBookingEndTime(){
		return bookingEndTime;
	}

	public void setDuration(String duration){
		this.duration = duration;
	}

	public String getDuration(){
		return duration;
	}

	public void setProfileImg(String profileImg){
		this.profileImg = profileImg;
	}

	public String getProfileImg(){
		return profileImg;
	}

	public void setCreatedDate(String createdDate){
		this.createdDate = createdDate;
	}

	public String getCreatedDate(){
		return createdDate;
	}

	public void setProviderFname(String providerFname){
		this.providerFname = providerFname;
	}

	public String getProviderFname(){
		return providerFname;
	}

	public void setPrice(String price){
		this.price = price;
	}

	public String getPrice(){
		return price;
	}

	public void setServiceStatus(String serviceStatus){
		this.serviceStatus = serviceStatus;
	}

	public String getServiceStatus(){
		return serviceStatus;
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

	public void setSubCategoryName(String subCategoryName){
		this.subCategoryName = subCategoryName;
	}

	public String getSubCategoryName(){
		return subCategoryName;
	}

	public void setBookingAddress(String bookingAddress){
		this.bookingAddress = bookingAddress;
	}

	public String getBookingAddress(){
		return bookingAddress;
	}

	public void setDateRequired(String dateRequired){
		this.dateRequired = dateRequired;
	}

	public String getDateRequired(){
		return dateRequired;
	}

	@Override
 	public String toString(){
		return 
			"UpcomingItem{" + 
			"payment_mode = '" + paymentMode + '\'' + 
			",category_name = '" + categoryName + '\'' + 
			",service_name = '" + serviceName + '\'' + 
			",service_request_id = '" + serviceRequestId + '\'' + 
			",description = '" + description + '\'' + 
			",provider_lnamem = '" + providerLnamem + '\'' + 
			",bookingStartTime = '" + bookingStartTime + '\'' + 
			",bookingEndTime = '" + bookingEndTime + '\'' + 
			",duration = '" + duration + '\'' + 
			",profile_img = '" + profileImg + '\'' + 
			",createdDate = '" + createdDate + '\'' + 
			",provider_fname = '" + providerFname + '\'' + 
			",price = '" + price + '\'' + 
			",serviceStatus = '" + serviceStatus + '\'' + 
			",provider_service_id = '" + providerServiceId + '\'' + 
			",service_id = '" + serviceId + '\'' + 
			",sub_category_name = '" + subCategoryName + '\'' + 
			",bookingAddress = '" + bookingAddress + '\'' + 
			",date_required = '" + dateRequired + '\'' + 
			"}";
		}
}