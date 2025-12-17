package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.app.bemyrider.model.CheckStripeConnectedPojo
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.ProfilePojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers

class ProviderMenuViewModel : ViewModel() {

    // Uso il nuovo Repository moderno (Kotlin + Coroutines)
    private val appRepository = AppRepository()

    fun getProfile(profileId: String): LiveData<ProfilePojo> = liveData(Dispatchers.IO) {
        try {
            val response = appRepository.getProfile(profileId)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!)
            } else {
                val error = ProfilePojo()
                // ProfilePojo è Kotlin data class -> uso properties
                error.status = false
                error.message = "Errore server: ${response.code()}"
                emit(error)
            }
        } catch (e: Exception) {
            val error = ProfilePojo()
            error.status = false
            error.message = e.localizedMessage ?: "Errore sconosciuto"
            emit(error)
        }
    }

    fun logout(userId: String): LiveData<CommonPojo> = liveData(Dispatchers.IO) {
        try {
            val response = appRepository.logout(userId)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!)
            } else {
                val error = CommonPojo()
                // CommonPojo è Java -> uso setter espliciti per sicurezza
                error.setStatus(false)
                error.setMessage("Errore server: ${response.code()}")
                emit(error)
            }
        } catch (e: Exception) {
            val error = CommonPojo()
            error.setStatus(false)
            error.setMessage(e.localizedMessage ?: "Errore sconosciuto")
            emit(error)
        }
    }

    fun checkStripeStatus(userId: String): LiveData<CheckStripeConnectedPojo?> = liveData(Dispatchers.IO) {
        try {
            val response = appRepository.checkStripeStatus(userId)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }
}
