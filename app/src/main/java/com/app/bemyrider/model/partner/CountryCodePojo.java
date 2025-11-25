package com.app.bemyrider.model.partner;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class CountryCodePojo{

	@SerializedName("data")
	private List<CountryCodePojoItem> data;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	public void setData(List<CountryCodePojoItem> data){
		this.data = data;
	}

	public List<CountryCodePojoItem> getData(){
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


}