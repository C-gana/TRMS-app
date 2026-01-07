package com.cgana.trmsdriver.data.model;

public class Seat {
    private int seat_number;
    private boolean occupied;
    private Destination destination;
    private Integer fare;
    private Double distance_remaining;
    private Integer eta_minutes;

    // Getters and Setters
    public int getSeatNumber() {
        return seat_number;
    }

    public void setSeatNumber(int seat_number) {
        this.seat_number = seat_number;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Integer getFare() {
        return fare;
    }

    public void setFare(Integer fare) {
        this.fare = fare;
    }

    public Double getDistanceRemaining() {
        return distance_remaining;
    }

    public void setDistanceRemaining(Double distance_remaining) {
        this.distance_remaining = distance_remaining;
    }

    public Integer getEtaMinutes() {
        return eta_minutes;
    }

    public void setEtaMinutes(Integer eta_minutes) {
        this.eta_minutes = eta_minutes;
    }

    // Helper method to get seat status
    public String getStatus() {
        if (!occupied) {
            return "vacant";
        } else if (destination == null) {
            return "awaiting";
        } else if (distance_remaining != null && distance_remaining < 1.0) {
            return "approaching";
        } else {
            return "active";
        }
    }
}

