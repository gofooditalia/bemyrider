package com.app.bemyrider.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class DepositHistoryPojo{

	@SerializedName("data")
	private List<DepositHistoryItem> data;

	@SerializedName("type")
	private String type;

	@SerializedName("message")
	private String message;

	@SerializedName("status")
	private boolean status;

	public void setData(List<DepositHistoryItem> data){
		this.data = data;
	}

	public List<DepositHistoryItem> getData(){
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
			"DepositHistoryPojo{" + 
			"data = '" + data + '\'' + 
			",type = '" + type + '\'' + 
			",message = '" + message + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}