package com.cgana.trmsownerapp.data.model;

public class LoginResponse {
    private boolean success;
    private String token;
    private User user;
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public String getError() {
        return error;
    }
}

