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
        android.util.Log.d("TokenManager", "Constructor called");

        // Always use Application context to avoid scope issues
        Context appContext = context.getApplicationContext();
        android.util.Log.d("TokenManager", "  - Using Application context: " + appContext.getClass().getSimpleName());

        gson = new Gson();

        try {
            android.util.Log.d("TokenManager", "  - Attempting EncryptedSharedPreferences...");

            // Create or get master key for encryption
            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Create encrypted shared preferences
            sharedPreferences = EncryptedSharedPreferences.create(
                    appContext,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            android.util.Log.d("TokenManager", "  - ✅ EncryptedSharedPreferences created successfully");
            android.util.Log.d("TokenManager", "  - Prefs name: " + PREFS_NAME);

        } catch (Exception e) {
            android.util.Log.e("TokenManager", "  - ❌ EncryptedSharedPreferences FAILED: " + e.getMessage());
            android.util.Log.e("TokenManager", "  - Exception type: " + e.getClass().getName());
            e.printStackTrace();

            // Fallback to normal SharedPreferences if encryption fails
            android.util.Log.w("TokenManager", "  - Falling back to normal SharedPreferences");
            sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            android.util.Log.d("TokenManager", "  - ✅ Normal SharedPreferences created");
        }

        // Verify SharedPreferences is working
        android.util.Log.d("TokenManager", "  - Testing SharedPreferences write/read...");
        try {
            boolean testWriteSuccess = sharedPreferences.edit().putString("test_key", "test_value").commit();
            android.util.Log.d("TokenManager", "  - Test write result: " + testWriteSuccess);

            String testValue = sharedPreferences.getString("test_key", null);

            if ("test_value".equals(testValue)) {
                android.util.Log.d("TokenManager", "  - ✅ SharedPreferences working correctly");
                sharedPreferences.edit().remove("test_key").commit();
            } else {
                android.util.Log.e("TokenManager", "  - ❌ SharedPreferences NOT working! Test write/read failed!");
                android.util.Log.e("TokenManager", "  - Test value was: " + testValue);

                // If encrypted version fails, force fallback to normal
                if (!(sharedPreferences instanceof android.content.SharedPreferences)) {
                    android.util.Log.e("TokenManager", "  - ⚠️ EncryptedSharedPreferences is broken! Forcing fallback...");
                    sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    android.util.Log.d("TokenManager", "  - Retesting with normal SharedPreferences...");

                    boolean retestSuccess = sharedPreferences.edit().putString("test_key", "test_value").commit();
                    String retestValue = sharedPreferences.getString("test_key", null);

                    if ("test_value".equals(retestValue)) {
                        android.util.Log.d("TokenManager", "  - ✅ Normal SharedPreferences works!");
                        sharedPreferences.edit().remove("test_key").commit();
                    } else {
                        android.util.Log.e("TokenManager", "  - ❌ FATAL: Even normal SharedPreferences doesn't work!");
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("TokenManager", "  - ❌ SharedPreferences test FAILED: " + e.getMessage());
            e.printStackTrace();

            // Try fallback to normal SharedPreferences
            android.util.Log.e("TokenManager", "  - ⚠️ Attempting fallback to normal SharedPreferences...");
            try {
                sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                android.util.Log.d("TokenManager", "  - Fallback created, retesting...");

                boolean retestSuccess = sharedPreferences.edit().putString("test_key", "test_value").commit();
                String retestValue = sharedPreferences.getString("test_key", null);

                if ("test_value".equals(retestValue)) {
                    android.util.Log.d("TokenManager", "  - ✅ Fallback works!");
                    sharedPreferences.edit().remove("test_key").commit();
                } else {
                    android.util.Log.e("TokenManager", "  - ❌ FATAL: Fallback also failed!");
                }
            } catch (Exception fallbackException) {
                android.util.Log.e("TokenManager", "  - ❌ FATAL: Fallback exception: " + fallbackException.getMessage());
            }
        }

        android.util.Log.d("TokenManager", "TokenManager initialization complete");
    }

    // Save JWT token
    public void saveToken(String token) {
        android.util.Log.d("TokenManager", "saveToken() called");

        if (token == null) {
            android.util.Log.e("TokenManager", "  - ❌ Token is NULL! Cannot save.");
            return;
        }

        android.util.Log.d("TokenManager", "  - Token length: " + token.length());
        android.util.Log.d("TokenManager", "  - Saving to SharedPreferences with key: " + KEY_TOKEN);

        boolean saved = sharedPreferences.edit().putString(KEY_TOKEN, token).commit();
        android.util.Log.d("TokenManager", "  - Save result: " + (saved ? "✅ SUCCESS" : "❌ FAILED"));

        // Verify save immediately
        android.util.Log.d("TokenManager", "  - Verifying save...");
        String retrieved = sharedPreferences.getString(KEY_TOKEN, null);

        if (retrieved != null) {
            android.util.Log.d("TokenManager", "  - ✅ Verification SUCCESS");
            android.util.Log.d("TokenManager", "     - Retrieved length: " + retrieved.length());
            android.util.Log.d("TokenManager", "     - Matches saved: " + retrieved.equals(token));
        } else {
            android.util.Log.e("TokenManager", "  - ❌ Verification FAILED - Retrieved NULL!");
            android.util.Log.e("TokenManager", "  - ⚠️ CRITICAL: SharedPreferences is not working!");
        }
    }

    // Get JWT token
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Save driver data
    public void saveDriver(Driver driver) {
        android.util.Log.d("TokenManager", "saveDriver() called");

        if (driver == null) {
            android.util.Log.e("TokenManager", "  - ❌ Driver is NULL! Cannot save.");
            return;
        }

        android.util.Log.d("TokenManager", "  - Driver details:");
        android.util.Log.d("TokenManager", "     - Full Name: " + driver.getFullName());
        android.util.Log.d("TokenManager", "     - Driver ID: " + driver.getDriverId());
        android.util.Log.d("TokenManager", "     - Phone: " + driver.getPhoneNumber());
        android.util.Log.d("TokenManager", "     - Vehicle ID: " + driver.getVehicleId());
        android.util.Log.d("TokenManager", "     - On Duty: " + driver.isOnDuty());

        String driverJson = gson.toJson(driver);
        android.util.Log.d("TokenManager", "  - JSON generated (length=" + (driverJson != null ? driverJson.length() : 0) + "):");
        android.util.Log.d("TokenManager", "     " + driverJson);

        android.util.Log.d("TokenManager", "  - Saving to SharedPreferences with key: " + KEY_DRIVER);
        boolean saved = sharedPreferences.edit().putString(KEY_DRIVER, driverJson).commit();
        android.util.Log.d("TokenManager", "  - Save result: " + (saved ? "✅ SUCCESS" : "❌ FAILED"));

        // Verify save immediately
        android.util.Log.d("TokenManager", "  - Verifying save...");
        String retrieved = sharedPreferences.getString(KEY_DRIVER, null);

        if (retrieved != null) {
            android.util.Log.d("TokenManager", "  - ✅ Verification SUCCESS");
            android.util.Log.d("TokenManager", "     - Retrieved length: " + retrieved.length());
            android.util.Log.d("TokenManager", "     - Matches saved: " + retrieved.equals(driverJson));

            if (!retrieved.equals(driverJson)) {
                android.util.Log.e("TokenManager", "  - ⚠️ WARNING: Retrieved JSON doesn't match saved JSON!");
                android.util.Log.e("TokenManager", "     - Saved: " + driverJson);
                android.util.Log.e("TokenManager", "     - Retrieved: " + retrieved);
            }
        } else {
            android.util.Log.e("TokenManager", "  - ❌ Verification FAILED - Retrieved NULL!");
        }
    }

    // Get driver data
    public Driver getDriver() {
        android.util.Log.d("TokenManager", "getDriver() called");

        // First check if SharedPreferences is still valid
        if (sharedPreferences == null) {
            android.util.Log.e("TokenManager", "  - ❌ CRITICAL: sharedPreferences is NULL!");
            return null;
        }

        String driverJson = null;
        try {
            driverJson = sharedPreferences.getString(KEY_DRIVER, null);
        } catch (Exception e) {
            android.util.Log.e("TokenManager", "  - ❌ Exception reading from SharedPreferences: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        if (driverJson != null) {
            android.util.Log.d("TokenManager", "  - JSON from prefs: Present (" + driverJson.length() + " chars)");
            android.util.Log.d("TokenManager", "  - JSON content: " + driverJson);

            try {
                Driver driver = gson.fromJson(driverJson, Driver.class);

                if (driver != null) {
                    android.util.Log.d("TokenManager", "  - ✅ Driver parsed successfully:");
                    android.util.Log.d("TokenManager", "     - Full Name: " + driver.getFullName());
                    android.util.Log.d("TokenManager", "     - Driver ID: " + driver.getDriverId());
                    android.util.Log.d("TokenManager", "     - Phone: " + driver.getPhoneNumber());
                    android.util.Log.d("TokenManager", "     - Vehicle ID: " + driver.getVehicleId());
                } else {
                    android.util.Log.e("TokenManager", "  - ❌ GSON returned NULL driver object!");
                }

                return driver;
            } catch (com.google.gson.JsonSyntaxException e) {
                android.util.Log.e("TokenManager", "  - ❌ JSON SYNTAX ERROR: " + e.getMessage());
                android.util.Log.e("TokenManager", "     JSON was: " + driverJson);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                android.util.Log.e("TokenManager", "  - ❌ UNEXPECTED ERROR parsing driver JSON: " + e.getMessage());
                android.util.Log.e("TokenManager", "     Exception type: " + e.getClass().getName());
                e.printStackTrace();
                return null;
            }
        }

        android.util.Log.e("TokenManager", "  - ❌ NO JSON DATA IN SHARED PREFERENCES!");
        android.util.Log.d("TokenManager", "  - This means data was NEVER saved or SharedPreferences was cleared");
        android.util.Log.d("TokenManager", "  - Checking all stored keys:");

        // Debug: List all keys in SharedPreferences
        try {
            java.util.Map<String, ?> allEntries = sharedPreferences.getAll();
            android.util.Log.d("TokenManager", "  - Total keys stored: " + allEntries.size());

            if (allEntries.isEmpty()) {
                android.util.Log.e("TokenManager", "  - ⚠️ SharedPreferences is COMPLETELY EMPTY!");
                android.util.Log.e("TokenManager", "  - Either data was never saved, or it was cleared");
            } else {
                for (java.util.Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    android.util.Log.d("TokenManager", "     Key: '" + entry.getKey() + "' | Has value: " + (entry.getValue() != null));
                    if (entry.getValue() != null) {
                        String valueStr = entry.getValue().toString();
                        android.util.Log.d("TokenManager", "       Value preview: " + valueStr.substring(0, Math.min(50, valueStr.length())) + "...");
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("TokenManager", "  - Could not list keys: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Save duty status
    public void saveDutyStatus(boolean onDuty) {
        android.util.Log.d("TokenManager", "saveDutyStatus() called: " + onDuty);
        sharedPreferences.edit().putBoolean(KEY_DUTY_STATUS, onDuty).commit(); // Using commit()
    }

    // Get duty status
    public boolean getDutyStatus() {
        // Get from shared preferences with false as default (not from driver object)
        boolean status = sharedPreferences.getBoolean(KEY_DUTY_STATUS, false);
        android.util.Log.d("TokenManager", "getDutyStatus() returning: " + status);
        return status;
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
        String token = getToken();
        Driver driver = getDriver();
        boolean loggedIn = token != null && driver != null;

        // Debug logging
        android.util.Log.d("TokenManager", "isLoggedIn() check:");
        android.util.Log.d("TokenManager", "  - Token present: " + (token != null));
        android.util.Log.d("TokenManager", "  - Driver present: " + (driver != null));
        android.util.Log.d("TokenManager", "  - Result: " + loggedIn);

        if (token != null) {
            android.util.Log.d("TokenManager", "  - Token (first 20 chars): " + token.substring(0, Math.min(20, token.length())) + "...");
        }
        if (driver != null) {
            android.util.Log.d("TokenManager", "  - Driver ID: " + driver.getDriverId());
            android.util.Log.d("TokenManager", "  - Driver Name: " + driver.getFullName());
        }

        return loggedIn;
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