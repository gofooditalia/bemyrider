package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.user.ProviderMainPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DeliveryTypeViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _providers = MutableLiveData<ProviderMainPojo?>()
    val providers: LiveData<ProviderMainPojo?> = _providers

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var loadJob: Job? = null

    fun loadProviders(type: Int, sort: String, rating: String, location: String,
                      lat: String, lng: String, keyword: String, page: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getDeliveryProviders(type, sort, rating, location, lat, lng, keyword, page)
                if (response.isSuccessful && response.body() != null) {
                    _providers.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _providers.postValue(null)
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _error.postValue(e.localizedMessage ?: "Errore di rete")
                    _providers.postValue(null)
                }
            }
        }
    }
}
