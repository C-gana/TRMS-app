package com.cgana.trmsdriver.data.model;

import java.util.List;

/**
 * Journey History Response model (Module 5 Part 2)
 * Response from journey history API with pagination
 */
public class JourneyHistoryResponse {
    private boolean success;
    private List<Journey> journeys;
    private Pagination pagination;
    private String message;

    public static class Pagination {
        private int current_page;
        private int total_pages;
        private int total_journeys;
        private int per_page;

        // Getters and Setters
        public int getCurrentPage() { return current_page; }
        public void setCurrentPage(int current_page) { this.current_page = current_page; }

        public int getTotalPages() { return total_pages; }
        public void setTotalPages(int total_pages) { this.total_pages = total_pages; }

        public int getTotalJourneys() { return total_journeys; }
        public void setTotalJourneys(int total_journeys) { this.total_journeys = total_journeys; }

        public int getPerPage() { return per_page; }
        public void setPerPage(int per_page) { this.per_page = per_page; }
    }

    // Constructors
    public JourneyHistoryResponse() {
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Journey> getJourneys() {
        return journeys;
    }

    public void setJourneys(List<Journey> journeys) {
        this.journeys = journeys;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

