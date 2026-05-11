package com.app.bemyrider.network

import com.app.bemyrider.model.BulkInvoicePojo
import com.app.bemyrider.model.VersionDataPOJO
import com.app.bemyrider.model.user.PopularTaskerPOJO
import com.app.bemyrider.model.DepositHistoryPojo
import com.app.bemyrider.model.FinancialInfoPojo
import com.app.bemyrider.model.PaymentHistoryPojo
import com.app.bemyrider.model.RedeemHistoryPojo
import com.app.bemyrider.model.WalletDetailsPojo
import com.app.bemyrider.model.partner.PartnerPaymentHistoryPojo
import com.app.bemyrider.model.DisputeDetailPojo
import com.app.bemyrider.model.DisputeListPojo
import com.app.bemyrider.model.SendDisputeMessagePojo
import com.app.bemyrider.model.DownloadInvoicePojo
import com.app.bemyrider.model.ProviderServiceRequestPojo
import com.app.bemyrider.model.ServiceReviewPojo
import com.app.bemyrider.model.partner.MyServiceListPojo
import com.app.bemyrider.model.CheckStripeConnectedPojo
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.LanguagePojo
import com.app.bemyrider.model.NewLoginPojo
import com.app.bemyrider.model.CustomerHistoryPojo
import com.app.bemyrider.model.user.FavoriteServiceListPojo
import com.app.bemyrider.model.user.ProviderListPOJO
import com.app.bemyrider.model.ProviderServiceDetailPOJO
import com.app.bemyrider.model.ServiceListPOJO
import com.app.bemyrider.model.partner.SubCategoryListPojo
import com.app.bemyrider.model.user.CategoryListPOJO
import com.app.bemyrider.model.user.ProviderMainPojo
import com.app.bemyrider.model.InfoPagePojo
import com.app.bemyrider.model.ProviderHistoryPojo
import com.app.bemyrider.model.MessageDetailPojo
import com.app.bemyrider.model.MessageListPojo
import com.app.bemyrider.model.SendMessagePojo
import com.app.bemyrider.model.NotificationDataPOJO
import com.app.bemyrider.model.NotificationListPojo
import com.app.bemyrider.model.ProfilePojo
import com.app.bemyrider.model.partner.CountryCodePojo
import com.app.bemyrider.model.MinMaxPricePojo
import com.app.bemyrider.model.WithoutBalancePojo
import com.app.bemyrider.model.user.FilterDataPOJO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Url

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
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("profile/deactiveuser")
    suspend fun deactivateAccount(
            @Field("user_id") userId: String,
            @Field("user_type") userType: String
    ): Response<ResponseBody>

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
    @POST("profile")
    suspend fun getPartnerProfile(
            @Field("loginuser_id") loginUserId: String,
            @Field("profile_id") profileId: String
    ): Response<ProfilePojo>

    @FormUrlEncoded
    @POST("profile/flag_user")
    suspend fun flagUser(
            @Field("user_id") userId: String,
            @Field("flag_user_id") flagUserId: String
    ): Response<CommonPojo>

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

    @FormUrlEncoded
    @POST("bulk-invoices")
    suspend fun bulkInvoices(
        @Field("user_id") userId: String,
        @Field("user_type") userType: String,
        @Field("period") period: String,
        @Field("date_from") dateFrom: String? = null,
        @Field("date_to") dateTo: String? = null
    ): Response<BulkInvoicePojo>

    @POST("other/getSiteSettingData/")
    suspend fun getSiteSettings(): Response<VersionDataPOJO>

    @FormUrlEncoded
    @POST("services/populartasker")
    suspend fun getPopularTaskers(
        @Field("subcategory_id") subcategoryId: String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
        @Field("user_id") userId: String
    ): Response<PopularTaskerPOJO>

    @FormUrlEncoded
    @POST("profile/walletdetails/")
    suspend fun getWalletDetails(
        @Field("user_id") userId: String
    ): Response<WalletDetailsPojo>

    @FormUrlEncoded
    @POST("finance/deposithistory")
    suspend fun getDepositHistory(
        @Field("user_id") userId: String
    ): Response<DepositHistoryPojo>

    @FormUrlEncoded
    @POST("finance/redeemhistory")
    suspend fun getRedeemHistory(
        @Field("user_id") userId: String
    ): Response<RedeemHistoryPojo>

    @FormUrlEncoded
    @POST("finance/sendredeemrequest")
    suspend fun sendRedeemRequest(
        @Field("user_id") userId: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("finance/paymenthistory")
    suspend fun getPaymentHistory(
        @Field("user_id") userId: String,
        @Field("page") page: Int
    ): Response<PaymentHistoryPojo>

    @FormUrlEncoded
    @POST("services/transectionhistory")
    suspend fun getPartnerPaymentHistory(
        @Field("user_id") userId: String,
        @Field("page") page: Int
    ): Response<PartnerPaymentHistoryPojo>

    @FormUrlEncoded
    @POST("finance/successpayment")
    suspend fun confirmStripePayment(
        @Field("user_id") userId: String,
        @Field("service_id") serviceId: String,
        @Field("payment_instant_id") paymentIntentId: String,
        @Field("payment_id") paymentId: String,
        @Field("amount") amount: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("finance/financialinfo")
    suspend fun getFinancialInfo(
        @Field("user_id") userId: String
    ): Response<FinancialInfoPojo>

    @FormUrlEncoded
    @POST("disputes/getdisputelist")
    suspend fun getDisputeList(
        @Field("user_id") userId: String,
        @Field("page") page: Int
    ): Response<DisputeListPojo>

    @FormUrlEncoded
    @POST("disputes/getdisputedetails")
    suspend fun getDisputeDetail(
        @Field("dispute_id") disputeId: String,
        @Field("page") page: Int,
        @Field("last_message_id") lastMessageId: String? = null
    ): Response<DisputeDetailPojo>

    @Multipart
    @POST("disputes/senddisputemessage")
    suspend fun sendDisputeMessage(
        @Part("dispute_id") disputeId: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("message_text") messageText: RequestBody?,
        @Part attachment: MultipartBody.Part?
    ): Response<SendDisputeMessagePojo>

    @FormUrlEncoded
    @POST("disputes/acceptdispute")
    suspend fun acceptDispute(
        @Field("dispute_id") disputeId: String,
        @Field("user_id") userId: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("disputes/escalatetoadmin")
    suspend fun escalateToAdmin(
        @Field("dispute_id") disputeId: String,
        @Field("user_id") userId: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("disputes/raisedispute")
    suspend fun raiseDispute(
        @Field("service_request_id") serviceRequestId: String,
        @Field("user_id") userId: String,
        @Field("title") title: String,
        @Field("message") message: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("services/providerservices")
    suspend fun getMyServices(
        @FieldMap params: Map<String, String>
    ): Response<MyServiceListPojo>

    @FormUrlEncoded
    @POST("services/deleteservices")
    suspend fun deleteService(
        @Field("provider_service_id") providerServiceId: String,
        @Field("user_id") userId: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("services/providerservice")
    suspend fun getServiceRequestDetail(
        @Field("user_id") userId: String,
        @Field("service_request_id") serviceRequestId: String
    ): Response<ProviderServiceRequestPojo>

    @FormUrlEncoded
    @POST
    suspend fun downloadInvoice(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Response<DownloadInvoicePojo>

    @FormUrlEncoded
    @POST("services/providerreviews")
    suspend fun getProviderReviews(
        @FieldMap params: Map<String, String>
    ): Response<ServiceReviewPojo>

    @FormUrlEncoded
    @POST("notifications/getNotifications")
    suspend fun getNotifications(
        @Field("user_id") userId: String,
        @Field("user_type") userType: String,
        @Field("page") page: Int
    ): Response<NotificationDataPOJO>

    @FormUrlEncoded
    @POST("messages/getmessagelist")
    suspend fun getMessageList(
        @Field("user_id") userId: String,
        @Field("page") page: Int
    ): Response<MessageListPojo>

    @FormUrlEncoded
    @POST("messages/getmessage")
    suspend fun getMessageDetail(
        @Field("from_user_id") fromUserId: String,
        @Field("to_user_id") toUserId: String,
        @Field("service_master_id") masterServiceId: String,
        @Field("page") page: Int,
        @Field("last_message_id") lastMessageId: String? = null,
        @Field("service_booking_id") bookingId: String? = null
    ): Response<MessageDetailPojo>

    @Multipart
    @POST("messages/sendmessage")
    suspend fun sendMessage(
        @Part("user_id") userId: RequestBody,
        @Part("to_user_id") toUserId: RequestBody,
        @Part("service_id") serviceId: RequestBody,
        @Part("service_master_id") masterServiceId: RequestBody,
        @Part("message_text") messageText: RequestBody?,
        @Part attachment: MultipartBody.Part?
    ): Response<SendMessagePojo>

    @FormUrlEncoded
    @POST("services/servicelist")
    suspend fun getServiceList(
        @Field("user_type") userType: String,
        @Field("subcategory_id") subcategoryId: String
    ): Response<ServiceListPOJO>

    @FormUrlEncoded
    @POST("services/categorylist")
    suspend fun getCategoryList(
        @Field("provider_id") providerId: String
    ): Response<CategoryListPOJO>

    @FormUrlEncoded
    @POST("services/subcategorylist")
    suspend fun getSubCategoryList(
        @Field("category_id") categoryId: String,
        @Field("provider_id") providerId: String
    ): Response<SubCategoryListPojo>

    @FormUrlEncoded
    @POST("services/popularservice")
    suspend fun getPopularServices(
        @Field("sub_category_id") subCategoryId: String,
        @Field("provider_id") providerId: String
    ): Response<ServiceListPOJO>

    @FormUrlEncoded
    @POST("services/getfavoriteservice")
    suspend fun getFavoriteList(
        @FieldMap params: Map<String, String>
    ): Response<FavoriteServiceListPojo>

    @FormUrlEncoded
    @POST("services/likedislikeservices")
    suspend fun toggleFavorite(
        @FieldMap params: Map<String, String>
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("services/providerservicedetail")
    suspend fun getServiceDetail(
        @FieldMap params: Map<String, String>
    ): Response<ProviderServiceDetailPOJO>

    @FormUrlEncoded
    @POST("services/homeproviderservicedetail")
    suspend fun getServiceDetailHome(
        @FieldMap params: Map<String, String>
    ): Response<ProviderServiceDetailPOJO>

    @FormUrlEncoded
    @POST("services/providerlist")
    suspend fun getProviderList(
        @FieldMap params: Map<String, String>
    ): Response<ProviderListPOJO>

    @FormUrlEncoded
    @POST("services/small")
    suspend fun getSmallDelivery(
        @Field("action") action: String,
        @Field("sort") sort: String,
        @Field("search_rating") rating: String,
        @Field("search_location") location: String,
        @Field("search_lat") lat: String,
        @Field("search_long") lng: String,
        @Field("search_keyword") keyword: String,
        @Field("page") page: Int
    ): Response<ProviderMainPojo>

    @FormUrlEncoded
    @POST("services/medium")
    suspend fun getMediumDelivery(
        @Field("action") action: String,
        @Field("sort") sort: String,
        @Field("search_rating") rating: String,
        @Field("search_location") location: String,
        @Field("search_lat") lat: String,
        @Field("search_long") lng: String,
        @Field("search_keyword") keyword: String,
        @Field("page") page: Int
    ): Response<ProviderMainPojo>

    @FormUrlEncoded
    @POST("services/large")
    suspend fun getLargeDelivery(
        @Field("action") action: String,
        @Field("sort") sort: String,
        @Field("search_rating") rating: String,
        @Field("search_location") location: String,
        @Field("search_lat") lat: String,
        @Field("search_long") lng: String,
        @Field("search_keyword") keyword: String,
        @Field("page") page: Int
    ): Response<ProviderMainPojo>

    @FormUrlEncoded
    @POST("services/customerservices")
    suspend fun getCustomerServiceHistory(
        @Field("user_id") userId: String,
        @Field("tab") tab: String,
        @Field("page") page: Int
    ): Response<CustomerHistoryPojo>

    @FormUrlEncoded
    @POST("services/providertasks")
    suspend fun getPartnerServiceRequests(
        @Field("user_id") userId: String,
        @Field("tab") tab: String,
        @Field("keyword") keyword: String,
        @Field("page") page: Int
    ): Response<ProviderHistoryPojo>

    @POST("cms/getcmslist")
    suspend fun getInfoList(): Response<InfoPagePojo>

    @FormUrlEncoded
    @POST("messages/contactus")
    suspend fun sendContactUs(
        @Field("user_id") userId: String,
        @Field("firstName") firstName: String,
        @Field("lastName") lastName: String,
        @Field("email") email: String,
        @Field("contact_number") contactNumber: String,
        @Field("country_code") countryCode: String,
        @Field("message") message: String
    ): Response<CommonPojo>

    @Multipart
    @POST("messages/feedback")
    suspend fun sendFeedback(
        @Part("user_id") userId: RequestBody,
        @Part("firstName") firstName: RequestBody,
        @Part("lastName") lastName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("message") message: RequestBody,
        @Part userImg: MultipartBody.Part?
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("profile/getnotificationlist")
    suspend fun getNotificationSettings(
        @Field("user_id") userId: String
    ): Response<NotificationListPojo>

    @FormUrlEncoded
    @POST("profile/updatenotification")
    suspend fun updateNotificationSettings(
        @FieldMap params: Map<String, String>
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("services/cancelservice")
    suspend fun cancelService(
        @Field("service_id") serviceId: String,
        @Field("user_id") userId: String,
        @Field("cancel_reason") cancelReason: String,
        @Field("user_type") userType: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("services/extendservice")
    suspend fun extendService(
        @Field("txt_service_request_id") serviceRequestId: String,
        @Field("sel_hours") selectedHours: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("services/extendservicepayment")
    suspend fun extendServicePayment(
        @Field("extend_id") extendId: String,
        @Field("service_request_token") serviceRequestToken: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("services/addproviderreview")
    suspend fun addReview(
        @Field("user_id") userId: String,
        @Field("service_id") serviceId: String,
        @Field("txt_ratting") rating: String,
        @Field("txt_description") description: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("services/acceptproposal")
    suspend fun acceptProposal(
        @Field("status_type") statusType: String,
        @Field("proposal_id") proposalId: String,
        @Field("user_id") userId: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("services/sendproposal")
    suspend fun sendProposal(
        @Field("sel_message_hour") selectedHours: String,
        @Field("txt_message") message: String,
        @Field("txt_proposal_id") proposalId: String,
        @Field("user_id") userId: String
    ): Response<CommonPojo>

    @FormUrlEncoded
    @POST("services/getList")
    suspend fun getFilterList(
        @FieldMap params: Map<String, String>
    ): Response<FilterDataPOJO>

    @POST("services/minmaxprice")
    suspend fun getMinMaxPrice(): Response<MinMaxPricePojo>

    @FormUrlEncoded
    @POST("finance/servicerequestpayment")
    suspend fun bookServiceRequest(
        @Field("user_id") userId: String,
        @Field("service_id") serviceId: String
    ): Response<WithoutBalancePojo>

    @Multipart
    @POST("profile/editprofile")
    suspend fun editPartnerProfile(
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part profilePic: MultipartBody.Part?,
        @Part signatureImg: MultipartBody.Part?
    ): Response<ResponseBody>
}
