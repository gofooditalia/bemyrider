package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class NotificationDataPOJO{

	@SerializedName("type")
	private String type;

	@SerializedName("message")
	private String message;

	@SerializedName("status")
	private boolean status;

	@SerializedName("data")
	private NotificationData notificationData;

	public String getType(){
		return type;
	}

	public String getMessage(){
		return message;
	}

	public boolean isStatus(){
		return status;
	}

	public NotificationData getNotificationData(){
		return notificationData;
	}
}