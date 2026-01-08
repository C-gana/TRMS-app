package com.cgana.trmsdriver.data.model;

public class DutyStatusResponse {
    private boolean success;
    private String message;
    private boolean on_duty;
    private String duty_started_at;

    // Constructor
    public DutyStatusResponse() {}

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isOnDuty() { return on_duty; }
    public void setOnDuty(boolean on_duty) { this.on_duty = on_duty; }

    public String getDutyStartedAt() { return duty_started_at; }
    public void setDutyStartedAt(String duty_started_at) {
        this.duty_started_at = duty_started_at;
    }
}