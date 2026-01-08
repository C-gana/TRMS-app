package com.cgana.trmsdriver.data.model;

public class LoginRequest {
    private String phone_number;
    private String password;

    // Constructor
    public LoginRequest(String phone_number, String password) {
        this.phone_number = phone_number;
        this.password = password;
    }

    // Getters
    public String getPhoneNumber() { return phone_number; }
    public String getPassword() { return password; }
}