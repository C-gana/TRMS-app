package com.cgana.trmsownerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.cgana.trmsownerapp.data.api.FCMApiService;
import com.cgana.trmsownerapp.data.api.RetrofitClient;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.local.VehicleManager;
import com.cgana.trmsownerapp.data.model.FCMTokenRequest;
import com.cgana.trmsownerapp.data.model.GenericResponse;
import com.cgana.trmsownerapp.data.model.User;
import com.cgana.trmsownerapp.ui.auth.LoginActivity;
import com.cgana.trmsownerapp.ui.common.VehicleSelectionDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NavController navController;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private TokenManager tokenManager;
    private VehicleManager vehicleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenManager = new TokenManager(this);
        vehicleManager = VehicleManager.getInstance(this);

        // Check authentication
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Auto-select vehicle if not already selected
        initializeVehicleSelection();

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottomNav);

        setSupportActionBar(toolbar);

        // Update toolbar subtitle with selected vehicle
        updateToolbarSubtitle();

        // Setup Navigation - Use NavHostFragment to get NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Update toolbar title when destination changes
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(destination.getLabel());
                }
            });
        }

        // Handle notification tap
        handleNotificationIntent(getIntent());

        // Check if FCM token needs to be updated (if it's a mock token)
        checkAndUpdateFCMToken();
    }

    private void initializeVehicleSelection() {
        User user = tokenManager.getUser();
        if (user != null && user.getVehicles() != null && !user.getVehicles().isEmpty()) {
            List<String> vehicles = user.getVehicles();

            // Auto-select vehicle if none selected
            if (!vehicleManager.hasSelectedVehicle()) {
                vehicleManager.autoSelectVehicle(vehicles);
            }

            // Validate that selected vehicle still exists in user's vehicle list
            String selected = vehicleManager.getSelectedVehicleId();
            if (selected != null && !vehicles.contains(selected)) {
                // Selected vehicle no longer available, auto-select first one
                vehicleManager.autoSelectVehicle(vehicles);
            }
        }
    }

    private void updateToolbarSubtitle() {
        if (getSupportActionBar() != null) {
            String vehicleId = vehicleManager.getSelectedVehicleId();
            if (vehicleId != null) {
                getSupportActionBar().setSubtitle("Vehicle: " + vehicleId);
            }
        }
    }

    private void showVehicleSelectionDialog() {
        User user = tokenManager.getUser();
        if (user == null || user.getVehicles() == null || user.getVehicles().isEmpty()) {
            return;
        }

        List<String> vehicles = user.getVehicles();
        if (vehicles.size() == 1) {
            // Only one vehicle, no need to show dialog
            return;
        }

        String currentVehicle = vehicleManager.getSelectedVehicleId();
        VehicleSelectionDialog dialog = VehicleSelectionDialog.newInstance(vehicles, currentVehicle);
        dialog.setOnVehicleSelectedListener(vehicleId -> {
            vehicleManager.setSelectedVehicle(vehicleId, vehicleId);
            updateToolbarSubtitle();

            // Refresh current fragment data
            refreshCurrentFragment();
        });
        dialog.show(getSupportFragmentManager(), "vehicle_selection");
    }

    private void refreshCurrentFragment() {
        // Send broadcast to fragments to refresh data
        Intent intent = new Intent("com.cgana.trmsownerapp.VEHICLE_CHANGED");
        sendBroadcast(intent);
    }

    private void checkAndUpdateFCMToken() {
        // Check if Google Play Services is available
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            Log.w(TAG, "Google Play Services not available: " + resultCode);
            return;
        }

        if (tokenManager.isMockFCMToken()) {
            Log.d(TAG, "Mock FCM token detected, fetching real token");
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM token failed", task.getException());
                            return;
                        }

                        // Get real FCM token
                        String realToken = task.getResult();
                        Log.d(TAG, "Real FCM Token obtained: " + realToken);

                        // Save and send to server
                        tokenManager.saveFCMToken(realToken);
                        sendTokenToServer(realToken);
                    });
        } else {
            Log.d(TAG, "Valid FCM token already exists");
        }
    }

    private void sendTokenToServer(String fcmToken) {
        String authToken = tokenManager.getToken();
        if (authToken == null) {
            Log.w(TAG, "Not authenticated, cannot send FCM token");
            return;
        }

        FCMApiService apiService = RetrofitClient.getInstance().getFCMApi();
        FCMTokenRequest request = new FCMTokenRequest(fcmToken);

        apiService.registerFCMToken(request, "Bearer " + authToken)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Real FCM token registered successfully, mock token replaced");
                        } else {
                            Log.e(TAG, "Failed to register FCM token: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        Log.e(TAG, "Error registering FCM token: " + t.getMessage());
                    }
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNotificationIntent(intent);
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null && navController != null) {
            String type = intent.getStringExtra("type");

            if (type != null) {
                switch (type) {
                    case "boarding":
                    case "proximity":
                    case "dashboard_update":
                        // Navigate to Dashboard
                        navController.navigate(R.id.dashboardFragment);
                        break;

                    case "missed_stop":
                    case "timeout":
                        // Navigate to Alerts
                        navController.navigate(R.id.alertsFragment);
                        break;

                    case "destination_sync":
                        // Navigate to Destinations
                        navController.navigate(R.id.destinationsFragment);
                        break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_switch_vehicle) {
            showVehicleSelectionDialog();
            return true;
        } else if (item.getItemId() == R.id.action_help) {
            showHelp();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            navController.navigate(R.id.settingsFragment);
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHelp() {
        Intent intent = new Intent(this, com.cgana.trmsownerapp.ui.help.HelpActivity.class);
        startActivity(intent);
    }

    private void logout() {
        tokenManager.clearAuth();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
