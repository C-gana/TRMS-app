package com.cgana.trmsdriver.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx. security.crypto.MasterKey;
import com.google.gson. Gson;
import com.cgana.trmsdriver.data.model. Driver;

public class TokenManager {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_DRIVER = "driver_data";
    private static final String KEY_DUTY_STATUS = "duty_status";
    private static final String KEY_DUTY_STARTED_AT = "duty_started_at";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public TokenManager(Context context) {
        gson = new Gson();

        try {
            // Create or get master key for encryption
            MasterKey masterKey = new MasterKey. Builder(context)
                    . setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Create encrypted shared preferences
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme. AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to normal SharedPreferences if encryption fails
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    // Save JWT token
    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    // Get JWT token
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Save driver data
    public void saveDriver(Driver driver) {
        String driverJson = gson.toJson(driver);
        sharedPreferences.edit().putString(KEY_DRIVER, driverJson).apply();
    }

    // Get driver data
    public Driver getDriver() {
        String driverJson = sharedPreferences.getString(KEY_DRIVER, null);
        if (driverJson != null) {
            return gson.fromJson(driverJson, Driver.class);
        }
        return null;
    }

    // Save duty status
    public void saveDutyStatus(boolean onDuty) {
        sharedPreferences.edit().putBoolean(KEY_DUTY_STATUS, onDuty).apply();
    }

    // Get duty status
    public boolean getDutyStatus() {
        return sharedPreferences.getBoolean(KEY_DUTY_STATUS, false);
    }

    // Save duty started time
    public void saveDutyStartedAt(String timestamp) {
        sharedPreferences.edit().putString(KEY_DUTY_STARTED_AT, timestamp).apply();
    }

    // Get duty started time
    public String getDutyStartedAt() {
        return sharedPreferences.getString(KEY_DUTY_STARTED_AT, null);
    }

    // Save remember me preference
    public void saveRememberMe(boolean rememberMe) {
        sharedPreferences.edit().putBoolean(KEY_REMEMBER_ME, rememberMe).apply();
    }

    // Get remember me preference
    public boolean getRememberMe() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return getToken() != null && getDriver() != null;
    }

    // Clear all authentication data
    public void clearAuth() {
        sharedPreferences.edit().clear().apply();
    }

    // Clear only duty-related data (for logout while keeping login credentials if remember me is checked)
    public void clearDutyData() {
        sharedPreferences.edit()
                .remove(KEY_DUTY_STATUS)
                .remove(KEY_DUTY_STARTED_AT)
                .apply();
    }

    public void saveFCMToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FCM_TOKEN, token);
        editor.apply();
    }

    public String getFCMToken() {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null);
    }
}