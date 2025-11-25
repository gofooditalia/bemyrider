package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProposalServiceDataItem implements Serializable {

	@SerializedName("hours")
	private String hours;

	@SerializedName("proposal_id")
	private String proposalId;

	@SerializedName("id")
	private String id;

	@SerializedName("message")
	private String message;

	@SerializedName("created_by")
	private String createdBy;

	@SerializedName("status")
	private String status;

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setHours(String hours){
		this.hours = hours;
	}

	public String getHours(){
		return hours;
	}

	public void setProposalId(String proposalId){
		this.proposalId = proposalId;
	}

	public String getProposalId(){
		return proposalId;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setCreatedBy(String createdBy){
		this.createdBy = createdBy;
	}

	public String getCreatedBy(){
		return createdBy;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}

	@Override
 	public String toString(){
		return 
			"ProposalServiceDataItem{" + 
			"hours = '" + hours + '\'' + 
			",proposal_id = '" + proposalId + '\'' + 
			",message = '" + message + '\'' + 
			",created_by = '" + createdBy + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}