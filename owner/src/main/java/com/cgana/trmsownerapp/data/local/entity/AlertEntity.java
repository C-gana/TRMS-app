package com.cgana.trmsownerapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alerts_cache")
public class AlertEntity {

    @PrimaryKey(autoGenerate = false)
    private int alert_id;

    private String vehicle_id;
    private String alert_type;
    private String severity;
    private int journey_id;
    private int seat_number;
    private String destination_name;
    private String message;
    private String created_at;
    private boolean acknowledged;
    private String acknowledged_by;
    private String acknowledged_at;
    private String notes;
    private long cached_at;

    public AlertEntity() {
        this.cached_at = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getAlert_id() {
        return alert_id;
    }

    public void setAlert_id(int alert_id) {
        this.alert_id = alert_id;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getAlert_type() {
        return alert_type;
    }

    public void setAlert_type(String alert_type) {
        this.alert_type = alert_type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public int getJourney_id() {
        return journey_id;
    }

    public void setJourney_id(int journey_id) {
        this.journey_id = journey_id;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public String getAcknowledged_by() {
        return acknowledged_by;
    }

    public void setAcknowledged_by(String acknowledged_by) {
        this.acknowledged_by = acknowledged_by;
    }

    public String getAcknowledged_at() {
        return acknowledged_at;
    }

    public void setAcknowledged_at(String acknowledged_at) {
        this.acknowledged_at = acknowledged_at;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCached_at() {
        return cached_at;
    }

    public void setCached_at(long cached_at) {
        this.cached_at = cached_at;
    }
}

