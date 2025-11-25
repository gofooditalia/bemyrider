package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class MinMaxPricePojoItem {

	@SerializedName("max_price")
	private String maxPrice;

	@SerializedName("min_price")
	private String minPrice;

	@SerializedName("min_duration")
	private String minDuration;

	@SerializedName("max_duration")
	private String maxDuration;

	public void setMaxPrice(String maxPrice){
		this.maxPrice = maxPrice;
	}

	public String getMaxPrice(){
		return maxPrice;
	}

	public void setMinPrice(String minPrice){
		this.minPrice = minPrice;
	}

	public String getMinPrice(){
		return minPrice;
	}

	public void setMinDuration(String minDuration){
		this.minDuration = minDuration;
	}

	public String getMinDuration(){
		return minDuration;
	}

	public void setMaxDuration(String maxDuration){
		this.maxDuration = maxDuration;
	}

	public String getMaxDuration(){
		return maxDuration;
	}

	@Override
 	public String toString(){
		return 
			"MinMaxPricePojoItem{" +
			"max_price = '" + maxPrice + '\'' + 
			",min_price = '" + minPrice + '\'' + 
			",min_duration = '" + minDuration + '\'' + 
			",max_duration = '" + maxDuration + '\'' + 
			"}";
		}
}