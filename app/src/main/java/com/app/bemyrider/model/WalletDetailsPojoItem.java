package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class WalletDetailsPojoItem {

	@SerializedName("redeem_requested_amount")
	private String redeemRequestedAmount;

	@SerializedName("hold_amount")
	private String holdAmount;

	@SerializedName("wallet_amount")
	private String walletAmount;

	public void setRedeemRequestedAmount(String redeemRequestedAmount){
		this.redeemRequestedAmount = redeemRequestedAmount;
	}

	public String getRedeemRequestedAmount(){
		return redeemRequestedAmount;
	}

	public void setHoldAmount(String holdAmount){
		this.holdAmount = holdAmount;
	}

	public String getHoldAmount(){
		return holdAmount;
	}

	public void setWalletAmount(String walletAmount){
		this.walletAmount = walletAmount;
	}

	public String getWalletAmount(){
		return walletAmount;
	}

	@Override
 	public String toString(){
		return 
			"WalletDetailsPojoItem{" +
			"redeem_requested_amount = '" + redeemRequestedAmount + '\'' + 
			",hold_amount = '" + holdAmount + '\'' + 
			",wallet_amount = '" + walletAmount + '\'' + 
			"}";
		}
}