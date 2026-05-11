package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.FinancialInfoPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PartnerFinancialInfoViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _info = MutableLiveData<FinancialInfoPojo?>()
    val info: LiveData<FinancialInfoPojo?> = _info

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadFinancialInfo(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getFinancialInfo(userId)
                if (r.isSuccessful && r.body() != null) { _info.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore del server (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }
}
