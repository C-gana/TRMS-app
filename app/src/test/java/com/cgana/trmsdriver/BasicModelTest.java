package com.cgana.trmsdriver;

import static org.junit.Assert.*;

import com.cgana.trmsdriver.data.model.Seat;
import com.cgana.trmsdriver.data.model.SeatStatus;
import com.cgana.trmsdriver.data.model.Destination;

import org.junit.Test;

/**
 * Basic Model Tests - Simplified working version
 * Tests for core data models
 */
public class BasicModelTest {

    @Test
    public void testSeatModel_Creation() {
        Seat seat = new Seat();
        assertNotNull(seat);
    }

    @Test
    public void testSeatModel_VacantStatus() {
        Seat seat = new Seat();
        seat.setSeat_number(1);
        seat.setStatus("vacant");

        assertEquals(1, seat.getSeat_number());
        assertEquals("vacant", seat.getStatus());
        assertTrue(seat.isVacant());
    }

    @Test
    public void testSeatModel_ActiveJourney() {
        Seat seat = new Seat();
        seat.setSeat_number(2);
        seat.setStatus("active_journey");
        seat.setDestination("Lilongwe");
        seat.setFare(5000);

        assertEquals("active_journey", seat.getStatus());
        assertEquals("Lilongwe", seat.getDestination());
        assertEquals(Integer.valueOf(5000), seat.getFare());
        assertTrue(seat.isActive());
    }

    @Test
    public void testSeatModel_AwaitingDestination() {
        Seat seat = new Seat();
        seat.setSeat_number(3);
        seat.setStatus("awaiting_destination");
        seat.setTimeout_seconds(90);

        assertTrue(seat.isAwaiting());
        assertEquals(Integer.valueOf(90), seat.getTimeout_seconds());
    }

    @Test
    public void testSeatStatus_Validation() {
        assertTrue(SeatStatus.isValidStatus("vacant"));
        assertTrue(SeatStatus.isValidStatus("awaiting_destination"));
        assertTrue(SeatStatus.isValidStatus("active_journey"));
        assertTrue(SeatStatus.isValidStatus("approaching_destination"));
        assertFalse(SeatStatus.isValidStatus("invalid"));
        assertFalse(SeatStatus.isValidStatus(null));
    }

    @Test
    public void testDestinationModel_Creation() {
        Destination destination = new Destination(
            1,
            "Lilongwe",
            5000,
            45.5,
            60,
            500,
            "active"
        );

        assertNotNull(destination);
        assertEquals(1, destination.getDestinationId());
        assertEquals("Lilongwe", destination.getName());
        assertEquals(5000, destination.getFare());
    }

    @Test
    public void testDestinationModel_FormattedValues() {
        Destination destination = new Destination(
            2,
            "Blantyre",
            8000,
            120.0,
            180,
            1000,
            "active"
        );

        String formattedFare = destination.getFormattedFare();
        String formattedDistance = destination.getFormattedDistance();
        String formattedTime = destination.getFormattedTime();

        assertNotNull(formattedFare);
        assertNotNull(formattedDistance);
        assertNotNull(formattedTime);
        assertTrue(formattedFare.contains("8,000"));
        assertTrue(formattedDistance.contains("120"));
        assertTrue(formattedTime.contains("180"));
    }
}

