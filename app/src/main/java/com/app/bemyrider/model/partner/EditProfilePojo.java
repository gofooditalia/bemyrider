package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class EditProfilePojo{

	@SerializedName("redirect")
	private String redirect;

	@SerializedName("message")
	private String message;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	public void setRedirect(String redirect){
		this.redirect = redirect;
	}

	public String getRedirect(){
		return redirect;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
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
			"EditProfilePojo{" + 
			"redirect = '" + redirect + '\'' + 
			",message = '" + message + '\'' + 
			",type = '" + type + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}