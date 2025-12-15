package com.app.bemyrider.model

import com.google.gson.annotations.SerializedName

/**
 * Modernized by Gemini on 2024.
 */
data class ProfilePojo(
    @SerializedName("data")
    var data: ProfileItem? = null,

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
