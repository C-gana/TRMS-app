package com.cgana.trmsdriver.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cgana.trmsdriver.data.api.DashboardApiService;
import com.cgana.trmsdriver.data.api.RetrofitClient;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.LocationUpdateRequest;
import com.cgana.trmsdriver.data.model.LocationUpdateResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Location Tracking Repository (Module 6 Part 1)
 * Handles sending location updates to the backend
 */
public class LocationTrackingRepository {

    private static final String TAG = "LocationTrackingRepo";

    private final DashboardApiService apiService;
    private final TokenManager tokenManager;

    public LocationTrackingRepository(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
        this.apiService = RetrofitClient.getInstance().create(DashboardApiService.class);
    }

    /**
     * Send location update to backend
     */
    public void sendLocationUpdate(String vehicleId, double latitude, double longitude) {
        String token = tokenManager.getToken();
        if (token == null) {
            Log.e(TAG, "No token available, cannot send location update");
            return;
        }

        LocationUpdateRequest request = new LocationUpdateRequest(vehicleId, latitude, longitude);

        Log.d(TAG, "Sending location update: Vehicle=" + vehicleId +
            ", Lat=" + latitude + ", Lon=" + longitude);

        apiService.sendLocationUpdate("Bearer " + token, request)
                .enqueue(new Callback<LocationUpdateResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LocationUpdateResponse> call,
                                         @NonNull Response<LocationUpdateResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            LocationUpdateResponse locationResponse = response.body();
                            if (locationResponse.isSuccess()) {
                                Log.d(TAG, "Location update sent successfully");
                            } else {
                                Log.e(TAG, "Location update failed: " + locationResponse.getMessage());
                            }
                        } else {
                            Log.e(TAG, "Location update failed: " + response.code() +": " +response);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LocationUpdateResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "Location update network error: " + t.getMessage());
                    }
                });
    }

    /**
     * Send location update with LiveData result (for UI updates)
     */
    public LiveData<Result<LocationUpdateResponse>> sendLocationUpdateWithResult(
            String vehicleId, double latitude, double longitude) {

        MutableLiveData<Result<LocationUpdateResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        LocationUpdateRequest request = new LocationUpdateRequest(vehicleId, latitude, longitude);

        apiService.sendLocationUpdate("Bearer " + token, request)
                .enqueue(new Callback<LocationUpdateResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LocationUpdateResponse> call,
                                         @NonNull Response<LocationUpdateResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(Result.success(response.body()));
                        } else {
                            result.setValue(Result.error("Failed to update location"));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LocationUpdateResponse> call, @NonNull Throwable t) {
                        result.setValue(Result.error(t.getMessage()));
                    }
                });

        return result;
    }

    /**
     * Result wrapper class
     */
    public static class Result<T> {
        private Status status;
        private T data;
        private String error;

        private Result(Status status, T data, String error) {
            this.status = status;
            this.data = data;
            this.error = error;
        }

        public static <T> Result<T> success(T data) {
            return new Result<>(Status.SUCCESS, data, null);
        }

        public static <T> Result<T> error(String error) {
            return new Result<>(Status.ERROR, null, error);
        }

        public Status getStatus() { return status; }
        public T getData() { return data; }
        public String getError() { return error; }

        public enum Status {
            SUCCESS, ERROR
        }
    }
}

