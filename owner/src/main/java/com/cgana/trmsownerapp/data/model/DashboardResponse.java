package com.cgana.trmsownerapp.data.model;

import java.util.List;

public class DashboardResponse {
    private String vehicle_id;
    private String registration;
    private String status;
    private String last_seen;
    private Location current_location;
    private List<Seat> seats;
    private int active_journeys;
    private String driver_phone_number;

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

    public Location getCurrentLocation() {
        return current_location;
    }

    public void setCurrentLocation(Location current_location) {
        this.current_location = current_location;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public int getActiveJourneys() {
        return active_journeys;
    }

    public void setActiveJourneys(int active_journeys) {
        this.active_journeys = active_journeys;
    }

    public String getDriverPhoneNumber() {
        return driver_phone_number;
    }

    public void setDriverPhoneNumber(String driver_phone_number) {
        this.driver_phone_number = driver_phone_number;
    }
}

