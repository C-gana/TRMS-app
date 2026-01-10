package com.cgana.trmsdriver.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Dashboard Cache Entity (Module 6 Part 1)
 * Stores dashboard state for offline viewing
 */
@Entity(tableName = "dashboard_cache")
public class DashboardCacheEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String vehicle_id;
    private String duty_status;
    private int passengers_today;
    private int revenue_today;
    private String seats_json; // JSON string of Seat[] array
    private long cached_at;

    // Constructors
    public DashboardCacheEntity() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getVehicle_id() { return vehicle_id; }
    public void setVehicle_id(String vehicle_id) { this.vehicle_id = vehicle_id; }

    public String getDuty_status() { return duty_status; }
    public void setDuty_status(String duty_status) { this.duty_status = duty_status; }

    public int getPassengers_today() { return passengers_today; }
    public void setPassengers_today(int passengers_today) { this.passengers_today = passengers_today; }

    public int getRevenue_today() { return revenue_today; }
    public void setRevenue_today(int revenue_today) { this.revenue_today = revenue_today; }

    public String getSeats_json() { return seats_json; }
    public void setSeats_json(String seats_json) { this.seats_json = seats_json; }

    public long getCached_at() { return cached_at; }
    public void setCached_at(long cached_at) { this.cached_at = cached_at; }
}

