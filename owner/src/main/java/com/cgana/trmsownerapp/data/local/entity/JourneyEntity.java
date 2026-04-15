package com.cgana.trmsownerapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.cgana.trmsownerapp.data.local.converters.Converters;
import com.cgana.trmsownerapp.data.model.Location;

import java.util.List;

@Entity(tableName = "journeys_cache")
@TypeConverters(Converters.class)
public class JourneyEntity {

    @PrimaryKey(autoGenerate = false)
    private int journey_id;

    private String vehicle_id;
    private String boarding_time;
    private Location boarding_location;
    private int seat_number;
    private String destination_name;
    private String alighting_time;
    private Location alighting_location;
    private int fare_collected;
    private double actual_distance;
    private int duration_minutes;
    private List<String> alerts;
    private long cached_at;

    public JourneyEntity() {
        this.cached_at = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getJourney_id() {
        return journey_id;
    }

    public void setJourney_id(int journey_id) {
        this.journey_id = journey_id;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getBoarding_time() {
        return boarding_time;
    }

    public void setBoarding_time(String boarding_time) {
        this.boarding_time = boarding_time;
    }

    public Location getBoarding_location() {
        return boarding_location;
    }

    public void setBoarding_location(Location boarding_location) {
        this.boarding_location = boarding_location;
    }

    public int getSeat_number() {
        return seat_number;
    }

    public void setSeat_number(int seat_number) {
        this.seat_number = seat_number;
    }

    public String getDestination_name() {
        return destination_name;
    }

    public void setDestination_name(String destination_name) {
        this.destination_name = destination_name;
    }

    public String getAlighting_time() {
        return alighting_time;
    }

    public void setAlighting_time(String alighting_time) {
        this.alighting_time = alighting_time;
    }

    public Location getAlighting_location() {
        return alighting_location;
    }

    public void setAlighting_location(Location alighting_location) {
        this.alighting_location = alighting_location;
    }

    public int getFare_collected() {
        return fare_collected;
    }

    public void setFare_collected(int fare_collected) {
        this.fare_collected = fare_collected;
    }

    public double getActual_distance() {
        return actual_distance;
    }

    public void setActual_distance(double actual_distance) {
        this.actual_distance = actual_distance;
    }

    public int getDuration_minutes() {
        return duration_minutes;
    }

    public void setDuration_minutes(int duration_minutes) {
        this.duration_minutes = duration_minutes;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<String> alerts) {
        this.alerts = alerts;
    }

    public long getCached_at() {
        return cached_at;
    }

    public void setCached_at(long cached_at) {
        this.cached_at = cached_at;
    }
}

