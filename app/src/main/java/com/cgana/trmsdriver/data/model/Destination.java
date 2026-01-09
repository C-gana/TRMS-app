package com.cgana.trmsdriver.data.model;

/**
 * Destination model (Module 3 Part 1)
 * Represents a destination with fare, distance, and ETA information
 */
public class Destination {
    private int destination_id;
    private String name;
    private int fare;
    private double distance_km;
    private int estimated_time_minutes;
    private int alert_radius;
    private String status;

    // Constructors
    public Destination() {
    }

    public Destination(int destination_id, String name, int fare, double distance_km,
                      int estimated_time_minutes, int alert_radius, String status) {
        this.destination_id = destination_id;
        this.name = name;
        this.fare = fare;
        this.distance_km = distance_km;
        this.estimated_time_minutes = estimated_time_minutes;
        this.alert_radius = alert_radius;
        this.status = status;
    }

    // Getters and Setters
    public int getDestinationId() {
        return destination_id;
    }

    public void setDestinationId(int destination_id) {
        this.destination_id = destination_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFare() {
        return fare;
    }

    public void setFare(int fare) {
        this.fare = fare;
    }

    public double getDistanceKm() {
        return distance_km;
    }

    public void setDistanceKm(double distance_km) {
        this.distance_km = distance_km;
    }

    public int getEstimatedTimeMinutes() {
        return estimated_time_minutes;
    }

    public void setEstimatedTimeMinutes(int estimated_time_minutes) {
        this.estimated_time_minutes = estimated_time_minutes;
    }

    public int getAlertRadius() {
        return alert_radius;
    }

    public void setAlertRadius(int alert_radius) {
        this.alert_radius = alert_radius;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper methods
    public String getFormattedFare() {
        return String.format("%,d MK", fare);
    }

    public String getFormattedDistance() {
        return String.format("%.1fkm", distance_km);
    }

    public String getFormattedTime() {
        return String.format("%dmin", estimated_time_minutes);
    }

    public String getFormattedDetails() {
        return String.format("%s · %s", getFormattedDistance(), getFormattedTime());
    }

    @Override
    public String toString() {
        return "Destination{" +
                "destination_id=" + destination_id +
                ", name='" + name + '\'' +
                ", fare=" + fare +
                ", distance_km=" + distance_km +
                ", estimated_time_minutes=" + estimated_time_minutes +
                ", status='" + status + '\'' +
                '}';
    }
}

