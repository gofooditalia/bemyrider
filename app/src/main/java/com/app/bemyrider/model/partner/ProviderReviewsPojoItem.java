package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class ProviderReviewsPojoItem {

	@SerializedName("review_id")
	private String reviewId;

	@SerializedName("profile_img")
	private String profileImg;

	@SerializedName("customer_lastname")
	private String customerLastname;

	@SerializedName("customer_firstname")
	private String customerFirstname;

	@SerializedName("review")
	private String review;

	@SerializedName("rattings")
	private String rattings;

	public void setReviewId(String reviewId){
		this.reviewId = reviewId;
	}

	public String getReviewId(){
		return reviewId;
	}

	public void setProfileImg(String profileImg){
		this.profileImg = profileImg;
	}

	public String getProfileImg(){
		return profileImg;
	}

	public void setCustomerLastname(String customerLastname){
		this.customerLastname = customerLastname;
	}

	public String getCustomerLastname(){
		return customerLastname;
	}

	public void setCustomerFirstname(String customerFirstname){
		this.customerFirstname = customerFirstname;
	}

	public String getCustomerFirstname(){
		return customerFirstname;
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

	@Override
 	public String toString(){
		return 
			"ProviderReviewsPojoItem{" +
			"review_id = '" + reviewId + '\'' + 
			",profile_img = '" + profileImg + '\'' + 
			",customer_lastname = '" + customerLastname + '\'' + 
			",customer_firstname = '" + customerFirstname + '\'' + 
			",review = '" + review + '\'' + 
			",rattings = '" + rattings + '\'' + 
			"}";
		}
}