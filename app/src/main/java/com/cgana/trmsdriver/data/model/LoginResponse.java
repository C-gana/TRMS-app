package com.cgana.trmsdriver.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private boolean success;
    private String token;
    private Driver driver;
    private User user;

    private String message;
    private String error;

    // Constructor
    public LoginResponse() {}

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getRole() {
        if (driver != null && driver.getRole() != null && !driver.getRole().trim().isEmpty()) {
            return driver.getRole().trim();
        }
        if (user != null && user.getRole() != null && !user.getRole().trim().isEmpty()) {
            return user.getRole().trim();
        }
        return null;
    }
}
