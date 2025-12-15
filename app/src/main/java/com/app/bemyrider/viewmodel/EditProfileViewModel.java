package com.app.bemyrider.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.model.partner.CountryCodePojo;
import com.app.bemyrider.repository.UserRepository;

import java.util.Map;

public class EditProfileViewModel extends ViewModel {

    private UserRepository userRepository;

    public EditProfileViewModel() {
        userRepository = new UserRepository();
    }

    public LiveData<CountryCodePojo> getCountryCodes() {
        return userRepository.getCountryCodes();
    }

    public LiveData<ProfilePojo> updateProfile(Map<String, String> textParams, String imagePath) {
        return userRepository.editProfile(
                textParams.get("user_id"),
                textParams.get("firstName"),
                textParams.get("lastName"),
                imagePath,
                textParams.get("contact_number"),
                textParams.get("country_code"),
                textParams.get("city_of_company"),
                textParams.get("address"),
                textParams.get("payment_mode"),
                textParams.get("company_name"),
                textParams.get("vat"),
                textParams.get("certified_email"),
                textParams.get("receipt_code"),
                textParams.get("latitude"),
                textParams.get("longitude")
        );
    }
}
