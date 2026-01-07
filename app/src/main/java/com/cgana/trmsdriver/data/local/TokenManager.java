package com.cgana.trmsdriver.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.cgana.trmsdriver.data.model.Driver;
import com.cgana.trmsdriver.data.model.User;
import com.google.gson.Gson;

public class TokenManager {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_DRIVER = "driver_data";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final String KEY_DUTY_STATUS = "duty_status";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public TokenManager(Context context) {
        gson = new Gson();

        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to regular SharedPreferences
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public void saveDriver(Driver driver) {
        String driverJson = gson.toJson(driver);
        sharedPreferences.edit().putString(KEY_DRIVER, driverJson).apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public Driver getDriver() {
        String driverJson = sharedPreferences.getString(KEY_DRIVER, null);
        if (driverJson != null) {
            return gson.fromJson(driverJson, Driver.class);
        }
        return null;
    }

    public void saveDutyStatus(boolean onDuty) {
        sharedPreferences.edit().putBoolean(KEY_DUTY_STATUS, onDuty).apply();
    }

    public boolean getDutyStatus() {
        return sharedPreferences.getBoolean(KEY_DUTY_STATUS, false);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void saveFCMToken(String fcmToken) {
        sharedPreferences.edit().putString(KEY_FCM_TOKEN, fcmToken).apply();
    }

    public String getFCMToken() {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null);
    }

    public boolean isMockFCMToken() {
        String token = getFCMToken();
        return token != null && token.equals("mock_fcm_token_placeholder");
    }

    public void clearAuth() {
        sharedPreferences.edit().clear().apply();
    }
}

