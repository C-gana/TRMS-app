package com.cgana.trmsownerapp;

import android.app.Application;
import android.util.Log;

import com.cgana.trmsownerapp.utils.ThemeManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;

public class TRMSApplication extends Application {

    private static final String TAG = "TRMSApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        initializeFirebase();

        // Apply saved theme on app startup
        ThemeManager themeManager = new ThemeManager(this);
        themeManager.applyTheme(themeManager.getThemeMode());
    }

    private void initializeFirebase() {
        // Check if Google Play Services is available
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            Log.w(TAG, "Google Play Services not available: " + resultCode +
                  ". Firebase Cloud Messaging features will not be available.");
            // Continue without Firebase initialization - app will work but FCM won't
            return;
        }

        try {
            // Initialize Firebase if Google Play Services is available
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase", e);
        }
    }
}

