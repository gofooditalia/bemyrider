package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProviderReviewsPojo{

	@SerializedName("data")
	private List<ProviderReviewsPojoItem> data;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	public void setData(List<ProviderReviewsPojoItem> data){
		this.data = data;
	}

	public List<ProviderReviewsPojoItem> getData(){
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
			"ProviderReviewsPojo{" + 
			"data = '" + data + '\'' + 
			",type = '" + type + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}