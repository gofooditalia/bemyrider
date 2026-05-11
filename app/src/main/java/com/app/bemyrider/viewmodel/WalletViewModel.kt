package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.DepositHistoryPojo
import com.app.bemyrider.model.RedeemHistoryPojo
import com.app.bemyrider.model.WalletDetailsPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _walletDetails = MutableLiveData<WalletDetailsPojo?>()
    val walletDetails: LiveData<WalletDetailsPojo?> = _walletDetails

    private val _depositHistory = MutableLiveData<DepositHistoryPojo?>()
    val depositHistory: LiveData<DepositHistoryPojo?> = _depositHistory

    private val _redeemHistory = MutableLiveData<RedeemHistoryPojo?>()
    val redeemHistory: LiveData<RedeemHistoryPojo?> = _redeemHistory

    private val _redeemResult = MutableLiveData<CommonPojo?>()
    val redeemResult: LiveData<CommonPojo?> = _redeemResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadWalletDetails(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getWalletDetails(userId)
                if (r.isSuccessful && r.body() != null) _walletDetails.postValue(r.body())
                else _error.postValue("Errore wallet (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }

    fun loadDepositHistory(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getDepositHistory(userId)
                if (r.isSuccessful && r.body() != null) _depositHistory.postValue(r.body())
                else _error.postValue("Errore storico depositi (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }

    fun loadRedeemHistory(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getRedeemHistory(userId)
                if (r.isSuccessful && r.body() != null) _redeemHistory.postValue(r.body())
                else _error.postValue("Errore storico prelievi (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }

    fun sendRedeemRequest(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.sendRedeemRequest(userId)
                if (r.isSuccessful && r.body() != null) _redeemResult.postValue(r.body())
                else _error.postValue("Errore richiesta prelievo (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }
}
