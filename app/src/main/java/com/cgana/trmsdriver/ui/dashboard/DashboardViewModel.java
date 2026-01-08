package com.cgana.trmsdriver.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.cgana.trmsdriver.data.model.BoardingResponse;
import com.cgana.trmsdriver.data.model.DashboardResponse;
import com.cgana.trmsdriver.data.repository.DashboardRepository;

public class DashboardViewModel extends AndroidViewModel {
    private final DashboardRepository repository;
    private final MutableLiveData<DashboardResponse> dashboardData = new MutableLiveData<>();
    private final MutableLiveData<BoardingResponse> boardingResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MediatorLiveData<Long> lastUpdateTime = new MediatorLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.repository = new DashboardRepository(application);
        lastUpdateTime.setValue(System.currentTimeMillis());
    }

    /**
     * Fetch dashboard status from backend
     */
    public void loadDashboardData(String vehicleId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        LiveData<DashboardResponse> result = repository.getDashboardStatus(vehicleId);

        // Use MediatorLiveData to observe the repository result
        MediatorLiveData<DashboardResponse> mediator = new MediatorLiveData<>();
        mediator.addSource(result, response -> {
            isLoading.setValue(false);
            if (response != null) {
                dashboardData.setValue(response);
                lastUpdateTime.setValue(System.currentTimeMillis());
            } else {
                errorMessage.setValue("Failed to load dashboard data");
            }
            mediator.removeSource(result);
        });
    }

    /**
     * Record passenger boarding
     */
    public void recordBoarding(String vehicleId, int seatNumber, double latitude, double longitude) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        LiveData<BoardingResponse> result = repository.recordBoarding(vehicleId, seatNumber, latitude, longitude);

        MediatorLiveData<BoardingResponse> mediator = new MediatorLiveData<>();
        mediator.addSource(result, response -> {
            isLoading.setValue(false);
            if (response != null && response.isSuccess()) {
                boardingResult.setValue(response);
                // Refresh dashboard after successful boarding
                loadDashboardData(vehicleId);
            } else {
                errorMessage.setValue("Failed to record boarding");
            }
            mediator.removeSource(result);
        });
    }

    // Getters for LiveData
    public LiveData<DashboardResponse> getDashboardData() {
        return dashboardData;
    }

    public LiveData<BoardingResponse> getBoardingResult() {
        return boardingResult;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Long> getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Clear boarding result after handling
     */
    public void clearBoardingResult() {
        boardingResult.setValue(null);
    }

    /**
     * Clear error message after displaying
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
}

