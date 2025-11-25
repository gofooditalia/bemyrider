package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class CountryPojoItem {

    @SerializedName("CountryId")
    private String countryId;

    @SerializedName("countryName")
    private String countryName;

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @Override
    public String toString() {
        return countryName.toString();
    }
}