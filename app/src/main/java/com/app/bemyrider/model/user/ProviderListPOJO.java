package com.app.bemyrider.model.user;

import com.google.gson.annotations.SerializedName;

public class ProviderListPOJO{

	@SerializedName("data")
	//private  data;
	private ProviderListData data;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	public void setData(ProviderListData data){
		this.data = data;
	}

	public ProviderListData getData(){
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
			"ProviderListPOJO{" + 
			"data = '" + data + '\'' + 
			",type = '" + type + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}