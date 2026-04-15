package com.cgana.trmsdriver.data.model;

import java.util.List;

public class User {
    private String user_id;
    private String full_name;
    private String role;
    private String phone_number;
    private String email;
    private List<String> vehicles;

    public String getUserId() {
        return user_id;
    }

    public String getFullName() {
        return full_name;
    }

    public String getRole() {
        return role;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getVehicles() {
        return vehicles;
    }
}
