package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.ProfilePojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _profile = MutableLiveData<ProfilePojo?>()
    val profile: LiveData<ProfilePojo?> = _profile

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadProfile(profileId: String) {
        _isLoading.postValue(true)
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
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
