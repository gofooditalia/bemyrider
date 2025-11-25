package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class TransectionListItem{

	@SerializedName("admin_fees")
	private String adminFees;

	@SerializedName("date")
	private String date;

	@SerializedName("transaction_id")
	private String transactionId;

	@SerializedName("amount")
	private String amount;

	public void setAdminFees(String adminFees){
		this.adminFees = adminFees;
	}

	public String getAdminFees(){
		return adminFees;
	}

	public void setDate(String date){
		this.date = date;
	}

	public String getDate(){
		return date;
	}

	public void setTransactionId(String transactionId){
		this.transactionId = transactionId;
	}

	public String getTransactionId(){
		return transactionId;
	}

	public void setAmount(String amount){
		this.amount = amount;
	}

	public String getAmount(){
		return amount;
	}

	@Override
 	public String toString(){
		return 
			"TransectionListItem{" + 
			"admin_fees = '" + adminFees + '\'' + 
			",date = '" + date + '\'' + 
			",transaction_id = '" + transactionId + '\'' + 
			",amount = '" + amount + '\'' + 
			"}";
		}
}