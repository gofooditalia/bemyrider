package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CustomerHistoryPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomerServiceHistoryViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _services = MutableLiveData<CustomerHistoryPojo?>()
    val services: LiveData<CustomerHistoryPojo?> = _services

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadServices(userId: String, tab: String, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getCustomerServiceHistory(userId, tab, page)
                if (response.isSuccessful && response.body() != null) {
                    _services.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _services.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _services.postValue(null)
            }
        }
    }
}
