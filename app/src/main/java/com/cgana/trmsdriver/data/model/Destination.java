package com.cgana.trmsdriver.data.model;

public class Destination {
    private int destination_id;
    private String name;
    private double latitude;
    private double longitude;
    private int fare_amount;
    private int alert_radius;
    private String status; // "active" or "inactive"

    // Constructor for creating new destination
    public Destination(String name, double latitude, double longitude, int fare_amount, int alert_radius) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fare_amount = fare_amount;
        this.alert_radius = alert_radius;
    }

    // Default constructor
    public Destination() {}

    // Getters and Setters
    public int getDestinationId() {
        return destination_id;
    }

    public void setDestinationId(int destination_id) {
        this.destination_id = destination_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getFareAmount() {
        return fare_amount;
    }

    public void setFareAmount(int fare_amount) {
        this.fare_amount = fare_amount;
    }

    public int getAlertRadius() {
        return alert_radius;
    }

    public void setAlertRadius(int alert_radius) {
        this.alert_radius = alert_radius;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
