package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.NewLoginPojo
import com.app.bemyrider.model.ProfilePojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers

class ProviderProfileViewModel : ViewModel() {

    private val appRepository = AppRepository()

    fun getProfile(profileId: String): LiveData<ProfilePojo> = liveData(Dispatchers.IO) {
        try {
            val response = appRepository.getProfile(profileId)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!)
            } else {
                val error = ProfilePojo()
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

    fun updateAvailabilityStatus(userId: String, isAvailable: String): LiveData<CommonPojo> = liveData(Dispatchers.IO) {
        try {
            val response = appRepository.updateAvailabilityStatus(userId, isAvailable)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!)
            } else {
                val error = CommonPojo()
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

    fun socialSignIn(email: String?, firstName: String?, lastName: String?, profileImageUrl: String?, loginType: String, socialId: String, userId: String): LiveData<NewLoginPojo> = liveData(Dispatchers.IO) {
         try {
            val fbId = if (loginType == "f") socialId else ""
            val googleId = if (loginType == "g") socialId else ""
            val linkedInId = if (loginType == "l") socialId else ""
            
            // Passo userId come deviceToken per replicare la logica del vecchio codice
            val response = appRepository.socialLogin(
                firstName ?: "", 
                lastName ?: "", 
                email ?: "", 
                loginType, 
                fbId, 
                googleId, 
                linkedInId, 
                profileImageUrl ?: "", 
                userId 
            )
            
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
