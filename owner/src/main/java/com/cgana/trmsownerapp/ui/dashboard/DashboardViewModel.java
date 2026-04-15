package com.cgana.trmsownerapp.ui.dashboard;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgana.trmsownerapp.data.model.DashboardResponse;
import com.cgana.trmsownerapp.data.repository.DashboardRepository;

public class DashboardViewModel extends ViewModel {
    private DashboardRepository repository;
    private MutableLiveData<DashboardState> dashboardState = new MutableLiveData<>();
    private Handler handler;
    private Runnable refreshRunnable;
    private String currentVehicleId;

    private static final long REFRESH_INTERVAL = 10000; // 10 seconds

    public DashboardViewModel(DashboardRepository repository) {
        this.repository = repository;
        dashboardState.setValue(DashboardState.idle());
        handler = new Handler(Looper.getMainLooper());
    }

    public LiveData<DashboardState> getDashboardState() {
        return dashboardState;
    }

    public void loadDashboard(String vehicleId) {
        this.currentVehicleId = vehicleId;
        fetchDashboard(false);
    }

    public void refresh() {
        if (currentVehicleId != null) {
            fetchDashboard(true);
        }
    }

    private void fetchDashboard(boolean isRefresh) {
        if (!isRefresh) {
            dashboardState.setValue(DashboardState.loading());
        }

        LiveData<DashboardRepository.Result<DashboardResponse>> result =
                repository.getDashboard(currentVehicleId);

        result.observeForever(dashboardResult -> {
            if (dashboardResult.isSuccess()) {
                dashboardState.setValue(DashboardState.success(
                        dashboardResult.getData(),
                        dashboardResult.isOffline() // Pass offline flag
                ));

                // Only auto-refresh if online
                if (!dashboardResult.isOffline()) {
                    scheduleNextRefresh();
                }
            } else {
                dashboardState.setValue(DashboardState.error(
                        dashboardResult.getError(),
                        dashboardResult.isOffline()
                ));
            }
        });
    }

    private void scheduleNextRefresh() {
        // Cancel previous refresh
        if (refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }

        // Schedule next refresh
        refreshRunnable = () -> {
            if (currentVehicleId != null) {
                fetchDashboard(true);
            }
        };
        handler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    public void stopRefresh() {
        if (refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopRefresh();
    }

    // Updated State class
    public static class DashboardState {
        public enum Status { IDLE, LOADING, SUCCESS, ERROR }

        private Status status;
        private DashboardResponse data;
        private String error;
        private boolean isOffline; // NEW

        private DashboardState(Status status, DashboardResponse data, String error, boolean isOffline) {
            this.status = status;
            this.data = data;
            this.error = error;
            this.isOffline = isOffline;
        }

        public static DashboardState idle() {
            return new DashboardState(Status.IDLE, null, null, false);
        }

        public static DashboardState loading() {
            return new DashboardState(Status.LOADING, null, null, false);
        }

        public static DashboardState success(DashboardResponse data, boolean isOffline) {
            return new DashboardState(Status.SUCCESS, data, null, isOffline);
        }

        public static DashboardState error(String error, boolean isOffline) {
            return new DashboardState(Status.ERROR, null, error, isOffline);
        }

        public Status getStatus() {
            return status;
        }

        public DashboardResponse getData() {
            return data;
        }

        public String getError() {
            return error;
        }

        public boolean isOffline() {
            return isOffline;
        }
    }
}

