package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.PaymentHistoryPojo
import com.app.bemyrider.model.partner.PartnerPaymentHistoryPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentHistoryViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _history = MutableLiveData<PaymentHistoryPojo?>()
    val history: LiveData<PaymentHistoryPojo?> = _history

    private val _partnerHistory = MutableLiveData<PartnerPaymentHistoryPojo?>()
    val partnerHistory: LiveData<PartnerPaymentHistoryPojo?> = _partnerHistory

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadPaymentHistory(userId: String, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getPaymentHistory(userId, page)
                if (r.isSuccessful && r.body() != null) { _history.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore del server (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }

    fun loadPartnerPaymentHistory(userId: String, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getPartnerPaymentHistory(userId, page)
                if (r.isSuccessful && r.body() != null) { _partnerHistory.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore del server (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }
}
