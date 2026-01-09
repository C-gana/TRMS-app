package com.cgana.trmsdriver.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Preferences Manager (Module 7 Part 3)
 * Manages app preferences and settings
 */
public class PreferencesManager {

    private static final String PREFS_NAME = "trms_preferences";

    // Theme preferences
    private static final String KEY_THEME_MODE = "theme_mode";
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;
    public static final int THEME_SYSTEM = 3;

    // Notification preferences
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_PROXIMITY_ALERTS = "proximity_alerts";
    private static final String KEY_BOARDING_ALERTS = "boarding_alerts";
    private static final String KEY_JOURNEY_COMPLETED_ALERTS = "journey_completed_alerts";
    private static final String KEY_NOTIFICATION_SOUND = "notification_sound";
    private static final String KEY_NOTIFICATION_VIBRATION = "notification_vibration";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Theme methods
    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }

    public void setThemeMode(int themeMode) {
        prefs.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }

    // Notification methods
    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean isProximityAlertsEnabled() {
        return prefs.getBoolean(KEY_PROXIMITY_ALERTS, true);
    }

    public void setProximityAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PROXIMITY_ALERTS, enabled).apply();
    }

    public boolean isBoardingAlertsEnabled() {
        return prefs.getBoolean(KEY_BOARDING_ALERTS, true);
    }

    public void setBoardingAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BOARDING_ALERTS, enabled).apply();
    }

    public boolean isJourneyCompletedAlertsEnabled() {
        return prefs.getBoolean(KEY_JOURNEY_COMPLETED_ALERTS, true);
    }

    public void setJourneyCompletedAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_JOURNEY_COMPLETED_ALERTS, enabled).apply();
    }

    public boolean isNotificationSoundEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATION_SOUND, true);
    }

    public void setNotificationSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_SOUND, enabled).apply();
    }

    public boolean isNotificationVibrationEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATION_VIBRATION, true);
    }

    public void setNotificationVibrationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_VIBRATION, enabled).apply();
    }
}

