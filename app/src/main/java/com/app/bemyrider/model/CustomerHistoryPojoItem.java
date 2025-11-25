package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CustomerHistoryPojoItem implements Serializable {

    @SerializedName("booking_hours")
    private String bookingHours;

    @SerializedName("booking_amount")
    private String bookingAmount;

    @SerializedName("extend_service_data")
    private List<ExtendServiceListPojoItem> extendServiceData;

    @SerializedName("proposal_service_data")
    private List<ProposalServiceDataItem> proposalServiceData;

    @SerializedName("booking_end_time")
    private String bookingEndTime;

    @SerializedName("provider_lname")
    private String providerLname;
    //	payment_mode
    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("address")
    private String address;

    @SerializedName("service_price")
    private String servicePrice;

    @SerializedName("service_booking_id")
    private String serviceBookingId;

    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("booking_date")
    private String bookingDate;

    @SerializedName("service_request_id")
    private String serviceRequestId;

    @SerializedName("description")
    private String description;

    @SerializedName("booking_address")
    private String bookingAddress;

    @SerializedName("provider_image")
    private String providerImage;

    @SerializedName("service_type")
    private String serviceType;

    @SerializedName("provider_fname")
    private String providerFname;

    @SerializedName("provider_service_id")
    private String providerServiceId;

    @SerializedName("service_id")
    private String serviceId;

    @SerializedName("service_status")
    private String serviceStatus;

    @SerializedName("sub_category_name")
    private String subCategoryName;

    @SerializedName("provider_id")
    private String providerId;

    @SerializedName("booking_details")
    private String booking_details;

    @SerializedName("total_reviews")
    private String total_reviews;

    //	total_my_reviews
    @SerializedName("booking_start_time")
    private String bookingStartTime;

    @SerializedName("total_my_reviews")
    private String total_my_reviews;

    @SerializedName("customer_address")
    private String customer_address;

    @SerializedName("customer_commission")
    private String customer_commission;

    @SerializedName("customer_commission_amount")
    private String customer_commission_amount;

    @SerializedName("payment_mode")
    private String payment_mode;

    @SerializedName("service_status_dis")
    private String service_status_dis;

    @SerializedName("delivery_type")
    private String deliveryType;

    @SerializedName("request_type")
    private String requestType;

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
        return service_status_dis;
    }

    public void setServiceStatusDisplayName(String service_status_dis) {
        this.service_status_dis = service_status_dis;
    }

    public String getPaymentMode() {
        return payment_mode;
    }

    public void setPaymentMode(String payment_mode) {
        this.payment_mode = payment_mode;
    }

    public String getTotalMyReviews() {
        return total_my_reviews;
    }

    public void setTotalMyReviews(String total_my_reviews) {
        this.total_my_reviews = total_my_reviews;
    }

    public String getCustomerCommissionAmount() {
        return customer_commission_amount;
    }

    public void setCustomerCommissionAmount(String customer_commission_amount) {
        this.customer_commission_amount = customer_commission_amount;
    }

    public String getCustomerCommission() {
        return customer_commission;
    }

    public void setCustomerCommission(String customer_commission) {
        this.customer_commission = customer_commission;
    }

    public String getCustomerAddress() {
        return customer_address;
    }

    public void setCustomerAddress(String customer_address) {
        this.customer_address = customer_address;
    }

    public String getTotalReviews() {
        return total_reviews;
    }

    public void setTotalReviews(String total_reviews) {
        this.total_reviews = total_reviews;
    }

    public String getBookingDetails() {
        return booking_details;
    }

    public void setBookingDetails(String booking_details) {
        this.booking_details = booking_details;
    }

    public String getBookingHours() {
        return bookingHours;
    }

    public void setBookingHours(String bookingHours) {
        this.bookingHours = bookingHours;
    }

    public String getBookingAmount() {
        return bookingAmount;
    }

    public void setBookingAmount(String bookingAmount) {
        this.bookingAmount = bookingAmount;
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

    public String getBookingEndTime() {
        return bookingEndTime;
    }

    public void setBookingEndTime(String bookingEndTime) {
        this.bookingEndTime = bookingEndTime;
    }

    public String getProviderLname() {
        return providerLname;
    }

    public void setProviderLname(String providerLname) {
        this.providerLname = providerLname;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public String getServiceBookingId() {
        return serviceBookingId;
    }

    public void setServiceBookingId(String serviceBookingId) {
        this.serviceBookingId = serviceBookingId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(String serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBookingAddress() {
        return bookingAddress;
    }

    public void setBookingAddress(String bookingAddress) {
        this.bookingAddress = bookingAddress;
    }

    public String getProviderImage() {
        return providerImage;
    }

    public void setProviderImage(String providerImage) {
        this.providerImage = providerImage;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getProviderFname() {
        return providerFname;
    }

    public void setProviderFname(String providerFname) {
        this.providerFname = providerFname;
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

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getBookingStartTime() {
        return bookingStartTime;
    }

    public void setBookingStartTime(String bookingStartTime) {
        this.bookingStartTime = bookingStartTime;
    }

    @Override
    public String toString() {
        return
                "CustomerHistoryPojoItem{" +
                        "booking_hours = '" + bookingHours + '\'' +
                        ",booking_amount = '" + bookingAmount + '\'' +
                        ",extend_service_data = '" + extendServiceData + '\'' +
                        ",proposal_service_data = '" + proposalServiceData + '\'' +
                        ",booking_end_time = '" + bookingEndTime + '\'' +
                        ",provider_lname = '" + providerLname + '\'' +
                        ",category_name = '" + categoryName + '\'' +
                        ",address = '" + address + '\'' +
                        ",service_price = '" + servicePrice + '\'' +
                        ",service_booking_id = '" + serviceBookingId + '\'' +
                        ",service_name = '" + serviceName + '\'' +
                        ",booking_date = '" + bookingDate + '\'' +
                        ",service_request_id = '" + serviceRequestId + '\'' +
                        ",description = '" + description + '\'' +
                        ",booking_address = '" + bookingAddress + '\'' +
                        ",provider_image = '" + providerImage + '\'' +
                        ",service_type = '" + serviceType + '\'' +
                        ",provider_fname = '" + providerFname + '\'' +
                        ",provider_service_id = '" + providerServiceId + '\'' +
                        ",service_id = '" + serviceId + '\'' +
                        ",service_status = '" + serviceStatus + '\'' +
                        ",sub_category_name = '" + subCategoryName + '\'' +
                        ",provider_id = '" + providerId + '\'' +
                        ",booking_start_time = '" + bookingStartTime + '\'' +
                        "}";
    }
}