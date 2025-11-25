package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaymentHistoryPojoItem{

	@SerializedName("pagination")
	private Pagination pagination;

	@SerializedName("transection_list")
	private List<TransectionListItem> transectionList;

	public void setPagination(Pagination pagination){
		this.pagination = pagination;
	}

	public Pagination getPagination(){
		return pagination;
	}

	public void setTransectionList(List<TransectionListItem> transectionList){
		this.transectionList = transectionList;
	}

	public List<TransectionListItem> getTransectionList(){
		return transectionList;
	}

	@Override
 	public String toString(){
		return 
			"PaymentHistoryPojoItem{" + 
			"pagination = '" + pagination + '\'' + 
			",transectionList = '" + transectionList + '\'' + 
			"}";
		}
}