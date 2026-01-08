package com.cgana.trmsdriver.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cgana.trmsdriver.data.api.AuthApiService;
import com.cgana.trmsdriver.data.api.RetrofitClient;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.DutyStatusRequest;
import com.cgana.trmsdriver.data.model.DutyStatusResponse;
import com.cgana.trmsdriver.data.model.Location;
import com.cgana.trmsdriver.data.model.LoginRequest;
import com.cgana.trmsdriver.data.model.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private AuthApiService apiService;
    private TokenManager tokenManager;

    public AuthRepository(TokenManager tokenManager) {
        this.apiService = RetrofitClient.getInstance().getAuthApi();
        this.tokenManager = tokenManager;
    }

    /**
     * Login driver
     */
    public LiveData<Result<LoginResponse>> login(String phoneNumber, String password) {
        MutableLiveData<Result<LoginResponse>> result = new MutableLiveData<>();

        LoginRequest request = new LoginRequest(phoneNumber, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.isSuccess()) {
                        // Save token and driver data
                        tokenManager. saveToken(loginResponse.getToken());
                        tokenManager.saveDriver(loginResponse.getDriver());

                        // Save duty status if available
                        if (loginResponse. getDriver() != null) {
                            tokenManager.saveDutyStatus(loginResponse.getDriver().isOnDuty());
                            if (loginResponse.getDriver().getDutyStartedAt() != null) {
                                tokenManager. saveDutyStartedAt(loginResponse.getDriver().getDutyStartedAt());
                            }
                        }

                        result.setValue(Result.success(loginResponse));
                    } else {
                        result.setValue(Result.error(loginResponse.getMessage()));
                    }
                } else {
                    result.setValue(Result. error("Login failed.  Please check your credentials."));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                result.setValue(Result.error("Network error:  " + t.getMessage()));
            }
        });

        return result;
    }

    /**
     * Update duty status (ON/OFF)
     */
    public LiveData<Result<DutyStatusResponse>> updateDutyStatus(String vehicleId,
                                                                 boolean onDuty,
                                                                 Location location) {
        MutableLiveData<Result<DutyStatusResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        DutyStatusRequest request = new DutyStatusRequest(vehicleId, onDuty, location);

        apiService.updateDutyStatus(request, "Bearer " + token)
                .enqueue(new Callback<DutyStatusResponse>() {
                    @Override
                    public void onResponse(Call<DutyStatusResponse> call, Response<DutyStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            DutyStatusResponse dutyResponse = response.body();

                            if (dutyResponse.isSuccess()) {
                                // Update local storage
                                tokenManager.saveDutyStatus(dutyResponse.isOnDuty());
                                if (dutyResponse.getDutyStartedAt() != null) {
                                    tokenManager.saveDutyStartedAt(dutyResponse.getDutyStartedAt());
                                }

                                result.setValue(Result.success(dutyResponse));
                            } else {
                                result.setValue(Result.error(dutyResponse.getMessage()));
                            }
                        } else {
                            result.setValue(Result.error("Failed to update duty status"));
                        }
                    }

                    @Override
                    public void onFailure(Call<DutyStatusResponse> call, Throwable t) {
                        result.setValue(Result.error("Network error: " + t.getMessage()));
                    }
                });

        return result;
    }

    /**
     * Result wrapper class
     */
    public static class Result<T> {
        private T data;
        private String error;
        private boolean success;

        private Result(T data, String error, boolean success) {
            this.data = data;
            this.error = error;
            this.success = success;
        }

        public static <T> Result<T> success(T data) {
            return new Result<>(data, null, true);
        }

        public static <T> Result<T> error(String error) {
            return new Result<>(null, error, false);
        }

        public T getData() { return data; }
        public String getError() { return error; }
        public boolean isSuccess() { return success; }
    }
}