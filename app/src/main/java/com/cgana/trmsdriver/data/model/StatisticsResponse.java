package com.cgana.trmsdriver.data.model;

/**
 * Statistics Response model (Module 5 Part 2)
 */
public class StatisticsResponse {
    private boolean success;
    private Period period;
    private Statistics statistics;
    private String message;

    public static class Period {
        private String start_date;
        private String end_date;
        private String label;

        // Getters and Setters
        public String getStartDate() { return start_date; }
        public void setStartDate(String start_date) { this.start_date = start_date; }

        public String getEndDate() { return end_date; }
        public void setEndDate(String end_date) { this.end_date = end_date; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    // Constructor
    public StatisticsResponse() {}

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Period getPeriod() { return period; }
    public void setPeriod(Period period) { this.period = period; }

    public Statistics getStatistics() { return statistics; }
    public void setStatistics(Statistics statistics) { this.statistics = statistics; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

