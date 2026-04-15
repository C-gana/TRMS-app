package com.cgana.trmsownerapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cgana.trmsownerapp.data.api.AlertsApiService;
import com.cgana.trmsownerapp.data.api.RetrofitClient;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.model.AcknowledgeRequest;
import com.cgana.trmsownerapp.data.model.AlertsResponse;
import com.cgana.trmsownerapp.data.model.GenericResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertsRepository {
    private AlertsApiService apiService;
    private TokenManager tokenManager;

    public AlertsRepository(TokenManager tokenManager) {
        this.apiService = RetrofitClient.getInstance().getAlertsApi();
        this.tokenManager = tokenManager;
    }

    public LiveData<Result<AlertsResponse>> getAlerts(String vehicleId, Boolean unreadOnly) {
        MutableLiveData<Result<AlertsResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        apiService.getAlerts(vehicleId, unreadOnly, "Bearer " + token)
                .enqueue(new Callback<AlertsResponse>() {
                    @Override
                    public void onResponse(Call<AlertsResponse> call, Response<AlertsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(Result.success(response.body()));
                        } else {
                            result.setValue(Result.error("Server error: " + response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<AlertsResponse> call, Throwable t) {
                        result.setValue(Result.error(t.getMessage()));
                    }
                });

        return result;
    }

    public LiveData<Result<GenericResponse>> acknowledgeAlert(int alertId, String notes) {
        MutableLiveData<Result<GenericResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        AcknowledgeRequest request = new AcknowledgeRequest(notes);

        apiService.acknowledgeAlert(alertId, request, "Bearer " + token)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(Result.success(response.body()));
                        } else {
                            result.setValue(Result.error("Failed to acknowledge alert"));
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        result.setValue(Result.error(t.getMessage()));
                    }
                });

        return result;
    }

    // Result wrapper class
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

        public T getData() {
            return data;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}

