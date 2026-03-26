package com.app.bemyrider.network

import com.app.bemyrider.model.JobResponsePojo
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service for Job Board (Bacheca) feature.
 * Created by Gemini on 2024.
 */
interface JobApiService {

    // --- CUSTOMER ENDPOINTS ---

    @FormUrlEncoded
    @POST("jobs/create")
    suspend fun createJob(
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("vehicle_required") vehicleRequired: String,
        @Field("start_at") startAt: String,
        @Field("end_at") endAt: String,
        @Field("compensation") compensation: String,
        @Field("compensation_type") compensationType: String,
        @Field("address") address: String,
        @Field("lat") lat: Double,
        @Field("lng") lng: Double,
        @Field("user_id") userId: String
    ): Response<JobResponsePojo>

    @GET("jobs/my-posts")
    suspend fun getMyJobPosts(
        @Query("user_id") userId: String
    ): Response<JobResponsePojo>

    @GET("jobs/{job_id}/applicants")
    suspend fun getJobApplicants(
        @Path("job_id") jobId: String
    ): Response<JobResponsePojo>

    @FormUrlEncoded
    @POST("jobs/hire")
    suspend fun hireRider(
        @Field("job_id") jobId: String,
        @Field("rider_id") riderId: String
    ): Response<JobResponsePojo>


    // --- RIDER ENDPOINTS ---

    @GET("jobs/available")
    suspend fun getAvailableJobs(
        @Query("user_id") userId: String,
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?,
        @Query("vehicle_type") vehicleType: String?
    ): Response<JobResponsePojo>

    @FormUrlEncoded
    @POST("jobs/apply")
    suspend fun applyToJob(
        @Field("job_id") jobId: String,
        @Field("user_id") userId: String
    ): Response<JobResponsePojo>
}
