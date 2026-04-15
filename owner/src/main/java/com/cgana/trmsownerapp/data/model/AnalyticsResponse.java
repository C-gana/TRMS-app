package com.cgana.trmsownerapp.data.model;

import java.util.List;

public class AnalyticsResponse {
    private String period;
    private int total_journeys;
    private int total_revenue;
    private List<DestinationStats> destinations;
    private List<PeakHour> peak_hours;
    private DriverPerformance driver_performance;

    // Getters and Setters
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getTotalJourneys() {
        return total_journeys;
    }

    public void setTotalJourneys(int total_journeys) {
        this.total_journeys = total_journeys;
    }

    public int getTotalRevenue() {
        return total_revenue;
    }

    public void setTotalRevenue(int total_revenue) {
        this.total_revenue = total_revenue;
    }

    public List<DestinationStats> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<DestinationStats> destinations) {
        this.destinations = destinations;
    }

    public List<PeakHour> getPeakHours() {
        return peak_hours;
    }

    public void setPeakHours(List<PeakHour> peak_hours) {
        this.peak_hours = peak_hours;
    }

    public DriverPerformance getDriverPerformance() {
        return driver_performance;
    }

    public void setDriverPerformance(DriverPerformance driver_performance) {
        this.driver_performance = driver_performance;
    }

    // Helper method to get top destination
    public String getTopDestination() {
        if (destinations != null && !destinations.isEmpty()) {
            DestinationStats top = destinations.get(0);
            for (DestinationStats dest : destinations) {
                if (dest.getTripCount() > top.getTripCount()) {
                    top = dest;
                }
            }
            return top.getName() + " (" + top.getTripCount() + " trips)";
        }
        return "N/A";
    }

    // Helper method to get average journey duration
    public int getAvgJourneyDuration() {
        if (destinations != null && !destinations.isEmpty()) {
            int totalDuration = 0;
            int count = 0;
            for (DestinationStats dest : destinations) {
                totalDuration += dest.getAvgDurationMinutes() * dest.getTripCount();
                count += dest.getTripCount();
            }
            return count > 0 ? totalDuration / count : 0;
        }
        return 0;
    }
}

