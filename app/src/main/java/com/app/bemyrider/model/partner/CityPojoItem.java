package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class CityPojoItem {

	@SerializedName("CityId")
	private String cityId;

	@SerializedName("cityName")
	private String cityName;

	public void setCityId(String cityId){
		this.cityId = cityId;
	}

	public String getCityId(){
		return cityId;
	}

	public void setCityName(String cityName){
		this.cityName = cityName;
	}

	public String getCityName(){
		return cityName;
	}

	@Override
	public String toString() {
		return cityName.toString();
	}
}