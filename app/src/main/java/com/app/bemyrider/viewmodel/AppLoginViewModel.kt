package com.app.bemyrider.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.app.bemyrider.model.NewLoginPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers

// Renamed to avoid conflict with legacy LoginViewModel.java
class AppLoginViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository()

    fun login(email: String, password: String, deviceToken: String?): LiveData<NewLoginPojo> = liveData(Dispatchers.IO) {
        try {
            val token = deviceToken ?: ""
            val response = appRepository.login(email, password, token)
            emit(response)
        } catch (e: Exception) {
            val error = NewLoginPojo()
            error.setStatus(false)
            error.setMessage(e.localizedMessage ?: "Errore sconosciuto")
            emit(error)
        }
    }

    fun forgotPassword(email: String): LiveData<NewLoginPojo> = liveData(Dispatchers.IO) {
        try {
            val response = appRepository.forgotPassword(email)
            emit(response)
        } catch (e: Exception) {
            val error = NewLoginPojo()
            error.setStatus(false)
            error.setMessage(e.localizedMessage ?: "Errore sconosciuto")
            emit(error)
        }
    }

    fun resendActivationMail(email: String): LiveData<NewLoginPojo> = liveData(Dispatchers.IO) {
        try {
            val response = appRepository.resendActivationMail(email)
            emit(response)
        } catch (e: Exception) {
            val error = NewLoginPojo()
            error.setStatus(false)
            error.setMessage(e.localizedMessage ?: "Errore sconosciuto")
            emit(error)
        }
    }
    
    fun getOfflineData(userId: String): LiveData<String> = liveData(Dispatchers.IO) {
        try {
            val response = appRepository.getOfflineData(userId)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!)
            } else {
                emit("")
            }
        } catch (e: Exception) {
            emit("")
        }
    }
}
