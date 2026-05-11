package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.DisputeDetailPojo
import com.app.bemyrider.model.SendDisputeMessagePojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DisputeDetailViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _detail = MutableLiveData<DisputeDetailPojo?>()
    val detail: LiveData<DisputeDetailPojo?> = _detail

    private val _sendResult = MutableLiveData<SendDisputeMessagePojo?>()
    val sendResult: LiveData<SendDisputeMessagePojo?> = _sendResult

    private val _actionResult = MutableLiveData<CommonPojo?>()
    val actionResult: LiveData<CommonPojo?> = _actionResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDetail(disputeId: String, page: Int, lastMessageId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getDisputeDetail(disputeId, page, lastMessageId)
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

    fun sendMessage(disputeId: String, userId: String, messageText: String?, filePath: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.sendDisputeMessage(disputeId, userId, messageText, filePath)
                if (response.isSuccessful && response.body() != null) {
                    _sendResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore invio messaggio")
                    _sendResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _sendResult.postValue(null)
            }
        }
    }

    fun acceptDispute(disputeId: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.acceptDispute(disputeId, userId)
                if (response.isSuccessful && response.body() != null) {
                    _actionResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore accettazione disputa")
                    _actionResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _actionResult.postValue(null)
            }
        }
    }

    fun escalateToAdmin(disputeId: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.escalateToAdmin(disputeId, userId)
                if (response.isSuccessful && response.body() != null) {
                    _actionResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore escalation")
                    _actionResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _actionResult.postValue(null)
            }
        }
    }
}
