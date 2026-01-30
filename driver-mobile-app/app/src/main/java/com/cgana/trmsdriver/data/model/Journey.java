package com.cgana.trmsdriver.data.model;

/**
 * Complete Journey model with all details (Module 5 Part 2)
 */
public class Journey {
    private long journey_id;
    private long passenger_id;
    private String passenger_name;
    private String passenger_phone;
    private int seat_number;
    private String destination;
    private int fare;
    private BoardingLocation boarding_location;
    private AlightingLocation alighting_location;
    private String boarding_time;
    private String alighting_time;
    private int duration_minutes;
    private double distance_km;
    private int fare_collected; // API returns 0 or 1, not boolean
    private String status;
    private String route_polyline;
    private String notes;

    // Inner classes for locations
    public static class BoardingLocation {
        private double latitude;
        private double longitude;
        private String address;

        // Getters and Setters
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class AlightingLocation {
        private double latitude;
        private double longitude;
        private String address;

        // Getters and Setters
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    // Constructor
    public Journey() {}

    // Getters and Setters
    public long getJourneyId() { return journey_id; }
    public void setJourneyId(long journey_id) { this.journey_id = journey_id; }

    public long getPassengerId() { return passenger_id; }
    public void setPassengerId(long passenger_id) { this.passenger_id = passenger_id; }

    public String getPassengerName() { return passenger_name; }
    public void setPassengerName(String passenger_name) { this.passenger_name = passenger_name; }

    public String getPassengerPhone() { return passenger_phone; }
    public void setPassengerPhone(String passenger_phone) { this.passenger_phone = passenger_phone; }

    public int getSeatNumber() { return seat_number; }
    public void setSeatNumber(int seat_number) { this.seat_number = seat_number; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public int getFare() { return fare; }
    public void setFare(int fare) { this.fare = fare; }

    public BoardingLocation getBoardingLocation() { return boarding_location; }
    public void setBoardingLocation(BoardingLocation boarding_location) {
        this.boarding_location = boarding_location;
    }

    public AlightingLocation getAlightingLocation() { return alighting_location; }
    public void setAlightingLocation(AlightingLocation alighting_location) {
        this.alighting_location = alighting_location;
    }

    public String getBoardingTime() { return boarding_time; }
    public void setBoardingTime(String boarding_time) { this.boarding_time = boarding_time; }

    public String getAlightingTime() { return alighting_time; }
    public void setAlightingTime(String alighting_time) { this.alighting_time = alighting_time; }

    public int getDurationMinutes() { return duration_minutes; }
    public void setDurationMinutes(int duration_minutes) { this.duration_minutes = duration_minutes; }

    public double getDistanceKm() { return distance_km; }
    public void setDistanceKm(double distance_km) { this.distance_km = distance_km; }

    public boolean isFareCollected() { return fare_collected == 1; }
    public void setFareCollected(int fare_collected) { this.fare_collected = fare_collected; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRoutePolyline() { return route_polyline; }
    public void setRoutePolyline(String route_polyline) { this.route_polyline = route_polyline; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

