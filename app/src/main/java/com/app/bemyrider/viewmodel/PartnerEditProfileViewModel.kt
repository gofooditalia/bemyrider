package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.ProfilePojo
import com.app.bemyrider.model.partner.CountryCodePojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PartnerEditProfileViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _countryCodes = MutableLiveData<CountryCodePojo?>()
    val countryCodes: LiveData<CountryCodePojo?> = _countryCodes

    private val _updateResult = MutableLiveData<ProfilePojo?>()
    val updateResult: LiveData<ProfilePojo?> = _updateResult

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadCountryCodes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getCountryCodes()
                if (r.isSuccessful && r.body() != null) { _countryCodes.postValue(r.body()); _error.postValue(null) }
                else { _error.postValue("Errore ${r.code()}"); _countryCodes.postValue(null) }
            } catch (e: Exception) { _error.postValue(e.localizedMessage); _countryCodes.postValue(null) }
        }
    }

    fun updateProfile(
        params: Map<String, String>,
        profileImagePath: String?,
        signatureImagePath: String?
    ) {
        _loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.editPartnerProfile(params, profileImagePath, signatureImagePath)
                _updateResult.postValue(result)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _updateResult.postValue(null)
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
