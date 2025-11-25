package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class PendingTransactionPojoItem {

	@SerializedName("id")
	private String id;

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	@Override
 	public String toString(){
		return 
			"PendingTransactionPojoItem{" +
			"id = '" + id + '\'' + 
			"}";
		}
}