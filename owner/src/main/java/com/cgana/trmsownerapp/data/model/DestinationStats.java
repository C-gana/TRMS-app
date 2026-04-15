package com.cgana.trmsownerapp.data.model;

public class DestinationStats {
    private String name;
    private int trip_count;
    private int revenue;
    private int avg_duration_minutes;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTripCount() {
        return trip_count;
    }

    public void setTripCount(int trip_count) {
        this.trip_count = trip_count;
    }

    public int getRevenue() {
        return revenue;
    }

    public void setRevenue(int revenue) {
        this.revenue = revenue;
    }

    public int getAvgDurationMinutes() {
        return avg_duration_minutes;
    }

    public void setAvgDurationMinutes(int avg_duration_minutes) {
        this.avg_duration_minutes = avg_duration_minutes;
    }
}

