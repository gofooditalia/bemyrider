package com.app.bemyrider.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class InfoPagePojo{

	@SerializedName("type")
	private String type;

	@SerializedName("message")
	private String message;

	@SerializedName("data")
	private List<InfoPagePojoItem> infoPageList;

	@SerializedName("status")
	private boolean status;

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setInfoPageList(List<InfoPagePojoItem> infoPageList){
		this.infoPageList = infoPageList;
	}

	public List<InfoPagePojoItem> getInfoPageList(){
		return infoPageList;
	}

	public void setStatus(boolean status){
		this.status = status;
	}

	public boolean isStatus(){
		return status;
	}

	@Override
 	public String toString(){
		return 
			"InfoPagePojo{" + 
			"type = '" + type + '\'' + 
			",message = '" + message + '\'' + 
			",infoPagePojo = '" + infoPageList + '\'' +
			",status = '" + status + '\'' + 
			"}";
		}
}