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

class PartnerServiceDetailViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _detail = MutableLiveData<ProviderServiceDetailPOJO?>()
    val detail: LiveData<ProviderServiceDetailPOJO?> = _detail

    private val _deleteResult = MutableLiveData<CommonPojo?>()
    val deleteResult: LiveData<CommonPojo?> = _deleteResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDetail(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getServiceDetail(params)
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

    fun deleteService(providerServiceId: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.deleteService(providerServiceId, userId)
                if (response.isSuccessful && response.body() != null) {
                    _deleteResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore eliminazione servizio")
                    _deleteResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _deleteResult.postValue(null)
            }
        }
    }
}
