package com.cgana.trmsdriver;

import static org.junit.Assert.*;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Basic Utility Tests - Simplified working version
 * Tests for utility functions
 */
public class BasicUtilityTest {

    @Test
    public void testDateParsing() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date date = format.parse("2026-01-10");
            assertNotNull(date);
        } catch (Exception e) {
            fail("Date parsing should not fail");
        }
    }

    @Test
    public void testDateFormatting() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String formatted = format.format(new Date());
        assertNotNull(formatted);
        assertTrue(formatted.length() > 0);
    }

    @Test
    public void testTimeCalculation() {
        long now = System.currentTimeMillis();
        long oneHour = 60 * 60 * 1000;
        long oneHourAgo = now - oneHour;

        long difference = now - oneHourAgo;
        assertEquals(oneHour, difference);
    }

    @Test
    public void testNumberFormatting() {
        int fare = 5000;
        String formatted = String.format(Locale.US, "%,d", fare);
        assertEquals("5,000", formatted);
    }

    @Test
    public void testDistanceFormatting() {
        double distance = 45.5;
        String formatted = String.format(Locale.US, "%.1fkm", distance);
        assertEquals("45.5km", formatted);
    }

    @Test
    public void testTimeFormatting() {
        int minutes = 90;
        String formatted = String.format(Locale.US, "%dmin", minutes);
        assertEquals("90min", formatted);
    }
}

