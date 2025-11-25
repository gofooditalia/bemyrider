package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProviderServiceMediaDataItem implements Serializable {

	@SerializedName("media_id")
	private String mediaId;

	@SerializedName("media_name")
	private String mediaName;

	@SerializedName("media_url")
	private String mediaUrl;

	public void setMediaId(String mediaId){
		this.mediaId = mediaId;
	}

	public String getMediaId(){
		return mediaId;
	}

	public void setMediaName(String mediaName){
		this.mediaName = mediaName;
	}

	public String getMediaName(){
		return mediaName;
	}

	public void setMediaUrl(String mediaUrl){
		this.mediaUrl = mediaUrl;
	}

	public String getMediaUrl(){
		return mediaUrl;
	}

	@Override
 	public String toString(){
		return 
			"ProviderServiceMediaDataItem{" +
			"media_id = '" + mediaId + '\'' + 
			",media_name = '" + mediaName + '\'' + 
			",media_url = '" + mediaUrl + '\'' + 
			"}";
		}
}