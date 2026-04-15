package com.cgana.trmsownerapp.data.model;

import java.util.List;

public class RouteHistoryResponse {
    private String vehicle_id;
    private List<RoutePoint> route_points;
    private double total_distance_km;
    private String start_time;
    private String end_time;

    // Getters and Setters
    public String getVehicleId() {
        return vehicle_id;
    }

    public void setVehicleId(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public List<RoutePoint> getRoutePoints() {
        return route_points;
    }

    public void setRoutePoints(List<RoutePoint> route_points) {
        this.route_points = route_points;
    }

    public double getTotalDistanceKm() {
        return total_distance_km;
    }

    public void setTotalDistanceKm(double total_distance_km) {
        this.total_distance_km = total_distance_km;
    }

    public String getStartTime() {
        return start_time;
    }

    public void setStartTime(String start_time) {
        this.start_time = start_time;
    }

    public String getEndTime() {
        return end_time;
    }

    public void setEndTime(String end_time) {
        this.end_time = end_time;
    }
}

