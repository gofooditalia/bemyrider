package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class SignupPojo{

	@SerializedName("data")
	private SignupPojoItem data;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	public void setData(SignupPojoItem data){
		this.data = data;
	}

	public SignupPojoItem getData(){
		return data;
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
			"SignupPojo{" + 
			"data = '" + data + '\'' + 
			",type = '" + type + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}