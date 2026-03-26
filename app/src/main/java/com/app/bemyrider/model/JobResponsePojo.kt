package com.app.bemyrider.model

import com.google.gson.annotations.SerializedName

/**
 * Top level response for Job Board APIs.
 * Created by Gemini on 2024.
 */
data class JobResponsePojo(
    @SerializedName("data")
    var data: JobDataPojo? = null,

    @SerializedName("message")
    var message: String? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("status")
    var status: Boolean = false
) {
    fun isStatus(): Boolean {
        return status
    }
}
