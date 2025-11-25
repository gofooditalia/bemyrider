package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class InfoPagePojoItem{

	@SerializedName("page_title")
	private String pageTitle;

	@SerializedName("id")
	private String id;

	@SerializedName("url")
	private String url;

	public void setPageTitle(String pageTitle){
		this.pageTitle = pageTitle;
	}

	public String getPageTitle(){
		return pageTitle;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setUrl(String url){
		this.url = url;
	}

	public String getUrl(){
		return url;
	}

	@Override
 	public String toString(){
		return 
			"InfoPagePojoItem{" + 
			"page_title = '" + pageTitle + '\'' + 
			",id = '" + id + '\'' + 
			",url = '" + url + '\'' + 
			"}";
		}
}