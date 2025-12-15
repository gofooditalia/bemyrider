package com.app.bemyrider.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.repository.UserRepository;

public class UserProfileViewModel extends ViewModel {

    private UserRepository userRepository;

    public UserProfileViewModel() {
        userRepository = new UserRepository();
    }

    public LiveData<ProfilePojo> getProfile(String profileId) {
        return userRepository.getProfile(profileId);
    }
}
