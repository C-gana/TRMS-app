package com.cgana.trmsdriver.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Destinations Cache Entity (Module 6 Part 1)
 * Stores destinations for offline selection
 */
@Entity(tableName = "destinations_cache")
public class DestinationCacheEntity {

    @PrimaryKey
    private int destination_id;

    private String vehicle_id;
    private String name;
    private int fare;
    private double distance_km;
    private int estimated_time_minutes;
    private int alert_radius;
    private String status;
    private long cached_at;

    // Constructors
    public DestinationCacheEntity() {}

    // Getters and Setters
    public int getDestination_id() { return destination_id; }
    public void setDestination_id(int destination_id) { this.destination_id = destination_id; }

    public String getVehicle_id() { return vehicle_id; }
    public void setVehicle_id(String vehicle_id) { this.vehicle_id = vehicle_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getFare() { return fare; }
    public void setFare(int fare) { this.fare = fare; }

    public double getDistance_km() { return distance_km; }
    public void setDistance_km(double distance_km) { this.distance_km = distance_km; }

    public int getEstimated_time_minutes() { return estimated_time_minutes; }
    public void setEstimated_time_minutes(int estimated_time_minutes) {
        this.estimated_time_minutes = estimated_time_minutes;
    }

    public int getAlert_radius() { return alert_radius; }
    public void setAlert_radius(int alert_radius) { this.alert_radius = alert_radius; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCached_at() { return cached_at; }
    public void setCached_at(long cached_at) { this.cached_at = cached_at; }
}

