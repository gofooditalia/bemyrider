package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.NotificationDataPOJO
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _notifications = MutableLiveData<NotificationDataPOJO?>()
    val notifications: LiveData<NotificationDataPOJO?> = _notifications

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadNotifications(userId: String, userType: String, page: Int) {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getNotifications(userId, userType, page)
                if (response.isSuccessful && response.body() != null) {
                    _notifications.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _notifications.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _notifications.postValue(null)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
