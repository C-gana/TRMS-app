package com.cgana.trmsdriver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.DashboardResponse;
import com.cgana.trmsdriver.data.model.SeatStatus;
import com.cgana.trmsdriver.ui.auth.LoginActivity;
import com.cgana.trmsdriver.ui.dashboard.BoardingDialogFragment;
import com.cgana.trmsdriver.ui.dashboard.DashboardViewModel;
import com.cgana.trmsdriver.data.repository.DashboardRepository;
import com.cgana.trmsdriver.ui.dashboard.DashboardViewModelFactory;
import com.cgana.trmsdriver.ui.dashboard.SeatCardBinder;
import com.cgana.trmsdriver.ui.duty.DutyStatusActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MainActivity - Real-Time Dashboard with 4-Seat Grid (Module 2)
 * Displays live seat status, handles boarding, and auto-refreshes every 5 seconds
 */
public class MainActivity extends AppCompatActivity implements BoardingDialogFragment.BoardingConfirmListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private GridLayout seatsGrid;
    private TextView tvVehicleId;
    private TextView tvVehicleRegistration;
    private TextView tvDutyStatus;
    private TextView tvDutyDuration;
    private TextView tvActiveSeats;
    private TextView tvTodayPassengers;
    private TextView tvTodayRevenue;
    private TextView tvLastUpdated;
    private View loadingOverlay;

    // ViewModels & Data
    private DashboardViewModel viewModel;
    private TokenManager tokenManager;
    private FusedLocationProviderClient fusedLocationClient;

    // Seat card views cache
    private final Map<Integer, View> seatCardViews = new HashMap<>();

    // Current vehicle ID
    private String vehicleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize TokenManager and Location Client
        tokenManager = new TokenManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check authentication - redirect to login if not authenticated
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Check duty status - redirect to duty screen if not on duty
        if (!tokenManager.getDutyStatus()) {
            navigateToDutyStatus();
            return;
        }

        // Get vehicle ID from driver data
        if (tokenManager.getDriver() != null && tokenManager.getDriver().getVehicleId() != null) {
            vehicleId = tokenManager.getDriver().getVehicleId();
        } else {
            // Fallback vehicle ID (should not happen in production)
            vehicleId = "TRM-BT-001";
        }

        // User is authenticated and on duty - show dashboard
        setContentView(R.layout.activity_main);

        // Initialize views
        initializeViews();

        // Initialize ViewModel (Module 2 Part 3)
        DashboardRepository repository = new DashboardRepository(this);
        DashboardViewModelFactory factory = new DashboardViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        // Setup observers
        setupObservers();

        // Setup toolbar menu
        setupToolbarMenu();

        // Setup swipe refresh
        setupSwipeRefresh();

        // Initialize seat cards
        initializeSeatCards();

        // Load initial data and start auto-refresh (Module 2 Part 3)
        viewModel.loadDashboardData(vehicleId);
        viewModel.startAutoRefresh(vehicleId);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        seatsGrid = findViewById(R.id.seatsGrid);
        tvVehicleId = findViewById(R.id.tvVehicleId);
        tvVehicleRegistration = findViewById(R.id.tvVehicleRegistration);
        tvDutyStatus = findViewById(R.id.tvDutyStatus);
        tvDutyDuration = findViewById(R.id.tvDutyDuration);
        tvActiveSeats = findViewById(R.id.tvActiveSeats);
        tvTodayPassengers = findViewById(R.id.tvTodayPassengers);
        tvTodayRevenue = findViewById(R.id.tvTodayRevenue);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        setSupportActionBar(toolbar);
    }

    private void setupObservers() {
        // Dashboard data observer
        viewModel.getDashboardData().observe(this, this::updateDashboard);

        // Boarding result observer
        viewModel.getBoardingResult().observe(this, boardingResponse -> {
            if (boardingResponse != null && boardingResponse.isSuccess()) {
                Toast.makeText(this, R.string.boarding_success, Toast.LENGTH_SHORT).show();
                viewModel.clearBoardingResult();
            }
        });

        // Loading state observer
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                swipeRefresh.setRefreshing(false);
            }
        });

        // Error message observer
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });

        // Last update time observer
        viewModel.getLastUpdateTime().observe(this, timestamp -> {
            if (timestamp != null) {
                updateLastUpdatedText(timestamp);
            }
        });
    }

    private void setupToolbarMenu() {
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_refresh) {
                loadDashboardData();
                return true;
            } else if (itemId == R.id.action_end_duty) {
                showEndDutyDialog();
                return true;
            } else if (itemId == R.id.action_logout) {
                showLogoutDialog();
                return true;
            }

            return false;
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> viewModel.refreshNow()); // Module 2 Part 3
    }

    private void initializeSeatCards() {
        seatsGrid.removeAllViews();
        seatCardViews.clear();

        LayoutInflater inflater = LayoutInflater.from(this);

        // Create 4 seat cards (2x2 grid)
        for (int seatNumber = 1; seatNumber <= 4; seatNumber++) {
            View seatCard = inflater.inflate(R.layout.item_seat_card, seatsGrid, false);

            // Configure GridLayout params
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = (int) getResources().getDimension(R.dimen.seat_card_size);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(
                (int) getResources().getDimension(R.dimen.seat_card_margin),
                (int) getResources().getDimension(R.dimen.seat_card_margin),
                (int) getResources().getDimension(R.dimen.seat_card_margin),
                (int) getResources().getDimension(R.dimen.seat_card_margin)
            );
            seatCard.setLayoutParams(params);

            // Set click listener
            final int finalSeatNumber = seatNumber;
            seatCard.setOnClickListener(v -> onSeatCardClicked(finalSeatNumber));

            // Add to grid and cache
            seatsGrid.addView(seatCard);
            seatCardViews.put(seatNumber, seatCard);
        }
    }

    private void loadDashboardData() {
        if (vehicleId != null && !vehicleId.isEmpty()) {
            viewModel.loadDashboardData(vehicleId);
        }
    }

    private void updateDashboard(DashboardResponse dashboard) {
        if (dashboard == null) {
            return;
        }

        // Update vehicle info
        if (dashboard.getVehicleId() != null) {
            tvVehicleId.setText(dashboard.getVehicleId());
        }
        if (dashboard.getRegistration() != null) {
            tvVehicleRegistration.setText(dashboard.getRegistration());
        }

        // Update duty duration
        updateDutyDuration();

        // Update active seats count
        int activeCount = dashboard.getActiveJourneys();
        tvActiveSeats.setText(getString(R.string.active_seats, activeCount));

        // Update seats
        updateSeatCards(dashboard.getSeats());

        // Update today's stats
        if (dashboard.getTodaysStats() != null) {
            tvTodayPassengers.setText(String.valueOf(dashboard.getTodaysStats().getPassengers()));
            tvTodayRevenue.setText(formatRevenue(dashboard.getTodaysStats().getRevenue()));
        }
    }

    private void updateSeatCards(List<SeatStatus> seats) {
        if (seats == null || seats.isEmpty()) {
            return;
        }

        for (SeatStatus seat : seats) {
            View seatCard = seatCardViews.get(seat.getSeatNumber());
            if (seatCard != null) {
                SeatCardBinder.bindSeatCard(seatCard, seat, this);
            }
        }
    }

    private void updateDutyDuration() {
        // Get duty start time from TokenManager (Module 2 Part 3)
        String dutyStartTime = tokenManager.getDutyStartedAt();
        if (dutyStartTime != null && !dutyStartTime.isEmpty()) {
            String duration = com.cgana.trmsdriver.utils.DateUtils.calculateDuration(dutyStartTime);
            tvDutyDuration.setText(getString(R.string.duration_format, duration));
        } else {
            tvDutyDuration.setText(getString(R.string.duration_format, "0m"));
        }
    }

    private void updateLastUpdatedText(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000;

        String text;
        if (seconds < 5) {
            text = getString(R.string.just_now);
        } else if (seconds < 60) {
            text = getString(R.string.seconds_ago, seconds);
        } else {
            long minutes = seconds / 60;
            text = getString(R.string.minutes_ago, minutes);
        }

        tvLastUpdated.setText(getString(R.string.last_updated, text));
    }

    private void onSeatCardClicked(int seatNumber) {
        // Get current seat status
        DashboardResponse dashboard = viewModel.getDashboardData().getValue();
        if (dashboard == null || dashboard.getSeats() == null) {
            return;
        }

        SeatStatus seat = null;
        for (SeatStatus s : dashboard.getSeats()) {
            if (s.getSeatNumber() == seatNumber) {
                seat = s;
                break;
            }
        }

        if (seat == null) {
            return;
        }

        // Only allow boarding on vacant seats
        if (seat.isVacant()) {
            confirmBoarding(seatNumber);
        } else {
            // For non-vacant seats, you can show details dialog
            Toast.makeText(this, "Seat " + seatNumber + " is " + seat.getStatus(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmBoarding(int seatNumber) {
        // Get seat object for the dialog
        DashboardResponse dashboard = viewModel.getDashboardData().getValue();
        if (dashboard == null || dashboard.getSeats() == null) {
            return;
        }

        SeatStatus seat = null;
        for (SeatStatus s : dashboard.getSeats()) {
            if (s.getSeatNumber() == seatNumber) {
                seat = s;
                break;
            }
        }

        if (seat == null) return;

        // Show professional Material dialog (Module 2 Part 3)
        // Convert SeatStatus to Seat for dialog (they should be compatible)
        com.cgana.trmsdriver.data.model.Seat dialogSeat = new com.cgana.trmsdriver.data.model.Seat();
        dialogSeat.setSeat_number(seat.getSeatNumber());
        dialogSeat.setStatus(seat.getStatus());

        BoardingDialogFragment dialog = BoardingDialogFragment.newInstance(dialogSeat);
        dialog.show(getSupportFragmentManager(), "BoardingDialog");
    }

    /**
     * Implementation of BoardingConfirmListener (Module 2 Part 3)
     * Called when user confirms boarding in the dialog
     */
    @Override
    public void onConfirmBoarding(int seatNumber) {
        recordBoarding(seatNumber);
    }

    private void recordBoarding(int seatNumber) {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Get current location and record boarding
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    double latitude = -15.7891; // Default
                    double longitude = 35.0412; // Default

                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }

                    viewModel.recordBoarding(vehicleId, seatNumber, latitude, longitude);
                })
                .addOnFailureListener(e -> {
                    // Use default location on failure
                    viewModel.recordBoarding(vehicleId, seatNumber, -15.7891, 35.0412);
                });
    }

    // Auto-refresh now handled by ViewModel (Module 2 Part 3)
    // See onResume() and onPause() methods

    private void showEndDutyDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.end_duty_confirmation_title)
                .setMessage(R.string.end_duty_confirmation_message)
                .setPositiveButton(R.string.yes_end_duty, (dialog, which) -> {
                    navigateToDutyStatus();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    tokenManager.clearAuth();
                    navigateToLogin();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToDutyStatus() {
        Intent intent = new Intent(this, DutyStatusActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String formatRevenue(int revenue) {
        return String.format(Locale.getDefault(), "%,d", revenue);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start ViewModel auto-refresh (Module 2 Part 3)
        viewModel.startAutoRefresh(vehicleId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop ViewModel auto-refresh to save battery and network
        viewModel.stopAutoRefresh();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to login/duty when on dashboard
        // User must logout or end duty explicitly
        moveTaskToBack(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

