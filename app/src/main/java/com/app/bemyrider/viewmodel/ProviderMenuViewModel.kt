package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.app.bemyrider.model.CheckStripeConnectedPojo
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.ProfilePojo
import com.app.bemyrider.repository.UserRepository

class ProviderMenuViewModel : ViewModel() {

    private val userRepository: UserRepository = UserRepository()

    fun getProfile(profileId: String): LiveData<ProfilePojo> {
        return userRepository.getProfile(profileId)
    }

    fun logout(userId: String): LiveData<CommonPojo> {
        return userRepository.logout(userId)
    }

    fun checkStripeStatus(userId: String): LiveData<CheckStripeConnectedPojo> {
        return userRepository.checkStripeStatus(userId)
    }
}
