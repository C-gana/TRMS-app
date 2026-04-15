package com.cgana.trmsownerapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cgana.trmsownerapp.data.api.JourneysApiService;
import com.cgana.trmsownerapp.data.api.RetrofitClient;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.model.JourneysResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JourneysRepository {
    private JourneysApiService apiService;
    private TokenManager tokenManager;

    public JourneysRepository(TokenManager tokenManager) {
        this.apiService = RetrofitClient.getInstance().getJourneysApi();
        this.tokenManager = tokenManager;
    }

    public LiveData<Result<JourneysResponse>> getJourneys(String vehicleId, String startDate,
                                                           String endDate, int page, int limit) {
        MutableLiveData<Result<JourneysResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        apiService.getJourneys(vehicleId, startDate, endDate, page, limit, "Bearer " + token)
                .enqueue(new Callback<JourneysResponse>() {
                    @Override
                    public void onResponse(Call<JourneysResponse> call, Response<JourneysResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(Result.success(response.body()));
                        } else {
                            result.setValue(Result.error("Server error: " + response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<JourneysResponse> call, Throwable t) {
                        result.setValue(Result.error(t.getMessage()));
                    }
                });

        return result;
    }

    public LiveData<Result<ResponseBody>> exportJourneys(String vehicleId, String startDate, String endDate) {
        MutableLiveData<Result<ResponseBody>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        apiService.exportJourneys(vehicleId, startDate, endDate, "Bearer " + token)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(Result.success(response.body()));
                        } else {
                            result.setValue(Result.error("Export failed: " + response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
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

