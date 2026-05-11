package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.user.PopularTaskerPOJO
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskersListViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _taskers = MutableLiveData<PopularTaskerPOJO?>()
    val taskers: LiveData<PopularTaskerPOJO?> = _taskers

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadPopularTaskers(subcategoryId: String, latitude: String, longitude: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getPopularTaskers(subcategoryId, latitude, longitude, userId)
                if (r.isSuccessful && r.body() != null) { _taskers.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore del server (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }
}
