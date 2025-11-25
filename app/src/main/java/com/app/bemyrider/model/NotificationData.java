package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationData{

	@SerializedName("notificationList")
	private List<NotificationListItem> notificationList;

	@SerializedName("pagination")
	private Pagination pagination;

	public List<NotificationListItem> getNotificationList(){
		return notificationList;
	}

	public Pagination getPagination(){
		return pagination;
	}
}