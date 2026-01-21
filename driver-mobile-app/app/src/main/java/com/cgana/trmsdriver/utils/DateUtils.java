package com.cgana.trmsdriver.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date and time operations (Module 2 Part 3)
 */
public class DateUtils {

    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("h:mm a", Locale.US);

    /**
     * Calculate duration from a start time to now
     * Returns formatted string like "2h 30m"
     */
    public static String calculateDuration(String startTimeISO) {
        if (startTimeISO == null || startTimeISO.isEmpty()) {
            return "0m";
        }

        try {
            Date startDate = ISO_FORMAT.parse(startTimeISO);
            if (startDate == null) return "0m";

            long durationMs = System.currentTimeMillis() - startDate.getTime();
            long hours = TimeUnit.MILLISECONDS.toHours(durationMs);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60;

            if (hours > 0) {
                return String.format(Locale.US, "%dh %dm", hours, minutes);
            } else {
                return String.format(Locale.US, "%dm", minutes);
            }
        } catch (ParseException e) {
            return "0m";
        }
    }

    /**
     * Format ISO time to display format
     */
    public static String formatTime(String isoTime) {
        if (isoTime == null || isoTime.isEmpty()) {
            return "";
        }

        try {
            Date date = ISO_FORMAT.parse(isoTime);
            return date != null ? DISPLAY_FORMAT.format(date) : "";
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Get relative time string (e.g., "2 minutes ago")
     */
    public static String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        if (seconds < 60) {
            return "Just now";
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        return days + " day" + (days > 1 ? "s" : "") + " ago";
    }
}

