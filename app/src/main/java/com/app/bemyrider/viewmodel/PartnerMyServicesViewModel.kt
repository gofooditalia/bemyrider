package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.partner.MyServiceListPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PartnerMyServicesViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _services = MutableLiveData<MyServiceListPojo?>()
    val services: LiveData<MyServiceListPojo?> = _services

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMyServices(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getMyServices(params)
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
