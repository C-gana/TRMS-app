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
     * Fetch dashboard status from backend
     */
    public LiveData<DashboardResponse> getDashboardStatus(String vehicleId) {
        MutableLiveData<DashboardResponse> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No auth token available");
            result.setValue(null);
            return result;
        }

        String authHeader = "Bearer " + token;

        apiService.getDashboardStatus(vehicleId, authHeader).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call,
                                   @NonNull Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Dashboard status fetched successfully");
                    result.setValue(response.body());
                } else {
                    Log.e(TAG, "Failed to fetch dashboard: " + response.code());
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error fetching dashboard", t);
                result.setValue(null);
            }
        });

        return result;
    }

    /**
     * Record passenger boarding
     */
    public LiveData<BoardingResponse> recordBoarding(String vehicleId, int seatNumber,
                                                      double latitude, double longitude) {
        MutableLiveData<BoardingResponse> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No auth token available");
            result.setValue(null);
            return result;
        }

        String authHeader = "Bearer " + token;
        BoardingRequest request = new BoardingRequest(vehicleId, seatNumber, latitude, longitude);

        apiService.recordBoarding(request, authHeader).enqueue(new Callback<BoardingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BoardingResponse> call,
                                   @NonNull Response<BoardingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Boarding recorded successfully");
                    result.setValue(response.body());
                } else {
                    Log.e(TAG, "Failed to record boarding: " + response.code());
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BoardingResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error recording boarding", t);
                result.setValue(null);
            }
        });

        return result;
    }
}

