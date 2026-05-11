package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.ProviderHistoryPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PartnerServiceRequestViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _requests = MutableLiveData<ProviderHistoryPojo?>()
    val requests: LiveData<ProviderHistoryPojo?> = _requests

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadRequests(userId: String, tab: String, keyword: String, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getPartnerServiceRequests(userId, tab, keyword, page)
                if (response.isSuccessful && response.body() != null) {
                    _requests.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _requests.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _requests.postValue(null)
            }
        }
    }
}
