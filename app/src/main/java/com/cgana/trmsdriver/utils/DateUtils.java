package com.cgana.trmsdriver.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date and time operations
 */
public class DateUtils {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * Format time from timestamp string
     */
    public static String formatTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "--:--";
        }
        try {
            Date date = DATE_TIME_FORMAT.parse(timestamp);
            if (date != null) {
                return TIME_FORMAT.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "--:--";
    }

    /**
     * Calculate duration from start time to now
     */
    public static String calculateDuration(String startTimeStr) {
        if (startTimeStr == null || startTimeStr.isEmpty()) {
            return "0h 0m";
        }

        try {
            Date startTime = DATE_TIME_FORMAT.parse(startTimeStr);
            if (startTime == null) {
                return "0h 0m";
            }

            long durationMillis = System.currentTimeMillis() - startTime.getTime();
            long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;

            return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
        } catch (ParseException e) {
            e.printStackTrace();
            return "0h 0m";
        }
    }

    /**
     * Get current timestamp as string
     */
    public static String getCurrentTimestamp() {
        return DATE_TIME_FORMAT.format(new Date());
    }

    /**
     * Format date and time for display
     */
    public static String formatDateTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "";
        }
        try {
            Date date = DATE_TIME_FORMAT.parse(timestamp);
            if (date != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }
}

