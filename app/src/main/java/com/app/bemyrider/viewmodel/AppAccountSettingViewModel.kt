package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.LanguagePojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers

class AppAccountSettingViewModel : ViewModel() {

    private val appRepository = AppRepository()

    fun getLanguages(): LiveData<LanguagePojo?> = liveData(Dispatchers.IO) {
        try {
            val response = appRepository.getLanguages()
            if (response.isSuccessful && response.body() != null) {
                emit(response.body())
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    fun changePassword(currentPwd: String, newPwd: String, reNewPwd: String, userId: String): LiveData<CommonPojo> = liveData(Dispatchers.IO) {
        try {
            val response = appRepository.changePassword(currentPwd, newPwd, reNewPwd, userId)
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

    fun deactivateAccount(userId: String, userType: String): LiveData<CommonPojo> = liveData(Dispatchers.IO) {
        try {
            val response = appRepository.deactivateAccount(userId, userType)
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
}
