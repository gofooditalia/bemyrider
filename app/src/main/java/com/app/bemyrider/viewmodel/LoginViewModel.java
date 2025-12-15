package com.app.bemyrider.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private MutableLiveData<NewLoginPojo> loginResponse;
    private MutableLiveData<String> offlineDataResponse;
    private MutableLiveData<NewLoginPojo> forgotPasswordResponse;
    private MutableLiveData<NewLoginPojo> resendMailResponse;


    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository();
        loginResponse = new MutableLiveData<>();
        offlineDataResponse = new MutableLiveData<>();
        forgotPasswordResponse = new MutableLiveData<>();
        resendMailResponse = new MutableLiveData<>();
    }

    public LiveData<NewLoginPojo> login(String email, String password, String deviceToken) {
        userRepository.login(email, password, deviceToken).observeForever(newLoginPojo -> {
            loginResponse.setValue(newLoginPojo);
        });
        return loginResponse;
    }
    
    public LiveData<NewLoginPojo> forgotPassword(String email) {
        userRepository.forgotPassword(email).observeForever(response -> {
            forgotPasswordResponse.setValue(response);
        });
        return forgotPasswordResponse;
    }

    public LiveData<NewLoginPojo> resendActivationMail(String email) {
        userRepository.resendActivationMail(email).observeForever(response -> {
            resendMailResponse.setValue(response);
        });
        return resendMailResponse;
    }
    
    public LiveData<String> getOfflineData(String userId) {
        userRepository.getOfflineData(userId).observeForever(s -> {
            offlineDataResponse.setValue(s);
        });
        return offlineDataResponse;
    }
}
