package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class FinancialInfoPojo{

	@SerializedName("data")
	private FinancialInfoPojoItem data;

	@SerializedName("type")
	private String type;

	@SerializedName("message")
	private String message;

	@SerializedName("status")
	private boolean status;

	public void setData(FinancialInfoPojoItem data){
		this.data = data;
	}

	public FinancialInfoPojoItem getData(){
		return data;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
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
			"FinancialInfoPojo{" + 
			"data = '" + data + '\'' + 
			",type = '" + type + '\'' + 
			",message = '" + message + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}