package com.cgana.trmsownerapp.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cgana.trmsownerapp.data.api.DashboardApiService;
import com.cgana.trmsownerapp.data.api.RetrofitClient;
import com.cgana.trmsownerapp.data.local.TRMSDatabase;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.local.entity.DashboardEntity;
import com.cgana.trmsownerapp.data.model.DashboardResponse;
import com.cgana.trmsownerapp.data.model.Location;
import com.cgana.trmsownerapp.utils.NetworkUtils;

import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardRepository {
    private DashboardApiService apiService;
    private TokenManager tokenManager;
    private TRMSDatabase database;
    private Context context;

    public DashboardRepository(TokenManager tokenManager, Context context) {
        this.apiService = RetrofitClient.getInstance().getDashboardApi();
        this.tokenManager = tokenManager;
        this.database = TRMSDatabase.getInstance(context);
        this.context = context;
    }

    public LiveData<Result<DashboardResponse>> getDashboard(String vehicleId) {
        MutableLiveData<Result<DashboardResponse>> result = new MutableLiveData<>();

        // Check network availability
        if (NetworkUtils.isNetworkAvailable(context)) {
            // Online: Fetch from API
            fetchFromApi(vehicleId, result);
        } else {
            // Offline: Load from cache
            loadFromCache(vehicleId, result);
        }

        return result;
    }

    private void fetchFromApi(String vehicleId, MutableLiveData<Result<DashboardResponse>> result) {
        String token = tokenManager.getToken();
        if (token == null) {
            result.setValue(Result.error("Not authenticated", false));
            return;
        }

        apiService.getDashboard(vehicleId, "Bearer " + token).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse data = response.body();

                    // Cache the response
                    cacheData(vehicleId, data);

                    result.setValue(Result.success(data, false)); // false = online
                } else {
                    // API failed, try cache as fallback
                    loadFromCache(vehicleId, result);
                }
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                // Network error, load from cache
                loadFromCache(vehicleId, result);
            }
        });
    }

    private void loadFromCache(String vehicleId, MutableLiveData<Result<DashboardResponse>> result) {
        Executors.newSingleThreadExecutor().execute(() -> {
            DashboardEntity cached = database.dashboardDao().getDashboard(vehicleId);

            if (cached != null) {
                // Convert entity to response
                DashboardResponse response = entityToResponse(cached);
                result.postValue(Result.success(response, true)); // true = offline
            } else {
                result.postValue(Result.error("No cached data available", true));
            }
        });
    }

    private void cacheData(String vehicleId, DashboardResponse data) {
        Executors.newSingleThreadExecutor().execute(() -> {
            DashboardEntity entity = responseToEntity(vehicleId, data);
            database.dashboardDao().insert(entity);
        });
    }

    private DashboardEntity responseToEntity(String vehicleId, DashboardResponse data) {
        DashboardEntity entity = new DashboardEntity();
        entity.setVehicle_id(data.getVehicleId());
        entity.setRegistration(data.getRegistration());
        entity.setStatus(data.getStatus());
        entity.setLast_seen(data.getLastSeen());

        if (data.getCurrentLocation() != null) {
            entity.setCurrent_latitude(data.getCurrentLocation().getLatitude());
            entity.setCurrent_longitude(data.getCurrentLocation().getLongitude());
        }

        entity.setSeats(data.getSeats());
        entity.setActive_journeys(data.getActiveJourneys());

        return entity;
    }

    private DashboardResponse entityToResponse(DashboardEntity entity) {
        DashboardResponse response = new DashboardResponse();
        response.setVehicleId(entity.getVehicle_id());
        response.setRegistration(entity.getRegistration());
        response.setStatus(entity.getStatus());
        response.setLastSeen(entity.getLast_seen());
        response.setSeats(entity.getSeats());
        response.setActiveJourneys(entity.getActive_journeys());

        // Set location
        Location location = new Location(
                entity.getCurrent_latitude(),
                entity.getCurrent_longitude()
        );
        response.setCurrentLocation(location);

        return response;
    }

    // Result wrapper class with offline flag
    public static class Result<T> {
        private T data;
        private String error;
        private boolean success;
        private boolean isOffline; // NEW: indicates if data is from cache

        private Result(T data, String error, boolean success, boolean isOffline) {
            this.data = data;
            this.error = error;
            this.success = success;
            this.isOffline = isOffline;
        }

        public static <T> Result<T> success(T data, boolean isOffline) {
            return new Result<>(data, null, true, isOffline);
        }

        public static <T> Result<T> error(String error, boolean isOffline) {
            return new Result<>(null, error, false, isOffline);
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

        public boolean isOffline() {
            return isOffline;
        }
    }
}

