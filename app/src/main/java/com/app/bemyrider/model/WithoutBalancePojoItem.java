package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class WithoutBalancePojoItem {

	@SerializedName("wallet_Amount")
	private String walletAmount;

	@SerializedName("customer_commission")
	private String customerCommission;
//	service_amount
	@SerializedName("deposit_commission")
	private String depositCommission;

	@SerializedName("service_amount")
	private String service_amount;

	@SerializedName("booking_amount")
	private String bookingAmount;
	@SerializedName("total_fees")
	private String totalFees;
	@SerializedName("subtotal")
	private String subTotal;
	@SerializedName("provider_commission")
	private String providerCommission;
	@SerializedName("total_amount_to_charge")
	private String totalAmountToCharge;
	@SerializedName("total_amount_to_charge_full")
	private String totalAmountToChargeFull;

	@SerializedName("paymentIntentClientSecret")
	private String paymentIntentClientSecret;

	@SerializedName("payment_url")
	private String paymentUrl;

	public String getBookingAmount() {
		return bookingAmount;
	}

	public String getTotalFees() {
		return totalFees;
	}

	public void setTotalFees(String totalFees) {
		this.totalFees = totalFees;
	}

	public String getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(String subTotal) {
		this.subTotal = subTotal;
	}

	public void setBookingAmount(String bookingAmount) {
		this.bookingAmount = bookingAmount;
	}

	public String getProviderCommission() {
		return providerCommission;
	}

	public void setProviderCommission(String providerCommission) {
		this.providerCommission = providerCommission;
	}

	public String getTotalAmountToCharge() {
		return totalAmountToCharge;
	}

	public void setTotalAmountToCharge(String totalAmountToCharge) {
		this.totalAmountToCharge = totalAmountToCharge;
	}

	public String getPaymentIntentClientSecret() {
		return paymentIntentClientSecret;
	}

	public void setPaymentIntentClientSecret(String paymentIntentClientSecret) {
		this.paymentIntentClientSecret = paymentIntentClientSecret;
	}


	public String getTotalAmountToChargeFull() {
		return totalAmountToChargeFull;
	}

	public void setTotalAmountToChargeFull(String totalAmountToChargeFull) {
		this.totalAmountToChargeFull = totalAmountToChargeFull;
	}

	public void setServiceAmount(String service_amount){
		this.service_amount= service_amount;
	}

	public String getServiceAmount(){
		return service_amount;
	}

	public void setWalletAmount(String walletAmount){
		this.walletAmount = walletAmount;
	}

	public String getWalletAmount(){
		return walletAmount;
	}

	public void setCustomerCommission(String customerCommission){
		this.customerCommission = customerCommission;
	}

	public String getCustomerCommission(){
		return customerCommission;
	}

	public void setDepositCommission(String depositCommission){
		this.depositCommission = depositCommission;
	}
	public String getPaymentUrl() {
		return paymentUrl;
	}

	public void setPaymentUrl(String paymentUrl) {
		this.paymentUrl = paymentUrl;
	}

	public String getDepositCommission(){
		return depositCommission;
	}

	@Override
	public String toString() {
		return "WithoutBalancePojoItem{" +
				"walletAmount='" + walletAmount + '\'' +
				", customerCommission='" + customerCommission + '\'' +
				", depositCommission='" + depositCommission + '\'' +
				", service_amount='" + service_amount + '\'' +
				", bookingAmount='" + bookingAmount + '\'' +
				", providerCommission='" + providerCommission + '\'' +
				", totalAmountToCharge='" + totalAmountToCharge + '\'' +
				", totalAmountToChargeFull='" + totalAmountToChargeFull + '\'' +
				", paymentIntentClientSecret='" + paymentIntentClientSecret + '\'' +
				", paymentUrl='" + paymentUrl + '\'' +
				'}';
	}
}