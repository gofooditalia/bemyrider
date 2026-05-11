package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.partner.SubCategoryListPojo
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Carica categoria → subcategoria in sequenza. */
class SubCategoryViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _subcategories = MutableLiveData<SubCategoryListPojo?>()
    val subcategories: LiveData<SubCategoryListPojo?> = _subcategories

    private val _categoryName = MutableLiveData<String?>()
    val categoryName: LiveData<String?> = _categoryName

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadSubcategories(providerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val catResponse = repository.getCategoryList(providerId)
                if (!catResponse.isSuccessful || catResponse.body() == null ||
                    catResponse.body()!!.data.isNullOrEmpty()) {
                    _error.postValue("Errore caricamento categorie")
                    return@launch
                }
                val firstCategory = catResponse.body()!!.data[0]
                _categoryName.postValue(firstCategory.categoryName)

                val subCatResponse = repository.getSubCategoryList(firstCategory.categoryId, providerId)
                if (subCatResponse.isSuccessful && subCatResponse.body() != null) {
                    _subcategories.postValue(subCatResponse.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Errore caricamento subcategorie")
                    _subcategories.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "Errore di rete")
                _subcategories.postValue(null)
            }
        }
    }
}
