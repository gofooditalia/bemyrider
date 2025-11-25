package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class NotificationListPojoItem {

	@SerializedName("checked")
	private String checked;

	@SerializedName("id")
	private String id;

	@SerializedName("title")
	private String title;

	public void setChecked(String checked){
		this.checked = checked;
	}

	public String getChecked(){
		return checked;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}

	@Override
 	public String toString(){
		return 
			"NotificationListPojoItem{" +
			"checked = '" + checked + '\'' + 
			",id = '" + id + '\'' + 
			",title = '" + title + '\'' + 
			"}";
		}
}