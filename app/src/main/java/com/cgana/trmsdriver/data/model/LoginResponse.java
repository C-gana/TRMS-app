package com.cgana.trmsdriver.data.model;

public class LoginResponse {
    private boolean success;
    private String token;
    private Driver driver;
    private String error;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getToken() {
        return token;
    }

    public Driver getUser() {
        return driver;
    }

    public String getError() {
        return error != null ? error : message;
    }
}
