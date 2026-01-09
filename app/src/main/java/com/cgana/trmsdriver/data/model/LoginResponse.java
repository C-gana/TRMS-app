package com.cgana.trmsdriver.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private boolean success;
    private String token;
    private Driver driver;

    private String message;

    // Constructor
    public LoginResponse() {}

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}