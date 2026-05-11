package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookedDetailViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _proposalResult = MutableLiveData<CommonPojo?>()
    val proposalResult: LiveData<CommonPojo?> = _proposalResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun acceptProposal(statusType: String, proposalId: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.acceptProposal(statusType, proposalId, userId)
                if (response.isSuccessful && response.body() != null) {
                    _proposalResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _proposalResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _proposalResult.postValue(null)
            }
        }
    }

    fun sendProposal(selectedHours: String, message: String, proposalId: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.sendProposal(selectedHours, message, proposalId, userId)
                if (response.isSuccessful && response.body() != null) {
                    _proposalResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _proposalResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _proposalResult.postValue(null)
            }
        }
    }
}
