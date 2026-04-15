package com.cgana.trmsownerapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cgana.trmsownerapp.data.api.DestinationsApiService;
import com.cgana.trmsownerapp.data.api.RetrofitClient;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.model.DestinationRequest;
import com.cgana.trmsownerapp.data.model.DestinationsResponse;
import com.cgana.trmsownerapp.data.model.GenericResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DestinationsRepository {
    private DestinationsApiService apiService;
    private TokenManager tokenManager;

    public DestinationsRepository(TokenManager tokenManager) {
        this.apiService = RetrofitClient.getInstance().getDestinationsApi();
        this.tokenManager = tokenManager;
    }

    public LiveData<Result<DestinationsResponse>> getDestinations(String vehicleId) {
        MutableLiveData<Result<DestinationsResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        apiService.getDestinations(vehicleId, "Bearer " + token)
                .enqueue(new Callback<DestinationsResponse>() {
                    @Override
                    public void onResponse(Call<DestinationsResponse> call, Response<DestinationsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(Result.success(response.body()));
                        } else {
                            result.setValue(Result.error("Server error: " + response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<DestinationsResponse> call, Throwable t) {
                        result.setValue(Result.error(t.getMessage()));
                    }
                });

        return result;
    }

    public LiveData<Result<GenericResponse>> createDestination(String vehicleId, String name,
                                                                double latitude, double longitude,
                                                                int fareAmount, int alertRadius) {
        MutableLiveData<Result<GenericResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        DestinationRequest request = new DestinationRequest(name, latitude, longitude, fareAmount, alertRadius);

        apiService.createDestination(vehicleId, request, "Bearer " + token)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(Result.success(response.body()));
                        } else {
                            result.setValue(Result.error("Failed to create destination"));
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        result.setValue(Result.error(t.getMessage()));
                    }
                });

        return result;
    }

    public LiveData<Result<GenericResponse>> updateDestination(int destinationId, String name,
                                                                double latitude, double longitude,
                                                                int fareAmount, int alertRadius) {
        MutableLiveData<Result<GenericResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        DestinationRequest request = new DestinationRequest(name, latitude, longitude, fareAmount, alertRadius);

        apiService.updateDestination(destinationId, request, "Bearer " + token)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(Result.success(response.body()));
                        } else {
                            result.setValue(Result.error("Failed to update destination"));
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        result.setValue(Result.error(t.getMessage()));
                    }
                });

        return result;
    }

    public LiveData<Result<GenericResponse>> deleteDestination(int destinationId) {
        MutableLiveData<Result<GenericResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        apiService.deleteDestination(destinationId, "Bearer " + token)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(Result.success(response.body()));
                        } else {
                            result.setValue(Result.error("Failed to delete destination"));
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

