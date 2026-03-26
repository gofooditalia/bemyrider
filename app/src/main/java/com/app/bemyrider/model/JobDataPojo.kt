package com.app.bemyrider.model

import com.google.gson.annotations.SerializedName

data class JobDataPojo(
    @SerializedName("job_list")
    var jobList: List<JobPojoItem>? = null,

    @SerializedName("applicant_list")
    var applicantList: List<JobApplicantPojoItem>? = null,

    @SerializedName("serviceRequestId") // Returned when hiring
    val serviceRequestId: String? = null,

    @SerializedName("pagination")
    var pagination: Pagination? = null
)

data class JobApplicantPojoItem(
    @SerializedName("rider_id") val riderId: String? = null,
    @SerializedName("rider_name") val riderName: String? = null,
    @SerializedName("rider_image") val riderImage: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("total_deliveries") val totalDeliveries: String? = null,
    @SerializedName("vehicle_type") val vehicleType: String? = null
)
