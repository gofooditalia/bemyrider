package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class ProviderHistoryPojo {

	@SerializedName("data")
	private ProviderHistoryData data;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	public void setData(ProviderHistoryData data){
		this.data = data;
	}

	public ProviderHistoryData getData(){
		return data;
	}

	public void setType(String type){
		this.type = type;
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
			"ProviderHistoryPojo{" + 
			"data = '" + data + '\'' + 
			",type = '" + type + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}