package com.cgana.trmsownerapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cgana.trmsownerapp.data.api.AnalyticsApiService;
import com.cgana.trmsownerapp.data.api.RetrofitClient;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.model.AnalyticsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyticsRepository {
    private AnalyticsApiService apiService;
    private TokenManager tokenManager;

    public AnalyticsRepository(TokenManager tokenManager) {
        this.apiService = RetrofitClient.getInstance().getAnalyticsApi();
        this.tokenManager = tokenManager;
    }

    public LiveData<Result<AnalyticsResponse>> getAnalytics(String vehicleId, String period) {
        MutableLiveData<Result<AnalyticsResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        apiService.getAnalytics(vehicleId, period, "Bearer " + token)
                .enqueue(new Callback<AnalyticsResponse>() {
                    @Override
                    public void onResponse(Call<AnalyticsResponse> call, Response<AnalyticsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(Result.success(response.body()));
                        } else {
                            result.setValue(Result.error("Server error: " + response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<AnalyticsResponse> call, Throwable t) {
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

