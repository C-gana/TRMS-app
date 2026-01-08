package com.cgana.trmsdriver.ui.duty;

import android. Manifest;
import android.content. Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os. Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget. FrameLayout;
import android.widget.ImageView;
import android. widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx. appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.google. android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material. button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.cgana.trmsdriver.MainActivity;
import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.Driver;
import com.cgana.trmsdriver.data.repository.AuthRepository;
import com.cgana.trmsdriver.ui.auth.LoginActivity;
import com.cgana.trmsdriver.utils.DateUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util. Locale;

public class DutyStatusActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private TextView tvDriverInitial, tvDriverName, tvDriverPhone;
    private TextView tvVehicleId, tvVehicleRegistration, tvVehicleStatus;
    private View statusIndicator;
    private LinearLayout offDutyContainer, onDutyContainer;
    private ImageView ivDutyOn;
    private TextView tvDutyStartTime, tvDutyDuration;
    private MaterialButton btnToggleDuty;
    private MaterialCardView summaryCard;
    private TextView tvPassengerCount, tvActiveSeats, tvRevenue;
    private FrameLayout loadingOverlay;
    private TextView tvLoadingMessage;

    // ViewModel & Data
    private DutyStatusViewModel viewModel;
    private TokenManager tokenManager;
    private Driver driver;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private Handler durationHandler;
    private Runnable durationRunnable;

    // Constants
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duty_status);

        // Initialize
        tokenManager = new TokenManager(this);
        driver = tokenManager.getDriver();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        durationHandler = new Handler(Looper.getMainLooper());

        // Check authentication
        if (driver == null) {
            navigateToLogin();
            return;
        }

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Initialize ViewModel
        AuthRepository repository = new AuthRepository(tokenManager);
        DutyStatusViewModelFactory factory = new DutyStatusViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(DutyStatusViewModel.class);

        // Setup UI with driver data
        setupDriverInfo();

        // Setup listeners
        setupListeners();

        // Observe ViewModel
        observeViewModel();

        // Load current duty status
        loadCurrentDutyStatus();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvDriverInitial = findViewById(R.id.tvDriverInitial);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvDriverPhone = findViewById(R.id.tvDriverPhone);
        tvVehicleId = findViewById(R.id. tvVehicleId);
        tvVehicleRegistration = findViewById(R.id.tvVehicleRegistration);
        tvVehicleStatus = findViewById(R. id.tvVehicleStatus);
        statusIndicator = findViewById(R.id.statusIndicator);
        offDutyContainer = findViewById(R.id.offDutyContainer);
        onDutyContainer = findViewById(R. id.onDutyContainer);
        ivDutyOn = findViewById(R.id.ivDutyOn);
        tvDutyStartTime = findViewById(R.id.tvDutyStartTime);
        tvDutyDuration = findViewById(R.id.tvDutyDuration);
        btnToggleDuty = findViewById(R.id.btnToggleDuty);
        summaryCard = findViewById(R. id.summaryCard);
        tvPassengerCount = findViewById(R.id.tvPassengerCount);
        tvActiveSeats = findViewById(R.id.tvActiveSeats);
        tvRevenue = findViewById(R.id. tvRevenue);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDriverInfo() {
        // Driver info
        tvDriverInitial.setText(driver.getInitials());
        tvDriverName.setText(driver.getFullName());
        tvDriverPhone.setText(driver.getPhoneNumber());

        // Vehicle info
        tvVehicleId.setText(driver.getVehicleId());
        tvVehicleRegistration.setText(driver.getVehicleRegistration());
    }

    private void setupListeners() {
        btnToggleDuty.setOnClickListener(v -> toggleDutyStatus());
    }

    private void observeViewModel() {
        viewModel.getDutyState().observe(this, state -> {
            switch (state.getStatus()) {
                case LOADING:
                    showLoading(true, getString(R.string.updating_status));
                    break;

                case SUCCESS:
                    showLoading(false, null);

                    // Update UI based on new duty status
                    boolean isOnDuty = state.getData().isOnDuty();
                    updateDutyUI(isOnDuty, state.getData().getDutyStartedAt());

                    // Show success message
                    String message = isOnDuty ?
                            getString(R.string.duty_started_successfully) :
                            getString(R.string.duty_ended_successfully);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                    // Haptic feedback
                    btnToggleDuty.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM);

                    // Navigate to dashboard if going ON DUTY
                    if (isOnDuty) {
                        navigateToDashboard();
                    }
                    break;

                case ERROR:
                    showLoading(false, null);
                    showErrorDialog(state.getError());

                    // Haptic feedback
                    btnToggleDuty.performHapticFeedback(android.view.HapticFeedbackConstants.REJECT);
                    break;

                case IDLE:
                    showLoading(false, null);
                    break;
            }
        });
    }

    private void loadCurrentDutyStatus() {
        boolean isOnDuty = tokenManager.getDutyStatus();
        String dutyStartedAt = tokenManager.getDutyStartedAt();

        updateDutyUI(isOnDuty, dutyStartedAt);
    }

    private void updateDutyUI(boolean isOnDuty, String dutyStartedAt) {
        if (isOnDuty) {
            // Show ON DUTY state
            offDutyContainer.setVisibility(View.GONE);
            onDutyContainer.setVisibility(View. VISIBLE);
            summaryCard.setVisibility(View.VISIBLE);

            // Update button
            btnToggleDuty. setText(R.string.end_duty);
            btnToggleDuty.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_stop));
            btnToggleDuty.setBackgroundColor(ContextCompat.getColor(this, R.color.danger));

            // Update duty start time
            if (dutyStartedAt != null) {
                tvDutyStartTime.setText(getString(R.string.started_at,
                        DateUtils.formatTime(dutyStartedAt)));

                // Start duration updates
                startDurationUpdates(dutyStartedAt);

                // Start pulse animation
                startPulseAnimation();
            }

            // TODO: Load today's summary stats from API
            loadTodaySummary();

        } else {
            // Show OFF DUTY state
            offDutyContainer.setVisibility(View.VISIBLE);
            onDutyContainer.setVisibility(View.GONE);
            summaryCard.setVisibility(View.GONE);

            // Update button
            btnToggleDuty.setText(R.string.start_duty);
            btnToggleDuty.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
            btnToggleDuty.setBackgroundColor(ContextCompat.getColor(this, R.color.success));

            // Stop duration updates
            stopDurationUpdates();

            // Stop pulse animation
            stopPulseAnimation();
        }
    }

    private void toggleDutyStatus() {
        boolean currentStatus = tokenManager.getDutyStatus();

        if (currentStatus) {
            // Going OFF DUTY - show confirmation
            showEndDutyConfirmation();
        } else {
            // Going ON DUTY - check location permission
            checkLocationPermissionAndStartDuty();
        }
    }

    private void showEndDutyConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.end_duty_confirmation_title)
                .setMessage(R.string.end_duty_confirmation_message)
                .setIcon(R.drawable.ic_duty_off)
                .setPositiveButton(R.string.yes_end_duty, (dialog, which) -> {
                    endDuty();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void endDuty() {
        // Get current location (optional for OFF DUTY)
        getCurrentLocationAndUpdateStatus(false);
    }

    private void checkLocationPermissionAndStartDuty() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Permission granted, get location
            getCurrentLocationAndUpdateStatus(true);

        } else {
            // Request permission
            showLocationPermissionRationale();
        }
    }

    private void showLocationPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.location_permission_title)
                .setMessage(R.string.location_permission_message)
                .setIcon(R.drawable.ic_location)
                .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                    requestLocationPermission();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST
        );
    }

    private void getCurrentLocationAndUpdateStatus(boolean goingOnDuty) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission. ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // No permission, use default location
            updateDutyStatus(goingOnDuty, null);
            return;
        }

        showLoading(true, getString(R.string.getting_location));

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    currentLocation = location;

                    com.cgana.trmsdriver.data. model.Location loc = null;
                    if (location != null) {
                        loc = new com.cgana.trmsdriver.data.model.Location(
                                location.getLatitude(),
                                location.getLongitude()
                        );
                    }

                    updateDutyStatus(goingOnDuty, loc);
                })
                .addOnFailureListener(this, e -> {
                    // Failed to get location, proceed anyway
                    showLoading(false, null);
                    Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_SHORT).show();
                    updateDutyStatus(goingOnDuty, null);
                });
    }

    private void updateDutyStatus(boolean onDuty, com.cgana.trmsdriver.data. model.Location location) {
        String vehicleId = driver.getVehicleId();

        // If location is null, use default (0,0)
        if (location == null) {
            location = new com.cgana.trmsdriver.data.model.Location(0.0, 0.0);
        }

        viewModel.updateDutyStatus(vehicleId, onDuty, location);
    }

    private void startDurationUpdates(String startTime) {
        durationRunnable = new Runnable() {
            @Override
            public void run() {
                String duration = DateUtils.calculateDuration(startTime);
                tvDutyDuration.setText(getString(R.string.duration_format, duration));
                durationHandler.postDelayed(this, 60000); // Update every minute
            }
        };
        durationHandler.post(durationRunnable);
    }

    private void stopDurationUpdates() {
        if (durationRunnable != null) {
            durationHandler.removeCallbacks(durationRunnable);
        }
    }

    private void startPulseAnimation() {
        // Simple alpha animation for duty icon
        ivDutyOn. animate()
                .alpha(0.5f)
                .setDuration(1000)
                .withEndAction(() -> {
                    ivDutyOn.animate()
                            .alpha(1f)
                            .setDuration(1000)
                            .withEndAction(() -> {
                                if (tokenManager.getDutyStatus()) {
                                    startPulseAnimation(); // Repeat
                                }
                            })
                            .start();
                })
                .start();
    }

    private void stopPulseAnimation() {
        if(ivDutyOn != null){
            ivDutyOn.clearAnimation();
            ivDutyOn.setAlpha(1f);
        }
    }

    private void loadTodaySummary() {
        // TODO: Implement API call to get today's summary
        // For now, show placeholder data
        tvPassengerCount. setText("0");
        tvActiveSeats. setText("0/4");
        tvRevenue.setText("0 MK");
    }

    private void showLoading(boolean show, String message) {
        if (show) {
            loadingOverlay.setVisibility(View.VISIBLE);
            if (message != null) {
                tvLoadingMessage.setText(message);
            }
        } else {
            loadingOverlay. setVisibility(View.GONE);
        }
    }

    private void showErrorDialog(String message) {
        new AlertDialog. Builder(this)
                .setTitle(R.string.error)
                .setMessage(message)
                .setIcon(R. drawable.ic_error)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void navigateToDashboard() {
        // Delay navigation slightly for better UX
        btnToggleDuty.postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 800);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_duty_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmation() {
        new AlertDialog. Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R. string.logout_confirmation)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    logout();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void logout() {
        // Clear duty data
        tokenManager.clearDutyData();

        // Clear auth if remember me is not checked
        if (!tokenManager.getRememberMe()) {
            tokenManager.clearAuth();
        }

        navigateToLogin();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start duty
                getCurrentLocationAndUpdateStatus(true);

                // Check for background location permission (Android 10+)
                if (Build. VERSION.SDK_INT >= Build. VERSION_CODES.Q) {
                    requestBackgroundLocationPermission();
                }
            } else {
                // Permission denied
                showPermissionDeniedDialog();
            }
        }
    }

    private void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES. Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                new AlertDialog.Builder(this)
                        .setTitle(R. string.background_location_title)
                        .setMessage(R.string.background_location_message)
                        .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                            ActivityCompat.requestPermissions(
                                    this,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    BACKGROUND_LOCATION_PERMISSION_REQUEST
                            );
                        })
                        .setNegativeButton(R.string.skip, null)
                        .show();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_denied)
                .setMessage(R.string.permission_denied_message)
                .setIcon(R.drawable.ic_error)
                .setPositiveButton(R.string.open_settings, (dialog, which) -> {
                    openAppSettings();
                })
                .setNegativeButton(R. string.cancel, null)
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDurationUpdates();
        stopPulseAnimation();
    }

    @Override
    public void onBackPressed() {
        // If ON DUTY, don't allow back - must use End Duty button
        if (tokenManager.getDutyStatus()) {
            Toast.makeText(this, R. string.use_end_duty_button, Toast.LENGTH_SHORT).show();
            return;
        }

        // If OFF DUTY, move to background
        moveTaskToBack(true);
    }
}