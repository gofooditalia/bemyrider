package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class MessageListPojoItem {

	@SerializedName("to_user")
	private String toUser;

	@SerializedName("message_text")
	private String messageText;

	@SerializedName("to_user_email")
	private String toUserEmail;

	@SerializedName("service_name")
	private String serviceName;

	@SerializedName("service_id")
	private String serviceId;

	@SerializedName("isRead")
	private String isRead;

	@SerializedName("to_user_name")
	private String toUserName;

	@SerializedName("message_id")
	private String messageId;

	@SerializedName("to_user_type")
	private String toUserType;

	@SerializedName("service_master_id")
	private String serviceMasterId;

	@SerializedName("to_profile_img")
	private String toProfileImg;

	@SerializedName("createdDate")
	private String createdDate;

	public void setCreatedDate(String createdDate){
		this.createdDate= createdDate;
	}

	public String getCreatedDate(){
		return createdDate;
	}

	public void setToUser(String toUser){
		this.toUser = toUser;
	}

	public String getToUser(){
		return toUser;
	}

	public void setMessageText(String messageText){
		this.messageText = messageText;
	}

	public String getMessageText(){
		return messageText;
	}

	public void setToUserEmail(String toUserEmail){
		this.toUserEmail = toUserEmail;
	}

	public String getToUserEmail(){
		return toUserEmail;
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

	public void setIsRead(String isRead){
		this.isRead = isRead;
	}

	public String getIsRead(){
		return isRead;
	}

	public void setToUserName(String toUserName){
		this.toUserName = toUserName;
	}

	public String getToUserName(){
		return toUserName;
	}

	public void setMessageId(String messageId){
		this.messageId = messageId;
	}

	public String getMessageId(){
		return messageId;
	}

	public void setToUserType(String toUserType){
		this.toUserType = toUserType;
	}

	public String getToUserType(){
		return toUserType;
	}

	public void setServiceMasterId(String serviceMasterId){
		this.serviceMasterId = serviceMasterId;
	}

	public String getServiceMasterId(){
		return serviceMasterId;
	}

	public void setToProfileImg(String toProfileImg){
		this.toProfileImg = toProfileImg;
	}

	public String getToProfileImg(){
		return toProfileImg;
	}

	@Override
 	public String toString(){
		return 
			"MessageListPojoItem{" +
			"to_user = '" + toUser + '\'' + 
			",message_text = '" + messageText + '\'' + 
			",to_user_email = '" + toUserEmail + '\'' + 
			",service_name = '" + serviceName + '\'' + 
			",service_id = '" + serviceId + '\'' + 
			",isRead = '" + isRead + '\'' + 
			",to_user_name = '" + toUserName + '\'' + 
			",message_id = '" + messageId + '\'' + 
			",to_user_type = '" + toUserType + '\'' + 
			",service_master_id = '" + serviceMasterId + '\'' + 
			",to_profile_img = '" + toProfileImg + '\'' + 
			"}";
		}
}