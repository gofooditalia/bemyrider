package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.ProviderListPOJO
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProviderListViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _providers = MutableLiveData<ProviderListPOJO?>()
    val providers: LiveData<ProviderListPOJO?> = _providers

    private val _toggleResult = MutableLiveData<CommonPojo?>()
    val toggleResult: LiveData<CommonPojo?> = _toggleResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadProviders(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getProviderList(params)
                if (response.isSuccessful && response.body() != null) {
                    _providers.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _providers.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _providers.postValue(null)
            }
        }
    }

    fun toggleFavourite(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.toggleFavorite(params)
                if (response.isSuccessful && response.body() != null) {
                    _toggleResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore aggiornamento preferito")
                    _toggleResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _toggleResult.postValue(null)
            }
        }
    }
}
