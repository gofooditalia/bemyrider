package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.ProviderServiceDetailPOJO
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServiceDetailViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _detail = MutableLiveData<ProviderServiceDetailPOJO?>()
    val detail: LiveData<ProviderServiceDetailPOJO?> = _detail

    private val _favouriteResult = MutableLiveData<CommonPojo?>()
    val favouriteResult: LiveData<CommonPojo?> = _favouriteResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDetail(params: Map<String, String>, isFromHome: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = if (isFromHome)
                    repository.getServiceDetailHome(params)
                else
                    repository.getServiceDetail(params)
                if (response.isSuccessful && response.body() != null) {
                    _detail.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _detail.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _detail.postValue(null)
            }
        }
    }

    fun toggleFavourite(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.toggleFavorite(params)
                if (response.isSuccessful && response.body() != null) {
                    _favouriteResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore aggiornamento preferito")
                    _favouriteResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _favouriteResult.postValue(null)
            }
        }
    }
}
