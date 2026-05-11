package com.app.bemyrider.repository

import android.util.Log
import com.app.bemyrider.model.BulkInvoicePojo
import com.app.bemyrider.model.CheckStripeConnectedPojo
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.LanguagePojo
import com.app.bemyrider.model.NewLoginPojo
import com.app.bemyrider.model.CustomerHistoryPojo
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
import com.app.bemyrider.network.ApiServiceKt
import com.app.bemyrider.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.Locale

class AppRepository {

    private val TAG = "AppRepository"
    private val apiService: ApiServiceKt = RetrofitClient.getClient().create(ApiServiceKt::class.java)
    private val gson: Gson = GsonBuilder().setDateFormat("M/d/yy hh:mm a").create()

    // Helper per creare RequestBody da String
    private fun createPartFromString(descriptionString: String?): RequestBody {
        return (descriptionString ?: "").toRequestBody(MultipartBody.FORM)
    }

    suspend fun getLanguages(): Response<LanguagePojo> = apiService.getLanguages()

    suspend fun getCountryCodes(): Response<CountryCodePojo> = apiService.getCountryCodes()

    suspend fun updateAvailabilityStatus(userId: String, isAvailable: String): Response<CommonPojo> {
        return apiService.updateAvailabilityStatus(userId, isAvailable)
    }

    suspend fun logout(userId: String): Response<CommonPojo> = apiService.logout(userId)

    suspend fun checkStripeStatus(userId: String): Response<CheckStripeConnectedPojo> = apiService.checkStripeStatus(userId)

    suspend fun editProfile(
        userId: String, firstName: String, lastName: String, imagePath: String?,
        contactNumber: String, countryCode: String, cityOfCompany: String, address: String,
        paymentMode: String, companyName: String, vat: String, certifiedEmail: String,
        receiptCode: String, latitude: String, longitude: String
    ): ProfilePojo {
        
        var imagePart: MultipartBody.Part? = null
        if (!imagePath.isNullOrEmpty()) {
            val file = File(imagePath)
            if (file.exists()) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                imagePart = MultipartBody.Part.createFormData("profile_pic", file.name, requestFile)
            }
        }

        val response = apiService.editProfile(
            createPartFromString(userId),
            createPartFromString(firstName),
            createPartFromString(lastName),
            imagePart,
            createPartFromString(contactNumber),
            createPartFromString(countryCode),
            createPartFromString(cityOfCompany),
            createPartFromString(address),
            createPartFromString(paymentMode),
            createPartFromString(companyName),
            createPartFromString(vat),
            createPartFromString(certifiedEmail),
            createPartFromString(receiptCode),
            createPartFromString(latitude),
            createPartFromString(longitude)
        )

