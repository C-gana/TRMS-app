package com.cgana.trmsownerapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "destinations_cache")
public class DestinationEntity {

    @PrimaryKey(autoGenerate = false)
    private int destination_id;

    private String vehicle_id;
    private String name;
    private double latitude;
    private double longitude;
    private int fare_amount;
    private int alert_radius;
    private String status;
    private long cached_at;

    public DestinationEntity() {
        this.cached_at = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getDestination_id() {
        return destination_id;
    }

    public void setDestination_id(int destination_id) {
        this.destination_id = destination_id;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
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

    public int getFare_amount() {
        return fare_amount;
    }

    public void setFare_amount(int fare_amount) {
        this.fare_amount = fare_amount;
    }

    public int getAlert_radius() {
        return alert_radius;
    }

    public void setAlert_radius(int alert_radius) {
        this.alert_radius = alert_radius;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCached_at() {
        return cached_at;
    }

    public void setCached_at(long cached_at) {
        this.cached_at = cached_at;
    }
}

