package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class DownloadInvoicePojo{

	@SerializedName("data")
	private DownloadInvoicePojoItem data;

	@SerializedName("message")
	private String message;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private boolean status;

	public void setData(DownloadInvoicePojoItem data){
		this.data = data;
	}

	public DownloadInvoicePojoItem getData(){
		return data;
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
			"DownloadInvoicePojo{" + 
			"data = '" + data + '\'' + 
			",message = '" + message + '\'' + 
			",type = '" + type + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}