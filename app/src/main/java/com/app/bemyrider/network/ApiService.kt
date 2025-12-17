package com.app.bemyrider.network

import com.app.bemyrider.model.CheckStripeConnectedPojo
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.LanguagePojo
import com.app.bemyrider.model.NewLoginPojo
import com.app.bemyrider.model.ProfilePojo
import com.app.bemyrider.model.partner.CountryCodePojo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiServiceKt {

    @GET("language/getlanguages")
    suspend fun getLanguages(): Response<LanguagePojo>
    
    @POST("profile/countrycodelist")
    suspend fun getCountryCodes(): Response<CountryCodePojo>

    @FormUrlEncoded
    @POST("profile/updateavailablestatus")
    suspend fun updateAvailabilityStatus(
            @Field("user_id") userId: String,
            @Field("isAvailable") isAvailable: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("profile/logout")
    suspend fun logout(@Field("user_id") userId: String): Response<CommonPojo>

    @FormUrlEncoded
    @POST("profile/stripe_connect")
    suspend fun checkStripeStatus(@Field("user_id") userId: String): Response<CheckStripeConnectedPojo>
    
    @Multipart
    @POST("profile/editprofile")
    suspend fun editProfile(
            @Part("user_id") userId: RequestBody,
            @Part("firstName") firstName: RequestBody,
            @Part("lastName") lastName: RequestBody,
            @Part profilePic: MultipartBody.Part?,
            @Part("contact_number") contactNumber: RequestBody,
            @Part("country_code") countryCode: RequestBody,
            @Part("city_of_company") cityOfCompany: RequestBody,
            @Part("address") address: RequestBody,
            @Part("payment_mode") paymentMode: RequestBody,
            @Part("company_name") companyName: RequestBody,
            @Part("vat") vat: RequestBody,
            @Part("certified_email") certifiedEmail: RequestBody,
            @Part("receipt_code") receiptCode: RequestBody,
            @Part("latitude") latitude: RequestBody,
            @Part("longitude") longitude: RequestBody
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("profile/changepassword")
    suspend fun changePassword(
            @Field("currentpwd") currentPwd: String,
            @Field("newpwd") newPwd: String,
            @Field("renewpwd") reNewPwd: String,
            @Field("user_id") userId: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("profile/deactiveuser")
    suspend fun deactivateAccount(
            @Field("user_id") userId: String,
            @Field("user_type") userType: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("profile/login")
    suspend fun login(
            @Field("email") email: String,
            @Field("password") password: String,
            @Field("device_token") deviceToken: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("profile/register")
    suspend fun signup(
            @Field("first_name") firstName: String,
            @Field("last_name") lastName: String,
            @Field("email") email: String,
            @Field("user_type") userType: String,
            @Field("contact_number") contactNumber: String,
            @Field("password") password: String,
            @Field("repassword") rePassword: String,
            @Field("country_code_id") countryCodeId: String,
            @Field("device_token") deviceToken: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("profile")
    suspend fun updateProfile(
            @Field("user_id") userId: String,
            @Field("first_name") firstName: String,
            @Field("last_name") lastName: String,
            @Field("contact_number") contactNumber: String,
            @Field("user_type") userType: String,
            @Field("company_name") companyName: String,
            @Field("vat") vat: String,
            @Field("tax_id_code") taxIdCode: String,
            @Field("receipt_code") receiptCode: String,
            @Field("certified_email") certifiedEmail: String,
            @Field("address") address: String
    ): Response<NewLoginPojo>


    @FormUrlEncoded
    @POST("profile")
    suspend fun socialLogin(
            @Field("first_name") firstName: String,
            @Field("last_name") lastName: String,
            @Field("email") email: String,
            @Field("login_type") loginType: String,
            @Field("fbid") fbId: String,
            @Field("googleid") googleId: String,
            @Field("linkedinid") linkedInId: String,
            @Field("picture") pictureUrl: String,
            @Field("device_token") deviceToken: String
    ): Response<NewLoginPojo>

    @FormUrlEncoded
    @POST("profile")
    suspend fun getProfile(
            @Field("profile_id") profileId: String
    ): Response<ProfilePojo>

    @FormUrlEncoded
    @POST("profile/getOfflineData")
    suspend fun getOfflineData(
            @Field("user_id") userId: String
    ): Response<String>

    @FormUrlEncoded
    @POST("profile/forgotpassword")
    suspend fun forgotPassword(
            @Field("email") email: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("profile/resend-activation")
    suspend fun resendActivationMail(
            @Field("email") email: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("services/send_service_request")
    suspend fun bookService(
            @Field("provider_service_id") providerServiceId: String,
            @Field("login_service_id") loginServiceId: String,
            @Field("service_start_time") serviceStartTime: String,
            @Field("provider_service_hours") providerServiceHours: String,
            @Field("sel_hours") selHours: String,
            @Field("user_id") userId: String,
            @Field("service_address") serviceAddress: String,
            @Field("service_details") serviceDetails: String,
            @Field("bookingLat") bookingLat: String,
            @Field("bookingLong") bookingLong: String,
            @Field("delivery_type") deliveryType: String,
            @Field("request_type") requestType: String
    ): Response<ResponseBody>
}
