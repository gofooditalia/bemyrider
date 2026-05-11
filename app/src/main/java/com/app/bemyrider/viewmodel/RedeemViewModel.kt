package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RedeemViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _result = MutableLiveData<CommonPojo?>()
    val result: LiveData<CommonPojo?> = _result

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun sendRedeemRequest(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.sendRedeemRequest(userId)
                if (r.isSuccessful && r.body() != null) { _result.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore del server (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }
}
