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

/**
 * DashboardViewModel - Enhanced with Result wrapper and state management (Module 2 Part 4)
 */
public class DashboardViewModel extends ViewModel {
    private static final long REFRESH_INTERVAL_MS = 5000; // 5 seconds as per Module 2 Part 3

    private final DashboardRepository repository;
    private final MutableLiveData<DashboardState> dashboardState = new MutableLiveData<>();
    private final MutableLiveData<BoardingState> boardingState = new MutableLiveData<>();

    // Auto-refresh components
    private Handler handler;
    private Runnable refreshTask;
    private String currentVehicleId;

    public DashboardViewModel(DashboardRepository repository) {
        this.repository = repository;
        this.handler = new Handler(Looper.getMainLooper());
        dashboardState.setValue(DashboardState.idle());
        boardingState.setValue(BoardingState.idle());
    }

    /**
     * Fetch dashboard status from backend
     */
    public void loadDashboardData(String vehicleId) {
        this.currentVehicleId = vehicleId;
        fetchDashboard(false);
    }

    /**
     * Manual refresh (called by swipe-to-refresh)
     */
    public void refreshNow() {
        fetchDashboard(true);
    }

    private void fetchDashboard(boolean isRefresh) {
        android.util.Log.d("DashboardViewModel", "fetchDashboard called, isRefresh: " + isRefresh + ", vehicleId: " + currentVehicleId);

        if (!isRefresh) {
            android.util.Log.d("DashboardViewModel", "Setting state to LOADING");
            dashboardState.setValue(DashboardState.loading());
        }

        if (currentVehicleId == null || currentVehicleId.isEmpty()) {
            android.util.Log.e("DashboardViewModel", "Vehicle ID is null or empty!");
            dashboardState.setValue(DashboardState.error("Vehicle ID not set"));
            return;
        }

        android.util.Log.d("DashboardViewModel", "Calling repository.getDashboardStatus()");
        LiveData<DashboardRepository.Result<DashboardResponse>> result =
            repository.getDashboardStatus(currentVehicleId);

        // Observe forever since this is being called from ViewModel
        result.observeForever(dashboardResult -> {
            android.util.Log.d("DashboardViewModel", "Repository result received");

            if (dashboardResult == null) {
                android.util.Log.e("DashboardViewModel", "Dashboard result is null!");
                dashboardState.setValue(DashboardState.error("No response from server"));
                return;
            }

            if (dashboardResult.isSuccess()) {
                android.util.Log.d("DashboardViewModel", "Dashboard fetch successful");
                DashboardResponse data = dashboardResult.getData();
                if (data != null) {
                    android.util.Log.d("DashboardViewModel", "Dashboard data not null, seats: " +
                        (data.getSeats() != null ? data.getSeats().size() : "null"));
                } else {
                    android.util.Log.e("DashboardViewModel", "Dashboard data is null!");
                }
                dashboardState.setValue(DashboardState.success(dashboardResult.getData()));
                scheduleRefresh();
            } else {
                android.util.Log.e("DashboardViewModel", "Dashboard fetch failed: " + dashboardResult.getError());
                dashboardState.setValue(DashboardState.error(dashboardResult.getError()));
                // Retry after error
                scheduleRefresh();
            }
        });
    }

    /**
     * Record passenger boarding (Module 2 Part 4)
     */
    public void recordBoarding(int seatNumber, double latitude, double longitude) {
        boardingState.setValue(BoardingState.loading());

        LiveData<DashboardRepository.Result<BoardingResponse>> result =
            repository.recordBoarding(currentVehicleId, seatNumber, latitude, longitude);

        result.observeForever(boardingResult -> {
            if (boardingResult.isSuccess()) {
                boardingState.setValue(BoardingState.success(boardingResult.getData()));
                // Refresh dashboard immediately after successful boarding
                refreshNow();
            } else {
                boardingState.setValue(BoardingState.error(boardingResult.getError()));
            }
        });
    }

    // Getters for LiveData
    public LiveData<DashboardState> getDashboardState() {
        return dashboardState;
    }

    public LiveData<BoardingState> getBoardingState() {
        return boardingState;
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
     * Schedule next auto-refresh
     */
    private void scheduleRefresh() {
        stopAutoRefresh(); // Cancel any pending refresh

        refreshTask = () -> {
            if (currentVehicleId != null) {
                fetchDashboard(true);
            }
        };

        handler.postDelayed(refreshTask, REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopAutoRefresh(); // Clean up when ViewModel is destroyed
    }

    // Dashboard State
    public static class DashboardState {
        public enum Status { IDLE, LOADING, SUCCESS, ERROR }

        private Status status;
        private DashboardResponse data;
        private String error;

        private DashboardState(Status status, DashboardResponse data, String error) {
            this.status = status;
            this.data = data;
            this.error = error;
        }

        public static DashboardState idle() {
            return new DashboardState(Status.IDLE, null, null);
        }

        public static DashboardState loading() {
            return new DashboardState(Status.LOADING, null, null);
        }

        public static DashboardState success(DashboardResponse data) {
            return new DashboardState(Status.SUCCESS, data, null);
        }

        public static DashboardState error(String error) {
            return new DashboardState(Status.ERROR, null, error);
        }

        public Status getStatus() { return status; }
        public DashboardResponse getData() { return data; }
        public String getError() { return error; }
    }

    // Boarding State
    public static class BoardingState {
        public enum Status { IDLE, LOADING, SUCCESS, ERROR }

        private Status status;
        private BoardingResponse data;
        private String error;

        private BoardingState(Status status, BoardingResponse data, String error) {
            this.status = status;
            this.data = data;
            this.error = error;
        }

        public static BoardingState idle() {
            return new BoardingState(Status.IDLE, null, null);
        }

        public static BoardingState loading() {
            return new BoardingState(Status.LOADING, null, null);
        }

        public static BoardingState success(BoardingResponse data) {
            return new BoardingState(Status.SUCCESS, data, null);
        }

        public static BoardingState error(String error) {
            return new BoardingState(Status.ERROR, null, error);
        }

        public Status getStatus() { return status; }
        public BoardingResponse getData() { return data; }
        public String getError() { return error; }
    }
}
