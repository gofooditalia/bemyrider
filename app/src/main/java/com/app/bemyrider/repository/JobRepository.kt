package com.app.bemyrider.repository

import com.app.bemyrider.model.JobResponsePojo
import com.app.bemyrider.network.JobApiService
import com.app.bemyrider.network.RetrofitClient
import retrofit2.Response

/**
 * Repository for Job Board (Bacheca) feature.
 * Created by Gemini on 2024.
 */
class JobRepository {

    private val apiService: JobApiService = RetrofitClient.getClient().create(JobApiService::class.java)

    suspend fun createJob(
        title: String, description: String, vehicleRequired: String,
        startAt: String, endAt: String, compensation: String,
        compensationType: String, address: String, lat: Double,
        lng: Double, userId: String
    ): Response<JobResponsePojo> {
        return apiService.createJob(
            title, description, vehicleRequired, startAt, endAt,
            compensation, compensationType, address, lat, lng, userId
        )
    }

    suspend fun getMyJobPosts(userId: String): Response<JobResponsePojo> {
        return apiService.getMyJobPosts(userId)
    }

    suspend fun getJobApplicants(jobId: String): Response<JobResponsePojo> {
        return apiService.getJobApplicants(jobId)
    }

    suspend fun hireRider(jobId: String, riderId: String): Response<JobResponsePojo> {
        return apiService.hireRider(jobId, riderId)
    }

    suspend fun getAvailableJobs(
        userId: String, lat: Double?, lng: Double?, vehicleType: String?
    ): Response<JobResponsePojo> {
        return apiService.getAvailableJobs(userId, lat, lng, vehicleType)
    }

    suspend fun applyToJob(jobId: String, userId: String): Response<JobResponsePojo> {
        return apiService.applyToJob(jobId, userId)
    }
}
