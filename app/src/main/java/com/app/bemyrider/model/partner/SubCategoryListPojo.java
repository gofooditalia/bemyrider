package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubCategoryListPojo{

	@SerializedName("data")
	private List<SubCategoryItem> data;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	@SerializedName("message")
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setData(List<SubCategoryItem> data){
		this.data = data;
	}

	public List<SubCategoryItem> getData(){
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
			"SubCategoryListPojo{" + 
			"data = '" + data + '\'' + 
			",type = '" + type + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}