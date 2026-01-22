package com.app.bemyrider.repository

import com.app.bemyrider.model.CheckStripeConnectedPojo
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.LanguagePojo
import com.app.bemyrider.model.NewLoginPojo
import com.app.bemyrider.model.ProfilePojo
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

    private val apiService: ApiServiceKt = RetrofitClient.getClient().create(ApiServiceKt::class.java)
    private val gson: Gson = GsonBuilder().setDateFormat("M/d/yy hh:mm a").create()

    // Helper per creare RequestBody da String
    private fun createPartFromString(descriptionString: String?): RequestBody {
        return (descriptionString ?: "").toRequestBody(MultipartBody.FORM)
    }

    suspend fun getLanguages(): Response<LanguagePojo> = apiService.getLanguages()

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
    
    suspend fun changePassword(currentPwd: String, newPwd: String, reNewPwd: String, userId: String): Response<CommonPojo> {
        return apiService.changePassword(currentPwd, newPwd, reNewPwd, userId)
    }

    suspend fun deactivateAccount(userId: String, userType: String): Response<CommonPojo> {
        return apiService.deactivateAccount(userId, userType)
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

    suspend fun getOfflineData(userId: String): Response<String> = apiService.getOfflineData(userId)

    suspend fun forgotPassword(email: String): NewLoginPojo {
        val response = apiService.forgotPassword(email)
        return parseGenericResponse(response, NewLoginPojo::class.java)
    }

    suspend fun resendActivationMail(email: String): NewLoginPojo {
        val response = apiService.resendActivationMail(email)
        return parseGenericResponse(response, NewLoginPojo::class.java)
    }

    // --- Helpers per il parsing (Porting dal Java) ---

    private fun <T : Any> parseGenericResponse(response: Response<ResponseBody>, type: Class<T>): T {
        // Correzione per l'API deprecata newInstance()
        val result = type.getDeclaredConstructor().newInstance()
        
        try {
            if (response.isSuccessful && response.body() != null) {
                val rawJson = response.body()!!.string()
                try {
                    val parsed = gson.fromJson(rawJson, type)
                    return parsed ?: result
                } catch (e: JsonSyntaxException) {
                    setPojoStatus(result, false)
                    setPojoMessage(result, extractErrorMessage(rawJson))
                }
            } else {
                setPojoStatus(result, false)
                setPojoMessage(result, "Errore del server: ${response.code()}")
            }
        } catch (e: IOException) {
            setPojoStatus(result, false)
            setPojoMessage(result, "Errore di rete.")
        } catch (e: Exception) {
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
}