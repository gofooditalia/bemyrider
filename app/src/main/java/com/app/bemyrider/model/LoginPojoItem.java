package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class LoginPojoItem {

	@SerializedName("email_id")
	private String emailId;

	@SerializedName("user_type")
	private String userType;

	@SerializedName("user_id")
	private String userId;

	@SerializedName("user_name")
	private String userName;

	@SerializedName("country_code_id")
	private String countryCodeId;

	@SerializedName("last_name")
	private String lastName;

	@SerializedName("first_name")
	private String firstName;

	public void setEmailId(String emailId){
		this.emailId = emailId;
	}

	public String getEmailId(){
		return emailId;
	}

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

	public void setUserName(String userName){
		this.userName = userName;
	}

	public String getUserName(){
		return userName;
	}

	public void setCountryCodeId(String countryCodeId){
		this.countryCodeId = countryCodeId;
	}

	public String getCountryCodeId(){
		return countryCodeId;
	}

	public void setLastName(String lastName){
		this.lastName = lastName;
	}

	public String getLastName(){
		return lastName;
	}

	public void setFirstName(String firstName){
		this.firstName = firstName;
	}

	public String getFirstName(){
		return firstName;
	}

	@Override
 	public String toString(){
		return 
			"LoginPojoItem{" +
			"email_id = '" + emailId + '\'' + 
			",user_type = '" + userType + '\'' + 
			",user_id = '" + userId + '\'' + 
			",user_name = '" + userName + '\'' + 
			",country_code_id = '" + countryCodeId + '\'' + 
			",last_name = '" + lastName + '\'' + 
			",first_name = '" + firstName + '\'' + 
			"}";
		}
}