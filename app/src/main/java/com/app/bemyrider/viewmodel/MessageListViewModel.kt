package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.MessageListPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageListViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _messages = MutableLiveData<MessageListPojo?>()
    val messages: LiveData<MessageListPojo?> = _messages

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMessages(userId: String, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getMessageList(userId, page)
                if (response.isSuccessful && response.body() != null) {
                    _messages.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _messages.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _messages.postValue(null)
            }
        }
    }
}
