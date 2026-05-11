package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeedbackViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _result = MutableLiveData<CommonPojo?>()
    val result: LiveData<CommonPojo?> = _result

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun sendFeedback(
        userId: String, firstName: String, lastName: String,
        email: String, message: String, imagePath: String?
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.sendFeedback(
                    userId, firstName, lastName, email, message, imagePath
                )
                if (response.isSuccessful && response.body() != null) {
                    _result.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _result.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _result.postValue(null)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
