package com.app.bemyrider.model.user;

import com.google.gson.annotations.SerializedName;

public class FilterDataPOJO{

	@SerializedName("data")
	private FilterData filterData;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	public FilterData getFilterData(){
		return filterData;
	}

	public String getType(){
		return type;
	}

	public boolean isStatus(){
		return status;
	}
}