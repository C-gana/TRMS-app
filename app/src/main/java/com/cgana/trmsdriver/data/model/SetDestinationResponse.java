package com.cgana.trmsdriver.data.model;

/**
 * Response model for setting journey destination (Module 3 Part 1)
 */
public class SetDestinationResponse {
    private boolean success;
    private long journey_id;
    private int seat_number;
    private String destination;
    private int fare;
    private int estimated_time_minutes;
    private double distance_km;
    private String status;
    private String message;

    // Constructors
    public SetDestinationResponse() {
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

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getFare() {
        return fare;
    }

    public void setFare(int fare) {
        this.fare = fare;
    }

    public int getEstimatedTimeMinutes() {
        return estimated_time_minutes;
    }

    public void setEstimatedTimeMinutes(int estimated_time_minutes) {
        this.estimated_time_minutes = estimated_time_minutes;
    }

    public double getDistanceKm() {
        return distance_km;
    }

    public void setDistanceKm(double distance_km) {
        this.distance_km = distance_km;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

