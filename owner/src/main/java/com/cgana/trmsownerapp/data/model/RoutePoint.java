package com.cgana.trmsownerapp.data.model;

public class RoutePoint {
    private double latitude;
    private double longitude;
    private String timestamp;
    private double speed; // km/h

    public RoutePoint(double latitude, double longitude, String timestamp, double speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.speed = speed;
    }

    // Getters
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getSpeed() {
        return speed;
    }

    // Setters
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}

