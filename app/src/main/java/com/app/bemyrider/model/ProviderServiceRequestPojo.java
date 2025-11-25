package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class ProviderServiceRequestPojo {

	@SerializedName("data")
	private ProviderHistoryPojoItem data;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	public void setData(ProviderHistoryPojoItem data){
		this.data = data;
	}

	public ProviderHistoryPojoItem getData(){
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