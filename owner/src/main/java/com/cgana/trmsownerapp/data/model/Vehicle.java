package com.cgana.trmsownerapp.data.model;

public class Vehicle {
    private String vehicle_id;
    private String registration;
    private String status;
    private String last_seen;

    public Vehicle(String vehicle_id, String registration, String status, String last_seen) {
        this.vehicle_id = vehicle_id;
        this.registration = registration;
        this.status = status;
        this.last_seen = last_seen;
    }

    // Getters and Setters
    public String getVehicleId() {
        return vehicle_id;
    }

    public void setVehicleId(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastSeen() {
        return last_seen;
    }

    public void setLastSeen(String last_seen) {
        this.last_seen = last_seen;
    }
}

