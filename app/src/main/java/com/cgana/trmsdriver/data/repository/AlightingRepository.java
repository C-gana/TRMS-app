package com.cgana.trmsdriver.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import com.cgana.trmsdriver.data.api.DashboardApiService;
import com.cgana.trmsdriver.data.api.RetrofitClient;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.AlightingRequest;
import com.cgana.trmsdriver.data.model.AlightingResponse;
import com.cgana.trmsdriver.data.model.MissedStopRequest;
import com.cgana.trmsdriver.data.model.MissedStopResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Alighting Repository (Module 4 Part 2)
 * Handles alighting and missed stop API calls with error handling
 */
public class AlightingRepository {

    private static final String TAG = "AlightingRepository";

    private final DashboardApiService apiService;
    private final TokenManager tokenManager;

    public AlightingRepository(TokenManager tokenManager) {
        this.apiService = RetrofitClient.getInstance().create(DashboardApiService.class);
        this.tokenManager = tokenManager;
    }

    /**
     * Record passenger alighting (Module 4 Part 2)
     */
    public LiveData<Result<AlightingResponse>> recordAlighting(
            String vehicleId, long journeyId, int seatNumber,
            double latitude, double longitude, boolean fareCollected, boolean missedStop) {

        MutableLiveData<Result<AlightingResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No auth token available");
            result.setValue(Result.error("Authentication required"));
            return result;
        }

        String authHeader = "Bearer " + token;
        AlightingRequest request = new AlightingRequest(
            vehicleId, journeyId, seatNumber, latitude, longitude, fareCollected, missedStop
        );

        apiService.recordAlighting(request, authHeader)
            .enqueue(new Callback<AlightingResponse>() {
                @Override
                public void onResponse(@NonNull Call<AlightingResponse> call,
                                     @NonNull Response<AlightingResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AlightingResponse alightingResponse = response.body();
                        if (alightingResponse.isSuccess()) {
                            Log.d(TAG, "Alighting recorded successfully");
                            result.setValue(Result.success(alightingResponse));
                        } else {
                            result.setValue(Result.error(alightingResponse.getMessage()));
                        }
                    } else {
                        String error = "Failed to record alighting";
                        if (response.code() == 400) {
                            error = "Invalid journey data";
                        } else if (response.code() == 401) {
                            error = "Session expired. Please login again.";
                        } else if (response.code() == 404) {
                            error = "Journey not found";
                        } else if (response.code() == 409) {
                            error = "Journey already completed";
                        } else if (response.code() >= 500) {
                            error = "Server error. Please try again.";
                        }
                        Log.e(TAG, "Failed to record alighting: " + response.code());
                        result.setValue(Result.error(error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AlightingResponse> call, @NonNull Throwable t) {
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
                    Log.e(TAG, "Network error recording alighting", t);
                    result.setValue(Result.error(error));
                }
            });

        return result;
    }

    /**
     * Report missed stop (Module 4 Part 2)
     */
    public LiveData<Result<MissedStopResponse>> reportMissedStop(
            String vehicleId, long journeyId, int seatNumber,
            double latitude, double longitude, String notes) {

        MutableLiveData<Result<MissedStopResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No auth token available");
            result.setValue(Result.error("Authentication required"));
            return result;
        }

        String authHeader = "Bearer " + token;
        MissedStopRequest request = new MissedStopRequest(
            vehicleId, journeyId, seatNumber, latitude, longitude, notes
        );

        apiService.reportMissedStop(request, authHeader)
            .enqueue(new Callback<MissedStopResponse>() {
                @Override
                public void onResponse(@NonNull Call<MissedStopResponse> call,
                                     @NonNull Response<MissedStopResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        MissedStopResponse missedResponse = response.body();
                        if (missedResponse.isSuccess()) {
                            Log.d(TAG, "Missed stop reported successfully");
                            result.setValue(Result.success(missedResponse));
                        } else {
                            result.setValue(Result.error(missedResponse.getMessage()));
                        }
                    } else {
                        String error = "Failed to report missed stop";
                        if (response.code() == 400) {
                            error = "Invalid data";
                        } else if (response.code() == 401) {
                            error = "Session expired";
                        } else if (response.code() == 404) {
                            error = "Journey not found";
                        }
                        Log.e(TAG, "Failed to report missed stop: " + response.code());
                        result.setValue(Result.error(error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MissedStopResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "Network error reporting missed stop", t);
                    result.setValue(Result.error("Network error: " + t.getMessage()));
                }
            });

        return result;
    }

    /**
     * Result wrapper class (Module 4 Part 2)
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

