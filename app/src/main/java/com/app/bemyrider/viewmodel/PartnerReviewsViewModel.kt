package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.ServiceReviewPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PartnerReviewsViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _reviews = MutableLiveData<ServiceReviewPojo?>()
    val reviews: LiveData<ServiceReviewPojo?> = _reviews

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadReviews(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getProviderReviews(params)
                if (response.isSuccessful && response.body() != null) {
                    _reviews.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _reviews.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _reviews.postValue(null)
            }
        }
    }
}
