package com.cgana.trmsownerapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.cgana.trmsownerapp.data.local.converters.Converters;
import com.cgana.trmsownerapp.data.model.Seat;

import java.util.List;

@Entity(tableName = "dashboard_cache")
@TypeConverters(Converters.class)
public class DashboardEntity {

    @PrimaryKey
    @NonNull
    private String vehicle_id;

    private String registration;
    private String status;
    private String last_seen;
    private double current_latitude;
    private double current_longitude;
    private List<Seat> seats;
    private int active_journeys;
    private long cached_at; // Timestamp when cached

    // Constructor
    public DashboardEntity() {
        this.cached_at = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
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

    public String getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(String last_seen) {
        this.last_seen = last_seen;
    }

    public double getCurrent_latitude() {
        return current_latitude;
    }

    public void setCurrent_latitude(double current_latitude) {
        this.current_latitude = current_latitude;
    }

    public double getCurrent_longitude() {
        return current_longitude;
    }

    public void setCurrent_longitude(double current_longitude) {
        this.current_longitude = current_longitude;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public int getActive_journeys() {
        return active_journeys;
    }

    public void setActive_journeys(int active_journeys) {
        this.active_journeys = active_journeys;
    }

    public long getCached_at() {
        return cached_at;
    }

    public void setCached_at(long cached_at) {
        this.cached_at = cached_at;
    }
}

