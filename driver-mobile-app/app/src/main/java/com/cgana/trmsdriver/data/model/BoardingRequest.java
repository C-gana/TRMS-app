package com.cgana.trmsdriver.data.model;

public class BoardingRequest {
    private String vehicle_id;
    private int seat_number;
    private BoardingLocation boarding_location;

    public static class BoardingLocation {
        private double latitude;
        private double longitude;

        public BoardingLocation(double latitude, double longitude) {
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

    public BoardingRequest(String vehicle_id, int seat_number, double latitude, double longitude) {
        this.vehicle_id = vehicle_id;
        this.seat_number = seat_number;
        this.boarding_location = new BoardingLocation(latitude, longitude);
    }

    public String getVehicleId() {
        return vehicle_id;
    }

    public void setVehicleId(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public int getSeatNumber() {
        return seat_number;
    }

    public void setSeatNumber(int seat_number) {
        this.seat_number = seat_number;
    }

    public BoardingLocation getBoardingLocation() {
        return boarding_location;
    }

    public void setBoardingLocation(BoardingLocation boarding_location) {
        this.boarding_location = boarding_location;
    }
}

