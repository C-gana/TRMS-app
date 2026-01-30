package com.cgana.trmsdriver.data.model;

public class BoardingResponse {
    private boolean success;
    private String message;
    private Journey journey;

    // Nested Journey class to match API structure
    public static class Journey {
        private Long journey_id;
        private String vehicle_id;
        private int seat_number;
        private String status;
        private String boarding_time;

        public Long getJourneyId() {
            return journey_id;
        }

        public void setJourneyId(Long journey_id) {
            this.journey_id = journey_id;
        }

        public String getVehicleId() {
            return vehicle_id;
        }

        public void setVehicleId(String vehicle_id) {
            this.vehicle_id = vehicle_id;
        }

        public int getSeatNumber() {
            return seat_number;
        }

        public void setSeatNumber(int seat_number) {
            this.seat_number = seat_number;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getBoardingTime() {
            return boarding_time;
        }

        public void setBoardingTime(String boarding_time) {
            this.boarding_time = boarding_time;
        }
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Journey getJourney() {
        return journey;
    }

    public void setJourney(Journey journey) {
        this.journey = journey;
    }

    // Convenience methods for backward compatibility
    public Long getJourneyId() {
        return journey != null ? journey.getJourneyId() : null;
    }

    public int getSeatNumber() {
        return journey != null ? journey.getSeatNumber() : 0;
    }

    public String getStatus() {
        return journey != null ? journey.getStatus() : null;
    }

    public String getBoardingTime() {
        return journey != null ? journey.getBoardingTime() : null;
    }
}

