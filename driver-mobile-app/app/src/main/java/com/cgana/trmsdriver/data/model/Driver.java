package com.cgana.trmsdriver.data.model;

public class Driver {
    private String driver_id;
    private String full_name;
    private String phone_number;
    private String email;
    private String role;          // From backend
    private String vehicle_id;
    private String vehicle_registration;
    private boolean on_duty;
    private String duty_started_at;
    private String fcm_token;

    // Constructor
    public Driver() {}

    public Driver(String driver_id, String full_name, String phone_number,
                  String vehicle_id, String vehicle_registration) {
        this.driver_id = driver_id;
        this.full_name = full_name;
        this.phone_number = phone_number;
        this.vehicle_id = vehicle_id;
        this.vehicle_registration = vehicle_registration;
    }

    // Getters and Setters
    public String getDriverId() { return driver_id; }
    public void setDriverId(String driver_id) { this.driver_id = driver_id; }

    public String getFullName() { return full_name; }
    public void setFullName(String full_name) { this.full_name = full_name; }

    public String getPhoneNumber() { return phone_number; }
    public void setPhoneNumber(String phone_number) { this.phone_number = phone_number; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getVehicleId() { return vehicle_id; }
    public void setVehicleId(String vehicle_id) { this.vehicle_id = vehicle_id; }

    public String getVehicleRegistration() { return vehicle_registration; }
    public void setVehicleRegistration(String vehicle_registration) {
        this.vehicle_registration = vehicle_registration;
    }

    public boolean isOnDuty() { return on_duty; }
    public void setOnDuty(boolean on_duty) { this.on_duty = on_duty; }

    public String getDutyStartedAt() { return duty_started_at; }
    public void setDutyStartedAt(String duty_started_at) {
        this.duty_started_at = duty_started_at;
    }

    public String getFcmToken() { return fcm_token; }
    public void setFcmToken(String fcm_token) { this.fcm_token = fcm_token; }

    // Helper method to get driver initials for avatar
    public String getInitials() {
        if (full_name != null && !full_name.isEmpty()) {
            String[] parts = full_name.split(" ");
            if (parts.length >= 2) {
                return String.valueOf(parts[0].charAt(0)).toUpperCase() +
                        String.valueOf(parts[1].charAt(0)).toUpperCase();
            } else if (parts.length == 1) {
                return String.valueOf(parts[0].charAt(0)).toUpperCase();
            }
        }
        return "D";
    }
}