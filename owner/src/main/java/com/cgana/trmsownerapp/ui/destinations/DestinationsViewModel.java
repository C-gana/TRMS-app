package com.cgana.trmsownerapp.ui.destinations;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgana.trmsownerapp.data.model.Destination;
import com.cgana.trmsownerapp.data.model.DestinationsResponse;
import com.cgana.trmsownerapp.data.model.GenericResponse;
import com.cgana.trmsownerapp.data.repository.DestinationsRepository;

import java.util.List;

public class DestinationsViewModel extends ViewModel {
    private DestinationsRepository repository;
    private MutableLiveData<DestinationsState> destinationsState = new MutableLiveData<>();
    private MutableLiveData<ActionState> actionState = new MutableLiveData<>();

    private String currentVehicleId;

    public DestinationsViewModel(DestinationsRepository repository) {
        this.repository = repository;
        destinationsState.setValue(DestinationsState.idle());
        actionState.setValue(ActionState.idle());
    }

    public LiveData<DestinationsState> getDestinationsState() {
        return destinationsState;
    }

    public LiveData<ActionState> getActionState() {
        return actionState;
    }

    public void loadDestinations(String vehicleId) {
        this.currentVehicleId = vehicleId;
        destinationsState.setValue(DestinationsState.loading());

        LiveData<DestinationsRepository.Result<DestinationsResponse>> result =
                repository.getDestinations(vehicleId);

        result.observeForever(destinationsResult -> {
            if (destinationsResult.isSuccess()) {
                destinationsState.setValue(DestinationsState.success(destinationsResult.getData().getDestinations()));
            } else {
                destinationsState.setValue(DestinationsState.error(destinationsResult.getError()));
            }
        });
    }

    public void refresh() {
        if (currentVehicleId != null) {
            loadDestinations(currentVehicleId);
        }
    }

    public void deleteDestination(int destinationId) {
        actionState.setValue(ActionState.deleting());

        LiveData<DestinationsRepository.Result<GenericResponse>> result =
                repository.deleteDestination(destinationId);

        result.observeForever(deleteResult -> {
            if (deleteResult.isSuccess()) {
                actionState.setValue(ActionState.deleteSuccess());
                refresh(); // Reload list
            } else {
                actionState.setValue(ActionState.error(deleteResult.getError()));
            }
        });
    }

    // States
    public static class DestinationsState {
        public enum Status { IDLE, LOADING, SUCCESS, ERROR }

        private Status status;
        private List<Destination> destinations;
        private String error;

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

        public Status getStatus() {
            return status;
        }

        public List<Destination> getDestinations() {
            return destinations;
        }

        public String getError() {
            return error;
        }
    }

    public static class ActionState {
        public enum Status { IDLE, DELETING, DELETE_SUCCESS, ERROR }

        private Status status;
        private String error;

        private ActionState(Status status, String error) {
            this.status = status;
            this.error = error;
        }

        public static ActionState idle() {
            return new ActionState(Status.IDLE, null);
        }

        public static ActionState deleting() {
            return new ActionState(Status.DELETING, null);
        }

        public static ActionState deleteSuccess() {
            return new ActionState(Status.DELETE_SUCCESS, null);
        }

        public static ActionState error(String error) {
            return new ActionState(Status.ERROR, error);
        }

        public Status getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }
    }
}

