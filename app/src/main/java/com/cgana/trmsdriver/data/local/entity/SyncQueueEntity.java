package com.cgana.trmsdriver.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Sync Queue Entity (Module 6 Part 1)
 * Stores operations that need to be synced when online
 */
@Entity(tableName = "sync_queue")
public class SyncQueueEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String operation_type; // "BOARDING", "SET_DESTINATION", "ALIGHTING"
    private String request_data; // JSON string of request
    private long created_at;
    private int retry_count;
    private String status; // "PENDING", "SYNCING", "FAILED", "COMPLETED"
    private String error_message;

    // Constructors
    public SyncQueueEntity() {}

    public SyncQueueEntity(String operation_type, String request_data) {
        this.operation_type = operation_type;
        this.request_data = request_data;
        this.created_at = System.currentTimeMillis();
        this.retry_count = 0;
        this.status = "PENDING";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOperation_type() { return operation_type; }
    public void setOperation_type(String operation_type) { this.operation_type = operation_type; }

    public String getRequest_data() { return request_data; }
    public void setRequest_data(String request_data) { this.request_data = request_data; }

    public long getCreated_at() { return created_at; }
    public void setCreated_at(long created_at) { this.created_at = created_at; }

    public int getRetry_count() { return retry_count; }
    public void setRetry_count(int retry_count) { this.retry_count = retry_count; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getError_message() { return error_message; }
    public void setError_message(String error_message) { this.error_message = error_message; }
}

