package com.cgana.trmsownerapp.ui.journeys;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgana.trmsownerapp.data.model.Journey;
import com.cgana.trmsownerapp.data.model.JourneysResponse;
import com.cgana.trmsownerapp.data.repository.JourneysRepository;

import okhttp3.ResponseBody;

import java.util.ArrayList;
import java.util.List;

public class JourneysViewModel extends ViewModel {
    private JourneysRepository repository;
    private MutableLiveData<JourneysState> journeysState = new MutableLiveData<>();
    private MutableLiveData<ExportState> exportState = new MutableLiveData<>();

    private String currentVehicleId;
    private String currentStartDate;
    private String currentEndDate;
    private int currentPage = 1;
    private int totalPages = 1;
    private List<Journey> allJourneys = new ArrayList<>();

    private static final int PAGE_LIMIT = 50;

    public JourneysViewModel(JourneysRepository repository) {
        this.repository = repository;
        journeysState.setValue(JourneysState.idle());
        exportState.setValue(ExportState.idle());
    }

    public LiveData<JourneysState> getJourneysState() {
        return journeysState;
    }

    public LiveData<ExportState> getExportState() {
        return exportState;
    }

    public void loadJourneys(String vehicleId, String startDate, String endDate) {
        this.currentVehicleId = vehicleId;
        this.currentStartDate = startDate;
        this.currentEndDate = endDate;
        this.currentPage = 1;
        this.allJourneys.clear();

        journeysState.setValue(JourneysState.loading());
        fetchJourneys();
    }

    public void loadMore() {
        if (currentPage < totalPages) {
            currentPage++;
            journeysState.setValue(JourneysState.loadingMore());
            fetchJourneys();
        }
    }

    public void refresh() {
        currentPage = 1;
        allJourneys.clear();
        fetchJourneys();
    }

    private void fetchJourneys() {
        LiveData<JourneysRepository.Result<JourneysResponse>> result =
                repository.getJourneys(currentVehicleId, currentStartDate, currentEndDate, currentPage, PAGE_LIMIT);

        result.observeForever(journeysResult -> {
            if (journeysResult.isSuccess()) {
                JourneysResponse response = journeysResult.getData();
                totalPages = response.getPages();
                allJourneys.addAll(response.getJourneys());
                journeysState.setValue(JourneysState.success(new ArrayList<>(allJourneys), currentPage < totalPages));
            } else {
                journeysState.setValue(JourneysState.error(journeysResult.getError()));
            }
        });
    }

    public void exportJourneys() {
        exportState.setValue(ExportState.exporting());

        LiveData<JourneysRepository.Result<ResponseBody>> result =
                repository.exportJourneys(currentVehicleId, currentStartDate, currentEndDate);

        result.observeForever(exportResult -> {
            if (exportResult.isSuccess()) {
                exportState.setValue(ExportState.success(exportResult.getData()));
            } else {
                exportState.setValue(ExportState.error(exportResult.getError()));
            }
        });
    }

    // States
    public static class JourneysState {
        public enum Status {IDLE, LOADING, LOADING_MORE, SUCCESS, ERROR}

        private Status status;
        private List<Journey> journeys;
        private boolean hasMore;
        private String error;

        private JourneysState(Status status, List<Journey> journeys, boolean hasMore, String error) {
            this.status = status;
            this.journeys = journeys;
            this.hasMore = hasMore;
            this.error = error;
        }

        public static JourneysState idle() {
            return new JourneysState(Status.IDLE, null, false, null);
        }

        public static JourneysState loading() {
            return new JourneysState(Status.LOADING, null, false, null);
        }

        public static JourneysState loadingMore() {
            return new JourneysState(Status.LOADING_MORE, null, false, null);
        }

        public static JourneysState success(List<Journey> journeys, boolean hasMore) {
            return new JourneysState(Status.SUCCESS, journeys, hasMore, null);
        }

        public static JourneysState error(String error) {
            return new JourneysState(Status.ERROR, null, false, error);
        }

        public Status getStatus() {
            return status;
        }

        public List<Journey> getJourneys() {
            return journeys;
        }

        public boolean hasMore() {
            return hasMore;
        }

        public String getError() {
            return error;
        }
    }

    public static class ExportState {
        public enum Status {IDLE, EXPORTING, SUCCESS, ERROR}

        private Status status;
        private ResponseBody data;
        private String error;

        private ExportState(Status status, ResponseBody data, String error) {
            this.status = status;
            this.data = data;
            this.error = error;
        }

        public static ExportState idle() {
            return new ExportState(Status.IDLE, null, null);
        }

        public static ExportState exporting() {
            return new ExportState(Status.EXPORTING, null, null);
        }

        public static ExportState success(ResponseBody data) {
            return new ExportState(Status.SUCCESS, data, null);
        }

        public static ExportState error(String error) {
            return new ExportState(Status.ERROR, null, error);
        }

        public Status getStatus() {
            return status;
        }

        public ResponseBody getData() {
            return data;
        }

        public String getError() {
            return error;
        }
    }
}

