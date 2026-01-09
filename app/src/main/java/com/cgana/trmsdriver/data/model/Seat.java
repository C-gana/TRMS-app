package com.cgana.trmsdriver.data.model;

public class Seat {
    private int seat_number;
    private String status; // "vacant", "awaiting_destination", "active_journey", "approaching_destination"
    private Long journey_id;
    private String destination;
    private Integer fare;
    private String boarding_time;
    private Double distance_to_destination;
    private Integer eta_minutes;
    private Integer timeout_seconds; // for awaiting_destination state
    private Boolean alert; // for approaching_destination

    // Getters and Setters
    public int getSeat_number() {
        return seat_number;
    }

    public void setSeat_number(int seat_number) {
        this.seat_number = seat_number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getJourney_id() {
        return journey_id;
    }

    public void setJourney_id(Long journey_id) {
        this.journey_id = journey_id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Integer getFare() {
        return fare;
    }

    public void setFare(Integer fare) {
        this.fare = fare;
    }

    public String getBoarding_time() {
        return boarding_time;
    }

    public void setBoarding_time(String boarding_time) {
        this.boarding_time = boarding_time;
    }

    public Double getDistance_to_destination() {
        return distance_to_destination;
    }

    public void setDistance_to_destination(Double distance_to_destination) {
        this.distance_to_destination = distance_to_destination;
    }

    public Integer getEta_minutes() {
        return eta_minutes;
    }

    public void setEta_minutes(Integer eta_minutes) {
        this.eta_minutes = eta_minutes;
    }

    public Integer getTimeout_seconds() {
        return timeout_seconds;
    }

    public void setTimeout_seconds(Integer timeout_seconds) {
        this.timeout_seconds = timeout_seconds;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    // Helper methods
    public boolean isVacant() {
        return "vacant".equals(status);
    }

    public boolean isAwaiting() {
        return "awaiting_destination".equals(status);
    }

    public boolean isActive() {
        return "active_journey".equals(status);
    }

    public boolean isApproaching() {
        return "approaching_destination".equals(status);
    }

    public boolean hasAlert() {
        return alert != null && alert;
    }
}
