package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.BulkInvoicePojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BulkInvoiceViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _result = MutableLiveData<BulkInvoicePojo?>()
    val result: LiveData<BulkInvoicePojo?> = _result

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun downloadBulk(userId: String, userType: String, period: String,
                     dateFrom: String? = null, dateTo: String? = null) {
        _loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.bulkInvoices(userId, userType, period, dateFrom, dateTo)
                if (response.isSuccessful && response.body() != null) {
                    _result.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _result.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _result.postValue(null)
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
