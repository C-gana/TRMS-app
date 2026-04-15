package com.cgana.trmsownerapp.ui.alerts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgana.trmsownerapp.data.model.Alert;
import com.cgana.trmsownerapp.data.model.AlertsResponse;
import com.cgana.trmsownerapp.data.model.GenericResponse;
import com.cgana.trmsownerapp.data.repository.AlertsRepository;

import java.util.ArrayList;
import java.util.List;

public class AlertsViewModel extends ViewModel {
    private AlertsRepository repository;
    private MutableLiveData<AlertsState> alertsState = new MutableLiveData<>();
    private MutableLiveData<AcknowledgeState> acknowledgeState = new MutableLiveData<>();

    private String currentVehicleId;
    private String currentFilter = "all"; // "all", "unread", "timeout", "missed_stop", "proximity"
    private List<Alert> allAlerts = new ArrayList<>();

    public AlertsViewModel(AlertsRepository repository) {
        this.repository = repository;
        alertsState.setValue(AlertsState.idle());
        acknowledgeState.setValue(AcknowledgeState.idle());
    }

    public LiveData<AlertsState> getAlertsState() {
        return alertsState;
    }

    public LiveData<AcknowledgeState> getAcknowledgeState() {
        return acknowledgeState;
    }

    public void loadAlerts(String vehicleId) {
        this.currentVehicleId = vehicleId;
        fetchAlerts();
    }

    public void refresh() {
        if (currentVehicleId != null) {
            fetchAlerts();
        }
    }

    public void applyFilter(String filter) {
        this.currentFilter = filter;
        filterAlerts();
    }

    private void fetchAlerts() {
        alertsState.setValue(AlertsState.loading());

        Boolean unreadOnly = null;
        if ("unread".equals(currentFilter)) {
            unreadOnly = true;
        }

        LiveData<AlertsRepository.Result<AlertsResponse>> result =
                repository.getAlerts(currentVehicleId, unreadOnly);

        result.observeForever(alertsResult -> {
            if (alertsResult.isSuccess()) {
                allAlerts = alertsResult.getData().getAlerts();
                if (allAlerts == null) allAlerts = new ArrayList<>();
                filterAlerts();
            } else {
                alertsState.setValue(AlertsState.error(alertsResult.getError()));
            }
        });
    }

    private void filterAlerts() {
        List<Alert> filtered = new ArrayList<>();

        for (Alert alert : allAlerts) {
            boolean matches = false;

            switch (currentFilter) {
                case "all":
                    matches = true;
                    break;
                case "unread":
                    matches = !alert.isAcknowledged();
                    break;
                case "timeout":
                    matches = "timeout".equals(alert.getAlertType());
                    break;
                case "missed_stop":
                    matches = "missed_stop".equals(alert.getAlertType()) ||
                              "confirmed_missed_stop".equals(alert.getAlertType());
                    break;
                case "proximity":
                    matches = "proximity".equals(alert.getAlertType());
                    break;
            }

            if (matches) {
                filtered.add(alert);
            }
        }

        alertsState.setValue(AlertsState.success(filtered));
    }

    public void acknowledgeAlert(int alertId, String notes) {
        acknowledgeState.setValue(AcknowledgeState.acknowledging());

        LiveData<AlertsRepository.Result<GenericResponse>> result =
                repository.acknowledgeAlert(alertId, notes);

        result.observeForever(ackResult -> {
            if (ackResult.isSuccess()) {
                acknowledgeState.setValue(AcknowledgeState.success());
                refresh(); // Reload alerts
            } else {
                acknowledgeState.setValue(AcknowledgeState.error(ackResult.getError()));
            }
        });
    }

    public void resetAcknowledgeState() {
        acknowledgeState.setValue(AcknowledgeState.idle());
    }

    // States
    public static class AlertsState {
        public enum Status { IDLE, LOADING, SUCCESS, ERROR }

        private Status status;
        private List<Alert> alerts;
        private String error;

        private AlertsState(Status status, List<Alert> alerts, String error) {
            this.status = status;
            this.alerts = alerts;
            this.error = error;
        }

        public static AlertsState idle() {
            return new AlertsState(Status.IDLE, null, null);
        }

        public static AlertsState loading() {
            return new AlertsState(Status.LOADING, null, null);
        }

        public static AlertsState success(List<Alert> alerts) {
            return new AlertsState(Status.SUCCESS, alerts, null);
        }

        public static AlertsState error(String error) {
            return new AlertsState(Status.ERROR, null, error);
        }

        public Status getStatus() { return status; }
        public List<Alert> getAlerts() { return alerts; }
        public String getError() { return error; }
    }

    public static class AcknowledgeState {
        public enum Status { IDLE, ACKNOWLEDGING, SUCCESS, ERROR }

        private Status status;
        private String error;

        private AcknowledgeState(Status status, String error) {
            this.status = status;
            this.error = error;
        }

        public static AcknowledgeState idle() {
            return new AcknowledgeState(Status.IDLE, null);
        }

        public static AcknowledgeState acknowledging() {
            return new AcknowledgeState(Status.ACKNOWLEDGING, null);
        }

        public static AcknowledgeState success() {
            return new AcknowledgeState(Status.SUCCESS, null);
        }

        public static AcknowledgeState error(String error) {
            return new AcknowledgeState(Status.ERROR, error);
        }

        public Status getStatus() { return status; }
        public String getError() { return error; }
    }
}
