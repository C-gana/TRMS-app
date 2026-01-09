package com.cgana.trmsdriver.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cgana.trmsdriver.data.api.DashboardApiService;
import com.cgana.trmsdriver.data.api.RetrofitClient;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.Journey;
import com.cgana.trmsdriver.data.model.JourneyDetailResponse;
import com.cgana.trmsdriver.data.model.JourneyHistoryResponse;
import com.cgana.trmsdriver.data.model.StatisticsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Journey History Repository (Module 5 Part 2)
 * Handles journey history and statistics API calls with pagination
 */
public class JourneyHistoryRepository {

    private static final String TAG = "JourneyHistoryRepo";

    private final DashboardApiService apiService;
    private final TokenManager tokenManager;

    // Pagination
    private int currentPage = 1;
    private int perPage = 20;
    private boolean hasMorePages = true;

    public JourneyHistoryRepository(TokenManager tokenManager) {
        this.apiService = RetrofitClient.getInstance().create(DashboardApiService.class);
        this.tokenManager = tokenManager;
    }

    /**
     * Get journey history with pagination and filtering (Module 5 Part 2)
     */
    public LiveData<Result<List<Journey>>> getJourneyHistory(String vehicleId,
                                                             int page,
                                                             String filter) {
        MutableLiveData<Result<List<Journey>>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No auth token available");
            result.setValue(Result.error("Authentication required"));
            return result;
        }

        String authHeader = "Bearer " + token;

        apiService.getJourneyHistory(vehicleId, page, perPage, filter, authHeader)
            .enqueue(new Callback<JourneyHistoryResponse>() {
                @Override
                public void onResponse(@NonNull Call<JourneyHistoryResponse> call,
                                     @NonNull Response<JourneyHistoryResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JourneyHistoryResponse historyResponse = response.body();
                        if (historyResponse.isSuccess()) {
                            List<Journey> journeys = historyResponse.getJourneys();
                            if (journeys == null) {
                                journeys = new ArrayList<>();
                            }

                            // Update pagination info
                            if (historyResponse.getPagination() != null) {
                                currentPage = historyResponse.getPagination().getCurrentPage();
                                int totalPages = historyResponse.getPagination().getTotalPages();
                                hasMorePages = currentPage < totalPages;
                            }

                            Log.d(TAG, "Journey history loaded: " + journeys.size() + " journeys");
                            result.setValue(Result.success(journeys));
                        } else {
                            result.setValue(Result.error(historyResponse.getMessage()));
                        }
                    } else {
                        String error = "Failed to load journey history";
                        if (response.code() == 401) {
                            error = "Session expired. Please login again.";
                        } else if (response.code() == 404) {
                            error = "No journeys found";
                        } else if (response.code() >= 500) {
                            error = "Server error. Please try again.";
                        }
                        Log.e(TAG, "Failed to load journey history: " + response.code());
                        result.setValue(Result.error(error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JourneyHistoryResponse> call,
                                    @NonNull Throwable t) {
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
                    Log.e(TAG, "Network error loading journey history", t);
                    result.setValue(Result.error(error));
                }
            });

        return result;
    }

    /**
     * Get journey detail by ID (Module 5 Part 2)
     */
    public LiveData<Result<Journey>> getJourneyDetail(long journeyId) {
        MutableLiveData<Result<Journey>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No auth token available");
            result.setValue(Result.error("Authentication required"));
            return result;
        }

        String authHeader = "Bearer " + token;

        apiService.getJourneyDetail(journeyId, authHeader)
            .enqueue(new Callback<JourneyDetailResponse>() {
                @Override
                public void onResponse(@NonNull Call<JourneyDetailResponse> call,
                                     @NonNull Response<JourneyDetailResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JourneyDetailResponse detailResponse = response.body();
                        if (detailResponse.isSuccess() && detailResponse.getJourney() != null) {
                            Log.d(TAG, "Journey detail loaded: " + journeyId);
                            result.setValue(Result.success(detailResponse.getJourney()));
                        } else {
                            result.setValue(Result.error(detailResponse.getMessage()));
                        }
                    } else {
                        String error = "Failed to load journey details";
                        if (response.code() == 401) {
                            error = "Session expired";
                        } else if (response.code() == 404) {
                            error = "Journey not found";
                        }
                        Log.e(TAG, "Failed to load journey detail: " + response.code());
                        result.setValue(Result.error(error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JourneyDetailResponse> call,
                                    @NonNull Throwable t) {
                    Log.e(TAG, "Network error loading journey detail", t);
                    result.setValue(Result.error("Network error: " + t.getMessage()));
                }
            });

        return result;
    }

    /**
     * Get statistics for specified period (Module 5 Part 2)
     */
    public LiveData<Result<StatisticsResponse>> getStatistics(String vehicleId, String period) {
        MutableLiveData<Result<StatisticsResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No auth token available");
            result.setValue(Result.error("Authentication required"));
            return result;
        }

        String authHeader = "Bearer " + token;

        apiService.getStatistics(vehicleId, period, authHeader)
            .enqueue(new Callback<StatisticsResponse>() {
                @Override
                public void onResponse(@NonNull Call<StatisticsResponse> call,
                                     @NonNull Response<StatisticsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StatisticsResponse statsResponse = response.body();
                        if (statsResponse.isSuccess()) {
                            Log.d(TAG, "Statistics loaded for period: " + period);
                            result.setValue(Result.success(statsResponse));
                        } else {
                            result.setValue(Result.error(statsResponse.getMessage()));
                        }
                    } else {
                        String error = "Failed to load statistics";
                        if (response.code() == 401) {
                            error = "Session expired";
                        }
                        Log.e(TAG, "Failed to load statistics: " + response.code());
                        result.setValue(Result.error(error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StatisticsResponse> call,
                                    @NonNull Throwable t) {
                    Log.e(TAG, "Network error loading statistics", t);
                    result.setValue(Result.error("Network error: " + t.getMessage()));
                }
            });

        return result;
    }

    // Pagination helpers
    public boolean hasMorePages() {
        return hasMorePages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void resetPagination() {
        currentPage = 1;
        hasMorePages = true;
    }

    /**
     * Result wrapper class (Module 5 Part 2)
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

