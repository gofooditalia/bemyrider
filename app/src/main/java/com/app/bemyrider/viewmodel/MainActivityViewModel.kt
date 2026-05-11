package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.user.CategoryListPOJO
import com.app.bemyrider.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _categories = MutableLiveData<CategoryListPOJO?>()
    val categories: LiveData<CategoryListPOJO?> = _categories

    private val _logoutResult = MutableLiveData<CommonPojo?>()
    val logoutResult: LiveData<CommonPojo?> = _logoutResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadCategories(providerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.getCategoryList(providerId)
                if (r.isSuccessful && r.body() != null) { _categories.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore categorie (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }

    fun logout(userId: String, deviceToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = repository.logout(userId)
                if (r.isSuccessful && r.body() != null) { _logoutResult.postValue(r.body()); _error.postValue(null) }
                else _error.postValue("Errore logout (${r.code()})")
            } catch (e: Exception) { _error.postValue(e.localizedMessage ?: "Errore di rete") }
        }
    }
}
