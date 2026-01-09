package com.cgana.trmsdriver.data.model;

/**
 * Request model for recording passenger alighting (Module 4 Part 1)
 */
public class AlightingRequest {
    private String vehicle_id;
    private long journey_id;
    private int seat_number;
    private AlightingLocation alighting_location;
    private boolean fare_collected;
    private boolean missed_stop;

    public static class AlightingLocation {
        private double latitude;
        private double longitude;

        public AlightingLocation(double latitude, double longitude) {
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
    public AlightingRequest(String vehicle_id, long journey_id, int seat_number,
                           double latitude, double longitude, boolean fare_collected,
                           boolean missed_stop) {
        this.vehicle_id = vehicle_id;
        this.journey_id = journey_id;
        this.seat_number = seat_number;
        this.alighting_location = new AlightingLocation(latitude, longitude);
        this.fare_collected = fare_collected;
        this.missed_stop = missed_stop;
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

    public AlightingLocation getAlightingLocation() {
        return alighting_location;
    }

    public void setAlightingLocation(AlightingLocation alighting_location) {
        this.alighting_location = alighting_location;
    }

    public boolean isFareCollected() {
        return fare_collected;
    }

    public void setFareCollected(boolean fare_collected) {
        this.fare_collected = fare_collected;
    }

    public boolean isMissedStop() {
        return missed_stop;
    }

    public void setMissedStop(boolean missed_stop) {
        this.missed_stop = missed_stop;
    }
}

