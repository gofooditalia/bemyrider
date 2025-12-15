package com.app.bemyrider.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.bemyrider.model.CheckStripeConnectedPojo;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.LanguagePojo;
import com.app.bemyrider.model.NewLoginPojo;
import com.app.bemyrider.model.ProfilePojo;
import com.app.bemyrider.model.RegistrationPojo;
import com.app.bemyrider.model.partner.CountryCodePojo;
import com.app.bemyrider.network.ApiService;
import com.app.bemyrider.network.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private ApiService apiService;
    private Gson gson;

    public UserRepository() {
        apiService = RetrofitClient.getClient().create(ApiService.class);
        gson = new GsonBuilder().setDateFormat("M/d/yy hh:mm a").create();
    }
    
    public LiveData<CommonPojo> updateAvailabilityStatus(String userId, String isAvailable) {
        MutableLiveData<CommonPojo> data = new MutableLiveData<>();
        apiService.updateAvailabilityStatus(userId, isAvailable).enqueue(new Callback<CommonPojo>() {
            @Override
            public void onResponse(Call<CommonPojo> call, Response<CommonPojo> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    CommonPojo error = new CommonPojo();
                    error.setStatus(false);
                    error.setMessage("Errore del server");
                    data.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<CommonPojo> call, Throwable t) {
                CommonPojo error = new CommonPojo();
                error.setStatus(false);
                error.setMessage(getReadableErrorMessage(t));
                data.setValue(error);
            }
        });
        return data;
    }

    public LiveData<CommonPojo> logout(String userId) {
        MutableLiveData<CommonPojo> data = new MutableLiveData<>();
        apiService.logout(userId).enqueue(new Callback<CommonPojo>() {
            @Override
            public void onResponse(Call<CommonPojo> call, Response<CommonPojo> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    CommonPojo errorPojo = new CommonPojo();
                    errorPojo.setStatus(false);
                    errorPojo.setMessage("Errore del server: " + response.code());
                    data.setValue(errorPojo);
                }
            }
            @Override
            public void onFailure(Call<CommonPojo> call, Throwable t) {
                CommonPojo errorPojo = new CommonPojo();
                errorPojo.setStatus(false);
                errorPojo.setMessage(getReadableErrorMessage(t));
                data.setValue(errorPojo);
            }
        });
        return data;
    }

    public LiveData<CheckStripeConnectedPojo> checkStripeStatus(String userId) {
        MutableLiveData<CheckStripeConnectedPojo> data = new MutableLiveData<>();
        apiService.checkStripeStatus(userId).enqueue(new Callback<CheckStripeConnectedPojo>() {
            @Override
            public void onResponse(Call<CheckStripeConnectedPojo> call, Response<CheckStripeConnectedPojo> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<CheckStripeConnectedPojo> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    private RequestBody createPartFromString(String descriptionString) {
        if (descriptionString == null) {
            return RequestBody.create(MultipartBody.FORM, "");
        }
        return RequestBody.create(MultipartBody.FORM, descriptionString);
    }

    public LiveData<ProfilePojo> editProfile(String userId, String firstName, String lastName, String imagePath, String contactNumber, String countryCode, String cityOfCompany, String address, String paymentMode, String companyName, String vat, String certifiedEmail, String receiptCode, String latitude, String longitude) {
        MutableLiveData<ProfilePojo> data = new MutableLiveData<>();

        MultipartBody.Part imagePart = null;
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                imagePart = MultipartBody.Part.createFormData("profile_pic", file.getName(), requestFile);
            }
        }

        apiService.editProfile(
                createPartFromString(userId),
                createPartFromString(firstName),
                createPartFromString(lastName),
                imagePart,
                createPartFromString(contactNumber),
                createPartFromString(countryCode),
                createPartFromString(cityOfCompany),
                createPartFromString(address),
                createPartFromString(paymentMode),
                createPartFromString(companyName),
                createPartFromString(vat),
                createPartFromString(certifiedEmail),
                createPartFromString(receiptCode),
                createPartFromString(latitude),
                createPartFromString(longitude)
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProfilePojo result = new ProfilePojo();
                 try {
                    if (response.isSuccessful() && response.body() != null) {
                        String rawJson = response.body().string();
                        try {
                            result = gson.fromJson(rawJson, ProfilePojo.class);
                            if (!result.isStatus()) {
                                result.setMessage(extractErrorMessage(rawJson));
                            }
                        } catch (JsonSyntaxException e) {
                            result.setStatus(false);
                            result.setMessage(extractErrorMessage(rawJson));
                        }
                    } else {
                        result.setStatus(false);
                        result.setMessage("Errore del server: " + response.code());
                    }
                } catch (IOException e) {
                    result.setStatus(false);
                    result.setMessage("Errore di lettura risposta.");
                }
                data.setValue(result);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ProfilePojo errorPojo = new ProfilePojo();
                errorPojo.setStatus(false);
                errorPojo.setMessage(getReadableErrorMessage(t));
                data.setValue(errorPojo);
            }
        });
        return data;
    }

    public LiveData<CountryCodePojo> getCountryCodes() {
        MutableLiveData<CountryCodePojo> data = new MutableLiveData<>();
        apiService.getCountryCodes().enqueue(new Callback<CountryCodePojo>() {
            @Override
            public void onResponse(Call<CountryCodePojo> call, Response<CountryCodePojo> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(null);
                }
            }
            @Override
            public void onFailure(Call<CountryCodePojo> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    public LiveData<LanguagePojo> getLanguages() {
        MutableLiveData<LanguagePojo> data = new MutableLiveData<>();
        apiService.getLanguages().enqueue(new Callback<LanguagePojo>() {
            @Override
            public void onResponse(Call<LanguagePojo> call, Response<LanguagePojo> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<LanguagePojo> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    public LiveData<CommonPojo> changePassword(String currentPwd, String newPwd, String reNewPwd, String userId) {
        MutableLiveData<CommonPojo> data = new MutableLiveData<>();
        apiService.changePassword(currentPwd, newPwd, reNewPwd, userId).enqueue(new Callback<CommonPojo>() {
            @Override
            public void onResponse(Call<CommonPojo> call, Response<CommonPojo> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    CommonPojo errorPojo = new CommonPojo();
                    errorPojo.setStatus(false);
                    errorPojo.setMessage("Errore del server: " + response.code());
                    data.setValue(errorPojo);
                }
            }

            @Override
            public void onFailure(Call<CommonPojo> call, Throwable t) {
                CommonPojo errorPojo = new CommonPojo();
                errorPojo.setStatus(false);
                errorPojo.setMessage(getReadableErrorMessage(t));
                data.setValue(errorPojo);
            }
        });
        return data;
    }

    public LiveData<CommonPojo> deactivateAccount(String userId, String userType) {
        MutableLiveData<CommonPojo> data = new MutableLiveData<>();
        apiService.deactivateAccount(userId, userType).enqueue(new Callback<CommonPojo>() {
            @Override
            public void onResponse(Call<CommonPojo> call, Response<CommonPojo> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    CommonPojo errorPojo = new CommonPojo();
                    errorPojo.setStatus(false);
                    errorPojo.setMessage("Errore del server: " + response.code());
                    data.setValue(errorPojo);
                }
            }

            @Override
            public void onFailure(Call<CommonPojo> call, Throwable t) {
                CommonPojo errorPojo = new CommonPojo();
                errorPojo.setStatus(false);
                errorPojo.setMessage(getReadableErrorMessage(t));
                data.setValue(errorPojo);
            }
        });
        return data;
    }

    public LiveData<NewLoginPojo> login(String email, String password, String deviceToken) {
        MutableLiveData<NewLoginPojo> data = new MutableLiveData<>();
        apiService.login(email, password, deviceToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                NewLoginPojo result = new NewLoginPojo();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String rawJson = response.body().string();
                        try {
                            NewLoginPojo tempResult = gson.fromJson(rawJson, NewLoginPojo.class);
                            if (tempResult.isStatus()) {
                                result = tempResult;
                            } else {
                                result.setStatus(false);
                                result.setMessage(extractErrorMessage(rawJson));
                            }
                        } catch (JsonSyntaxException e) {
                            result.setStatus(false);
                            result.setMessage(extractErrorMessage(rawJson));
                        }
                    } else {
                        result.setStatus(false);
                        result.setMessage("Errore del server: " + response.code());
                    }
                } catch (IOException e) {
                    result.setStatus(false);
                    result.setMessage("Errore di lettura risposta.");
                }
                data.setValue(result);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                NewLoginPojo errorPojo = new NewLoginPojo();
                errorPojo.setStatus(false);
                errorPojo.setMessage(getReadableErrorMessage(t));
                data.setValue(errorPojo);
            }
        });
        return data;
    }

    public LiveData<RegistrationPojo> signup(String firstName, String lastName, String email, String userType, String contactNumber, String password, String rePassword, String countryCodeId, String deviceToken) {
        MutableLiveData<RegistrationPojo> data = new MutableLiveData<>();
        apiService.signup(firstName, lastName, email, userType, contactNumber, password, rePassword, countryCodeId, deviceToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                RegistrationPojo result = new RegistrationPojo();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String rawJson = response.body().string();
                        try {
                            RegistrationPojo tempResult = gson.fromJson(rawJson, RegistrationPojo.class);
                            if (tempResult.isStatus()) {
                                result = tempResult;
                            } else {
                                result.setStatus(false);
                                result.setMessage(extractErrorMessage(rawJson));
                            }
                        } catch (JsonSyntaxException e) {
                            result.setStatus(false);
                            result.setMessage(extractErrorMessage(rawJson));
                        }
                    } else {
                        result.setStatus(false);
                        result.setMessage("Errore del server: " + response.code());
                    }
                } catch (IOException e) {
                    result.setStatus(false);
                    result.setMessage("Errore di lettura risposta.");
                }
                data.setValue(result);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                RegistrationPojo errorPojo = new RegistrationPojo();
                errorPojo.setStatus(false);
                errorPojo.setMessage(getReadableErrorMessage(t));
                data.setValue(errorPojo);
            }
        });
        return data;
    }


    public LiveData<NewLoginPojo> socialLogin(String firstName, String lastName, String email, String loginType, String fbId, String googleId, String linkedInId, String pictureUrl, String deviceToken) {
        MutableLiveData<NewLoginPojo> data = new MutableLiveData<>();
        apiService.socialLogin(firstName, lastName, email, loginType, fbId, googleId, linkedInId, pictureUrl, deviceToken).enqueue(new Callback<NewLoginPojo>() {
            @Override
            public void onResponse(Call<NewLoginPojo> call, Response<NewLoginPojo> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    NewLoginPojo errorPojo = new NewLoginPojo();
                    errorPojo.setStatus(false);
                    errorPojo.setMessage("Errore del server: " + response.code());
                    data.setValue(errorPojo);
                }
            }

            @Override
            public void onFailure(Call<NewLoginPojo> call, Throwable t) {
                NewLoginPojo errorPojo = new NewLoginPojo();
                errorPojo.setStatus(false);
                errorPojo.setMessage(getReadableErrorMessage(t));
                data.setValue(errorPojo);
            }
        });
        return data;
    }

    public LiveData<String> getOfflineData(String userId) {
        MutableLiveData<String> data = new MutableLiveData<>();
        apiService.getOfflineData(userId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    public LiveData<NewLoginPojo> forgotPassword(String email) {
        MutableLiveData<NewLoginPojo> data = new MutableLiveData<>();
        apiService.forgotPassword(email).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleGenericResponse(response, data);
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                NewLoginPojo errorPojo = new NewLoginPojo();
                errorPojo.setStatus(false);
                errorPojo.setMessage(getReadableErrorMessage(t));
                data.setValue(errorPojo);
            }
        });
        return data;
    }

    public LiveData<NewLoginPojo> resendActivationMail(String email) {
        MutableLiveData<NewLoginPojo> data = new MutableLiveData<>();
        apiService.resendActivationMail(email).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleGenericResponse(response, data);
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                NewLoginPojo errorPojo = new NewLoginPojo();
                errorPojo.setStatus(false);
                errorPojo.setMessage(getReadableErrorMessage(t));
                data.setValue(errorPojo);
            }
        });
        return data;
    }
    
    public LiveData<CommonPojo> bookService(String providerServiceId, String loginServiceId, String serviceStartTime, String providerServiceHours, String selHours, String userId, String serviceAddress, String serviceDetails, String bookingLat, String bookingLong, String deliveryType, String requestType) {
        MutableLiveData<CommonPojo> data = new MutableLiveData<>();
        apiService.bookService(providerServiceId, loginServiceId, serviceStartTime, providerServiceHours, selHours, userId, serviceAddress, serviceDetails, bookingLat, bookingLong, deliveryType, requestType)
            .enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                CommonPojo result = new CommonPojo();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String rawJson = response.body().string();
                        try {
                            result = gson.fromJson(rawJson, CommonPojo.class);
                            if (!result.isStatus()) {
                                result.setMessage(extractErrorMessage(rawJson));
                            } else {
                                result.setMessage(translateMessage(result.getMessage()));
                            }
                        } catch (JsonSyntaxException e) {
                            result.setStatus(false);
                            result.setMessage(extractErrorMessage(rawJson));
                        }
                    } else {
                        result.setStatus(false);
                        result.setMessage("Errore del server: " + response.code());
                    }
                } catch (IOException e) {
                    result.setStatus(false);
                    result.setMessage("Errore di lettura risposta.");
                }
                data.setValue(result);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                CommonPojo errorPojo = new CommonPojo();
                errorPojo.setStatus(false);
                errorPojo.setMessage(getReadableErrorMessage(t));
                data.setValue(errorPojo);
            }
        });
        return data;
    }
    
    public LiveData<ProfilePojo> getProfile(String profileId) {
        MutableLiveData<ProfilePojo> data = new MutableLiveData<>();
        apiService.getProfile(profileId).enqueue(new Callback<ProfilePojo>() {
            @Override
            public void onResponse(Call<ProfilePojo> call, Response<ProfilePojo> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    ProfilePojo errorPojo = new ProfilePojo();
                    errorPojo.setStatus(false);
                    errorPojo.setMessage("Errore caricamento profilo");
                    data.setValue(errorPojo);
                }
            }

            @Override
            public void onFailure(Call<ProfilePojo> call, Throwable t) {
                ProfilePojo errorPojo = new ProfilePojo();
                errorPojo.setStatus(false);
                errorPojo.setMessage(getReadableErrorMessage(t));
                data.setValue(errorPojo);
            }
        });
        return data;
    }

    private void handleGenericResponse(Response<ResponseBody> response, MutableLiveData<NewLoginPojo> data) {
        NewLoginPojo result = new NewLoginPojo();
        try {
            if (response.isSuccessful() && response.body() != null) {
                String rawJson = response.body().string();
                try {
                    result = gson.fromJson(rawJson, NewLoginPojo.class);
                    if (result.getMessage() != null) {
                        result.setMessage(translateMessage(result.getMessage()));
                    }
                } catch (JsonSyntaxException e) {
                    result.setStatus(false);
                    result.setMessage(extractErrorMessage(rawJson));
                }
            } else {
                result.setStatus(false);
                result.setMessage("Errore del server: " + response.code());
            }
        } catch (IOException e) {
            result.setStatus(false);
            result.setMessage("Errore di lettura risposta.");
        }
        data.setValue(result);
    }
    
    private String getReadableErrorMessage(Throwable t) {
        if (t instanceof IOException) {
            return "Errore di rete. Controlla la tua connessione.";
        } else if (t != null) {
            return t.getMessage();
        }
        return "Errore sconosciuto";
    }

    private String extractErrorMessage(String rawJson) {
        String message = rawJson;
        try {
            JSONObject jsonObject = new JSONObject(rawJson);
            if (jsonObject.has("message")) {
                String msg = jsonObject.getString("message");
                if (msg != null && !msg.trim().isEmpty()) {
                    message = msg;
                }
            }
            if (jsonObject.has("data")) {
                Object dataObj = jsonObject.get("data");
                if (dataObj instanceof String) {
                    String dataMsg = (String) dataObj;
                    if (!dataMsg.trim().isEmpty()) {
                        message = dataMsg;
                    }
                }
            }
        } catch (JSONException e) {
        }
        
        return translateMessage(message);
    }
    
    private String translateMessage(String message) {
        if (message == null) return "";
        
        String lowerMsg = message.toLowerCase();
        
        if (lowerMsg.contains("invalid login") || lowerMsg.contains("password not match")) {
            return "Credenziali non valide. Controlla email e password.";
        }
        if (lowerMsg.contains("email is already exist") || lowerMsg.contains("email already registered")) {
            return "Questa email risulta già registrata.";
        }
        if (lowerMsg.contains("user does not exist") || lowerMsg.contains("email does not exists")) {
            return "Utente non trovato. Registrati per accedere.";
        }
        
        if (lowerMsg.contains("new password has been sent")) {
            return "Una nuova password è stata inviata alla tua email.";
        }
        if (lowerMsg.contains("activation link has been sent") || lowerMsg.contains("activation mail sent")) {
            return "Link di attivazione inviato. Controlla la tua email.";
        }
        if (lowerMsg.contains("service request sent successfully")) {
            return "Richiesta di servizio inviata con successo.";
        }
        
        return message;
    }
}
