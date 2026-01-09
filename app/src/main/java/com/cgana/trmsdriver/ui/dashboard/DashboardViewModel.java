package com.cgana.trmsdriver.ui.dashboard;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgana.trmsdriver.data.model.BoardingResponse;
import com.cgana.trmsdriver.data.model.DashboardResponse;
import com.cgana.trmsdriver.data.repository.DashboardRepository;

public class DashboardViewModel extends ViewModel {
    private static final long REFRESH_INTERVAL_MS = 5000; // 5 seconds as per Module 2 Part 3

    private final DashboardRepository repository;
    private final MutableLiveData<DashboardResponse> dashboardData = new MutableLiveData<>();
    private final MutableLiveData<BoardingResponse> boardingResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MediatorLiveData<Long> lastUpdateTime = new MediatorLiveData<>();

    // Auto-refresh components
    private Handler handler;
    private Runnable refreshTask;
    private String currentVehicleId;

    public DashboardViewModel(DashboardRepository repository) {
        this.repository = repository;
        this.handler = new Handler(Looper.getMainLooper());
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

                // Schedule next auto-refresh if auto-refresh is active
                if (currentVehicleId != null) {
                    scheduleRefresh();
                }
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

    /**
     * Start auto-refresh (Module 2 Part 3)
     */
    public void startAutoRefresh(String vehicleId) {
        this.currentVehicleId = vehicleId;
        scheduleRefresh();
    }

    /**
     * Stop auto-refresh
     */
    public void stopAutoRefresh() {
        if (refreshTask != null && handler != null) {
            handler.removeCallbacks(refreshTask);
            refreshTask = null;
        }
    }

    /**
     * Manual refresh (called by swipe-to-refresh)
     */
    public void refreshNow() {
        if (currentVehicleId != null) {
            loadDashboardData(currentVehicleId);
        }
    }

    /**
     * Schedule next auto-refresh
     */
    private void scheduleRefresh() {
        stopAutoRefresh(); // Cancel any pending refresh

        refreshTask = () -> {
            if (currentVehicleId != null) {
                loadDashboardData(currentVehicleId);
            }
        };

        handler.postDelayed(refreshTask, REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopAutoRefresh(); // Clean up when ViewModel is destroyed
    }
}
