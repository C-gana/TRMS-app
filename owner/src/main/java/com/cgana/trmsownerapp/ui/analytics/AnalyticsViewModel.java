package com.cgana.trmsownerapp.ui.analytics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgana.trmsownerapp.data.model.AnalyticsResponse;
import com.cgana.trmsownerapp.data.repository.AnalyticsRepository;

public class AnalyticsViewModel extends ViewModel {
    private AnalyticsRepository repository;
    private MutableLiveData<AnalyticsState> analyticsState = new MutableLiveData<>();

    private String currentVehicleId;
    private String currentPeriod = "today";

    public AnalyticsViewModel(AnalyticsRepository repository) {
        this.repository = repository;
        analyticsState.setValue(AnalyticsState.idle());
    }

    public LiveData<AnalyticsState> getAnalyticsState() {
        return analyticsState;
    }

    public void loadAnalytics(String vehicleId, String period) {
        this.currentVehicleId = vehicleId;
        this.currentPeriod = period;
        fetchAnalytics();
    }

    public void refresh() {
        if (currentVehicleId != null) {
            fetchAnalytics();
        }
    }

    public void changePeriod(String period) {
        if (currentVehicleId != null && !period.equals(currentPeriod)) {
            this.currentPeriod = period;
            fetchAnalytics();
        }
    }

    private void fetchAnalytics() {
        analyticsState.setValue(AnalyticsState.loading());

        LiveData<AnalyticsRepository.Result<AnalyticsResponse>> result =
                repository.getAnalytics(currentVehicleId, currentPeriod);

        result.observeForever(analyticsResult -> {
            if (analyticsResult.isSuccess()) {
                analyticsState.setValue(AnalyticsState.success(analyticsResult.getData()));
            } else {
                analyticsState.setValue(AnalyticsState.error(analyticsResult.getError()));
            }
        });
    }

    // State class
    public static class AnalyticsState {
        public enum Status {IDLE, LOADING, SUCCESS, ERROR}

        private Status status;
        private AnalyticsResponse data;
        private String error;

        private AnalyticsState(Status status, AnalyticsResponse data, String error) {
            this.status = status;
            this.data = data;
            this.error = error;
        }

        public static AnalyticsState idle() {
            return new AnalyticsState(Status.IDLE, null, null);
        }

        public static AnalyticsState loading() {
            return new AnalyticsState(Status.LOADING, null, null);
        }

        public static AnalyticsState success(AnalyticsResponse data) {
            return new AnalyticsState(Status.SUCCESS, data, null);
        }

        public static AnalyticsState error(String error) {
            return new AnalyticsState(Status.ERROR, null, error);
        }

        public Status getStatus() {
            return status;
        }

        public AnalyticsResponse getData() {
            return data;
        }

        public String getError() {
            return error;
        }
    }
}

