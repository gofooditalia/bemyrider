package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.DisputeListPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DisputeListViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _disputes = MutableLiveData<DisputeListPojo?>()
    val disputes: LiveData<DisputeListPojo?> = _disputes

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDisputes(userId: String, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getDisputeList(userId, page)
                if (response.isSuccessful && response.body() != null) {
                    _disputes.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _disputes.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _disputes.postValue(null)
            }
        }
    }
}
