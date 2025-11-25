package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class VersionData{

	@SerializedName("forced_update")
	private String forcedUpdate;

	@SerializedName("app_version")
	private String appVersion;

	public String getForcedUpdate(){
		return forcedUpdate;
	}

	public String getAppVersion(){
		return appVersion;
	}
}