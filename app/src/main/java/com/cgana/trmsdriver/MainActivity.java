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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.AlightingResponse;
import com.cgana.trmsdriver.data.model.BoardingResponse;
import com.cgana.trmsdriver.data.model.DashboardResponse;
import com.cgana.trmsdriver.data.model.Location;
import com.cgana.trmsdriver.data.model.MissedStopResponse;
import com.cgana.trmsdriver.data.model.SeatStatus;
import com.cgana.trmsdriver.data.repository.AlightingRepository;
import com.cgana.trmsdriver.ui.alighting.AlightingConfirmationDialog;
import com.cgana.trmsdriver.ui.alighting.MissedStopDialog;
import com.cgana.trmsdriver.ui.auth.LoginActivity;
import com.cgana.trmsdriver.ui.dashboard.BoardingDialog;
import com.cgana.trmsdriver.ui.dashboard.DashboardViewModel;
import com.cgana.trmsdriver.data.repository.DashboardRepository;
import com.cgana.trmsdriver.ui.dashboard.DashboardViewModelFactory;
import com.cgana.trmsdriver.ui.dashboard.SeatCardBinder;
import com.cgana.trmsdriver.ui.duty.DutyStatusActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MainActivity - Real-Time Dashboard with 4-Seat Grid (Module 2 Part 4)
 * Displays live seat status, handles boarding, and auto-refreshes every 5 seconds
 * Enhanced with professional boarding dialog and error handling
 */
