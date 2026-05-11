package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.DownloadInvoicePojo
import com.app.bemyrider.model.ProviderServiceDetailPOJO
import com.app.bemyrider.model.WithoutBalancePojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookedServiceDetailViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _serviceDetail = MutableLiveData<ProviderServiceDetailPOJO?>()
    val serviceDetail: LiveData<ProviderServiceDetailPOJO?> = _serviceDetail

    private val _favouriteResult = MutableLiveData<CommonPojo?>()
    val favouriteResult: LiveData<CommonPojo?> = _favouriteResult

    private val _cancelResult = MutableLiveData<CommonPojo?>()
    val cancelResult: LiveData<CommonPojo?> = _cancelResult

    private val _extendPaymentResult = MutableLiveData<CommonPojo?>()
    val extendPaymentResult: LiveData<CommonPojo?> = _extendPaymentResult

    private val _extendServiceResult = MutableLiveData<CommonPojo?>()
    val extendServiceResult: LiveData<CommonPojo?> = _extendServiceResult

    private val _reviewResult = MutableLiveData<CommonPojo?>()
    val reviewResult: LiveData<CommonPojo?> = _reviewResult

    private val _bookResult = MutableLiveData<WithoutBalancePojo?>()
    val bookResult: LiveData<WithoutBalancePojo?> = _bookResult

    private val _invoiceResult = MutableLiveData<DownloadInvoicePojo?>()
    val invoiceResult: LiveData<DownloadInvoicePojo?> = _invoiceResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadServiceDetail(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getServiceDetail(params)
                if (r.isSuccessful && r.body() != null) { _serviceDetail.postValue(r.body()); _error.postValue(null) }
                else { _error.postValue("Errore ${r.code()}"); _serviceDetail.postValue(null) }
            } catch (e: Exception) { _error.postValue(e.localizedMessage); _serviceDetail.postValue(null) }
        }
    }

    fun toggleFavourite(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.toggleFavorite(params)
                if (r.isSuccessful && r.body() != null) { _favouriteResult.postValue(r.body()); _error.postValue(null) }
                else { _error.postValue("Errore ${r.code()}"); _favouriteResult.postValue(null) }
            } catch (e: Exception) { _error.postValue(e.localizedMessage); _favouriteResult.postValue(null) }
        }
    }

    fun cancelService(serviceId: String, userId: String, cancelReason: String, userType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.cancelService(serviceId, userId, cancelReason, userType)
                if (r.isSuccessful && r.body() != null) { _cancelResult.postValue(r.body()); _error.postValue(null) }
                else { _error.postValue("Errore ${r.code()}"); _cancelResult.postValue(null) }
            } catch (e: Exception) { _error.postValue(e.localizedMessage); _cancelResult.postValue(null) }
        }
    }

    fun extendServicePayment(extendId: String, token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.extendServicePayment(extendId, token)
                if (r.isSuccessful && r.body() != null) { _extendPaymentResult.postValue(r.body()); _error.postValue(null) }
                else { _error.postValue("Errore ${r.code()}"); _extendPaymentResult.postValue(null) }
            } catch (e: Exception) { _error.postValue(e.localizedMessage); _extendPaymentResult.postValue(null) }
        }
    }

    fun extendService(serviceRequestId: String, selectedHours: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.extendService(serviceRequestId, selectedHours)
                if (r.isSuccessful && r.body() != null) { _extendServiceResult.postValue(r.body()); _error.postValue(null) }
                else { _error.postValue("Errore ${r.code()}"); _extendServiceResult.postValue(null) }
            } catch (e: Exception) { _error.postValue(e.localizedMessage); _extendServiceResult.postValue(null) }
        }
    }

    fun addReview(userId: String, serviceId: String, rating: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.addReview(userId, serviceId, rating, description)
                if (r.isSuccessful && r.body() != null) { _reviewResult.postValue(r.body()); _error.postValue(null) }
                else { _error.postValue("Errore ${r.code()}"); _reviewResult.postValue(null) }
            } catch (e: Exception) { _error.postValue(e.localizedMessage); _reviewResult.postValue(null) }
        }
    }

    fun bookServiceRequest(userId: String, serviceId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.bookServiceRequest(userId, serviceId)
                if (r.isSuccessful && r.body() != null) { _bookResult.postValue(r.body()); _error.postValue(null) }
                else { _error.postValue("Errore ${r.code()}"); _bookResult.postValue(null) }
            } catch (e: Exception) { _error.postValue(e.localizedMessage); _bookResult.postValue(null) }
        }
    }

    fun downloadInvoice(url: String, params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.downloadInvoice(url, params)
                if (r.isSuccessful && r.body() != null) { _invoiceResult.postValue(r.body()); _error.postValue(null) }
                else { _error.postValue("Errore ${r.code()}"); _invoiceResult.postValue(null) }
            } catch (e: Exception) { _error.postValue(e.localizedMessage); _invoiceResult.postValue(null) }
        }
    }
}
