package com.cgana.trmsownerapp.data.model;

public class DestinationRequest {
    private String name;
    private double latitude;
    private double longitude;
    private int fare_amount;
    private int alert_radius;

    public DestinationRequest(String name, double latitude, double longitude, int fare_amount, int alert_radius) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fare_amount = fare_amount;
        this.alert_radius = alert_radius;
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getFareAmount() {
        return fare_amount;
    }

    public int getAlertRadius() {
        return alert_radius;
    }
}

