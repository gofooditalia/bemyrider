package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class Pagination{

	@SerializedName("total_records")
	private int totalRecords;

	@SerializedName("total_pages")
	private int totalPages;

	@SerializedName("currentPage")
	private int currentPage;

	public void setTotalRecords(int totalRecords){
		this.totalRecords = totalRecords;
	}

	public int getTotalRecords(){
		return totalRecords;
	}

	public void setTotalPages(int totalPages){
		this.totalPages = totalPages;
	}

	public int getTotalPages(){
		return totalPages;
	}

	public void setCurrentPage(int currentPage){
		this.currentPage = currentPage;
	}

	public int getCurrentPage(){
		return currentPage;
	}

	@Override
 	public String toString(){
		return 
			"Pagination{" + 
			"total_records = '" + totalRecords + '\'' + 
			",total_pages = '" + totalPages + '\'' + 
			",currentPage = '" + currentPage + '\'' + 
			"}";
		}
}