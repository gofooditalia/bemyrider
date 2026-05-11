package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.VersionDataPOJO
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _siteSettings = MutableLiveData<VersionDataPOJO?>()
    val siteSettings: LiveData<VersionDataPOJO?> = _siteSettings

    private val _offlineData = MutableLiveData<String?>()
    val offlineData: LiveData<String?> = _offlineData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadSiteSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getSiteSettings()
                if (r.isSuccessful && r.body() != null) { _siteSettings.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore impostazioni (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }

    fun loadOfflineData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getOfflineData(userId)
                if (r.isSuccessful) { _offlineData.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore dati offline (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }
}
