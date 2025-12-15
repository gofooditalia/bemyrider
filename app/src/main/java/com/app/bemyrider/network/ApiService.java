package com.app.bemyrider.network;

import com.app.bemyrider.model.CheckStripeConnectedPojo;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.LanguagePojo;
import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.model.RegistrationPojo;
import com.app.bemyrider.model.partner.CountryCodePojo;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @GET("language/getlanguages")
    Call<LanguagePojo> getLanguages();
    
    @POST("profile/countrycodelist")
    Call<CountryCodePojo> getCountryCodes();

    @FormUrlEncoded
    @POST("profile/updateavailablestatus")
    Call<CommonPojo> updateAvailabilityStatus(
            @Field("user_id") String userId,
            @Field("isAvailable") String isAvailable
    );

    @FormUrlEncoded
    @POST("profile/logout")
    Call<CommonPojo> logout(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("profile/stripe_connect")
    Call<CheckStripeConnectedPojo> checkStripeStatus(@Field("user_id") String userId);
    
    @Multipart
    @POST("profile/editprofile")
    Call<ResponseBody> editProfile(
            @Part("user_id") RequestBody userId,
            @Part("firstName") RequestBody firstName,
            @Part("lastName") RequestBody lastName,
            @Part MultipartBody.Part profilePic,
            @Part("contact_number") RequestBody contactNumber,
            @Part("country_code") RequestBody countryCode,
            @Part("city_of_company") RequestBody cityOfCompany,
            @Part("address") RequestBody address,
            @Part("payment_mode") RequestBody paymentMode,
            @Part("company_name") RequestBody companyName,
            @Part("vat") RequestBody vat,
            @Part("certified_email") RequestBody certifiedEmail,
            @Part("receipt_code") RequestBody receiptCode,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude
    );

    @FormUrlEncoded
    @POST("profile/changepassword")
    Call<CommonPojo> changePassword(
            @Field("currentpwd") String currentPwd,
            @Field("newpwd") String newPwd,
            @Field("renewpwd") String reNewPwd,
            @Field("user_id") String userId
    );

    @FormUrlEncoded
    @POST("profile/deactiveuser")
    Call<CommonPojo> deactivateAccount(
            @Field("user_id") String userId,
            @Field("user_type") String userType
    );

    @FormUrlEncoded
    @POST("profile/login")
    Call<ResponseBody> login(
            @Field("email") String email,
            @Field("password") String password,
            @Field("device_token") String deviceToken
    );

    @FormUrlEncoded
    @POST("profile/register")
    Call<ResponseBody> signup(
            @Field("first_name") String firstName,
            @Field("last_name") String lastName,
            @Field("email") String email,
            @Field("user_type") String userType,
            @Field("contact_number") String contactNumber,
            @Field("password") String password,
            @Field("repassword") String rePassword,
            @Field("country_code_id") String countryCodeId,
            @Field("device_token") String deviceToken
    );

    @FormUrlEncoded
    @POST("profile")
    Call<NewLoginPojo> updateProfile(
            @Field("user_id") String userId,
            @Field("first_name") String firstName,
            @Field("last_name") String lastName,
            @Field("contact_number") String contactNumber,
            @Field("user_type") String userType,
            @Field("company_name") String companyName,
            @Field("vat") String vat,
            @Field("tax_id_code") String taxIdCode,
            @Field("receipt_code") String receiptCode,
            @Field("certified_email") String certifiedEmail,
            @Field("address") String address
    );


    @FormUrlEncoded
    @POST("profile")
    Call<NewLoginPojo> socialLogin(
            @Field("first_name") String firstName,
            @Field("last_name") String lastName,
            @Field("email") String email,
            @Field("login_type") String loginType,
            @Field("fbid") String fbId,
            @Field("googleid") String googleId,
            @Field("linkedinid") String linkedInId,
            @Field("picture") String pictureUrl,
            @Field("device_token") String deviceToken
    );

    @FormUrlEncoded
    @POST("profile")
    Call<ProfilePojo> getProfile(
            @Field("profile_id") String profileId
    );

    @FormUrlEncoded
    @POST("profile/getOfflineData")
    Call<String> getOfflineData(
            @Field("user_id") String userId
    );

    @FormUrlEncoded
    @POST("profile/forgotpassword")
    Call<ResponseBody> forgotPassword(
            @Field("email") String email
    );

    @FormUrlEncoded
    @POST("profile/resend-activation")
    Call<ResponseBody> resendActivationMail(
            @Field("email") String email
    );

    @FormUrlEncoded
    @POST("services/send_service_request")
    Call<ResponseBody> bookService(
            @Field("provider_service_id") String providerServiceId,
            @Field("login_service_id") String loginServiceId,
            @Field("service_start_time") String serviceStartTime,
            @Field("provider_service_hours") String providerServiceHours,
            @Field("sel_hours") String selHours,
            @Field("user_id") String userId,
            @Field("service_address") String serviceAddress,
            @Field("service_details") String serviceDetails,
            @Field("bookingLat") String bookingLat,
            @Field("bookingLong") String bookingLong,
            @Field("delivery_type") String deliveryType,
            @Field("request_type") String requestType
    );
}
