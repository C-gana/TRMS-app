package com.cgana.trmsownerapp.data.model;

public class AcknowledgeRequest {
    private String notes;

    public AcknowledgeRequest(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

