package com.cgana.trmsdriver.data.model;

public class GenericResponse {
    private boolean success;
    private String message;

    // Constructor
    public GenericResponse() {}

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}