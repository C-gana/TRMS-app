package com.cgana.trmsdriver.data.model;

/**
 * Request model for reporting missed stop (Module 4 Part 1)
 */
public class MissedStopRequest {
    private String vehicle_id;
    private long journey_id;
    private int seat_number;
    private Location current_location;
    private String notes;

    // Constructor
    public MissedStopRequest(String vehicle_id, long journey_id, int seat_number,
                            Location current_location, String notes) {
        this.vehicle_id = vehicle_id;
        this.journey_id = journey_id;
        this.seat_number = seat_number;
        this.current_location = current_location;
        this.notes = notes;
    }

    // Getters and Setters
    public String getVehicleId() {
        return vehicle_id;
    }

    public void setVehicleId(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public long getJourneyId() {
        return journey_id;
    }

    public void setJourneyId(long journey_id) {
        this.journey_id = journey_id;
    }

    public int getSeatNumber() {
        return seat_number;
    }

    public void setSeatNumber(int seat_number) {
        this.seat_number = seat_number;
    }

    public Location getCurrentLocation() {
        return current_location;
    }

    public void setCurrentLocation(Location current_location) {
        this.current_location = current_location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

