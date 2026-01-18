package com.cgana.trmsdriver.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cgana.trmsdriver.data.api.DashboardApiService;
import com.cgana.trmsdriver.data.api.RetrofitClient;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.BoardingRequest;
import com.cgana.trmsdriver.data.model.BoardingResponse;
import com.cgana.trmsdriver.data.model.DashboardResponse;
import com.cgana.trmsdriver.data.model.GenericResponse;
import com.cgana.trmsdriver.data.model.Location;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardRepository {
    private static final String TAG = "DashboardRepository";
    private final DashboardApiService apiService;
    private final TokenManager tokenManager;

    public DashboardRepository(Context context) {
        this.apiService = RetrofitClient.getInstance().create(DashboardApiService.class);
        this.tokenManager = new TokenManager(context);
    }

    /**
     * Fetch dashboard status from backend (Enhanced with error handling - Module 2 Part 4)
     */
    public LiveData<Result<DashboardResponse>> getDashboardStatus(String vehicleId) {
        Log.d(TAG, "getDashboardStatus called for vehicleId: " + vehicleId);
        MutableLiveData<Result<DashboardResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No auth token available");
            result.setValue(Result.error("Authentication required"));
            return result;
        }

        Log.d(TAG, "Token available, making API call");
        String authHeader = "Bearer " + token;

        apiService.getDashboardStatus(vehicleId, authHeader).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call,
                                   @NonNull Response<DashboardResponse> response) {
                Log.d(TAG, "API response received, code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Dashboard status fetched successfully");
                    DashboardResponse data = response.body();
                    Log.d(TAG, "Response body - vehicleId: " + data.getVehicleId() +
                        ", seats: " + (data.getSeats() != null ? data.getSeats().size() : "null"));
                    result.setValue(Result.success(response.body()));
                } else {
                    String error = "Failed to load dashboard";
                    if (response.code() == 401) {
                        error = "Session expired. Please login again.";
                    } else if (response.code() == 404) {
                        error = "Vehicle not found";
                    } else if (response.code() >= 500) {
                        error = "Server error. Please try again.";
                    }
                    Log.e(TAG, "Failed to fetch dashboard: " + response.code() + ", error: " + error);
                    result.setValue(Result.error(error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
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
                Log.e(TAG, "Network error fetching dashboard: " + error, t);
                result.setValue(Result.error(error));
            }
        });

        Log.d(TAG, "Returning LiveData result");
        return result;
    }

    /**
     * Record passenger boarding (Enhanced with error handling - Module 2 Part 4)
     */
    public LiveData<Result<BoardingResponse>> recordBoarding(String vehicleId, int seatNumber,
                                                              double latitude, double longitude) {
        MutableLiveData<Result<BoardingResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No auth token available");
            result.setValue(Result.error("Authentication required"));
            return result;
        }

        String authHeader = "Bearer " + token;
        BoardingRequest request = new BoardingRequest(vehicleId, seatNumber, latitude, longitude);

        apiService.recordBoarding(request, authHeader).enqueue(new Callback<BoardingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BoardingResponse> call,
                                   @NonNull Response<BoardingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BoardingResponse boardingResponse = response.body();
                    Log.d(TAG, "Boarding API response - success: " + boardingResponse.isSuccess() +
                        ", journeyId: " + boardingResponse.getJourneyId() +
                        ", seatNumber: " + boardingResponse.getSeatNumber() +
                        ", message: " + boardingResponse.getMessage());

                    if (boardingResponse.isSuccess()) {
                        Log.d(TAG, "Boarding recorded successfully");
                        result.setValue(Result.success(boardingResponse));
                    } else {
                        Log.e(TAG, "Boarding API returned success=false");
                        result.setValue(Result.error(boardingResponse.getMessage()));
                    }
                } else {
                    String error = "Boarding failed";
                    if (response.code() == 400) {
                        error = "Seat already occupied";
                    } else if (response.code() == 401) {
                        error = "Session expired";
                    } else if (response.code() == 409) {
                        error = "Seat not available";
                    }
                    Log.e(TAG, "Failed to record boarding: " + response.code());
                    result.setValue(Result.error(error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<BoardingResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error recording boarding", t);
                result.setValue(Result.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    /**
     * Board passenger with location (Module 2 Part 3)
     */
    public LiveData<Result<GenericResponse>> boardPassenger(String vehicleId, int seatNumber, Location boardingLocation) {
        MutableLiveData<Result<GenericResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            result.setValue(Result.error("Authentication required"));
            return result;
        }

        String authHeader = "Bearer " + token;

        // Create BoardingRequest with location
        BoardingRequest request = new BoardingRequest(vehicleId, seatNumber,
                boardingLocation.getLatitude(), boardingLocation.getLongitude());

        apiService.recordBoarding(request, authHeader).enqueue(new Callback<BoardingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BoardingResponse> call, @NonNull Response<BoardingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BoardingResponse boardingResp = response.body();

                    // Convert BoardingResponse to GenericResponse
                    GenericResponse genericResp = new GenericResponse();
                    genericResp.setSuccess(boardingResp.isSuccess());
                    genericResp.setMessage(boardingResp.getMessage());
                    genericResp.setData(boardingResp);

                    result.setValue(Result.success(genericResp));
                } else {
                    result.setValue(Result.error("Boarding failed"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<BoardingResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error recording boarding", t);
                result.setValue(Result.error(t.getMessage()));
            }
        });

        return result;
    }

    /**
     * Generic result wrapper (Module 2 Part 3)
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
