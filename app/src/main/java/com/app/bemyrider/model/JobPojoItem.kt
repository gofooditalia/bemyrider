package com.app.bemyrider.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Gemini for Job Board Feature.
 */
data class JobPojoItem(
    @SerializedName("job_id")
    val jobId: String? = null,

    @SerializedName("customer_id")
    val customerId: String? = null,

    @SerializedName("title")
    var title: String? = null,

    @SerializedName("description")
    var description: String? = null,

    @SerializedName("vehicle_required")
    var vehicleRequired: String? = null,

    @SerializedName("start_at")
    var startAt: String? = null,

    @SerializedName("end_at")
    var endAt: String? = null,

    @SerializedName("compensation")
    var compensation: String? = null,

    @SerializedName("compensation_type")
    var compensationType: String? = null,

    @SerializedName("address")
    var address: String? = null,

    @SerializedName("lat")
    var lat: String? = null,

    @SerializedName("lng")
    var lng: String? = null,

    @SerializedName("status")
    var status: String? = "open",

    @SerializedName("created_at")
    val createdAt: String? = null,

    // Dati extra per il Rider (se inclusi nella lista)
    @SerializedName("customer_name")
    val customerName: String? = null,

    @SerializedName("customer_image")
    val customerImage: String? = null
) : Serializable
