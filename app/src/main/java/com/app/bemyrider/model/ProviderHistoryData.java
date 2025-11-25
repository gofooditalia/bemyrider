package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProviderHistoryData {

	@SerializedName("pagination")
	private Pagination pagination;

	@SerializedName("service_list")
	private List<ProviderHistoryPojoItem> serviceList;

	public void setPagination(Pagination pagination){
		this.pagination = pagination;
	}

	public Pagination getPagination(){
		return pagination;
	}

	public void setServiceList(List<ProviderHistoryPojoItem> serviceList){
		this.serviceList = serviceList;
	}

	public List<ProviderHistoryPojoItem> getServiceList(){
		return serviceList;
	}

	@Override
 	public String toString(){
		return 
			"Data{" + 
			"pagination = '" + pagination + '\'' + 
			",service_list = '" + serviceList + '\'' + 
			"}";
		}
}