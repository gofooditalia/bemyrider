package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class VersionDataPOJO{

	@SerializedName("data")
	private VersionData versionData;

	@SerializedName("type")
	private String type;

	@SerializedName("message")
	private String message;

	@SerializedName("status")
	private boolean status;

	public VersionData getVersionData(){
		return versionData;
	}

	public String getType(){
		return type;
	}

	public String getMessage(){
		return message;
	}

	public boolean isStatus(){
		return status;
	}
}