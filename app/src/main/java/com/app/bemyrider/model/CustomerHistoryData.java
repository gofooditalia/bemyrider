package com.app.bemyrider.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class CustomerHistoryData{

	@SerializedName("pagination")
	private Pagination pagination;

	@SerializedName("service_list")
	private List<CustomerHistoryPojoItem> serviceList;

	public void setPagination(Pagination pagination){
		this.pagination = pagination;
	}

	public Pagination getPagination(){
		return pagination;
	}

	public void setServiceList(List<CustomerHistoryPojoItem> serviceList){
		this.serviceList = serviceList;
	}

	public List<CustomerHistoryPojoItem> getServiceList(){
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