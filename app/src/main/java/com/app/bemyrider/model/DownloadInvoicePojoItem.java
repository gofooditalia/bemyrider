package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class DownloadInvoicePojoItem {

	@SerializedName("file_name")
	private String fileName;

	public void setFileName(String fileName){
		this.fileName = fileName;
	}

	public String getFileName(){
		return fileName;
	}

	@Override
 	public String toString(){
		return 
			"DownloadInvoicePojoItem{" +
			"file_name = '" + fileName + '\'' + 
			"}";
		}
}