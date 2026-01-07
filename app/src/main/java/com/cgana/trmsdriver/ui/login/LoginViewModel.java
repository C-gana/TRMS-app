package com.cgana.trmsdriver.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> navigateHome = new MutableLiveData<>(false);

    private final LoginRepository repository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new LoginRepository(new TokenManager(application.getApplicationContext()));
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getNavigateHome() {
        return navigateHome;
    }

    public void login(String phone, String password, boolean remember) {
        loading.setValue(true);
        error.setValue("");

        repository.login(phone, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    repository.persistLogin(response.body().getToken(), response.body().getUser(), remember);
                    navigateHome.setValue(true);
                } else {
                    error.setValue(response.body() != null ? response.body().getError() : "Login failed");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loading.setValue(false);
                error.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void onNavigated() {
        navigateHome.setValue(false);
    }
}

