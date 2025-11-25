package com.app.bemyrider.model.user;

import com.google.gson.annotations.SerializedName;

public class DisputeListLastMessage{

	@SerializedName("message_time")
	private String messageTime;

	@SerializedName("message")
	private String message;

	public void setMessageTime(String messageTime){
		this.messageTime = messageTime;
	}

	public String getMessageTime(){
		return messageTime;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	@Override
 	public String toString(){
		return 
			"LastMessage{" + 
			"message_time = '" + messageTime + '\'' + 
			",message = '" + message + '\'' + 
			"}";
		}
}