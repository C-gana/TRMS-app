package com.cgana.trmsdriver.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import com.cgana.trmsdriver.data.api.DashboardApiService;
import com.cgana.trmsdriver.data.api.RetrofitClient;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.DestinationResponse;
import com.cgana.trmsdriver.data.model.SetDestinationRequest;
import com.cgana.trmsdriver.data.model.SetDestinationResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Destination Repository (Module 3 Part 2)
 * Handles destination-related API calls with error handling
 */
public class DestinationRepository {

    private static final String TAG = "DestinationRepository";

    private final DashboardApiService apiService;
    private final TokenManager tokenManager;

    public DestinationRepository(TokenManager tokenManager) {
        this.apiService = RetrofitClient.getInstance().create(DashboardApiService.class);
        this.tokenManager = tokenManager;
    }

    /**
     * Get destinations list for vehicle (Module 3 Part 2)
     */
    public LiveData<Result<DestinationResponse>> getDestinations(String vehicleId) {
        MutableLiveData<Result<DestinationResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No auth token available");
            result.setValue(Result.error("Authentication required"));
            return result;
        }

        String authHeader = "Bearer " + token;

        apiService.getDestinations(vehicleId, authHeader)
            .enqueue(new Callback<DestinationResponse>() {
                @Override
                public void onResponse(@NonNull Call<DestinationResponse> call,
                                     @NonNull Response<DestinationResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "Destinations loaded successfully");
                        result.setValue(Result.success(response.body()));
                    } else {
                        String error = "Failed to load destinations";
                        if (response.code() == 401) {
                            error = "Session expired. Please login again.";
                        } else if (response.code() == 404) {
                            error = "No destinations found for this vehicle";
                        } else if (response.code() >= 500) {
                            error = "Server error. Please try again.";
                        }
                        Log.e(TAG, "Failed to load destinations: " + response.code());
                        result.setValue(Result.error(error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DestinationResponse> call, @NonNull Throwable t) {
                    String error = "Network error";
                    if (t.getMessage() != null) {
                        if (t.getMessage().contains("timeout")) {
                            error = "Connection timeout. Check your internet.";
                        } else if (t.getMessage().contains("Unable to resolve host")) {
                            error = "No internet connection";
                        } else {
                            error = t.getMessage();
                        }
                    }
                    Log.e(TAG, "Network error loading destinations", t);
                    result.setValue(Result.error(error));
                }
            });

        return result;
    }

    /**
     * Set destination for journey (Module 3 Part 2)
     */
    public LiveData<Result<SetDestinationResponse>> setDestination(
            String vehicleId, long journeyId, int seatNumber,
            int destinationId, double latitude, double longitude) {

        MutableLiveData<Result<SetDestinationResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No auth token available");
            result.setValue(Result.error("Authentication required"));
            return result;
        }

        String authHeader = "Bearer " + token;
        SetDestinationRequest request = new SetDestinationRequest(
            vehicleId, journeyId, seatNumber, destinationId, latitude, longitude
        );

        apiService.setDestination(request, authHeader)
            .enqueue(new Callback<SetDestinationResponse>() {
                @Override
                public void onResponse(@NonNull Call<SetDestinationResponse> call,
                                     @NonNull Response<SetDestinationResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SetDestinationResponse destResponse = response.body();
                        if (destResponse.isSuccess()) {
                            Log.d(TAG, "Destination set successfully");
                            result.setValue(Result.success(destResponse));
                        } else {
                            result.setValue(Result.error(destResponse.getMessage()));
                        }
                    } else {
                        String error = "Failed to set destination";
                        if (response.code() == 400) {
                            error = "Invalid destination";
                        } else if (response.code() == 401) {
                            error = "Session expired";
                        } else if (response.code() == 404) {
                            error = "Journey not found";
                        } else if (response.code() == 409) {
                            error = "Destination already set";
                        }
                        Log.e(TAG, "Failed to set destination: " + response.code());
                        result.setValue(Result.error(error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SetDestinationResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "Network error setting destination", t);
                    result.setValue(Result.error("Network error: " + t.getMessage()));
                }
            });

        return result;
    }

    /**
     * Result wrapper class (Module 3 Part 2)
     */
    public static class Result<T> {
        private final T data;
        private final String error;
        private final boolean success;

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

