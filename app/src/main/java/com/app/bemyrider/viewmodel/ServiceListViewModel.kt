package com.app.bemyrider.viewmodel

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.ServiceListPOJO
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Carica categoria → subcategoria → servizi popolari in sequenza. */
class ServiceListViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _popularServices = MutableLiveData<ServiceListPOJO?>()
    val popularServices: LiveData<ServiceListPOJO?> = _popularServices

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun loadPopularServices(providerId: String) {
        _loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val catResponse = repository.getCategoryList(providerId)
                if (!catResponse.isSuccessful || catResponse.body() == null ||
                    catResponse.body()!!.data.isNullOrEmpty()) {
                    _error.postValue(catResponse.body()?.message ?: "Errore categorie")
                    _loading.postValue(false)
                    return@launch
                }
                val categoryId = catResponse.body()!!.data[0].categoryId

                val subCatResponse = repository.getSubCategoryList(categoryId, providerId)
                if (!subCatResponse.isSuccessful || subCatResponse.body() == null ||
                    subCatResponse.body()!!.data.isNullOrEmpty()) {
                    _error.postValue(subCatResponse.body()?.message ?: "Errore subcategorie")
                    _loading.postValue(false)
                    return@launch
                }
                val subCategoryId = TextUtils.join(",", subCatResponse.body()!!.data
                    .mapNotNull { it.categoryId?.toString() })

                val popularResponse = repository.getPopularServices(subCategoryId, providerId)
                if (popularResponse.isSuccessful && popularResponse.body() != null) {
                    _popularServices.postValue(popularResponse.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore servizi popolari")
                    _popularServices.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _popularServices.postValue(null)
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
