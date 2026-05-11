package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.MinMaxPricePojo
import com.app.bemyrider.model.ServiceListPOJO
import com.app.bemyrider.model.partner.SubCategoryListPojo
import com.app.bemyrider.model.user.FilterDataPOJO
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilterProviderViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _minMaxPrice = MutableLiveData<MinMaxPricePojo?>()
    val minMaxPrice: LiveData<MinMaxPricePojo?> = _minMaxPrice

    private val _filterList = MutableLiveData<FilterDataPOJO?>()
    val filterList: LiveData<FilterDataPOJO?> = _filterList

    private val _subCategoryList = MutableLiveData<SubCategoryListPojo?>()
    val subCategoryList: LiveData<SubCategoryListPojo?> = _subCategoryList

    private val _serviceList = MutableLiveData<ServiceListPOJO?>()
    val serviceList: LiveData<ServiceListPOJO?> = _serviceList

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMinMaxPrice() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getMinMaxPrice()
                if (response.isSuccessful && response.body() != null) {
                    _minMaxPrice.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _minMaxPrice.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _minMaxPrice.postValue(null)
            }
        }
    }

    fun loadFilterList(params: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getFilterList(params)
                if (response.isSuccessful && response.body() != null) {
                    _filterList.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _filterList.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _filterList.postValue(null)
            }
        }
    }

    fun loadSubCategories(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getSubCategoryList(categoryId, "")
                if (response.isSuccessful && response.body() != null) {
                    _subCategoryList.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _subCategoryList.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _subCategoryList.postValue(null)
            }
        }
    }

    fun loadServices(subCategoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getServiceList("", subCategoryId)
                if (response.isSuccessful && response.body() != null) {
                    _serviceList.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore del server (${response.code()})")
                    _serviceList.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _serviceList.postValue(null)
            }
        }
    }
}
