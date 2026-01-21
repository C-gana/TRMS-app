package com.cgana.trmsdriver.data.model;

/**
 * Journey Detail Response model (Module 5 Part 2)
 */
public class JourneyDetailResponse {
    private boolean success;
    private Journey journey;
    private String message;

    // Constructor
    public JourneyDetailResponse() {}

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Journey getJourney() { return journey; }
    public void setJourney(Journey journey) { this.journey = journey; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

