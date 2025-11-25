package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServiceListPojo{

	@SerializedName("data")
	private List<ServiceListItem> data;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	public void setData(List<ServiceListItem> data){
		this.data = data;
	}

	public List<ServiceListItem> getData(){
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
			"ServiceListPojo{" + 
			"data = '" + data + '\'' + 
			",type = '" + type + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}