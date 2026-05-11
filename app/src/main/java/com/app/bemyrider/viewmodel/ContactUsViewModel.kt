package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.partner.CountryCodePojo
import com.app.bemyrider.model.partner.CountryCodePojoItem
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactUsViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _countryCodes = MutableLiveData<List<CountryCodePojoItem>?>()
    val countryCodes: LiveData<List<CountryCodePojoItem>?> = _countryCodes

    private val _sendResult = MutableLiveData<CommonPojo?>()
    val sendResult: LiveData<CommonPojo?> = _sendResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoadingCodes = MutableLiveData<Boolean>(false)
    @get:JvmName("getIsLoadingCodes")
    val isLoadingCodes: LiveData<Boolean> = _isLoadingCodes

    private val _isSending = MutableLiveData<Boolean>(false)
    @get:JvmName("getIsSending")
    val isSending: LiveData<Boolean> = _isSending

    fun loadCountryCodes() {
        _isLoadingCodes.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getCountryCodes()
                if (response.isSuccessful && response.body() != null) {
                    _countryCodes.postValue(response.body()!!.data)
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore caricamento prefissi (${response.code()})")
                    _countryCodes.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _countryCodes.postValue(null)
            } finally {
                _isLoadingCodes.postValue(false)
            }
        }
    }

    fun sendContactUs(
        userId: String, firstName: String, lastName: String,
        email: String, contactNumber: String, countryCode: String, message: String
    ) {
        _isSending.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.sendContactUs(
                    userId, firstName, lastName, email, contactNumber, countryCode, message
                )
                if (response.isSuccessful && response.body() != null) {
                    _sendResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _sendResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _sendResult.postValue(null)
            } finally {
                _isSending.postValue(false)
            }
        }
    }
}
