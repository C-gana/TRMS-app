package com.cgana.trmsownerapp.data.model;

public class DriverPerformance {
    private double destination_selection_compliance;
    private int missed_stops;
    private int avg_selection_time_seconds;

    // Getters and Setters
    public double getDestinationSelectionCompliance() {
        return destination_selection_compliance;
    }

    public void setDestinationSelectionCompliance(double destination_selection_compliance) {
        this.destination_selection_compliance = destination_selection_compliance;
    }

    public int getMissedStops() {
        return missed_stops;
    }

    public void setMissedStops(int missed_stops) {
        this.missed_stops = missed_stops;
    }

    public int getAvgSelectionTimeSeconds() {
        return avg_selection_time_seconds;
    }

    public void setAvgSelectionTimeSeconds(int avg_selection_time_seconds) {
        this.avg_selection_time_seconds = avg_selection_time_seconds;
    }
}

