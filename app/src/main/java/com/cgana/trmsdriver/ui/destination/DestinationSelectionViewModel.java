package com.cgana.trmsdriver.ui.destination;

import android.os.CountDownTimer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgana.trmsdriver.data.model.Destination;
import com.cgana.trmsdriver.data.model.DestinationResponse;
import com.cgana.trmsdriver.data.model.SetDestinationResponse;
import com.cgana.trmsdriver.data.repository.DestinationRepository;

import java.util.List;

/**
 * Destination Selection ViewModel with Countdown Timer (Module 3 Part 2)
 * Manages destination loading, selection, and 90-second countdown
 */
public class DestinationSelectionViewModel extends ViewModel {

    private static final long TIMEOUT_MILLISECONDS = 90000; // 90 seconds
    private static final long COUNTDOWN_INTERVAL = 1000; // 1 second

    private final DestinationRepository repository;
    private final MutableLiveData<DestinationsState> destinationsState = new MutableLiveData<>();
    private final MutableLiveData<SetDestinationState> setDestinationState = new MutableLiveData<>();
    private final MutableLiveData<Integer> countdownSeconds = new MutableLiveData<>();
    private final MutableLiveData<Boolean> timeoutExpired = new MutableLiveData<>();

    private CountDownTimer countDownTimer;

    public DestinationSelectionViewModel(DestinationRepository repository) {
        this.repository = repository;
        destinationsState.setValue(DestinationsState.idle());
        setDestinationState.setValue(SetDestinationState.idle());
        countdownSeconds.setValue(90);
        timeoutExpired.setValue(false);
    }

    public LiveData<DestinationsState> getDestinationsState() {
        return destinationsState;
    }

    public LiveData<SetDestinationState> getSetDestinationState() {
        return setDestinationState;
    }

    public LiveData<Integer> getCountdownSeconds() {
        return countdownSeconds;
    }

    public LiveData<Boolean> getTimeoutExpired() {
        return timeoutExpired;
    }

    /**
     * Load destinations for vehicle (Module 3 Part 2)
     */
    public void loadDestinations(String vehicleId) {
        destinationsState.setValue(DestinationsState.loading());

        LiveData<DestinationRepository.Result<DestinationResponse>> result =
            repository.getDestinations(vehicleId);

        result.observeForever(destinationsResult -> {
            if (destinationsResult != null) {
                if (destinationsResult.isSuccess() && destinationsResult.getData() != null) {
                    List<Destination> destinations = destinationsResult.getData().getDestinations();
                    destinationsState.setValue(DestinationsState.success(destinations));
                } else {
                    destinationsState.setValue(DestinationsState.error(destinationsResult.getError()));
                }
            }
        });
    }

    /**
     * Set destination for journey (Module 3 Part 2)
     */
    public void setDestination(String vehicleId, long journeyId, int seatNumber,
                               int destinationId, double latitude, double longitude) {
        setDestinationState.setValue(SetDestinationState.loading());

        LiveData<DestinationRepository.Result<SetDestinationResponse>> result =
            repository.setDestination(vehicleId, journeyId, seatNumber, destinationId,
                                    latitude, longitude);

        result.observeForever(setDestResult -> {
            if (setDestResult != null) {
                if (setDestResult.isSuccess()) {
                    setDestinationState.setValue(SetDestinationState.success(setDestResult.getData()));
                    // Stop countdown on success
                    stopCountdown();
                } else {
                    setDestinationState.setValue(SetDestinationState.error(setDestResult.getError()));
                }
            }
        });
    }

    /**
     * Start countdown timer (Module 3 Part 2)
     */
    public void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(TIMEOUT_MILLISECONDS, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                countdownSeconds.setValue(secondsRemaining);
            }

            @Override
            public void onFinish() {
                countdownSeconds.setValue(0);
                timeoutExpired.setValue(true);
            }
        };

        countDownTimer.start();
    }

    /**
     * Stop countdown timer (Module 3 Part 2)
     */
    public void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    /**
     * Reset state for new selection
     */
    public void resetSetDestinationState() {
        setDestinationState.setValue(SetDestinationState.idle());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopCountdown();
    }

    /**
     * Destinations State (Module 3 Part 2)
     */
    public static class DestinationsState {
        public enum Status { IDLE, LOADING, SUCCESS, ERROR }

        private final Status status;
        private final List<Destination> destinations;
        private final String error;

        private DestinationsState(Status status, List<Destination> destinations, String error) {
            this.status = status;
            this.destinations = destinations;
            this.error = error;
        }

        public static DestinationsState idle() {
            return new DestinationsState(Status.IDLE, null, null);
        }

        public static DestinationsState loading() {
            return new DestinationsState(Status.LOADING, null, null);
        }

        public static DestinationsState success(List<Destination> destinations) {
            return new DestinationsState(Status.SUCCESS, destinations, null);
        }

        public static DestinationsState error(String error) {
            return new DestinationsState(Status.ERROR, null, error);
        }

        public Status getStatus() { return status; }
        public List<Destination> getDestinations() { return destinations; }
        public String getError() { return error; }
    }

    /**
     * Set Destination State (Module 3 Part 2)
     */
    public static class SetDestinationState {
        public enum Status { IDLE, LOADING, SUCCESS, ERROR }

        private final Status status;
        private final SetDestinationResponse data;
        private final String error;

        private SetDestinationState(Status status, SetDestinationResponse data, String error) {
            this.status = status;
            this.data = data;
            this.error = error;
        }

        public static SetDestinationState idle() {
            return new SetDestinationState(Status.IDLE, null, null);
        }

        public static SetDestinationState loading() {
            return new SetDestinationState(Status.LOADING, null, null);
        }

        public static SetDestinationState success(SetDestinationResponse data) {
            return new SetDestinationState(Status.SUCCESS, data, null);
        }

        public static SetDestinationState error(String error) {
            return new SetDestinationState(Status.ERROR, null, error);
        }

        public Status getStatus() { return status; }
        public SetDestinationResponse getData() { return data; }
        public String getError() { return error; }
    }
}

