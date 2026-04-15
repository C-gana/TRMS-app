package com.cgana.trmsownerapp.data.model;

import java.util.List;

public class Journey {
    private int journey_id;
    private String boarding_time;
    private Location boarding_location;
    private int seat_number;
    private String destination_name;
    private String alighting_time;
    private Location alighting_location;
    private int fare_collected;
    private double actual_distance;
    private int duration_minutes;
    private List<String> alerts;

    // Getters and Setters
    public int getJourneyId() {
        return journey_id;
    }

    public void setJourneyId(int journey_id) {
        this.journey_id = journey_id;
    }

    public String getBoardingTime() {
        return boarding_time;
    }

    public void setBoardingTime(String boarding_time) {
        this.boarding_time = boarding_time;
    }

    public Location getBoardingLocation() {
        return boarding_location;
    }

    public void setBoardingLocation(Location boarding_location) {
        this.boarding_location = boarding_location;
    }

    public int getSeatNumber() {
        return seat_number;
    }

    public void setSeatNumber(int seat_number) {
        this.seat_number = seat_number;
    }

    public String getDestinationName() {
        return destination_name;
    }

    public void setDestinationName(String destination_name) {
        this.destination_name = destination_name;
    }

    public String getAlightingTime() {
        return alighting_time;
    }

    public void setAlightingTime(String alighting_time) {
        this.alighting_time = alighting_time;
    }

    public Location getAlightingLocation() {
        return alighting_location;
    }

    public void setAlightingLocation(Location alighting_location) {
        this.alighting_location = alighting_location;
    }

    public int getFareCollected() {
        return fare_collected;
    }

    public void setFareCollected(int fare_collected) {
        this.fare_collected = fare_collected;
    }

    public double getActualDistance() {
        return actual_distance;
    }

    public void setActualDistance(double actual_distance) {
        this.actual_distance = actual_distance;
    }

    public int getDurationMinutes() {
        return duration_minutes;
    }

    public void setDurationMinutes(int duration_minutes) {
        this.duration_minutes = duration_minutes;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<String> alerts) {
        this.alerts = alerts;
    }
}

