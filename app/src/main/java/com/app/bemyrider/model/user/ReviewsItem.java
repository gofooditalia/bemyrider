package com.app.bemyrider.model.user;

import com.google.gson.annotations.SerializedName;

public class ReviewsItem{

	@SerializedName("lastName")
	private String lastName;

	@SerializedName("profile_img")
	private String profileImg;

	@SerializedName("createdDate")
	private String createdDate;

	@SerializedName("review")
	private String review;

	@SerializedName("rattings")
	private String rattings;

	@SerializedName("first_name")
	private String firstName;

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

	public void setCreatedDate(String createdDate){
		this.createdDate = createdDate;
	}

	public String getCreatedDate(){
		return createdDate;
	}

	public void setReview(String review){
		this.review = review;
	}

	public String getReview(){
		return review;
	}

	public void setRattings(String rattings){
		this.rattings = rattings;
	}

	public String getRattings(){
		return rattings;
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
			"ReviewsItem{" + 
			"lastName = '" + lastName + '\'' + 
			",profile_img = '" + profileImg + '\'' + 
			",createdDate = '" + createdDate + '\'' + 
			",review = '" + review + '\'' + 
			",rattings = '" + rattings + '\'' + 
			",first_name = '" + firstName + '\'' + 
			"}";
		}
}