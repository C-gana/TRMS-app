package com.cgana.trmsdriver.data.model;

import java.util.List;

public class DashboardResponse {
    private String vehicle_id;
    private String registration;
    private String status;
    private String last_updated;
    private List<SeatStatus> seats;
    private int active_journeys;
    private TodaysStats todays_stats;

    public static class TodaysStats {
        private int passengers;
        private int revenue;

        public int getPassengers() {
            return passengers;
        }

        public void setPassengers(int passengers) {
            this.passengers = passengers;
        }

        public int getRevenue() {
            return revenue;
        }

        public void setRevenue(int revenue) {
            this.revenue = revenue;
        }
    }

    // Getters and Setters
    public String getVehicleId() {
        return vehicle_id;
    }

    public void setVehicleId(String vehicle_id) {
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

    public String getLastUpdated() {
        return last_updated;
    }

    public void setLastUpdated(String last_updated) {
        this.last_updated = last_updated;
    }

    public List<SeatStatus> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatStatus> seats) {
        this.seats = seats;
    }

    public int getActiveJourneys() {
        return active_journeys;
    }

    public void setActiveJourneys(int active_journeys) {
        this.active_journeys = active_journeys;
    }

    public TodaysStats getTodaysStats() {
        return todays_stats;
    }

    public void setTodaysStats(TodaysStats todays_stats) {
        this.todays_stats = todays_stats;
    }
}

