package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.ServiceListPOJO
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServiceSearchViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _services = MutableLiveData<ServiceListPOJO?>()
    val services: LiveData<ServiceListPOJO?> = _services

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadServices(userType: String, subcategoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getServiceList(userType, subcategoryId)
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
