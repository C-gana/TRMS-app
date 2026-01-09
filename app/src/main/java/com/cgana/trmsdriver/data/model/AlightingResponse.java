package com.cgana.trmsdriver.data.model;

/**
 * Response model for alighting API (Module 4 Part 1)
 */
public class AlightingResponse {
    private boolean success;
    private long journey_id;
    private int seat_number;
    private String status;
    private JourneySummary journey_summary;
    private String message;

    // Constructors
    public AlightingResponse() {
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getJourneyId() {
        return journey_id;
    }

    public void setJourneyId(long journey_id) {
        this.journey_id = journey_id;
    }

    public int getSeatNumber() {
        return seat_number;
    }

    public void setSeatNumber(int seat_number) {
        this.seat_number = seat_number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JourneySummary getJourneySummary() {
        return journey_summary;
    }

    public void setJourneySummary(JourneySummary journey_summary) {
        this.journey_summary = journey_summary;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

