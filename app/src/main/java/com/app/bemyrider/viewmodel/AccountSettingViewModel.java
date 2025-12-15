package com.app.bemyrider.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.LanguagePojo;
import com.app.bemyrider.repository.UserRepository;

public class AccountSettingViewModel extends ViewModel {

    private UserRepository userRepository;

    public AccountSettingViewModel() {
        userRepository = new UserRepository();
    }

    public LiveData<LanguagePojo> getLanguages() {
        return userRepository.getLanguages();
    }

    public LiveData<CommonPojo> changePassword(String currentPwd, String newPwd, String reNewPwd, String userId) {
        return userRepository.changePassword(currentPwd, newPwd, reNewPwd, userId);
    }

    public LiveData<CommonPojo> deactivateAccount(String userId, String userType) {
        return userRepository.deactivateAccount(userId, userType);
    }
}
