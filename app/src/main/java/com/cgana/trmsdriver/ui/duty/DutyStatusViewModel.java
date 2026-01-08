package com.cgana.trmsdriver.ui.duty;

import android.os.Handler;
import android.os. Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle. MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.cgana.trmsdriver. data.model.DutyStatusResponse;
import com.cgana.trmsdriver.data.model.Location;
import com.cgana.trmsdriver.data.repository. AuthRepository;

public class DutyStatusViewModel extends ViewModel {

    private AuthRepository repository;
    private MutableLiveData<DutyState> dutyState = new MutableLiveData<>();
    private Handler handler;
    private Runnable durationUpdateRunnable;

    public DutyStatusViewModel(AuthRepository repository) {
        this.repository = repository;
        dutyState.setValue(DutyState.idle());
        handler = new Handler(Looper. getMainLooper());
    }

    public LiveData<DutyState> getDutyState() {
        return dutyState;
    }

    /**
     * Update duty status (ON/OFF)
     */
    public void updateDutyStatus(String vehicleId, boolean onDuty, Location location) {
        // Show loading
        dutyState.setValue(DutyState.loading());

        // Call repository
        LiveData<AuthRepository.Result<DutyStatusResponse>> result =
                repository.updateDutyStatus(vehicleId, onDuty, location);

        result.observeForever(dutyResult -> {
            if (dutyResult.isSuccess()) {
                dutyState.setValue(DutyState. success(dutyResult.getData()));

                // Start duration updates if going ON DUTY
                if (onDuty) {
                    startDurationUpdates();
                } else {
                    stopDurationUpdates();
                }
            } else {
                dutyState.setValue(DutyState. error(dutyResult.getError()));
            }
        });
    }

    /**
     * Start periodic duration updates
     */
    private void startDurationUpdates() {
        durationUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                // Notify UI to update duration
                DutyState currentState = dutyState.getValue();
                if (currentState != null && currentState.getStatus() == DutyState.Status.SUCCESS) {
                    dutyState.setValue(currentState); // Trigger observer
                }
                handler.postDelayed(this, 60000); // Update every minute
            }
        };
        handler.postDelayed(durationUpdateRunnable, 60000);
    }

    /**
     * Stop duration updates
     */
    private void stopDurationUpdates() {
        if (durationUpdateRunnable != null) {
            handler.removeCallbacks(durationUpdateRunnable);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopDurationUpdates();
    }

    /**
     * Duty State class
     */
    public static class DutyState {
        public enum Status { IDLE, LOADING, SUCCESS, ERROR }

        private Status status;
        private DutyStatusResponse data;
        private String error;

        private DutyState(Status status, DutyStatusResponse data, String error) {
            this.status = status;
            this.data = data;
            this.error = error;
        }

        public static DutyState idle() {
            return new DutyState(Status.IDLE, null, null);
        }

        public static DutyState loading() {
            return new DutyState(Status.LOADING, null, null);
        }

        public static DutyState success(DutyStatusResponse data) {
            return new DutyState(Status.SUCCESS, data, null);
        }

        public static DutyState error(String error) {
            return new DutyState(Status.ERROR, null, error);
        }

        public Status getStatus() { return status; }
        public DutyStatusResponse getData() { return data; }
        public String getError() { return error; }
    }
}