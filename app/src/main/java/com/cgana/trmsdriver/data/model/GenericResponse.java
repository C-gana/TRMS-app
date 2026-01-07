package com.cgana.trmsdriver.data.model;

public class GenericResponse {
    private boolean success;
    private String message;
    private Integer destination_id;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Integer getDestinationId() {
        return destination_id;
    }
}

