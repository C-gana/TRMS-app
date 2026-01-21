package com.cgana.trmsdriver.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.cgana.trmsdriver.MainActivity;
import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.repository.LocationTrackingRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Location Tracking Service (Module 6 Part 1)
 * Foreground service that tracks driver location and sends updates to backend
 */
public class LocationTrackingService extends Service {

    private static final String TAG = "LocationTrackingService";
    private static final String CHANNEL_ID = "location_tracking_channel";
    private static final int NOTIFICATION_ID = 1001;

    // Location update intervals (in milliseconds)
    private static final long UPDATE_INTERVAL = 30000; // 30 seconds
    private static final long FASTEST_INTERVAL = 15000; // 15 seconds

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationTrackingRepository repository;
    private TokenManager tokenManager;

    private String vehicleId;
    private boolean isTracking = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationTrackingService created");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tokenManager = new TokenManager(this);
        repository = new LocationTrackingRepository(tokenManager);

        // Get vehicle ID from token manager
        if (tokenManager.getDriver() != null && tokenManager.getDriver().getVehicleId() != null) {
            vehicleId = tokenManager.getDriver().getVehicleId();
        }

        setupLocationCallback();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "START_TRACKING":
                        vehicleId = intent.getStringExtra("vehicle_id");
                        startLocationTracking();
                        break;
                    case "STOP_TRACKING":
                        stopLocationTracking();
                        stopSelf();
                        break;
                }
            }
        }

        return START_STICKY; // Service will restart if killed
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // This is a started service, not a bound service
    }

    /**
     * Setup location callback to handle location updates
     */
    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.d(TAG, "Location update received: " +
                        location.getLatitude() + ", " + location.getLongitude());

                    // Send location to backend
                    sendLocationUpdate(location);
                }
            }
        };
    }

    /**
     * Start tracking location
     */
    private void startLocationTracking() {
        if (isTracking) {
            Log.d(TAG, "Already tracking location");
            return;
        }

        Log.d(TAG, "Starting location tracking for vehicle: " + vehicleId);

        // Create notification and start foreground service
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                    .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                    .setWaitForAccurateLocation(false)
                    .build();

            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );

            isTracking = true;
            Log.d(TAG, "Location updates requested successfully");
        } else {
            Log.e(TAG, "Location permission not granted");
            stopSelf();
        }
    }

    /**
     * Stop tracking location
     */
    private void stopLocationTracking() {
        if (!isTracking) {
            Log.d(TAG, "Not currently tracking");
            return;
        }

        Log.d(TAG, "Stopping location tracking");

        fusedLocationClient.removeLocationUpdates(locationCallback);
        isTracking = false;

        Log.d(TAG, "Location tracking stopped");
    }

    /**
     * Send location update to backend
     */
    private void sendLocationUpdate(Location location) {
        if (vehicleId == null) {
            Log.e(TAG, "Vehicle ID is null, cannot send location update");
            return;
        }

        repository.sendLocationUpdate(vehicleId, location.getLatitude(), location.getLongitude());
    }

    /**
     * Create notification for foreground service
     */
    private Notification createNotification() {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.location_tracking_title))
                .setContentText(getString(R.string.location_tracking_message))
                .setSmallIcon(R.drawable.ic_location)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    /**
     * Create notification channel (required for Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.location_tracking_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(getString(R.string.location_tracking_channel_description));
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LocationTrackingService destroyed");
        stopLocationTracking();
    }

    /**
     * Static helper methods to start/stop service
     */
    public static void startTracking(Context context, String vehicleId) {
        Intent intent = new Intent(context, LocationTrackingService.class);
        intent.setAction("START_TRACKING");
        intent.putExtra("vehicle_id", vehicleId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopTracking(Context context) {
        Intent intent = new Intent(context, LocationTrackingService.class);
        intent.setAction("STOP_TRACKING");
        context.startService(intent);
    }
}

