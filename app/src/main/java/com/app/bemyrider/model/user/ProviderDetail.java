package com.app.bemyrider.model.user;

public class ProviderDetail{
	private String lastName;
	private String profileImg;
	private String payment_mode;
	private String cityName;
	private String stateName;
	private String rattings;
	private String countryName;
	private String first_name;
	private String contactNumber;
	private String email;
	private String provider_description;

	public void setLastName(String lastName){
		this.lastName = lastName;
	}

	public String getLastName(){
		return lastName;
	}

	public void setProfileImg(String profileImg){
		this.profileImg = profileImg;
	}

	public String getProfileImg(){
		return profileImg;
	}

	public void setPaymentMode(String paymentMode){
		this.payment_mode = paymentMode;
	}

	public String getPaymentMode(){
		return payment_mode;
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
		this.first_name = firstName;
	}

	public String getFirstName(){
		return first_name;
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

	public void setProvider_description(String provider_description){
		this.provider_description = provider_description;
	}

	public String getProvider_description(){
		return provider_description;
	}

	@Override
 	public String toString(){
		return 
			"ProviderDetail{" + 
			"lastName = '" + lastName + '\'' + 
			",profile_img = '" + profileImg + '\'' + 
			",payment_mode = '" + payment_mode + '\'' +
			",cityName = '" + cityName + '\'' + 
			",stateName = '" + stateName + '\'' + 
			",rattings = '" + rattings + '\'' + 
			",countryName = '" + countryName + '\'' + 
			",first_name = '" + first_name + '\'' +
			",contact_number = '" + contactNumber + '\'' + 
			",email = '" + email + '\'' + 
			"}";
		}
}
