package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ExtendServiceListPojoItem implements Serializable {

	@SerializedName("extend_hours")
	private String extendHours;

	@SerializedName("booking_end_time")
	private String bookingEndTime;

	@SerializedName("extend_id")
	private String extendId;

	@SerializedName("serviceStatus")
	private String serviceStatus;

	@SerializedName("extend_status")
	private String extend_status;

	@SerializedName("booking_amount")
	private String booking_amount;

	@SerializedName("booking_amt")
	private String bookingAmt;

	@SerializedName("booking_start_time")
	private String bookingStartTime;

	public void setExtendStatus(String extend_status){
		this.extend_status= extend_status;
	}

	public String getExtendStatus(){
		return extend_status;
	}

	public void setBookingAmount(String booking_amount){
		this.booking_amount= booking_amount;
	}

	public String getBookingAmount(){
		return booking_amount;
	}

	public void setExtendHours(String extendHours){
		this.extendHours = extendHours;
	}

	public String getExtendHours(){
		return extendHours;
	}

	public void setBookingEndTime(String bookingEndTime){
		this.bookingEndTime = bookingEndTime;
	}

	public String getBookingEndTime(){
		return bookingEndTime;
	}

	public void setExtendId(String extendId){
		this.extendId = extendId;
	}

	public String getExtendId(){
		return extendId;
	}

	public void setServiceStatus(String serviceStatus){
		this.serviceStatus = serviceStatus;
	}

	public String getServiceStatus(){
		return serviceStatus;
	}

	public void setBookingAmt(String bookingAmt){
		this.bookingAmt = bookingAmt;
	}

	public String getBookingAmt(){
		return bookingAmt;
	}

	public void setBookingStartTime(String bookingStartTime){
		this.bookingStartTime = bookingStartTime;
	}

	public String getBookingStartTime(){
		return bookingStartTime;
	}

	@Override
 	public String toString(){
		return 
			"ExtendServiceListPojoItem{" +
			"extend_hours = '" + extendHours + '\'' + 
			",booking_end_time = '" + bookingEndTime + '\'' + 
			",extend_id = '" + extendId + '\'' + 
			",serviceStatus = '" + serviceStatus + '\'' + 
			",booking_amt = '" + bookingAmt + '\'' + 
			",booking_start_time = '" + bookingStartTime + '\'' + 
			"}";
		}
}