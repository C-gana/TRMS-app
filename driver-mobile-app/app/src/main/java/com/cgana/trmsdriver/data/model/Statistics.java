package com.cgana.trmsdriver.data.model;

/**
 * Statistics model (Module 5 Part 2)
 */
public class Statistics {
    private int total_journeys;
    private int total_passengers;
    private int total_revenue;
    private double total_distance_km;
    private int total_duration_minutes;
    private int average_journey_duration;
    private int average_fare;
    private int fare_collected_count;
    private int fare_not_collected_count;
    private double fare_collection_rate;
    private String busiest_destination;
    private String busiest_hour;

    // Constructor
    public Statistics() {}

    // Getters and Setters
    public int getTotalJourneys() { return total_journeys; }
    public void setTotalJourneys(int total_journeys) { this.total_journeys = total_journeys; }

    public int getTotalPassengers() { return total_passengers; }
    public void setTotalPassengers(int total_passengers) { this.total_passengers = total_passengers; }

    public int getTotalRevenue() { return total_revenue; }
    public void setTotalRevenue(int total_revenue) { this.total_revenue = total_revenue; }

    public double getTotalDistanceKm() { return total_distance_km; }
    public void setTotalDistanceKm(double total_distance_km) {
        this.total_distance_km = total_distance_km;
    }

    public int getTotalDurationMinutes() { return total_duration_minutes; }
    public void setTotalDurationMinutes(int total_duration_minutes) {
        this.total_duration_minutes = total_duration_minutes;
    }

    public int getAverageJourneyDuration() { return average_journey_duration; }
    public void setAverageJourneyDuration(int average_journey_duration) {
        this.average_journey_duration = average_journey_duration;
    }

    public int getAverageFare() { return average_fare; }
    public void setAverageFare(int average_fare) { this.average_fare = average_fare; }

    public int getFareCollectedCount() { return fare_collected_count; }
    public void setFareCollectedCount(int fare_collected_count) {
        this.fare_collected_count = fare_collected_count;
    }

    public int getFareNotCollectedCount() { return fare_not_collected_count; }
    public void setFareNotCollectedCount(int fare_not_collected_count) {
        this.fare_not_collected_count = fare_not_collected_count;
    }

    public double getFareCollectionRate() { return fare_collection_rate; }
    public void setFareCollectionRate(double fare_collection_rate) {
        this.fare_collection_rate = fare_collection_rate;
    }

    public String getBusiestDestination() { return busiest_destination; }
    public void setBusiestDestination(String busiest_destination) {
        this.busiest_destination = busiest_destination;
    }

    public String getBusiestHour() { return busiest_hour; }
    public void setBusiestHour(String busiest_hour) { this.busiest_hour = busiest_hour; }
}

