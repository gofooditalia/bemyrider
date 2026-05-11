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

class CustomerMenuViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _profile = MutableLiveData<ProfilePojo?>()
    val profile: LiveData<ProfilePojo?> = _profile

    private val _logoutResult = MutableLiveData<CommonPojo?>()
    val logoutResult: LiveData<CommonPojo?> = _logoutResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadProfile(profileId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getProfile(profileId)
                if (r.isSuccessful && r.body() != null) { _profile.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore profilo (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }

    fun logout(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.logout(userId)
                if (r.isSuccessful && r.body() != null) { _logoutResult.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore logout (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }
}
