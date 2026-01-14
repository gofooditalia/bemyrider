package com.app.bemyrider.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.app.bemyrider.model.NewLoginPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers

class AppSignupViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository()

    // Note: Returns NewLoginPojo instead of RegistrationPojo because AppRepository normalizes the response type.
    fun signup(
        firstName: String, lastName: String, email: String, userType: String,
        contactNumber: String, password: String, rePassword: String,
        countryCodeId: String, deviceToken: String?
    ): LiveData<NewLoginPojo> = liveData(Dispatchers.IO) {
        try {
            val token = deviceToken ?: ""
            val response = appRepository.signup(firstName, lastName, email, userType, contactNumber, password, rePassword, countryCodeId, token)
            emit(response)
        } catch (e: Exception) {
            val error = NewLoginPojo()
            error.setStatus(false)
            error.setMessage(e.localizedMessage ?: "Errore sconosciuto")
            emit(error)
        }
    }

    fun socialLogin(
        firstName: String, lastName: String, email: String, loginType: String,
        fbId: String, googleId: String, linkedInId: String, pictureUrl: String, deviceToken: String?
    ): LiveData<NewLoginPojo> = liveData(Dispatchers.IO) {
        try {
            val token = deviceToken ?: ""
            // AppRepository.socialLogin returns Response<NewLoginPojo>
            val response = appRepository.socialLogin(firstName, lastName, email, loginType, fbId, googleId, linkedInId, pictureUrl, token)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!)
            } else {
                val error = NewLoginPojo()
                error.setStatus(false)
                error.setMessage("Errore server: ${response.code()}")
                emit(error)
            }
        } catch (e: Exception) {
            val error = NewLoginPojo()
            error.setStatus(false)
            error.setMessage(e.localizedMessage ?: "Errore sconosciuto")
            emit(error)
        }
    }
}
