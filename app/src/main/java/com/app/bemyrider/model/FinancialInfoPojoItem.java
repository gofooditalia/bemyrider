package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class FinancialInfoPojoItem {

	@SerializedName("total_earned")
	private String totalEarned;

	@SerializedName("total_completed_service")
	private int totalCompletedService;

	@SerializedName("total_commission")
	private String totalCommission;

	@SerializedName("total_net_earned")
	private String totalNetEarned;

	public void setTotalEarned(String totalEarned){
		this.totalEarned = totalEarned;
	}

	public String getTotalEarned(){
		return totalEarned;
	}

	public void setTotalCompletedService(int totalCompletedService){
		this.totalCompletedService = totalCompletedService;
	}

	public int getTotalCompletedService(){
		return totalCompletedService;
	}

	public void setTotalCommission(String totalCommission){
		this.totalCommission = totalCommission;
	}

	public String getTotalCommission(){
		return totalCommission;
	}

	public void setTotalNetEarned(String totalNetEarned){
		this.totalNetEarned = totalNetEarned;
	}

	public String getTotalNetEarned(){
		return totalNetEarned;
	}

	@Override
 	public String toString(){
		return 
			"FinancialInfoPojoItem{" +
			"total_earned = '" + totalEarned + '\'' + 
			",total_completed_service = '" + totalCompletedService + '\'' + 
			",total_commission = '" + totalCommission + '\'' + 
			",total_net_earned = '" + totalNetEarned + '\'' + 
			"}";
		}
}