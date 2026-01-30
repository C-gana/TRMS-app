package com.cgana.trmsdriver.data.model;

/**
 * Location Update Response (Module 6 Part 1)
 * Response model for location update API calls
 */
public class LocationUpdateResponse {
    private boolean success;
    private String message;
    private LocationData location;

    // Inner class for location data
    public static class LocationData {
        private String vehicle_id;
        private double latitude;
        private double longitude;
        private long timestamp;
        private String updated_at;

        // Getters and Setters
        public String getVehicleId() {
            return vehicle_id;
        }

        public void setVehicleId(String vehicle_id) {
            this.vehicle_id = vehicle_id;
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

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getUpdatedAt() {
            return updated_at;
        }

        public void setUpdatedAt(String updated_at) {
            this.updated_at = updated_at;
        }
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocationData getLocation() {
        return location;
    }

    public void setLocation(LocationData location) {
        this.location = location;
    }
}

