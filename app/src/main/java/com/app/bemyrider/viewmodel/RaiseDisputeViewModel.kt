package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RaiseDisputeViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _result = MutableLiveData<CommonPojo?>()
    val result: LiveData<CommonPojo?> = _result

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun raiseDispute(serviceRequestId: String, userId: String, title: String, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.raiseDispute(serviceRequestId, userId, title, message)
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
            }
        }
    }
}
