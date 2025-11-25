package com.app.bemyrider.model.user;

import com.app.bemyrider.model.partner.SubCategoryItem;
import com.google.gson.annotations.SerializedName;
import com.app.bemyrider.model.ServiceDataItem;

import java.util.List;

public class FilterData{

	@SerializedName("category_list")
	private List<CategoryDataItem> categoryList;

	@SerializedName("sub_category_list")
	private List<SubCategoryItem> subCategoryList;

	@SerializedName("services_list")
	private List<ServiceDataItem> servicesList;

	public List<CategoryDataItem> getCategoryList(){
		return categoryList;
	}

	public List<SubCategoryItem> getSubCategoryList(){
		return subCategoryList;
	}

	public List<ServiceDataItem> getServicesList(){
		return servicesList;
	}
}