package com.cgana.trmsdriver.data.model;

/**
 * Journey Summary model (Module 4 Part 1)
 */
public class JourneySummary {
    private long passenger_id;
    private String destination;
    private int fare;
    private String boarding_time;
    private String alighting_time;
    private int duration_minutes;
    private double distance_km;
    private boolean fare_collected;

    // Constructors
    public JourneySummary() {
    }

    // Getters and Setters
    public long getPassengerId() {
        return passenger_id;
    }

    public void setPassengerId(long passenger_id) {
        this.passenger_id = passenger_id;
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

    public String getBoardingTime() {
        return boarding_time;
    }

    public void setBoardingTime(String boarding_time) {
        this.boarding_time = boarding_time;
    }

    public String getAlightingTime() {
        return alighting_time;
    }

    public void setAlightingTime(String alighting_time) {
        this.alighting_time = alighting_time;
    }

    public int getDurationMinutes() {
        return duration_minutes;
    }

    public void setDurationMinutes(int duration_minutes) {
        this.duration_minutes = duration_minutes;
    }

    public double getDistanceKm() {
        return distance_km;
    }

    public void setDistanceKm(double distance_km) {
        this.distance_km = distance_km;
    }

    public boolean isFareCollected() {
        return fare_collected;
    }

    public void setFareCollected(boolean fare_collected) {
        this.fare_collected = fare_collected;
    }

    // Helper methods
    public String getFormattedFare() {
        return String.format("%,d MK", fare);
    }

    public String getFormattedDistance() {
        return String.format("%.1f km", distance_km);
    }

    public String getFormattedDuration() {
        return duration_minutes + " minutes";
    }
}

