package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.MessageDetailPojo
import com.app.bemyrider.model.SendMessagePojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageDetailViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _detail = MutableLiveData<MessageDetailPojo?>()
    val detail: LiveData<MessageDetailPojo?> = _detail

    private val _sendResult = MutableLiveData<SendMessagePojo?>()
    val sendResult: LiveData<SendMessagePojo?> = _sendResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDetail(
        fromUserId: String, toUserId: String, masterServiceId: String,
        page: Int, lastMessageId: String? = null, bookingId: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getMessageDetail(
                    fromUserId, toUserId, masterServiceId, page, lastMessageId, bookingId
                )
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

    fun sendMessage(
        userId: String, toUserId: String, serviceId: String,
        masterServiceId: String, messageText: String?, attachmentPath: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.sendMessage(
                    userId, toUserId, serviceId, masterServiceId, messageText, attachmentPath
                )
                if (response.isSuccessful && response.body() != null) {
                    _sendResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore invio messaggio (${response.code()})")
                    _sendResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _sendResult.postValue(null)
            }
        }
    }
}
