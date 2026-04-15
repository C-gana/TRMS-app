package com.cgana.trmsownerapp.data.model;

import com.cgana.trmsownerapp.R;

public class Alert {
    private int alert_id;
    private String alert_type; // "timeout", "proximity", "missed_stop", "confirmed_missed_stop"
    private String severity; // "low", "medium", "high"
    private int journey_id;
    private int seat_number;
    private String destination_name;
    private String message;
    private String created_at; // ISO 8601 timestamp
    private boolean acknowledged;
    private String acknowledged_by;
    private String acknowledged_at;
    private String notes;

    // Getters and Setters
    public int getAlertId() {
        return alert_id;
    }

    public void setAlertId(int alert_id) {
        this.alert_id = alert_id;
    }

    public String getAlertType() {
        return alert_type;
    }

    public void setAlertType(String alert_type) {
        this.alert_type = alert_type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public int getJourneyId() {
        return journey_id;
    }

    public void setJourneyId(int journey_id) {
        this.journey_id = journey_id;
    }

    public int getSeatNumber() {
        return seat_number;
    }

    public void setSeatNumber(int seat_number) {
        this.seat_number = seat_number;
    }

    public String getDestinationName() {
        return destination_name;
    }

    public void setDestinationName(String destination_name) {
        this.destination_name = destination_name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public String getAcknowledgedBy() {
        return acknowledged_by;
    }

    public void setAcknowledgedBy(String acknowledged_by) {
        this.acknowledged_by = acknowledged_by;
    }

    public String getAcknowledgedAt() {
        return acknowledged_at;
    }

    public void setAcknowledgedAt(String acknowledged_at) {
        this.acknowledged_at = acknowledged_at;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper methods
    public String getAlertIcon() {
        if (alert_type == null) return "ℹ️";
        switch (alert_type) {
            case "timeout":
                return "⏱️";
            case "proximity":
                return "📍";
            case "missed_stop":
                return "⚠️";
            case "confirmed_missed_stop":
                return "🚨";
            default:
                return "ℹ️";
        }
    }

    public int getSeverityColor() {
        if (severity == null) return R.color.text_secondary;
        switch (severity) {
            case "low":
                return R.color.success;
            case "medium":
                return R.color.warning;
            case "high":
                return R.color.danger;
            default:
                return R.color.text_secondary;
        }
    }

    public String getAlertTitle() {
        if (alert_type == null) return "Alert";
        switch (alert_type) {
            case "timeout":
                return "Timeout Alert";
            case "proximity":
                return "Proximity Alert";
            case "missed_stop":
                return "Missed Stop Alert";
            case "confirmed_missed_stop":
                return "Confirmed Missed Stop";
            default:
                return "Alert";
        }
    }
}

