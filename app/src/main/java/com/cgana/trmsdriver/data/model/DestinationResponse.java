package com.cgana.trmsdriver.data.model;

import java.util.List;

/**
 * Response model for destinations list API (Module 3 Part 1)
 */
public class DestinationResponse {
    private boolean success;
    private List<Destination> destinations;
    private int total;
    private String message;

    // Constructors
    public DestinationResponse() {
    }

    public DestinationResponse(boolean success, List<Destination> destinations, int total, String message) {
        this.success = success;
        this.destinations = destinations;
        this.total = total;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

