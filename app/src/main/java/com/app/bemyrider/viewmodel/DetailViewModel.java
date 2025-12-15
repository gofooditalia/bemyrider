package com.app.bemyrider.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.repository.UserRepository;

public class DetailViewModel extends ViewModel {

    private UserRepository userRepository;

    public DetailViewModel() {
        userRepository = new UserRepository();
    }

    public LiveData<CommonPojo> bookService(String providerServiceId, String loginServiceId, String serviceStartTime, String providerServiceHours, String selHours, String userId, String serviceAddress, String serviceDetails, String bookingLat, String bookingLong, String deliveryType, String requestType) {
        return userRepository.bookService(providerServiceId, loginServiceId, serviceStartTime, providerServiceHours, selHours, userId, serviceAddress, serviceDetails, bookingLat, bookingLong, deliveryType, requestType);
    }
}
