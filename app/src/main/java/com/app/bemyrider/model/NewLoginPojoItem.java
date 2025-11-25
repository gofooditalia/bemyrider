package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NewLoginPojoItem implements Serializable {

    @SerializedName("email_id")
    private String emailId;

    @SerializedName("user_type")
    private String userType;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("currency_sign")
    private String currencySign;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("country_code_id")
    private String countryCodeId;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("isUserActive")
    private String isUserActive;

    @SerializedName("profile_img")
    private String profileImg;

    @SerializedName("address")
    private String address;

    @SerializedName("company_name")
    private String companyName;

    @SerializedName("vat")
    private String vat;

    @SerializedName("tax_id")
    private String taxId;

    @SerializedName("certified_email")
    private String certifiedEmail;

    @SerializedName("receipt_code")
    private String receiptCode;

    @SerializedName("contact_number")
    private String contactNumber;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("longitude")
    private String longitude;

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getReceiptCode() {
        return receiptCode;
    }

    public String getCertifiedEmail() {
        return certifiedEmail;
    }

    public String getTaxId() {
        return taxId;
    }

    public String getVat() {
        return vat;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIsUserActive() {
        return isUserActive;
    }

    public void setIsUserActive(String isUserActive) {
        this.isUserActive = isUserActive;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrencySign() {
        return currencySign;
    }

    public void setCurrencySign(String currencySign) {
        this.currencySign = currencySign;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCountryCodeId() {
        return countryCodeId;
    }

    public void setCountryCodeId(String countryCodeId) {
        this.countryCodeId = countryCodeId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    @Override
    public String toString() {
        return "NewLoginPojoItem{" +
                "emailId='" + emailId + '\'' +
                ", userType='" + userType + '\'' +
                ", userId='" + userId + '\'' +
                ", currencySign='" + currencySign + '\'' +
                ", userName='" + userName + '\'' +
                ", countryCodeId='" + countryCodeId + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", isUserActive='" + isUserActive + '\'' +
                ", profileImg='" + profileImg + '\'' +
                '}';
    }
}