package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class RegistrationItem {

	@SerializedName("user_type")
	private String userType;

	@SerializedName("user_id")
	private String userId;

	public void setUserType(String userType){
		this.userType = userType;
	}

	public String getUserType(){
		return userType;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return userId;
	}

	@Override
 	public String toString(){
		return 
			"ChangeProfileItem{" +
			"user_type = '" + userType + '\'' + 
			",user_id = '" + userId + '\'' + 
			"}";
		}
}