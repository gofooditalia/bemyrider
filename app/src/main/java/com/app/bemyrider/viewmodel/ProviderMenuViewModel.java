// File converted to Kotlin: ProviderMenuViewModel.kt
// package com.app.bemyrider.viewmodel;
//
// import androidx.lifecycle.LiveData;
// import androidx.lifecycle.ViewModel;
//
// import com.app.bemyrider.model.CheckStripeConnectedPojo;
// import com.app.bemyrider.model.CommonPojo;
// import com.app.bemyrider.model.ProfilePojo;
// import com.app.bemyrider.repository.UserRepository;
//
// public class ProviderMenuViewModel extends ViewModel {
//
//    private final UserRepository userRepository;
//
//    public ProviderMenuViewModel() {
//        userRepository = new UserRepository();
//    }
//
//    public LiveData<ProfilePojo> getProfile(String profileId) {
//        return userRepository.getProfile(profileId);
//    }
//
//    public LiveData<CommonPojo> logout(String userId) {
//        return userRepository.logout(userId);
//    }
//
//    public LiveData<CheckStripeConnectedPojo> checkStripeStatus(String userId) {
//        return userRepository.checkStripeStatus(userId);
//    }
// }
