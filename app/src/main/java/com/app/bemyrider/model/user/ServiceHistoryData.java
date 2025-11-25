package com.app.bemyrider.model.user;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServiceHistoryData{

	@SerializedName("ongoin")
	private List<ServiceHistoryItem> ongoin;

	@SerializedName("past")
	private List<ServiceHistoryItem> past;

	@SerializedName("upcoming")
	private List<ServiceHistoryItem> upcoming;

	public void setOngoin(List<ServiceHistoryItem> ongoin){
		this.ongoin = ongoin;
	}

	public List<ServiceHistoryItem> getOngoin(){
		return ongoin;
	}

	public void setPast(List<ServiceHistoryItem> past){
		this.past = past;
	}

	public List<ServiceHistoryItem> getPast(){
		return past;
	}

	public void setUpcoming(List<ServiceHistoryItem> upcoming){
		this.upcoming = upcoming;
	}

	public List<ServiceHistoryItem> getUpcoming(){
		return upcoming;
	}

	@Override
 	public String toString(){
		return 
			"ProfileItem{" +
			"ongoin = '" + ongoin + '\'' + 
			",past = '" + past + '\'' + 
			",upcoming = '" + upcoming + '\'' + 
			"}";
		}
}