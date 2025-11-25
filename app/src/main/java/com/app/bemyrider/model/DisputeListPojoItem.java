package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class DisputeListPojoItem {

	@SerializedName("dispute_title")
	private String disputeTitle;

	@SerializedName("customer_lastname")
	private String customerLastname;

	@SerializedName("created_user")
	private String createdUser;

	@SerializedName("dispute_message")
	private String disputeMessage;
//	service_request_id
	@SerializedName("escalate_admin")
	private String escalate_admin;

	@SerializedName("service_request_id")
	private String service_request_id;

	@SerializedName("provider_firstname")
	private String providerFirstname;

	@SerializedName("customer_firstname")
	private String customerFirstname;

	@SerializedName("provider_lastname")
	private String providerLastname;

	@SerializedName("service_name")
	private String serviceName;

	@SerializedName("dispute_id")
	private String disputeId;

	@SerializedName("customer_img")
	private String customerImg;

	@SerializedName("createdDate")
	private String createdDate;

	@SerializedName("service_id")
	private String serviceId;

	@SerializedName("provider_id")
	private String providerId;

	@SerializedName("dispute_message_date")
	private String disputeMessageDate;

	@SerializedName("customer_id")
	private String customerId;

	@SerializedName("status")
	private String status;

	@SerializedName("provider_img")
	private String providerImg;

	public void setServiceRequestId(String service_request_id){
		this.service_request_id= service_request_id;
	}

	public String getServiceRequestId(){
		return service_request_id;
	}

	public void setEscalateToAdmin(String escalate_admin){
		this.escalate_admin= escalate_admin;
	}

	public String getEscalateToAdmin(){
		return escalate_admin;
	}

	public void setDisputeTitle(String disputeTitle){
		this.disputeTitle = disputeTitle;
	}

	public String getDisputeTitle(){
		return disputeTitle;
	}

	public void setCustomerLastname(String customerLastname){
		this.customerLastname = customerLastname;
	}

	public String getCustomerLastname(){
		return customerLastname;
	}

	public void setCreatedUser(String createdUser){
		this.createdUser = createdUser;
	}

	public String getCreatedUser(){
		return createdUser;
	}

	public void setDisputeMessage(String disputeMessage){
		this.disputeMessage = disputeMessage;
	}

	public String getDisputeMessage(){
		return disputeMessage;
	}

	public void setProviderFirstname(String providerFirstname){
		this.providerFirstname = providerFirstname;
	}

	public String getProviderFirstname(){
		return providerFirstname;
	}

	public void setCustomerFirstname(String customerFirstname){
		this.customerFirstname = customerFirstname;
	}

	public String getCustomerFirstname(){
		return customerFirstname;
	}

	public void setProviderLastname(String providerLastname){
		this.providerLastname = providerLastname;
	}

	public String getProviderLastname(){
		return providerLastname;
	}

	public void setServiceName(String serviceName){
		this.serviceName = serviceName;
	}

	public String getServiceName(){
		return serviceName;
	}

	public void setDisputeId(String disputeId){
		this.disputeId = disputeId;
	}

	public String getDisputeId(){
		return disputeId;
	}

	public void setCustomerImg(String customerImg){
		this.customerImg = customerImg;
	}

	public String getCustomerImg(){
		return customerImg;
	}

	public void setCreatedDate(String createdDate){
		this.createdDate = createdDate;
	}

	public String getCreatedDate(){
		return createdDate;
	}

	public void setServiceId(String serviceId){
		this.serviceId = serviceId;
	}

	public String getServiceId(){
		return serviceId;
	}

	public void setProviderId(String providerId){
		this.providerId = providerId;
	}

	public String getProviderId(){
		return providerId;
	}

	public void setDisputeMessageDate(String disputeMessageDate){
		this.disputeMessageDate = disputeMessageDate;
	}

	public String getDisputeMessageDate(){
		return disputeMessageDate;
	}

	public void setCustomerId(String customerId){
		this.customerId = customerId;
	}

	public String getCustomerId(){
		return customerId;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}

	public void setProviderImg(String providerImg){
		this.providerImg = providerImg;
	}

	public String getProviderImg(){
		return providerImg;
	}

	@Override
 	public String toString(){
		return
			"DisputeListPojoItem{" +
			"dispute_title = '" + disputeTitle + '\'' +
			",customer_lastname = '" + customerLastname + '\'' +
			",created_user = '" + createdUser + '\'' +
			",dispute_message = '" + disputeMessage + '\'' +
			",provider_firstname = '" + providerFirstname + '\'' +
			",customer_firstname = '" + customerFirstname + '\'' +
			",provider_lastname = '" + providerLastname + '\'' +
			",service_name = '" + serviceName + '\'' +
			",dispute_id = '" + disputeId + '\'' +
			",customer_img = '" + customerImg + '\'' +
			",createdDate = '" + createdDate + '\'' +
			",service_id = '" + serviceId + '\'' +
			",provider_id = '" + providerId + '\'' +
			",dispute_message_date = '" + disputeMessageDate + '\'' +
			",customer_id = '" + customerId + '\'' +
			",status = '" + status + '\'' +
			",provider_img = '" + providerImg + '\'' +
			"}";
		}
}