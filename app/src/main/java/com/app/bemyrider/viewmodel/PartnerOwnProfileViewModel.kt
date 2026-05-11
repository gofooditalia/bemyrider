package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.ProfilePojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PartnerOwnProfileViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _profile = MutableLiveData<ProfilePojo?>()
    val profile: LiveData<ProfilePojo?> = _profile

    private val _availabilityResult = MutableLiveData<CommonPojo?>()
    val availabilityResult: LiveData<CommonPojo?> = _availabilityResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadProfile(profileId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getProfile(profileId)
                if (response.isSuccessful && response.body() != null) {
                    _profile.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _profile.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _profile.postValue(null)
            }
        }
    }

    fun updateAvailability(userId: String, isAvailable: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.updateAvailabilityStatus(userId, isAvailable)
                if (response.isSuccessful && response.body() != null) {
                    _availabilityResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore aggiornamento disponibilità")
                    _availabilityResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _availabilityResult.postValue(null)
            }
        }
    }
}