        return parseGenericResponse(response, ProfilePojo::class.java)
    }
    
    suspend fun changePassword(currentPwd: String, newPwd: String, reNewPwd: String, userId: String): CommonPojo {
        val response = apiService.changePassword(currentPwd, newPwd, reNewPwd, userId)
        return parseGenericResponse(response, CommonPojo::class.java)
    }

    suspend fun deactivateAccount(userId: String, userType: String): CommonPojo {
        val response = apiService.deactivateAccount(userId, userType)
        return parseGenericResponse(response, CommonPojo::class.java)
    }

    suspend fun login(email: String, password: String, deviceToken: String): NewLoginPojo {
        val response = apiService.login(email, password, deviceToken)
        return parseGenericResponse(response, NewLoginPojo::class.java)
    }

    suspend fun signup(
        firstName: String, lastName: String, email: String, userType: String,
        contactNumber: String, password: String, rePassword: String,
        countryCodeId: String, deviceToken: String
    ): NewLoginPojo { 
        val response = apiService.signup(firstName, lastName, email, userType, contactNumber, password, rePassword, countryCodeId, deviceToken)
        return parseRegistrationResponse(response)
    }
    
    suspend fun socialLogin(
        firstName: String, lastName: String, email: String, loginType: String,
        fbId: String, googleId: String, linkedInId: String, pictureUrl: String, deviceToken: String
    ): Response<NewLoginPojo> {
        return apiService.socialLogin(firstName, lastName, email, loginType, fbId, googleId, linkedInId, pictureUrl, deviceToken)
    }

    suspend fun getProfile(profileId: String): Response<ProfilePojo> = apiService.getProfile(profileId)

    suspend fun getPartnerProfile(loginUserId: String, profileId: String): Response<ProfilePojo> =
        apiService.getPartnerProfile(loginUserId, profileId)

    suspend fun flagUser(userId: String, flagUserId: String): Response<CommonPojo> =
        apiService.flagUser(userId, flagUserId)

    suspend fun getOfflineData(userId: String): Response<String> = apiService.getOfflineData(userId)

    suspend fun forgotPassword(email: String): NewLoginPojo {
        val response = apiService.forgotPassword(email)
        return parseGenericResponse(response, NewLoginPojo::class.java)
    }

    suspend fun resendActivationMail(email: String): NewLoginPojo {
        val response = apiService.resendActivationMail(email)
        return parseGenericResponse(response, NewLoginPojo::class.java)
    }

    suspend fun bulkInvoices(userId: String, userType: String, period: String, dateFrom: String? = null, dateTo: String? = null): Response<BulkInvoicePojo> {
        return apiService.bulkInvoices(userId, userType, period, dateFrom, dateTo)
    }

    // --- Helpers per il parsing (Porting dal Java) ---

    private fun <T : Any> parseGenericResponse(response: Response<ResponseBody>, type: Class<T>): T {
        val result = type.getDeclaredConstructor().newInstance()
        
        try {
            if (response.isSuccessful && response.body() != null) {
                val rawJson = response.body()!!.string()
                Log.d(TAG, "Raw Response for ${type.simpleName}: $rawJson")
                try {
                    val parsed = gson.fromJson(rawJson, type)
                    return parsed ?: result
                } catch (e: JsonSyntaxException) {
                    Log.e(TAG, "JSON Syntax Error: ${e.message}")
                    setPojoStatus(result, false)
                    setPojoMessage(result, extractErrorMessage(rawJson))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Empty error body"
                Log.e(TAG, "Server Error: ${response.code()} - $errorBody")
                setPojoStatus(result, false)
                setPojoMessage(result, "Errore del server: ${response.code()}")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network Error: ${e.message}")
            setPojoStatus(result, false)
            setPojoMessage(result, "Errore di rete.")
        } catch (e: Exception) {
            Log.e(TAG, "Unknown Error: ${e.message}")
            setPojoStatus(result, false)
            setPojoMessage(result, "Errore sconosciuto: ${e.message}")
        }
        return result
    }

    private fun parseRegistrationResponse(response: Response<ResponseBody>): NewLoginPojo {
       return parseGenericResponse(response, NewLoginPojo::class.java)
    }

    // Reflection helpers
    private fun setPojoStatus(obj: Any, status: Boolean) {
        try {
            val method = obj.javaClass.getMethod("setStatus", Boolean::class.javaPrimitiveType)
            method.invoke(obj, status)
        } catch (e: Exception) { /* ignore */ }
    }

    private fun setPojoMessage(obj: Any, message: String) {
        try {
            val method = obj.javaClass.getMethod("setMessage", String::class.java)
            method.invoke(obj, message)
        } catch (e: Exception) { /* ignore */ }
    }

    private fun extractErrorMessage(rawJson: String): String {
        var message = rawJson
        try {
            val jsonObject = JSONObject(rawJson)
            if (jsonObject.has("message")) {
                val msg = jsonObject.getString("message")
                if (!msg.trim().isEmpty()) {
                    message = msg
                }
            }
            if (jsonObject.has("data")) {
                val dataObj = jsonObject.get("data")
                if (dataObj is String) {
                    if (!dataObj.trim().isEmpty()) {
                        message = dataObj
                    }
                }
            }
        } catch (e: Exception) { }
        
        return translateMessage(message)
    }

    private fun translateMessage(message: String?): String {
        if (message == null) return ""
        val lowerMsg = message.lowercase(Locale.US)
        
        if (lowerMsg.contains("invalid login") || lowerMsg.contains("password not match")) {
            return "Credenziali non valide. Controlla email e password."
        }
        if (lowerMsg.contains("email is already exist") || lowerMsg.contains("email already registered")) {
            return "Questa email risulta già registrata."
        }
        if (lowerMsg.contains("user does not exist") || lowerMsg.contains("email does not exists")) {
            return "Utente non trovato. Registrati per accedere."
        }
        if (lowerMsg.contains("new password has been sent")) {
            return "Una nuova password è stata inviata alla tua email."
        }
        if (lowerMsg.contains("activation link has been sent") || lowerMsg.contains("activation mail sent")) {
            return "Link di attivazione inviato. Controlla la tua email."
        }
        if (lowerMsg.contains("service request sent successfully")) {
            return "Richiesta di servizio inviata con successo."
        }
        return message
    }

    suspend fun getNotifications(userId: String, userType: String, page: Int): Response<NotificationDataPOJO> {
        return apiService.getNotifications(userId, userType, page)
    }

    suspend fun getMessageList(userId: String, page: Int): Response<MessageListPojo> =
        apiService.getMessageList(userId, page)

    suspend fun getMessageDetail(
        fromUserId: String, toUserId: String, masterServiceId: String,
        page: Int, lastMessageId: String? = null, bookingId: String? = null
    ): Response<MessageDetailPojo> =
        apiService.getMessageDetail(fromUserId, toUserId, masterServiceId, page, lastMessageId, bookingId)

    suspend fun sendMessage(
        userId: String, toUserId: String, serviceId: String,
        masterServiceId: String, messageText: String?, attachmentPath: String?
    ): Response<SendMessagePojo> {
        val toBody = { s: String -> s.toRequestBody(MultipartBody.FORM) }
        val textPart = if (attachmentPath.isNullOrEmpty() && messageText != null)
            messageText.toRequestBody(MultipartBody.FORM) else null
        val filePart = if (!attachmentPath.isNullOrEmpty()) {
            val file = File(attachmentPath)
            if (file.exists()) MultipartBody.Part.createFormData("attachment", file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())) else null
        } else null
        return apiService.sendMessage(toBody(userId), toBody(toUserId), toBody(serviceId),
            toBody(masterServiceId), textPart, filePart)
    }

    suspend fun getServiceList(userType: String, subcategoryId: String): Response<ServiceListPOJO> =
        apiService.getServiceList(userType, subcategoryId)

    suspend fun getCategoryList(providerId: String): Response<CategoryListPOJO> =
        apiService.getCategoryList(providerId)

    suspend fun getSubCategoryList(categoryId: String, providerId: String): Response<SubCategoryListPojo> =
        apiService.getSubCategoryList(categoryId, providerId)

    suspend fun getPopularServices(subCategoryId: String, providerId: String): Response<ServiceListPOJO> =
        apiService.getPopularServices(subCategoryId, providerId)

    suspend fun getFavoriteList(params: Map<String, String>): Response<FavoriteServiceListPojo> =
        apiService.getFavoriteList(params)

    suspend fun toggleFavorite(params: Map<String, String>): Response<CommonPojo> =
        apiService.toggleFavorite(params)

    suspend fun getServiceDetail(params: Map<String, String>): Response<ProviderServiceDetailPOJO> =
        apiService.getServiceDetail(params)

    suspend fun getServiceDetailHome(params: Map<String, String>): Response<ProviderServiceDetailPOJO> =
        apiService.getServiceDetailHome(params)

    suspend fun getProviderList(params: Map<String, String>): Response<ProviderListPOJO> =
        apiService.getProviderList(params)

    suspend fun getDeliveryProviders(type: Int, sort: String, rating: String, location: String,
                                     lat: String, lng: String, keyword: String, page: Int): Response<ProviderMainPojo> {
        val action = when (type) { 2 -> "large"; 1 -> "medium"; else -> "small" }
        return when (type) {
            2 -> apiService.getLargeDelivery(action, sort, rating, location, lat, lng, keyword, page)
            1 -> apiService.getMediumDelivery(action, sort, rating, location, lat, lng, keyword, page)
            else -> apiService.getSmallDelivery(action, sort, rating, location, lat, lng, keyword, page)
        }
    }

    suspend fun getSiteSettings(): Response<VersionDataPOJO> = apiService.getSiteSettings()

    suspend fun getPopularTaskers(subcategoryId: String, latitude: String, longitude: String, userId: String): Response<PopularTaskerPOJO> =
        apiService.getPopularTaskers(subcategoryId, latitude, longitude, userId)

    suspend fun getWalletDetails(userId: String): Response<WalletDetailsPojo> = apiService.getWalletDetails(userId)
    suspend fun getDepositHistory(userId: String): Response<DepositHistoryPojo> = apiService.getDepositHistory(userId)
    suspend fun getRedeemHistory(userId: String): Response<RedeemHistoryPojo> = apiService.getRedeemHistory(userId)
    suspend fun sendRedeemRequest(userId: String): Response<CommonPojo> = apiService.sendRedeemRequest(userId)
    suspend fun getPaymentHistory(userId: String, page: Int): Response<PaymentHistoryPojo> = apiService.getPaymentHistory(userId, page)
    suspend fun getPartnerPaymentHistory(userId: String, page: Int): Response<PartnerPaymentHistoryPojo> = apiService.getPartnerPaymentHistory(userId, page)
    suspend fun confirmStripePayment(userId: String, serviceId: String, paymentIntentId: String, paymentId: String, amount: String): Response<CommonPojo> =
        apiService.confirmStripePayment(userId, serviceId, paymentIntentId, paymentId, amount)
    suspend fun getFinancialInfo(userId: String): Response<FinancialInfoPojo> = apiService.getFinancialInfo(userId)

    suspend fun getDisputeList(userId: String, page: Int): Response<DisputeListPojo> =
        apiService.getDisputeList(userId, page)

    suspend fun getDisputeDetail(disputeId: String, page: Int, lastMessageId: String? = null): Response<DisputeDetailPojo> =
        apiService.getDisputeDetail(disputeId, page, lastMessageId)

    suspend fun sendDisputeMessage(disputeId: String, userId: String, messageText: String?, filePath: String?): Response<SendDisputeMessagePojo> {
        val toBody = { s: String -> s.toRequestBody(MultipartBody.FORM) }
        val textPart = if (filePath.isNullOrEmpty() && messageText != null) messageText.toRequestBody(MultipartBody.FORM) else null
        val filePart = if (!filePath.isNullOrEmpty()) {
            val file = File(filePath)
            if (file.exists()) MultipartBody.Part.createFormData("attachment", file.name, file.asRequestBody("image/*".toMediaTypeOrNull())) else null
        } else null
        return apiService.sendDisputeMessage(toBody(disputeId), toBody(userId), textPart, filePart)
    }

    suspend fun acceptDispute(disputeId: String, userId: String): Response<CommonPojo> =
        apiService.acceptDispute(disputeId, userId)

    suspend fun escalateToAdmin(disputeId: String, userId: String): Response<CommonPojo> =
        apiService.escalateToAdmin(disputeId, userId)

    suspend fun raiseDispute(serviceRequestId: String, userId: String, title: String, message: String): Response<CommonPojo> =
        apiService.raiseDispute(serviceRequestId, userId, title, message)

    suspend fun getMyServices(params: Map<String, String>): Response<MyServiceListPojo> =
        apiService.getMyServices(params)

    suspend fun deleteService(providerServiceId: String, userId: String): Response<CommonPojo> =
        apiService.deleteService(providerServiceId, userId)

    suspend fun getServiceRequestDetail(userId: String, serviceRequestId: String): Response<ProviderServiceRequestPojo> =
        apiService.getServiceRequestDetail(userId, serviceRequestId)

    suspend fun downloadInvoice(url: String, params: Map<String, String>): Response<DownloadInvoicePojo> =
        apiService.downloadInvoice(url, params)

    suspend fun getProviderReviews(params: Map<String, String>): Response<ServiceReviewPojo> =
        apiService.getProviderReviews(params)

    suspend fun getCustomerServiceHistory(userId: String, tab: String, page: Int): Response<CustomerHistoryPojo> =
        apiService.getCustomerServiceHistory(userId, tab, page)

    suspend fun getPartnerServiceRequests(userId: String, tab: String, keyword: String, page: Int): Response<ProviderHistoryPojo> =
        apiService.getPartnerServiceRequests(userId, tab, keyword, page)

    suspend fun getInfoList(): Response<InfoPagePojo> {
        return apiService.getInfoList()
    }

    suspend fun sendContactUs(
        userId: String, firstName: String, lastName: String,
        email: String, contactNumber: String, countryCode: String, message: String
    ): Response<CommonPojo> {
        return apiService.sendContactUs(userId, firstName, lastName, email, contactNumber, countryCode, message)
    }

    suspend fun sendFeedback(
        userId: String, firstName: String, lastName: String,
        email: String, message: String, imagePath: String?
    ): Response<CommonPojo> {
        val toBody = { s: String -> s.toRequestBody(MultipartBody.FORM) }
        val imagePart = if (!imagePath.isNullOrEmpty()) {
            val file = File(imagePath)
            if (file.exists()) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("user_img", file.name, requestFile)
            } else null
        } else null
        return apiService.sendFeedback(
            toBody(userId), toBody(firstName), toBody(lastName),
            toBody(email), toBody(message), imagePart
        )
    }

    suspend fun getNotificationSettings(userId: String): Response<NotificationListPojo> {
        return apiService.getNotificationSettings(userId)
    }

    suspend fun updateNotificationSettings(params: Map<String, String>): Response<CommonPojo> {
        return apiService.updateNotificationSettings(params)
    }

    suspend fun cancelService(serviceId: String, userId: String, cancelReason: String, userType: String): Response<CommonPojo> =
        apiService.cancelService(serviceId, userId, cancelReason, userType)

    suspend fun extendService(serviceRequestId: String, selectedHours: String): Response<CommonPojo> =
        apiService.extendService(serviceRequestId, selectedHours)

    suspend fun extendServicePayment(extendId: String, serviceRequestToken: String): Response<CommonPojo> =
        apiService.extendServicePayment(extendId, serviceRequestToken)

    suspend fun addReview(userId: String, serviceId: String, rating: String, description: String): Response<CommonPojo> =
        apiService.addReview(userId, serviceId, rating, description)

    suspend fun acceptProposal(statusType: String, proposalId: String, userId: String): Response<CommonPojo> =
        apiService.acceptProposal(statusType, proposalId, userId)

    suspend fun sendProposal(selectedHours: String, message: String, proposalId: String, userId: String): Response<CommonPojo> =
        apiService.sendProposal(selectedHours, message, proposalId, userId)

    suspend fun getFilterList(params: Map<String, String>): Response<FilterDataPOJO> =
        apiService.getFilterList(params)

    suspend fun getMinMaxPrice(): Response<MinMaxPricePojo> =
        apiService.getMinMaxPrice()

    suspend fun bookServiceRequest(userId: String, serviceId: String): Response<WithoutBalancePojo> =
        apiService.bookServiceRequest(userId, serviceId)

    suspend fun editPartnerProfile(
        params: Map<String, String>,
        profileImagePath: String?,
        signatureImagePath: String?
    ): ProfilePojo {
        val partMap = params.mapValues { createPartFromString(it.value) }

        var profilePicPart: MultipartBody.Part? = null
        if (!profileImagePath.isNullOrEmpty()) {
            val file = File(profileImagePath)
            if (file.exists()) {
                profilePicPart = MultipartBody.Part.createFormData("profile_pic", file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull()))
            }
        }

        var signaturePart: MultipartBody.Part? = null
        if (!signatureImagePath.isNullOrEmpty()) {
            val file = File(signatureImagePath)
            if (file.exists()) {
                signaturePart = MultipartBody.Part.createFormData("signature_img", file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull()))
            }
        }

        val response = apiService.editPartnerProfile(partMap, profilePicPart, signaturePart)
        return parseGenericResponse(response, ProfilePojo::class.java)
    }
}