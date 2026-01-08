package com.cgana.trmsdriver.data.model;

public class BoardingResponse {
    private boolean success;
    private Long journey_id;
    private int seat_number;
    private String status;
    private String boarding_time;
    private String message;

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getJourneyId() {
        return journey_id;
    }

    public void setJourneyId(Long journey_id) {
        this.journey_id = journey_id;
    }

    public int getSeatNumber() {
        return seat_number;
    }

    public void setSeatNumber(int seat_number) {
        this.seat_number = seat_number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBoardingTime() {
        return boarding_time;
    }

    public void setBoardingTime(String boarding_time) {
        this.boarding_time = boarding_time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

