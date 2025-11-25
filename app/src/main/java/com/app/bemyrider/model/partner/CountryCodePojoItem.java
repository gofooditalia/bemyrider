package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class CountryCodePojoItem {

	@SerializedName("country_code")
	private String countryCode;

	@SerializedName("country_name")
	private String countryName;

	@SerializedName("id")
	private String id;

	public void setCountryCode(String countryCode){
		this.countryCode = countryCode;
	}

	public String getCountryCode(){
		return countryCode;
	}

	public void setCountryName(String countryName){
		this.countryName = countryName;
	}

	public String getCountryName(){
		return countryName;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	@Override
	public String toString(){
		return countryCode + " ("+countryName + ")";
	}
}