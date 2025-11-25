package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class RedeemHistoryPojoItem {

	@SerializedName("admin_fees")
	private String adminFees;

	@SerializedName("redeemed_date")
	private String redeemedDate;

	@SerializedName("redeemed_amount")
	private String redeemedAmount;

	@SerializedName("requested_date")
	private String requestedDate;

	@SerializedName("requested_amount")
	private String requestedAmount;

	public void setAdminFees(String adminFees){
		this.adminFees = adminFees;
	}

	public String getAdminFees(){
		return adminFees;
	}

	public void setRedeemedDate(String redeemedDate){
		this.redeemedDate = redeemedDate;
	}

	public String getRedeemedDate(){
		return redeemedDate;
	}

	public void setRedeemedAmount(String redeemedAmount){
		this.redeemedAmount = redeemedAmount;
	}

	public String getRedeemedAmount(){
		return redeemedAmount;
	}

	public void setRequestedDate(String requestedDate){
		this.requestedDate = requestedDate;
	}

	public String getRequestedDate(){
		return requestedDate;
	}

	public void setRequestedAmount(String requestedAmount){
		this.requestedAmount = requestedAmount;
	}

	public String getRequestedAmount(){
		return requestedAmount;
	}

	@Override
 	public String toString(){
		return 
			"RedeemHistoryPojoItem{" +
			"admin_fees = '" + adminFees + '\'' + 
			",redeemed_date = '" + redeemedDate + '\'' + 
			",redeemed_amount = '" + redeemedAmount + '\'' + 
			",requested_date = '" + requestedDate + '\'' + 
			",requested_amount = '" + requestedAmount + '\'' + 
			"}";
		}
}