package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class ServiceReviewPojo{

	@SerializedName("data")
	private ServiceReviewDataPojo data;

	@SerializedName("type")
	private String type;

	@SerializedName("message")
	private String message;

	@SerializedName("status")
	private boolean status;

	public void setData(ServiceReviewDataPojo data){
		this.data = data;
	}

	public ServiceReviewDataPojo getData(){
		return data;
	}

	public void setMessage(String type){
		this.type = type;
	}

	public String getMessage(){
		return message;
	}


	public void setType(String message){
		this.message = message;
	}

	public String getType(){
		return type;
	}

	public void setStatus(boolean status){
		this.status = status;
	}

	public boolean isStatus(){
		return status;
	}

	@Override
 	public String toString(){
		return 
			"ServiceReviewPojo{" + 
			"data = '" + data + '\'' + 
			",type = '" + type + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}