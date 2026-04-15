package com.cgana.trmsownerapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Manages the currently selected vehicle for the owner.
 * Persists the selection across app sessions.
 */
public class VehicleManager {
    private static final String PREFS_NAME = "vehicle_prefs";
    private static final String KEY_SELECTED_VEHICLE_ID = "selected_vehicle_id";
    private static final String KEY_SELECTED_VEHICLE_REGISTRATION = "selected_vehicle_registration";

    private SharedPreferences sharedPreferences;
    private static VehicleManager instance;

    private VehicleManager(Context context) {
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

    public static synchronized VehicleManager getInstance(Context context) {
        if (instance == null) {
            instance = new VehicleManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Save the selected vehicle
     */
    public void setSelectedVehicle(String vehicleId, String registration) {
        sharedPreferences.edit()
                .putString(KEY_SELECTED_VEHICLE_ID, vehicleId)
                .putString(KEY_SELECTED_VEHICLE_REGISTRATION, registration)
                .apply();
    }

    /**
     * Get the currently selected vehicle ID
     */
    public String getSelectedVehicleId() {
        return sharedPreferences.getString(KEY_SELECTED_VEHICLE_ID, null);
    }

    /**
     * Get the currently selected vehicle registration
     */
    public String getSelectedVehicleRegistration() {
        return sharedPreferences.getString(KEY_SELECTED_VEHICLE_REGISTRATION, null);
    }

    /**
     * Check if a vehicle is selected
     */
    public boolean hasSelectedVehicle() {
        return getSelectedVehicleId() != null;
    }

    /**
     * Clear the selected vehicle
     */
    public void clearSelectedVehicle() {
        sharedPreferences.edit()
                .remove(KEY_SELECTED_VEHICLE_ID)
                .remove(KEY_SELECTED_VEHICLE_REGISTRATION)
                .apply();
    }

    /**
     * Auto-select first vehicle if no selection exists
     */
    public String autoSelectVehicle(java.util.List<String> vehicleIds) {
        if (vehicleIds == null || vehicleIds.isEmpty()) {
            return null;
        }

        // If already selected and valid, return it
        String selected = getSelectedVehicleId();
        if (selected != null && vehicleIds.contains(selected)) {
            return selected;
        }

        // Otherwise select the first vehicle
        String firstVehicle = vehicleIds.get(0);
        setSelectedVehicle(firstVehicle, firstVehicle); // Registration same as ID for now
        return firstVehicle;
    }
}

