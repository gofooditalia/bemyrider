package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class ProfilePojo {

	@SerializedName("data")
	private ProfileItem data;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	public void setData(ProfileItem data){
		this.data = data;
	}

	public ProfileItem getData(){
		return data;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setStatus(boolean status){
		this.status = status;
	}

	public boolean isStatus(){
		return status;
	}

	@Override
 	public String toString(){
		return 
			"ProfilePojo{" + 
			"data = '" + data + '\'' + 
			",type = '" + type + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}