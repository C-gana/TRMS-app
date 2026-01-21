package com.cgana.trmsdriver.data.model;

/**
 * Response model for missed stop report (Module 4 Part 1)
 */
public class MissedStopResponse {
    private boolean success;
    private long journey_id;
    private boolean alert_created;
    private String message;

    // Constructors
    public MissedStopResponse() {
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

    public boolean isAlertCreated() {
        return alert_created;
    }

    public void setAlertCreated(boolean alert_created) {
        this.alert_created = alert_created;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

