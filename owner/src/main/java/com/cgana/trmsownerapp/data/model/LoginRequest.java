package com.cgana.trmsownerapp.data.model;

public class LoginRequest {
    private String phone_number;
    private String password;
    private String fcm_token;

    public LoginRequest(String phone_number, String password, String fcm_token) {
        this.phone_number = phone_number;
        this.password = password;
        this.fcm_token = fcm_token;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public String getPassword() {
        return password;
    }

    public String getFcmToken() {
        return fcm_token;
    }
}

