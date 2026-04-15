package com.cgana.trmsownerapp.data.model;

public class FCMTokenRequest {
    private String fcm_token;

    public FCMTokenRequest(String fcm_token) {
        this.fcm_token = fcm_token;
    }

    public String getFcmToken() {
        return fcm_token;
    }

    public void setFcmToken(String fcm_token) {
        this.fcm_token = fcm_token;
    }
}

