package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class LanguagePojoItem {

	@SerializedName("default_lan")
	private String defaultLan;

	@SerializedName("id")
	private String id;

	@SerializedName("languageName")
	private String languageName;

	public void setDefaultLan(String defaultLan){
		this.defaultLan = defaultLan;
	}

	public String getDefaultLan(){
		return defaultLan;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setLanguageName(String languageName){
		this.languageName = languageName;
	}

	public String getLanguageName(){
		return languageName;
	}

	@Override
 	public String toString(){
		return languageName ;
		}
}