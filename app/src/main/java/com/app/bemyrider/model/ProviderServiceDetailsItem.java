package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ProviderServiceDetailsItem implements Serializable {

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("description")
    private String description;

    @SerializedName("total_fees")
    private String totalFees;

    @SerializedName("provider_service_image")
    private String providerServiceImage;

    @SerializedName("service_master_type")
    private String serviceMasterType;

    @SerializedName("about_me")
    private String aboutMe;

    @SerializedName("total_favorite")
    private String totalFavorite;

    @SerializedName("duration")
    private String duration;

    @SerializedName("subcategory_id")
    private String subcategoryId;

    @SerializedName("category_id")
    private String categoryId;

    @SerializedName("price")
    private String price;

    @SerializedName("service_id")
    private String serviceId;

    @SerializedName("avg_rating")
    private double avgRating;

    @SerializedName("sub_category_name")
    private String subCategoryName;

    @SerializedName("id")
    private String id;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("email")
    private String email;

    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("contact_number")
    private String contactNumber;

    @SerializedName("country_code")
    private String countryCode;

    @SerializedName("provider_commission")
    private String provider_commission;

    @SerializedName("total_service")
    private String totalService;

    @SerializedName("hours")
    private String hours;

    @SerializedName("provider_id")
    private String providerId;

    @SerializedName("provider_service_hours")
    private String providerServiceHours;

    @SerializedName("customer_commission")
    private String customerCommission;

    @SerializedName("payment_preference")
    private String paymentPreference;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("service_description")
    private String serviceDescription;

    @SerializedName("service_status")
    private String serviceStatus;

    @SerializedName("service_address")
    private String serviceAddress;

    @SerializedName("isFavorite")
    private String isFavorite;

    @SerializedName("service_latitude")
    private String serviceLatitude;

    @SerializedName("service_longitude")
    private String serviceLongitude;

    @SerializedName("admin_fees")
    private String adminFees;

    @SerializedName("booking_hours")
    private String bookingHours;

    @SerializedName("provider_image")
    private String providerImage;

    @SerializedName("customer_commission_amount")
    private String customerCommissionAmount;

    @SerializedName("booking_amt")
    private String bookingAmt;

    @SerializedName("service_booking_id")
    private String serviceBookingId;

    @SerializedName("service_request_id")
    private String serviceRequestId;

    @SerializedName("customer_address")
    private String customerAddress;

    @SerializedName("isReviewGiven")
    private String isReviewGiven;

    @SerializedName("dispute_id")
    private String disputeId;

    @SerializedName("extend_service_data")
    private List<ExtendServiceListPojoItem> extendServiceData;

    @SerializedName("proposal_service_data")
    private List<ProposalServiceDataItem> proposalServiceData;

    @SerializedName("review_data")
    private List<ProviderServiceReviewDataItem> reviewData;

    //	provider_commission
    @SerializedName("media_data")
    private List<ProviderServiceMediaDataItem> mediaData;

    @SerializedName("createdUser")
    private String createdUser;

    @SerializedName("service_status_dis")
    private String serviceStatusDisplayName;

    @SerializedName("delivery_type")
    private String deliveryType;

    @SerializedName("request_type")
    private String requestType;

    @SerializedName("small_delivery")
    private String smallDelivery;

    @SerializedName("medium_delivery")
    private String mediumDelivery;

    @SerializedName("large_delivery")
    private String largeDelivery;

    @SerializedName("available_days")
    private String availableDays;

    @SerializedName("available_days_list")
    private String availableDaysList;

  @SerializedName("available_time_start")
    private String availableTimeStart;

    @SerializedName("available_time_end")
    private String availableTimeEnd;

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public String getAvailableDaysList() {
        return availableDaysList;
    }

    public void setAvailableDaysList(String availableDaysList) {
        this.availableDaysList = availableDaysList;
    }

    public String getAvailableTimeStart() {
        return availableTimeStart;
    }

    public void setAvailableTimeStart(String availableTimeStart) {
        this.availableTimeStart = availableTimeStart;
    }

    public String getAvailableTimeEnd() {
        return availableTimeEnd;
    }

    public void setAvailableTimeEnd(String availableTimeEnd) {
        this.availableTimeEnd = availableTimeEnd;
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

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getServiceStatusDisplayName() {
        return serviceStatusDisplayName;
    }

    public void setServiceStatusDisplayName(String serviceStatusDisplayName) {
        this.serviceStatusDisplayName = serviceStatusDisplayName;
    }

    public String getDisputeId() {
        return disputeId;
    }

    public void setDisputeId(String disputeId) {
        this.disputeId = disputeId;
    }

    public String getProviderCommission() {
        return provider_commission;
    }

    public void setProviderCommission(String provider_commission) {
        this.provider_commission = provider_commission;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(String totalFees) {
        this.totalFees = totalFees;
    }

    public String getProviderServiceImage() {
        return providerServiceImage;
    }

    public void setProviderServiceImage(String providerServiceImage) {
        this.providerServiceImage = providerServiceImage;
    }

    public String getServiceMasterType() {
        return serviceMasterType;
    }

    public void setServiceMasterType(String serviceMasterType) {
        this.serviceMasterType = serviceMasterType;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getTotalFavorite() {
        return totalFavorite;
    }

    public void setTotalFavorite(String totalFavorite) {
        this.totalFavorite = totalFavorite;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(String subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getTotalService() {
        return totalService;
    }

    public void setTotalService(String totalService) {
        this.totalService = totalService;
    }

    public List<ProviderServiceReviewDataItem> getReviewData() {
        return reviewData;
    }

    public void setReviewData(List<ProviderServiceReviewDataItem> reviewData) {
        this.reviewData = reviewData;
    }

    public List<ProviderServiceMediaDataItem> getMediaData() {
        return mediaData;
    }

    public void setMediaData(List<ProviderServiceMediaDataItem> mediaData) {
        this.mediaData = mediaData;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderServiceHours() {
        return providerServiceHours;
    }

    public void setProviderServiceHours(String providerServiceHours) {
        this.providerServiceHours = providerServiceHours;
    }

    public String getCustomerCommission() {
        return customerCommission;
    }

    public void setCustomerCommission(String customerCommission) {
        this.customerCommission = customerCommission;
    }

    public String getPaymentPreference() {
        return paymentPreference;
    }

    public void setPaymentPreference(String paymentPreference) {
        this.paymentPreference = paymentPreference;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(String isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getServiceLatitude() {
        return serviceLatitude;
    }

    public void setServiceLatitude(String serviceLatitude) {
        this.serviceLatitude = serviceLatitude;
    }

    public String getServiceLongitude() {
        return serviceLongitude;
    }

    public void setServiceLongitude(String serviceLongitude) {
        this.serviceLongitude = serviceLongitude;
    }

    public String getAdminFees() {
        return adminFees;
    }

    public void setAdminFees(String adminFees) {
        this.adminFees = adminFees;
    }

    public String getBookingHours() {
        return bookingHours;
    }

    public void setBookingHours(String bookingHours) {
        this.bookingHours = bookingHours;
    }

    public String getProviderImage() {
        return providerImage;
    }

    public void setProviderImage(String providerImage) {
        this.providerImage = providerImage;
    }

    public String getCustomerCommissionAmount() {
        return customerCommissionAmount;
    }

    public void setCustomerCommissionAmount(String customerCommissionAmount) {
        this.customerCommissionAmount = customerCommissionAmount;
    }

    public String getBookingAmt() {
        return bookingAmt;
    }

    public void setBookingAmt(String bookingAmt) {
        this.bookingAmt = bookingAmt;
    }

    public String getServiceBookingId() {
        return serviceBookingId;
    }

    public void setServiceBookingId(String serviceBookingId) {
        this.serviceBookingId = serviceBookingId;
    }

    public String getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(String serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getIsReviewGiven() {
        return isReviewGiven;
    }

    public void setIsReviewGiven(String isReviewGiven) {
        this.isReviewGiven = isReviewGiven;
    }

    public List<ExtendServiceListPojoItem> getExtendServiceData() {
        return extendServiceData;
    }

    public void setExtendServiceData(List<ExtendServiceListPojoItem> extendServiceData) {
        this.extendServiceData = extendServiceData;
    }

    public List<ProposalServiceDataItem> getProposalServiceData() {
        return proposalServiceData;
    }

    public void setProposalServiceData(List<ProposalServiceDataItem> proposalServiceData) {
        this.proposalServiceData = proposalServiceData;
    }

}