package com.cgana.trmsdriver.data.model;

public class DutyStatusRequest {
    private String vehicle_id;
    private boolean on_duty;
    private Location location;

    // Constructor
    public DutyStatusRequest(String vehicle_id, boolean on_duty, Location location) {
        this.vehicle_id = vehicle_id;
        this.on_duty = on_duty;
        this.location = location;
    }

    // Getters
    public String getVehicleId() { return vehicle_id; }
    public boolean isOnDuty() { return on_duty; }
    public Location getLocation() { return location; }
}