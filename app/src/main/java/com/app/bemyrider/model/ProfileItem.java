package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileItem implements Serializable {

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("available_days")
    private String availableDays;

    @SerializedName("contact_mask")
    private String contactMask;

    @SerializedName("available_time_end")
    private String availableTimeEnd;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("description")
    private String description;

    @SerializedName("user_type")
    private String userType;

    @SerializedName("id")
    private String id;

    @SerializedName("landmark")
    private String landmark;

    @SerializedName("email")
    private String email;

    @SerializedName("longitude")
    private String longitude;

    @SerializedName("address")
    private String address;

    @SerializedName("available_days_list")
    private String availableDaysList;

    @SerializedName("total_review")
    private String totalReview;

    @SerializedName("total_service")
    private String totalService;

    @SerializedName("positive_rating")
    private String positiveRating;

    @SerializedName("contact_number")
    private String contactNumber;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("profile_img")
    private String profileImg;

    @SerializedName("country_code")
    private String countryCode;

    @SerializedName("available_time_start")
    private String availableTimeStart;

    @SerializedName("fb_id")
    private String fbId;

    @SerializedName("email_mask")
    private String emailMask;

    @SerializedName("user_type_title")
    private String userTypeTitle;

    @SerializedName("gmail_id")
    private String gmailId;

    @SerializedName("task_assigned")
    private String taskAssigned;

    @SerializedName("payment_mode")
    private String payment_mode;
    //	paypal_email
    @SerializedName("is_available")
    private String is_available;

    @SerializedName("linkedin_id")
    private String linkedin_id;

    @SerializedName("paypal_email")
    private String paypal_email;

    @SerializedName("star_rating")
    private String star_rating;

    @SerializedName("is_flag")
    private String is_flag;

    @SerializedName("small_delivery")
    private String smallDelivery;
    @SerializedName("medium_delivery")
    private String mediumDelivery;
    @SerializedName("large_delivery")
    private String largeDelivery;

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

    @SerializedName("city_of_birth")
    private String city_of_birth;

    @SerializedName("date_of_birth")
    private String date_of_birth;

    @SerializedName("city_of_residence")
    private String city_of_residence;

   @SerializedName("city_of_company")
    private String city_of_company;

    public String getCity_of_company() {
        return city_of_company;
    }

    @SerializedName("residential_address")
    private String residential_address;

    @SerializedName("signature_img_url")
    private String signature_img;

    public String getSignature_img() {
        return signature_img;
    }

    public void setSignature_img(String signature_img) {
        this.signature_img = signature_img;
    }

    public String getResidential_address() {
        return residential_address;
    }

    public String getCity_of_residence() {
        return city_of_residence;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getCity_of_birth() {
        return city_of_birth;
    }

    public void setCity_of_birth(String city_of_birth) {
        this.city_of_birth = city_of_birth;
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

    public String getIs_flag() {
        return is_flag;
    }

    public void setIs_flag(String is_flag) {
        this.is_flag = is_flag;
    }

    public String getPaypalEmail() {
        return paypal_email;
    }

    public void setPaypalEmail(String paypal_email) {
        this.paypal_email = paypal_email;
    }

    public String getStartRating() {
        return star_rating;
    }

    public void setStartRating(String star_rating) {
        this.star_rating = star_rating;
    }

    public String getLinkedinId() {
        return linkedin_id;
    }

    public void setLinkedinId(String linkedin_id) {
        this.linkedin_id = linkedin_id;
    }

    public String getIsAvailable() {
        return is_available;
    }

    public void setIsAvailable(String is_available) {
        this.is_available = is_available;
    }

    public String getPaymentMode() {
        return payment_mode;
    }

    public void setPaymentMode(String payment_mode) {
        this.payment_mode = payment_mode;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public String getContactMask() {
        return contactMask;
    }

    public void setContactMask(String contactMask) {
        this.contactMask = contactMask;
    }

    public String getAvailableTimeEnd() {
        return availableTimeEnd;
    }

    public void setAvailableTimeEnd(String availableTimeEnd) {
        this.availableTimeEnd = availableTimeEnd;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvailableDaysList() {
        return availableDaysList;
    }

    public void setAvailableDaysList(String availableDaysList) {
        this.availableDaysList = availableDaysList;
    }

    public String getTotalReview() {
        return totalReview;
    }

    public void setTotalReview(String totalReview) {
        this.totalReview = totalReview;
    }

    public String getPositiveRating() {
        return positiveRating;
    }

    public void setPositiveRating(String positiveRating) {
        this.positiveRating = positiveRating;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAvailableTimeStart() {
        return availableTimeStart;
    }

    public void setAvailableTimeStart(String availableTimeStart) {
        this.availableTimeStart = availableTimeStart;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public String getEmailMask() {
        return emailMask;
    }

    public void setEmailMask(String emailMask) {
        this.emailMask = emailMask;
    }

    public String getUserTypeTitle() {
        return userTypeTitle;
    }

    public void setUserTypeTitle(String userTypeTitle) {
        this.userTypeTitle = userTypeTitle;
    }

    public String getGmailId() {
        return gmailId;
    }

    public void setGmailId(String gmailId) {
        this.gmailId = gmailId;
    }

    public String getTaskAssigned() {
        return taskAssigned;
    }

    public void setTaskAssigned(String taskAssigned) {
        this.taskAssigned = taskAssigned;
    }

    public String getTotalService() {
        return totalService;
    }

    public void setTotalService(String totalService) {
        this.totalService = totalService;
    }

    public String getSmallDelivery() {
        return smallDelivery;
    }

    public void setSmallDelivery(String smallDelivery) {
        this.smallDelivery = smallDelivery;
    }

    public String getMediumDelivery() {
        return mediumDelivery;
    }

    public void setMediumDelivery(String mediumDelivery) {
        this.mediumDelivery = mediumDelivery;
    }

    public String getLargeDelivery() {
        return largeDelivery;
    }

    public void setLargeDelivery(String largeDelivery) {
        this.largeDelivery = largeDelivery;
    }

    @Override
    public String toString() {
        return "ProfileItem{" +
                "lastName='" + lastName + '\'' +
                ", availableDays='" + availableDays + '\'' +
                ", contactMask='" + contactMask + '\'' +
                ", availableTimeEnd='" + availableTimeEnd + '\'' +
                ", userName='" + userName + '\'' +
                ", latitude='" + latitude + '\'' +
                ", description='" + description + '\'' +
                ", userType='" + userType + '\'' +
                ", id='" + id + '\'' +
                ", landmark='" + landmark + '\'' +
                ", email='" + email + '\'' +
                ", longitude='" + longitude + '\'' +
                ", address='" + address + '\'' +
                ", availableDaysList='" + availableDaysList + '\'' +
                ", totalReview='" + totalReview + '\'' +
                ", totalService='" + totalService + '\'' +
                ", positiveRating='" + positiveRating + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", firstName='" + firstName + '\'' +
                ", profileImg='" + profileImg + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", availableTimeStart='" + availableTimeStart + '\'' +
                ", fbId='" + fbId + '\'' +
                ", emailMask='" + emailMask + '\'' +
                ", userTypeTitle='" + userTypeTitle + '\'' +
                ", gmailId='" + gmailId + '\'' +
                ", taskAssigned='" + taskAssigned + '\'' +
                ", payment_mode='" + payment_mode + '\'' +
                ", is_available='" + is_available + '\'' +
                ", linkedin_id='" + linkedin_id + '\'' +
                ", paypal_email='" + paypal_email + '\'' +
                ", star_rating='" + star_rating + '\'' +
                ", is_flag='" + is_flag + '\'' +
                ", smallDelivery='" + smallDelivery + '\'' +
                ", mediumDelivery='" + mediumDelivery + '\'' +
                ", largeDelivery='" + largeDelivery + '\'' +
                ", companyName='" + companyName + '\'' +
                ", vat='" + vat + '\'' +
                ", taxId='" + taxId + '\'' +
                ", certifiedEmail='" + certifiedEmail + '\'' +
                ", receiptCode='" + receiptCode + '\'' +
                '}';
    }
}