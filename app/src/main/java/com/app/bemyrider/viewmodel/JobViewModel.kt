package com.app.bemyrider.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bemyrider.model.JobResponsePojo
import com.app.bemyrider.repository.JobRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Job Board (Bacheca) feature.
 * Created by Gemini on 2024.
 */
class JobViewModel : ViewModel() {

    private val repository = JobRepository()

    val jobListResponse = MutableLiveData<JobResponsePojo?>()
    val actionResponse = MutableLiveData<JobResponsePojo?>()
    val applicantResponse = MutableLiveData<JobResponsePojo?>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    fun getAvailableJobs(userId: String, lat: Double? = null, lng: Double? = null, vehicleType: String? = null) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getAvailableJobs(userId, lat, lng, vehicleType)
                if (response.isSuccessful) {
                    jobListResponse.postValue(response.body())
                } else {
                    errorMessage.postValue("Errore nel caricamento annunci: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.postValue("Errore di rete: ${e.localizedMessage}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun applyToJob(jobId: String, userId: String) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.applyToJob(jobId, userId)
                if (response.isSuccessful) {
                    actionResponse.postValue(response.body())
                } else {
                    errorMessage.postValue("Errore nella candidatura: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.postValue("Errore di rete: ${e.localizedMessage}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun createJob(
        title: String, description: String, vehicleRequired: String,
        startAt: String, endAt: String, compensation: String,
        compensationType: String, address: String, lat: Double,
        lng: Double, userId: String
    ) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.createJob(
                    title, description, vehicleRequired, startAt, endAt,
                    compensation, compensationType, address, lat, lng, userId
                )
                if (response.isSuccessful) {
                    actionResponse.postValue(response.body())
                } else {
                    errorMessage.postValue("Errore nella creazione: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.postValue("Errore di rete: ${e.localizedMessage}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun getJobApplicants(jobId: String) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getJobApplicants(jobId)
                if (response.isSuccessful) {
                    applicantResponse.postValue(response.body())
                } else {
                    errorMessage.postValue("Errore nel caricamento candidati: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.postValue("Errore di rete: ${e.localizedMessage}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun hireRider(jobId: String, riderId: String) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.hireRider(jobId, riderId)
                if (response.isSuccessful) {
                    actionResponse.postValue(response.body())
                } else {
                    errorMessage.postValue("Errore nell'ingaggio: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.postValue("Errore di rete: ${e.localizedMessage}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }
}