public class MainActivity extends AppCompatActivity implements BoardingDialog.BoardingDialogListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_DESTINATION_SELECTION = 2001;

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
    private AlightingRepository alightingRepository; // Module 4 Part 2

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

        // Initialize Alighting Repository (Module 4 Part 2)
        alightingRepository = new AlightingRepository(tokenManager);

        // Setup observers
        setupObservers();

        // Setup toolbar menu
        setupToolbarMenu();

        // Setup swipe refresh
        setupSwipeRefresh();

        // Initialize seat cards
        initializeSeatCards();

        // Load initial data and start auto-refresh (Module 2 Part 3)
        android.util.Log.d("MainActivity", "onCreate: Loading dashboard data for vehicleId: " + vehicleId);
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
    }

    private void setupObservers() {
        // Dashboard state observer (Module 2 Part 4 - Enhanced with state management)
        viewModel.getDashboardState().observe(this, state -> {
            android.util.Log.d("MainActivity", "Dashboard state: " + state.getStatus());

            switch (state.getStatus()) {
                case LOADING:
                    // Don't show loading overlay for refresh (SwipeRefresh handles it)
                    android.util.Log.d("MainActivity", "Loading dashboard...");
                    break;

                case SUCCESS:
                    loadingOverlay.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    android.util.Log.d("MainActivity", "Dashboard loaded successfully");
                    if (state.getData() != null && state.getData().getSeats() != null) {
                        android.util.Log.d("MainActivity", "Seats count: " + state.getData().getSeats().size());
                    }
                    updateDashboard(state.getData());
                    tvLastUpdated.setText(getString(R.string.just_now));
                    break;

                case ERROR:
                    loadingOverlay.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    android.util.Log.e("MainActivity", "Dashboard error: " + state.getError());
                    showError(state.getError());
                    break;

                case IDLE:
                default:
                    loadingOverlay.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    android.util.Log.d("MainActivity", "Dashboard idle");
                    break;
            }
        });

        // Boarding state observer (Module 2 Part 4 - Enhanced with state management)
        // Module 3 Part 3: Auto-launch destination selection after boarding
        viewModel.getBoardingState().observe(this, state -> {
            android.util.Log.d("MainActivity", "Boarding state changed: " + state.getStatus());

            switch (state.getStatus()) {
                case LOADING:
                    android.util.Log.d("MainActivity", "Boarding: LOADING");
                    loadingOverlay.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    android.util.Log.d("MainActivity", "Boarding: SUCCESS");
                    loadingOverlay.setVisibility(View.GONE);

                    BoardingResponse response = state.getData();

                    if (response == null) {
                        android.util.Log.e("MainActivity", "BoardingResponse is NULL!");
                        Toast.makeText(this, "Boarding response is null", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    android.util.Log.d("MainActivity", "BoardingResponse - journeyId: " + response.getJourneyId() +
                        ", seatNumber: " + response.getSeatNumber() +
                        ", message: " + response.getMessage());

                    // Show success message
                    Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();

                    // Haptic feedback
                    findViewById(android.R.id.content).performHapticFeedback(
                        android.view.HapticFeedbackConstants.CONFIRM
                    );

                    // AUTO-LAUNCH DESTINATION SELECTION (Module 3 Part 3)
                    // Check if journey_id is present before launching
                    if (response.getJourneyId() != null && response.getJourneyId() > 0) {
                        android.util.Log.d("MainActivity", "Launching destination selection activity...");
                        launchDestinationSelection(
                            response.getJourneyId(),
                            response.getSeatNumber()
                        );
                    } else {
                        android.util.Log.e("MainActivity", "Journey ID is null in boarding response! Cannot launch destination selection.");
                        Toast.makeText(this, "Boarding successful but journey ID missing. Please tap the seat again to select destination.", Toast.LENGTH_LONG).show();
                        // Refresh dashboard to show the awaiting state
                        viewModel.refreshNow();
                    }

                    // Dashboard will auto-refresh
                    break;

                case ERROR:
                    android.util.Log.e("MainActivity", "Boarding: ERROR - " + state.getError());
                    loadingOverlay.setVisibility(View.GONE);
                    showBoardingError(state.getError());
                    break;

                case IDLE:
                default:
                    android.util.Log.d("MainActivity", "Boarding: IDLE");
                    loadingOverlay.setVisibility(View.GONE);
                    break;
            }
        });
    }

    private void setupToolbarMenu() {
//        toolbar.inflateMenu(R.menu.menu_dashboard);
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_journey_history) {
                // Navigate to Journey History (Module 5)
                Intent intent = new Intent(this, com.cgana.trmsdriver.ui.history.JourneyHistoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_refresh) {
                viewModel.refreshNow();
                return true;
            } else if (itemId == R.id.action_end_duty) {
                showEndDutyDialog();
                return true;
            } else if (itemId == R.id.action_settings) {
                showSettings();
                return true;
            } else if (itemId == R.id.action_help) {
                showHelp();
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
            seatCard.setOnClickListener(v -> {
                android.util.Log.d("MainActivity", "Seat card clicked: " + finalSeatNumber);
                Toast.makeText(MainActivity.this, "Seat " + finalSeatNumber + " clicked!", Toast.LENGTH_SHORT).show();
                onSeatCardClicked(finalSeatNumber);
            });

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

    private void onSeatCardClicked(int seatNumber) {
        // Get current seat status from state (Module 2 Part 4, Module 4 Part 2)
        android.util.Log.d("MainActivity", "Seat " + seatNumber + " clicked");

        DashboardViewModel.DashboardState state = viewModel.getDashboardState().getValue();
        if (state == null) {
            android.util.Log.e("MainActivity", "State is null");
            Toast.makeText(this, "Dashboard data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (state.getData() == null) {
            android.util.Log.e("MainActivity", "Dashboard data is null");
            Toast.makeText(this, "Dashboard data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (state.getData().getSeats() == null) {
            android.util.Log.e("MainActivity", "Seats data is null");
            Toast.makeText(this, "Seats data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        DashboardResponse dashboard = state.getData();
        SeatStatus seat = null;
        for (SeatStatus s : dashboard.getSeats()) {
            if (s.getSeatNumber() == seatNumber) {
                seat = s;
                break;
            }
        }

        if (seat == null) {
            android.util.Log.e("MainActivity", "Seat " + seatNumber + " not found in seats list");
            Toast.makeText(this, "Seat " + seatNumber + " data not found", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("MainActivity", "Seat " + seatNumber + " status: " + seat.getStatus());

        // Handle based on seat status
        if (seat.isVacant()) {
            android.util.Log.d("MainActivity", "Showing boarding dialog for seat " + seatNumber);
            // Boarding flow (Module 2)
            confirmBoarding(seatNumber);
        } else if (seat.isActive() || seat.isApproaching()) {
            android.util.Log.d("MainActivity", "Showing alighting dialog for seat " + seatNumber);
            // Alighting flow (Module 4 Part 2)
            showAlightingConfirmationDialog(seat);
        } else if (seat.isAwaiting()) {
            android.util.Log.d("MainActivity", "Seat " + seatNumber + " is awaiting destination - launching destination selection");
            // Awaiting destination selection - allow manual launch
            if (seat.getJourneyId() != null && seat.getJourneyId() > 0) {
                android.util.Log.d("MainActivity", "Launching destination selection for journey: " + seat.getJourneyId());
                launchDestinationSelection(seat.getJourneyId(), seatNumber);
            } else {
                android.util.Log.e("MainActivity", "Journey ID not available for awaiting seat");
                Toast.makeText(this, "Journey data not available. Please try boarding again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            android.util.Log.d("MainActivity", "Seat " + seatNumber + " unknown status: " + seat.getStatus());
            // Other states
            Toast.makeText(this, "Seat " + seatNumber + " is " + seat.getStatus(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmBoarding(int seatNumber) {
        android.util.Log.d("MainActivity", "confirmBoarding called for seat " + seatNumber);
        Toast.makeText(this, "Opening boarding dialog for seat " + seatNumber, Toast.LENGTH_SHORT).show();

        // Show professional Material dialog (Module 2 Part 4)
        BoardingDialog dialog = BoardingDialog.newInstance(seatNumber);
        dialog.show(getSupportFragmentManager(), "BoardingDialog");
    }

    /**
     * Implementation of BoardingDialog.BoardingDialogListener (Module 2 Part 4)
     * Called when user confirms boarding in the dialog
     */
    @Override
    public void onConfirmBoarding(int seatNumber) {
        recordBoarding(seatNumber);
    }

    @Override
    public void onCancelBoarding() {
        // User cancelled boarding - no action needed
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

        // Get current location and record boarding (Module 2 Part 4)
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    double latitude = -15.7891; // Default
                    double longitude = 35.0412; // Default

                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }

                    viewModel.recordBoarding(seatNumber, latitude, longitude);
                })
                .addOnFailureListener(e -> {
                    // Use default location on failure
                    viewModel.recordBoarding(seatNumber, -15.7891, 35.0412);
                });
    }

    // Auto-refresh now handled by ViewModel (Module 2 Part 3)
    // See onResume() and onPause() methods

    /**
     * Show boarding success message (Module 2 Part 4)
     */
    private void showBoardingSuccess(String message) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.boarding_success)
            .setMessage(message != null ? message : getString(R.string.boarding_success_message))
            .setIcon(R.drawable.ic_check)
            .setPositiveButton(R.string.ok, null)
            .show();

        // Haptic feedback
        findViewById(android.R.id.content).performHapticFeedback(
            android.view.HapticFeedbackConstants.CONFIRM
        );
    }

    /**
     * Show boarding error message (Module 2 Part 4)
     */
    private void showBoardingError(String error) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.boarding_failed)
            .setMessage(error != null ? error : getString(R.string.boarding_failed_message))
            .setIcon(R.drawable.ic_error)
            .setPositiveButton(R.string.ok, null)
            .show();

        // Haptic feedback
        findViewById(android.R.id.content).performHapticFeedback(
            android.view.HapticFeedbackConstants.REJECT
        );
    }

    /**
     * Show general error message (Module 2 Part 4)
     */
    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    /**
     * Open Settings Activity (Module 7)
     */
    private void showSettings() {
        Intent intent = new Intent(this, com.cgana.trmsdriver.ui.settings.SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Help placeholder (Module 2 Part 5)
     */
    private void showHelp() {
        Intent intent = new Intent(this, com.cgana.trmsdriver.ui.settings.HelpSupportActivity.class);
        startActivity(intent);
    }

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

    /**
     * Launch destination selection activity (Module 3 Part 3)
     */
    private void launchDestinationSelection(long journeyId, int seatNumber) {
        android.util.Log.d("MainActivity", "launchDestinationSelection called");
        android.util.Log.d("MainActivity", "  - journeyId: " + journeyId);
        android.util.Log.d("MainActivity", "  - seatNumber: " + seatNumber);
        android.util.Log.d("MainActivity", "  - vehicleId: " + vehicleId);

        try {
            Intent intent = new Intent(this, com.cgana.trmsdriver.ui.destination.DestinationSelectionActivity.class);
            intent.putExtra(com.cgana.trmsdriver.ui.destination.DestinationSelectionActivity.EXTRA_VEHICLE_ID, vehicleId);
            intent.putExtra(com.cgana.trmsdriver.ui.destination.DestinationSelectionActivity.EXTRA_JOURNEY_ID, journeyId);
            intent.putExtra(com.cgana.trmsdriver.ui.destination.DestinationSelectionActivity.EXTRA_SEAT_NUMBER, seatNumber);

            android.util.Log.d("MainActivity", "Starting DestinationSelectionActivity...");
            startActivityForResult(intent, REQUEST_DESTINATION_SELECTION);
            android.util.Log.d("MainActivity", "Activity started successfully");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error launching destination selection", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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

    /**
     * Handle result from destination selection (Module 3 Part 3)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DESTINATION_SELECTION && resultCode == RESULT_OK) {
            // Destination was set successfully, refresh dashboard
            viewModel.refreshNow();
        }
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

    // ==================== Module 4 Part 2: Alighting Methods ====================

    /**
     * Show alighting confirmation dialog (Module 4 Part 2)
     */
    private void showAlightingConfirmationDialog(SeatStatus seat) {
        AlightingConfirmationDialog dialog = AlightingConfirmationDialog.newInstance(seat);
        dialog.setListener(new AlightingConfirmationDialog.AlightingDialogListener() {
            @Override
            public void onConfirmAlighting(long journeyId, int seatNumber, boolean fareCollected) {
                recordAlighting(journeyId, seatNumber, fareCollected, false);
            }

            @Override
            public void onMissedStop(long journeyId, int seatNumber) {
                showMissedStopDialog(journeyId, seatNumber);
            }
        });
        dialog.show(getSupportFragmentManager(), "AlightingDialog");
    }

    /**
     * Show missed stop dialog (Module 4 Part 2)
     */
    private void showMissedStopDialog(long journeyId, int seatNumber) {
        MissedStopDialog dialog = MissedStopDialog.newInstance(journeyId, seatNumber);
        dialog.setListener((jId, seatNum, notes) -> {
            reportMissedStop(jId, seatNum, notes);
        });
        dialog.show(getSupportFragmentManager(), "MissedStopDialog");
    }

    /**
     * Record passenger alighting (Module 4 Part 2)
     */
    private void recordAlighting(long journeyId, int seatNumber, boolean fareCollected, boolean missedStop) {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndRecordAlighting(journeyId, seatNumber, fareCollected, missedStop);
        } else {
            // Use default location
            performAlighting(journeyId, seatNumber, fareCollected, missedStop, -15.7891, 35.0412);
        }
    }

    /**
     * Get location and record alighting (Module 4 Part 2)
     */
    private void getCurrentLocationAndRecordAlighting(long journeyId, int seatNumber,
                                                     boolean fareCollected, boolean missedStop) {
        loadingOverlay.setVisibility(View.VISIBLE);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener(this, location -> {
                double latitude, longitude;
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    latitude = -15.7891;
                    longitude = 35.0412;
                }
                performAlighting(journeyId, seatNumber, fareCollected, missedStop, latitude, longitude);
            })
            .addOnFailureListener(this, e -> {
                // Use default location on failure
                performAlighting(journeyId, seatNumber, fareCollected, missedStop, -15.7891, 35.0412);
            });
    }

    /**
     * Perform alighting API call (Module 4 Part 2)
     */
    private void performAlighting(long journeyId, int seatNumber, boolean fareCollected,
                                  boolean missedStop, double latitude, double longitude) {
        loadingOverlay.setVisibility(View.VISIBLE);

        Location alightingLocation = new Location(latitude, longitude);

        LiveData<AlightingRepository.Result<AlightingResponse>> result =
            alightingRepository.recordAlighting(vehicleId, journeyId, seatNumber,
                                              alightingLocation, fareCollected, missedStop);

        result.observeForever(alightingResult -> {
            loadingOverlay.setVisibility(View.GONE);

            if (alightingResult != null && alightingResult.isSuccess()) {
                showAlightingSuccess(alightingResult.getData());
                // Refresh dashboard
                viewModel.refreshNow();
            } else {
                String error = alightingResult != null ? alightingResult.getError() : "Unknown error";
                showError(error);
            }
        });
    }

    /**
     * Report missed stop (Module 4 Part 2)
     */
    private void reportMissedStop(long journeyId, int seatNumber, String notes) {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndReportMissed(journeyId, seatNumber, notes);
        } else {
            // Use default location
            performReportMissed(journeyId, seatNumber, notes, -15.7891, 35.0412);
        }
    }

    /**
     * Get location and report missed stop (Module 4 Part 2)
     */
    private void getCurrentLocationAndReportMissed(long journeyId, int seatNumber, String notes) {
        loadingOverlay.setVisibility(View.VISIBLE);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener(this, location -> {
                double latitude, longitude;
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    latitude = -15.7891;
                    longitude = 35.0412;
                }
                performReportMissed(journeyId, seatNumber, notes, latitude, longitude);
            })
            .addOnFailureListener(this, e -> {
                // Use default location on failure
                performReportMissed(journeyId, seatNumber, notes, -15.7891, 35.0412);
            });
    }

    /**
     * Perform report missed stop API call (Module 4 Part 2)
     */
    private void performReportMissed(long journeyId, int seatNumber, String notes,
                                    double latitude, double longitude) {
        loadingOverlay.setVisibility(View.VISIBLE);

        Location missedLocation = new Location(latitude, longitude);

        LiveData<AlightingRepository.Result<MissedStopResponse>> result =
            alightingRepository.reportMissedStop(vehicleId, journeyId, seatNumber,
                                               missedLocation, notes);

        result.observeForever(missedResult -> {
            loadingOverlay.setVisibility(View.GONE);

            if (missedResult != null && missedResult.isSuccess()) {
                showMissedStopSuccess(missedResult.getData().getMessage());
                // Refresh dashboard
                viewModel.refreshNow();
            } else {
                String error = missedResult != null ? missedResult.getError() : "Unknown error";
                showError(error);
            }
        });
    }

    /**
     * Show alighting success dialog (Module 4 Part 2)
     */
    private void showAlightingSuccess(AlightingResponse response) {
        String message = response.getMessage();
        if (response.getJourneySummary() != null) {
            message += "\n\n" + response.getJourneySummary().getFormattedDuration() +
                      " · " + response.getJourneySummary().getFormattedDistance();
        }

        new AlertDialog.Builder(this)
            .setTitle(R.string.journey_completed)
            .setMessage(message)
            .setIcon(R.drawable.ic_check_circle)
            .setPositiveButton(R.string.ok, null)
            .show();

        // Haptic feedback
        findViewById(android.R.id.content).performHapticFeedback(
            android.view.HapticFeedbackConstants.CONFIRM
        );
    }

    /**
     * Show missed stop success (Module 4 Part 2)
     */
    private void showMissedStopSuccess(String message) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.missed_stop_reported)
            .setMessage(message)
            .setIcon(R.drawable.ic_alert)
            .setPositiveButton(R.string.ok, null)
            .show();
    }

    // ==================== Module 5: Menu & Navigation ====================
    // Menu handling is done in setupToolbarMenu() method above
    // No need for onCreateOptionsMenu() since toolbar.inflateMenu() is used directly
}

