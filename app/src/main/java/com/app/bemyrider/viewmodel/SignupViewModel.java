package com.app.bemyrider.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.RegistrationPojo;
import com.app.bemyrider.repository.UserRepository;

public class SignupViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private MutableLiveData<RegistrationPojo> signupResponse;
    private MutableLiveData<NewLoginPojo> socialLoginResponse;


    public SignupViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository();
        signupResponse = new MutableLiveData<>();
        socialLoginResponse = new MutableLiveData<>();
    }

    public LiveData<RegistrationPojo> signup(String firstName, String lastName, String email, String userType, String contactNumber, String password, String rePassword, String countryCodeId, String deviceToken) {
        userRepository.signup(firstName, lastName, email, userType, contactNumber, password, rePassword, countryCodeId, deviceToken).observeForever(registrationPojo -> {
            signupResponse.setValue(registrationPojo);
        });
        return signupResponse;
    }

    public LiveData<NewLoginPojo> socialLogin(String firstName, String lastName, String email, String loginType, String fbId, String googleId, String linkedInId, String pictureUrl, String deviceToken) {
        userRepository.socialLogin(firstName, lastName, email, loginType, fbId, googleId, linkedInId, pictureUrl, deviceToken).observeForever(newLoginPojo -> {
            socialLoginResponse.setValue(newLoginPojo);
        });
        return socialLoginResponse;
    }
}
