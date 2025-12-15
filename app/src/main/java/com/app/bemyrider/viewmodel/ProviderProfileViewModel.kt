package com.app.bemyrider.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.app.bemyrider.model.CommonPojo
import com.app.bemyrider.model.NewLoginPojo
import com.app.bemyrider.model.ProfilePojo
import com.app.bemyrider.repository.UserRepository

class ProviderProfileViewModel : ViewModel() {

    private val userRepository: UserRepository = UserRepository()

    fun getProfile(profileId: String): LiveData<ProfilePojo> {
        return userRepository.getProfile(profileId)
    }

    fun updateAvailabilityStatus(userId: String, isAvailable: String): LiveData<CommonPojo> {
        return userRepository.updateAvailabilityStatus(userId, isAvailable)
    }

    fun socialSignIn(email: String?, firstName: String?, lastName: String?, profileImageUrl: String?, loginType: String, socialId: String, userId: String): LiveData<NewLoginPojo> {
        return userRepository.socialLogin(firstName, lastName, email, loginType, socialId, null, null, profileImageUrl, userId)
    }
}
