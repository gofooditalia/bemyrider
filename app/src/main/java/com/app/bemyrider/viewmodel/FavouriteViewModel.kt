package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.FavoriteServiceListPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavouriteViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _favourites = MutableLiveData<FavoriteServiceListPojo?>()
    val favourites: LiveData<FavoriteServiceListPojo?> = _favourites

    private val _toggleResult = MutableLiveData<CommonPojo?>()
    val toggleResult: LiveData<CommonPojo?> = _toggleResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadFavourites(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getFavoriteList(params)
                if (response.isSuccessful && response.body() != null) {
                    _favourites.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _favourites.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _favourites.postValue(null)
            }
        }
    }

    fun toggleFavourite(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.toggleFavorite(params)
                if (response.isSuccessful && response.body() != null) {
                    _toggleResult.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore aggiornamento preferito")
                    _toggleResult.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _toggleResult.postValue(null)
            }
        }
    }
}
