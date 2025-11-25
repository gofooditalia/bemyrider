package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class ChangeProfileItem {

	@SerializedName("image")
	private String image;

	@SerializedName("image_url")
	private String imageUrl;

	public void setImage(String image){
		this.image = image;
	}

	public String getImage(){
		return image;
	}

	public void setImageUrl(String imageUrl){
		this.imageUrl = imageUrl;
	}

	public String getImageUrl(){
		return imageUrl;
	}

	@Override
 	public String toString(){
		return 
			"ChangeProfileItem{" +
			"image = '" + image + '\'' + 
			",image_url = '" + imageUrl + '\'' + 
			"}";
		}
}