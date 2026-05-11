package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.WalletDetailsPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DepositFundViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _walletDetails = MutableLiveData<WalletDetailsPojo?>()
    val walletDetails: LiveData<WalletDetailsPojo?> = _walletDetails

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadWalletDetails(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getWalletDetails(userId)
                if (r.isSuccessful && r.body() != null) { _walletDetails.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore del server (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }
}
