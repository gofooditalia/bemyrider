package com.app.bemyrider.model.user;

import java.util.List;

import com.app.bemyrider.model.Pagination;
import com.google.gson.annotations.SerializedName;


public class ProviderData {

@SerializedName("provider_list")

private List<ProviderItem> providerItemList = null;
@SerializedName("pagination")

private Pagination pagination;

public List<ProviderItem> getProviderList() {
return providerItemList;
}

public void setProviderList(List<ProviderItem> providerItemList) {
this.providerItemList = providerItemList;
}

public Pagination getPagination() {
return pagination;
}

public void setPagination(Pagination pagination) {
this.pagination = pagination;
}

}