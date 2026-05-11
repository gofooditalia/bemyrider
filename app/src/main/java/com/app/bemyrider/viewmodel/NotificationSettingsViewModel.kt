package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.NotificationListPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationSettingsViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _settings = MutableLiveData<NotificationListPojo?>()
    val settings: LiveData<NotificationListPojo?> = _settings

    private val _updateResult = MutableLiveData<CommonPojo?>()
    val updateResult: LiveData<CommonPojo?> = _updateResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadSettings(userId: String) {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getNotificationSettings(userId)
                if (response.isSuccessful && response.body() != null) {
                    _settings.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _settings.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _settings.postValue(null)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun updateSettings(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.updateNotificationSettings(params)
                if (response.isSuccessful && response.body() != null) {
                    _updateResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _updateResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _updateResult.postValue(null)
            }
        }
    }
}
