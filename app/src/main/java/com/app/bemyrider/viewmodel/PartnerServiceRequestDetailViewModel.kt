package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.DownloadInvoicePojo
import com.app.bemyrider.model.ProviderServiceRequestPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PartnerServiceRequestDetailViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _detail = MutableLiveData<ProviderServiceRequestPojo?>()
    val detail: LiveData<ProviderServiceRequestPojo?> = _detail

    private val _invoiceResult = MutableLiveData<DownloadInvoicePojo?>()
    val invoiceResult: LiveData<DownloadInvoicePojo?> = _invoiceResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDetail(userId: String, serviceRequestId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getServiceRequestDetail(userId, serviceRequestId)
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

    fun downloadInvoice(url: String, params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.downloadInvoice(url, params)
                if (response.isSuccessful && response.body() != null) {
                    _invoiceResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore download ricevuta")
                    _invoiceResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _invoiceResult.postValue(null)
            }
        }
    }
}
