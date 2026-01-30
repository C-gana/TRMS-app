package com.cgana.trmsdriver.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgana.trmsdriver.data.model.Journey;
import com.cgana.trmsdriver.data.repository.JourneyHistoryRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Journey History ViewModel (Module 5 Part 3)
 */
public class JourneyHistoryViewModel extends ViewModel {

    private static final int PAGE_SIZE = 20;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private JourneyHistoryRepository repository;
    private MutableLiveData<JourneyHistoryState> journeyHistoryState = new MutableLiveData<>();
    private List<Journey> allJourneys = new ArrayList<>();

    private int currentPage = 1;
    private boolean hasMorePages = true;
    private String currentFilter = "all";
    private String currentSearch = null;

    public JourneyHistoryViewModel(JourneyHistoryRepository repository) {
        this.repository = repository;
        journeyHistoryState.setValue(JourneyHistoryState.idle());
    }

    public LiveData<JourneyHistoryState> getJourneyHistoryState() {
        return journeyHistoryState;
    }

    /**
     * Load journeys for vehicle with filter
     */
    public void loadJourneys(String vehicleId, String filter) {
        currentFilter = filter;
        currentPage = 1;
        allJourneys.clear();
        journeyHistoryState.setValue(JourneyHistoryState.loading());

        repository.getJourneyHistory(vehicleId, currentPage, filter)
                .observeForever(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        List<Journey> journeys = result.getData();
                        allJourneys.addAll(journeys);
                        hasMorePages = journeys.size() >= PAGE_SIZE;
                        journeyHistoryState.setValue(JourneyHistoryState.success(new ArrayList<>(allJourneys)));
                    } else {
                        journeyHistoryState.setValue(JourneyHistoryState.error(result.getError()));
                    }
                });
    }

    /**
     * Load more journeys (pagination)
     */
    public void loadMoreJourneys(String vehicleId) {
        if (!hasMorePages || journeyHistoryState.getValue().getStatus() == Status.LOADING_MORE) {
            return;
        }

        currentPage++;
        journeyHistoryState.setValue(JourneyHistoryState.loadingMore(new ArrayList<>(allJourneys)));

        repository.getJourneyHistory(vehicleId, currentPage, currentFilter)
                .observeForever(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        List<Journey> journeys = result.getData();
                        allJourneys.addAll(journeys);
                        hasMorePages = journeys.size() >= PAGE_SIZE;
                        journeyHistoryState.setValue(JourneyHistoryState.success(new ArrayList<>(allJourneys)));
                    } else {
                        journeyHistoryState.setValue(JourneyHistoryState.error(result.getError()));
                    }
                });
    }

    /**
     * Search journeys by destination or journey ID
     */
    public void searchJourneys(String vehicleId, String query) {
        currentSearch = query;

        if (query == null || query.trim().isEmpty()) {
            loadJourneys(vehicleId, currentFilter);
            return;
        }

        List<Journey> filtered = new ArrayList<>();
        for (Journey journey : allJourneys) {
            if (journey.getDestination().toLowerCase().contains(query.toLowerCase()) ||
                String.valueOf(journey.getJourneyId()).contains(query)) {
                filtered.add(journey);
            }
        }

        journeyHistoryState.setValue(JourneyHistoryState.success(filtered));
    }

    /**
     * Refresh journeys
     */
    public void refreshJourneys(String vehicleId) {
        loadJourneys(vehicleId, currentFilter);
    }

    /**
     * Journey History State class
     */
    public static class JourneyHistoryState {
        private Status status;
        private List<Journey> journeys;
        private String error;

        private JourneyHistoryState(Status status, List<Journey> journeys, String error) {
            this.status = status;
            this.journeys = journeys;
            this.error = error;
        }

        public static JourneyHistoryState idle() {
            return new JourneyHistoryState(Status.IDLE, null, null);
        }

        public static JourneyHistoryState loading() {
            return new JourneyHistoryState(Status.LOADING, null, null);
        }

        public static JourneyHistoryState loadingMore(List<Journey> current) {
            return new JourneyHistoryState(Status.LOADING_MORE, current, null);
        }

        public static JourneyHistoryState success(List<Journey> journeys) {
            return new JourneyHistoryState(Status.SUCCESS, journeys, null);
        }

        public static JourneyHistoryState error(String error) {
            return new JourneyHistoryState(Status.ERROR, null, error);
        }

        public Status getStatus() { return status; }
        public List<Journey> getJourneys() { return journeys; }
        public String getError() { return error; }
    }

    /**
     * Status enum
     */
    public enum Status {
        IDLE, LOADING, LOADING_MORE, SUCCESS, ERROR
    }
}

