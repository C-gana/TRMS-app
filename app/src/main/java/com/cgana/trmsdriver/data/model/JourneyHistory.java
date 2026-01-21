package com.cgana.trmsdriver.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Journey History model (Module 5 Part 1)
 * Represents a completed journey
 */
public class JourneyHistory {
    private long journey_id;
    private int seat_number;
    private String destination;
    private String boarding_time;
    private String alighting_time;
    private int duration_minutes;
    private double distance_km;
    private int fare;
    private int fare_collected; // API returns 0 or 1, not boolean

    // Constructors
    public JourneyHistory() {
    }

    public JourneyHistory(long journey_id, int seat_number, String destination,
                         String boarding_time, String alighting_time,
                         int duration_minutes, double distance_km,
                         int fare, int fare_collected) {
        this.journey_id = journey_id;
        this.seat_number = seat_number;
        this.destination = destination;
        this.boarding_time = boarding_time;
        this.alighting_time = alighting_time;
        this.duration_minutes = duration_minutes;
        this.distance_km = distance_km;
        this.fare = fare;
        this.fare_collected = fare_collected;
    }

    // Getters and Setters
    public long getJourneyId() {
        return journey_id;
    }

    public void setJourneyId(long journey_id) {
        this.journey_id = journey_id;
    }

    public int getSeatNumber() {
        return seat_number;
    }

    public void setSeatNumber(int seat_number) {
        this.seat_number = seat_number;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getBoardingTime() {
        return boarding_time;
    }

    public void setBoardingTime(String boarding_time) {
        this.boarding_time = boarding_time;
    }

    public String getAlightingTime() {
        return alighting_time;
    }

    public void setAlightingTime(String alighting_time) {
        this.alighting_time = alighting_time;
    }

    public int getDurationMinutes() {
        return duration_minutes;
    }

    public void setDurationMinutes(int duration_minutes) {
        this.duration_minutes = duration_minutes;
    }

    public double getDistanceKm() {
        return distance_km;
    }

    public void setDistanceKm(double distance_km) {
        this.distance_km = distance_km;
    }

    public int getFare() {
        return fare;
    }

    public void setFare(int fare) {
        this.fare = fare;
    }

    public boolean isFareCollected() {
        return fare_collected == 1;
    }

    public void setFareCollected(int fare_collected) {
        this.fare_collected = fare_collected;
    }

    // Helper methods for formatting (Module 5 Part 1)

    /**
     * Format fare with currency
     */
    public String getFormattedFare() {
        return String.format(Locale.getDefault(), "%,d MK", fare);
    }

    /**
     * Format distance
     */
    public String getFormattedDistance() {
        return String.format(Locale.getDefault(), "%.1f km", distance_km);
    }

    /**
     * Format duration
     */
    public String getFormattedDuration() {
        return duration_minutes + " minutes";
    }

    /**
     * Format time info: "08:15 → 08:23 (8 min)"
     */
    public String getFormattedTimeInfo() {
        String boarding = formatTime(boarding_time);
        String alighting = formatTime(alighting_time);
        return String.format(Locale.getDefault(), "%s → %s (%d min)",
                           boarding, alighting, duration_minutes);
    }

    /**
     * Format date/time for display
     */
    public String getFormattedDateTime() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy - hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(boarding_time);
            return date != null ? outputFormat.format(date) : boarding_time;
        } catch (Exception e) {
            return boarding_time;
        }
    }

    /**
     * Helper to format time string
     */
    private String formatTime(String timeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(timeString);
            return date != null ? outputFormat.format(date) : timeString;
        } catch (Exception e) {
            return timeString;
        }
    }
}

