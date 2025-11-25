package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ProviderHistoryPojoItem implements Serializable {

    @SerializedName("proposal_service_data")
    private List<ProposalServiceDataItem> proposalServiceData;

    @SerializedName("booking_end_time")
    private String bookingEndTime;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("service_booking_id")
    private String serviceBookingId;

    @SerializedName("booking_date")
    private String bookingDate;

    @SerializedName("rating")
    private String rating;

    @SerializedName("description")
    private String description;

    @SerializedName("customer_fname")
    private String customerFname;

    @SerializedName("booking_address")
    private String bookingAddress;

    @SerializedName("review")
    private String review;

    @SerializedName("provider_service_id")
    private String providerServiceId;

    @SerializedName("service_id")
    private String serviceId;

    @SerializedName("sub_category_name")
    private String subCategoryName;

    @SerializedName("extend_service_data")
    private List<ExtendServiceListPojoItem> extendServiceData;

    @SerializedName("payment_mode")
    private String paymentMode;

    @SerializedName("address")
    private String address;

    @SerializedName("service_price")
    private String servicePrice;

    @SerializedName("total_proposal")
    private int totalProposal;

    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("service_request_id")
    private String serviceRequestId;

    @SerializedName("customer_contact_number")
    private String customerContactNumber;

    @SerializedName("country_code")
    private String countryCode;

    @SerializedName("service_type")
    private String serviceType;

    @SerializedName("customer_email")
    private String customerEmail;

    @SerializedName("service_status")
    private String serviceStatus;

    @SerializedName("provider_id")
    private String providerId;

    @SerializedName("customer_lname")
    private String customerLname;

    @SerializedName("booking_start_time")
    private String bookingStartTime;

    @SerializedName("customer_id")
    private String customerId;

    @SerializedName("customer_image")
    private String customerImage;
    //	booking_amount
    @SerializedName("booking_details")
    private String booking_details;

    @SerializedName("provider_address")
    private String provider_address;

    @SerializedName("provider_commission_amount")
    private String provider_commission_amount;

    @SerializedName("booking_amount")
    private String booking_amount;

    @SerializedName("service_status_dis")
    private String service_status_dis;

    @SerializedName("service_latitude")
    private String service_latitude;

    @SerializedName("service_longitude")
    private String service_longitude;

    @SerializedName("isactive")
    private String isactive;

    @SerializedName("dispute_id")
    private String disputeId;

    @SerializedName("delivery_type")
    private String deliveryType;

    @SerializedName("request_type")
    private String requestType;

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

    public String getDisputeId() {
        return disputeId;
    }

    public void setDisputeId(String disputeId) {
        this.disputeId = disputeId;
    }

    public String getIsActive() {
        return isactive;
    }

    public void setIsActive(String isactive) {
        this.isactive = isactive;
    }

    public String getServiceLatitude() {
        return service_latitude;
    }

    public void setServiceLatitude(String service_latitude) {
        this.service_latitude = service_latitude;
    }

    public String getServiceLongitude() {
        return service_longitude;
    }

    public void setServiceLongitude(String service_longitude) {
        this.service_longitude = service_longitude;
    }

    public String getServiceStatusDisplayName() {
        return service_status_dis;
    }

    public void setServiceStatusDisplayName(String service_status_dis) {
        this.service_status_dis = service_status_dis;
    }

    public List<ProposalServiceDataItem> getProposalServiceData() {
        return proposalServiceData;
    }

    public void setProposalServiceData(List<ProposalServiceDataItem> proposalServiceData) {
        this.proposalServiceData = proposalServiceData;
    }

    public String getBookingAmount() {
        return booking_amount;
    }

    public void setBookingAmount(String booking_amount) {
        this.booking_amount = booking_amount;
    }

    public String getProviderAddress() {
        return provider_address;
    }

    public void setProviderAddress(String provider_address) {
        this.provider_address = provider_address;
    }

    public String getProviderCommissionAmount() {
        return provider_commission_amount;
    }

    public void setProviderCommissionAmount(String provider_commission_amount) {
        this.provider_commission_amount = provider_commission_amount;
    }

    public String getBookingDetails() {
        return booking_details;
    }

    public void setBookingDetails(String booking_details) {
        this.booking_details = booking_details;
    }

    public String getBookingEndTime() {
        return bookingEndTime;
    }

    public void setBookingEndTime(String bookingEndTime) {
        this.bookingEndTime = bookingEndTime;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getServiceBookingId() {
        return serviceBookingId;
    }

    public void setServiceBookingId(String serviceBookingId) {
        this.serviceBookingId = serviceBookingId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCustomerFname() {
        return customerFname;
    }

    public void setCustomerFname(String customerFname) {
        this.customerFname = customerFname;
    }

    public String getBookingAddress() {
        return bookingAddress;
    }

    public void setBookingAddress(String bookingAddress) {
        this.bookingAddress = bookingAddress;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getProviderServiceId() {
        return providerServiceId;
    }

    public void setProviderServiceId(String providerServiceId) {
        this.providerServiceId = providerServiceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public List<ExtendServiceListPojoItem> getExtendServiceData() {
        return extendServiceData;
    }

    public void setExtendServiceData(List<ExtendServiceListPojoItem> extendServiceData) {
        this.extendServiceData = extendServiceData;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(String servicePrice) {
        this.servicePrice = servicePrice;
    }

    public int getTotalProposal() {
        return totalProposal;
    }

    public void setTotalProposal(int totalProposal) {
        this.totalProposal = totalProposal;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(String serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public String getCustomerContactNumber() {
        return customerContactNumber;
    }

    public void setCustomerContactNumber(String customerContactNumber) {
        this.customerContactNumber = customerContactNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getCustomerLname() {
        return customerLname;
    }

    public void setCustomerLname(String customerLname) {
        this.customerLname = customerLname;
    }

    public String getBookingStartTime() {
        return bookingStartTime;
    }

    public void setBookingStartTime(String bookingStartTime) {
        this.bookingStartTime = bookingStartTime;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerImage() {
        return customerImage;
    }

    public void setCustomerImage(String customerImage) {
        this.customerImage = customerImage;
    }

    @Override
    public String toString() {
        return
                "ProviderHistoryPojoItem{" +
                        "proposal_service_data = '" + proposalServiceData + '\'' +
                        ",booking_end_time = '" + bookingEndTime + '\'' +
                        ",category_name = '" + categoryName + '\'' +
                        ",service_booking_id = '" + serviceBookingId + '\'' +
                        ",booking_date = '" + bookingDate + '\'' +
                        ",rating = '" + rating + '\'' +
                        ",description = '" + description + '\'' +
                        ",customer_fname = '" + customerFname + '\'' +
                        ",booking_address = '" + bookingAddress + '\'' +
                        ",review = '" + review + '\'' +
                        ",provider_service_id = '" + providerServiceId + '\'' +
                        ",service_id = '" + serviceId + '\'' +
                        ",sub_category_name = '" + subCategoryName + '\'' +
                        ",extend_service_data = '" + extendServiceData + '\'' +
                        ",payment_mode = '" + paymentMode + '\'' +
                        ",address = '" + address + '\'' +
                        ",service_price = '" + servicePrice + '\'' +
                        ",total_proposal = '" + totalProposal + '\'' +
                        ",service_name = '" + serviceName + '\'' +
                        ",service_request_id = '" + serviceRequestId + '\'' +
                        ",customer_contact_number = '" + customerContactNumber + '\'' +
                        ",country_code = '" + countryCode + '\'' +
                        ",service_type = '" + serviceType + '\'' +
                        ",customer_email = '" + customerEmail + '\'' +
                        ",service_status = '" + serviceStatus + '\'' +
                        ",provider_id = '" + providerId + '\'' +
                        ",customer_lname = '" + customerLname + '\'' +
                        ",booking_start_time = '" + bookingStartTime + '\'' +
                        ",customer_id = '" + customerId + '\'' +
                        ",customer_image = '" + customerImage + '\'' +
                        "}";
    }
}