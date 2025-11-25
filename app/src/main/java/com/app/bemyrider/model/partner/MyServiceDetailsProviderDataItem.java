package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class MyServiceDetailsProviderDataItem {

	@SerializedName("lastName")
	private String lastName;

	@SerializedName("provider_description")
	private String providerDescription;

	@SerializedName("profile_img")
	private String profileImg;

	@SerializedName("payment_mode")
	private String paymentMode;

	@SerializedName("cityName")
	private String cityName;

	@SerializedName("stateName")
	private String stateName;

	@SerializedName("rattings")
	private String rattings;

	@SerializedName("countryName")
	private String countryName;

	@SerializedName("first_name")
	private String firstName;

	@SerializedName("contact_number")
	private String contactNumber;

	@SerializedName("email")
	private String email;

	public void setLastName(String lastName){
		this.lastName = lastName;
	}

	public String getLastName(){
		return lastName;
	}

	public void setProviderDescription(String providerDescription){
		this.providerDescription = providerDescription;
	}

	public String getProviderDescription(){
		return providerDescription;
	}

	public void setProfileImg(String profileImg){
		this.profileImg = profileImg;
	}

	public String getProfileImg(){
		return profileImg;
	}

	public void setPaymentMode(String paymentMode){
		this.paymentMode = paymentMode;
	}

	public String getPaymentMode(){
		return paymentMode;
	}

	public void setCityName(String cityName){
		this.cityName = cityName;
	}

	public String getCityName(){
		return cityName;
	}

	public void setStateName(String stateName){
		this.stateName = stateName;
	}

	public String getStateName(){
		return stateName;
	}

	public void setRattings(String rattings){
		this.rattings = rattings;
	}

	public String getRattings(){
		return rattings;
	}

	public void setCountryName(String countryName){
		this.countryName = countryName;
	}

	public String getCountryName(){
		return countryName;
	}

	public void setFirstName(String firstName){
		this.firstName = firstName;
	}

	public String getFirstName(){
		return firstName;
	}

	public void setContactNumber(String contactNumber){
		this.contactNumber = contactNumber;
	}

	public String getContactNumber(){
		return contactNumber;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return email;
	}

	@Override
 	public String toString(){
		return 
			"MyServiceDetailsProviderDataItem{" +
			"lastName = '" + lastName + '\'' + 
			",provider_description = '" + providerDescription + '\'' + 
			",profile_img = '" + profileImg + '\'' + 
			",payment_mode = '" + paymentMode + '\'' + 
			",cityName = '" + cityName + '\'' + 
			",stateName = '" + stateName + '\'' + 
			",rattings = '" + rattings + '\'' + 
			",countryName = '" + countryName + '\'' + 
			",first_name = '" + firstName + '\'' + 
			",contact_number = '" + contactNumber + '\'' + 
			",email = '" + email + '\'' + 
			"}";
		}
}