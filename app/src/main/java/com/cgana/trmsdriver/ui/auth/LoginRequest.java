package com.cgana.trmsdriver.ui.auth;

public class LoginRequest {
    private final String phone_number;
    private final String password;

    public LoginRequest(String phoneNumber, String password) {
        this.phone_number = phoneNumber;
        this.password = password;
    }
}

