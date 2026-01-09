package com.cgana.trmsdriver.data.model;

/**
 * Request model for reporting missed stop (Module 4 Part 1)
 */
public class MissedStopRequest {
    private String vehicle_id;
    private long journey_id;
    private int seat_number;
    private CurrentLocation current_location;
    private String notes;

    public static class CurrentLocation {
        private double latitude;
        private double longitude;

        public CurrentLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    // Constructor
    public MissedStopRequest(String vehicle_id, long journey_id, int seat_number,
                            double latitude, double longitude, String notes) {
        this.vehicle_id = vehicle_id;
        this.journey_id = journey_id;
        this.seat_number = seat_number;
        this.current_location = new CurrentLocation(latitude, longitude);
        this.notes = notes;
    }

    // Getters and Setters
    public String getVehicleId() {
        return vehicle_id;
    }

    public void setVehicleId(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

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

    public CurrentLocation getCurrentLocation() {
        return current_location;
    }

    public void setCurrentLocation(CurrentLocation current_location) {
        this.current_location = current_location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

