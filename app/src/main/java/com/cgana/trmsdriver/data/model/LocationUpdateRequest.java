package com.cgana.trmsdriver.data.model;

/**
 * Location Update Request (Module 6 Part 1)
 * Request model for sending location updates
 */
public class LocationUpdateRequest {
    private String vehicle_id;
    private double latitude;
    private double longitude;
    private long timestamp;

    public LocationUpdateRequest(String vehicle_id, double latitude, double longitude) {
        this.vehicle_id = vehicle_id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getVehicleId() {
        return vehicle_id;
    }

    public void setVehicleId(String vehicle_id) {
        this.vehicle_id = vehicle_id;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

