package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class ServiceRequestUserData {

	@SerializedName("email_id")
	private String emailId;

	@SerializedName("payment_mode")
	private String paymentMode;

	@SerializedName("last_name")
	private String lastName;

	@SerializedName("bookingStartTime")
	private String bookingStartTime;

	@SerializedName("contact_number")
	private String contactNumber;

	@SerializedName("bookingEndTime")
	private String bookingEndTime;

	@SerializedName("profile_img")
	private String profileImg;

	@SerializedName("cityName")
	private String cityName;

	@SerializedName("stateName")
	private String stateName;

	@SerializedName("review")
	private String review;

	@SerializedName("retting")
	private String retting;

	@SerializedName("review_time")
	private String reviewTime;

	@SerializedName("countryName")
	private String countryName;

	@SerializedName("first_name")
	private String firstName;

	@SerializedName("bookingAddress")
	private String bookingAddress;

	@SerializedName("date_required")
	private String dateRequired;

	public void setEmailId(String emailId){
		this.emailId = emailId;
	}

	public String getEmailId(){
		return emailId;
	}

	public void setPaymentMode(String paymentMode){
		this.paymentMode = paymentMode;
	}

	public String getPaymentMode(){
		return paymentMode;
	}

	public void setLastName(String lastName){
		this.lastName = lastName;
	}

	public String getLastName(){
		return lastName;
	}

	public void setBookingStartTime(String bookingStartTime){
		this.bookingStartTime = bookingStartTime;
	}

	public String getBookingStartTime(){
		return bookingStartTime;
	}

	public void setContactNumber(String contactNumber){
		this.contactNumber = contactNumber;
	}

	public String getContactNumber(){
		return contactNumber;
	}

	public void setBookingEndTime(String bookingEndTime){
		this.bookingEndTime = bookingEndTime;
	}

	public String getBookingEndTime(){
		return bookingEndTime;
	}

	public void setProfileImg(String profileImg){
		this.profileImg = profileImg;
	}

	public String getProfileImg(){
		return profileImg;
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

	public void setReview(String review){
		this.review = review;
	}

	public String getReview(){
		return review;
	}

	public void setRetting(String retting){
		this.retting = retting;
	}

	public String getRetting(){
		return retting;
	}

	public void setReviewTime(String reviewTime){
		this.reviewTime = reviewTime;
	}

	public String getReviewTime(){
		return reviewTime;
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

	public void setBookingAddress(String bookingAddress){
		this.bookingAddress = bookingAddress;
	}

	public String getBookingAddress(){
		return bookingAddress;
	}

	public void setDateRequired(String dateRequired){
		this.dateRequired = dateRequired;
	}

	public String getDateRequired(){
		return dateRequired;
	}

	@Override
 	public String toString(){
		return 
			"UserData{" + 
			"email_id = '" + emailId + '\'' + 
			",payment_mode = '" + paymentMode + '\'' + 
			",last_name = '" + lastName + '\'' + 
			",bookingStartTime = '" + bookingStartTime + '\'' + 
			",contact_number = '" + contactNumber + '\'' + 
			",bookingEndTime = '" + bookingEndTime + '\'' + 
			",profile_img = '" + profileImg + '\'' + 
			",cityName = '" + cityName + '\'' + 
			",stateName = '" + stateName + '\'' + 
			",review = '" + review + '\'' + 
			",retting = '" + retting + '\'' + 
			",review_time = '" + reviewTime + '\'' + 
			",countryName = '" + countryName + '\'' + 
			",first_name = '" + firstName + '\'' + 
			",bookingAddress = '" + bookingAddress + '\'' + 
			",date_required = '" + dateRequired + '\'' + 
			"}";
		}
}