package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class SignupPojoItem {

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
			"SignupPojoItem{" +
			"user_type = '" + userType + '\'' + 
			",user_id = '" + userId + '\'' + 
			"}";
		}
}