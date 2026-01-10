package com.cgana.trmsdriver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.cgana.trmsdriver.data.model.DashboardResponse;
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
        // Dashboard state observer (Module 2 Part 4 - Enhanced with state management)
        viewModel.getDashboardState().observe(this, state -> {
            switch (state.getStatus()) {
                case LOADING:
                    // Don't show loading overlay for refresh (SwipeRefresh handles it)
                    break;

                case SUCCESS:
                    loadingOverlay.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    updateDashboard(state.getData());
                    tvLastUpdated.setText(getString(R.string.just_now));
                    break;

                case ERROR:
                    loadingOverlay.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    showError(state.getError());
                    break;

                case IDLE:
                default:
                    loadingOverlay.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    break;
            }
        });

        // Boarding state observer (Module 2 Part 4 - Enhanced with state management)
        // Module 3 Part 3: Auto-launch destination selection after boarding
        viewModel.getBoardingState().observe(this, state -> {
            switch (state.getStatus()) {
                case LOADING:
                    loadingOverlay.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    loadingOverlay.setVisibility(View.GONE);

                    // Show success message
                    Toast.makeText(this, state.getData().getMessage(), Toast.LENGTH_SHORT).show();

                    // Haptic feedback
                    findViewById(android.R.id.content).performHapticFeedback(
                        android.view.HapticFeedbackConstants.CONFIRM
                    );

                    // AUTO-LAUNCH DESTINATION SELECTION (Module 3 Part 3)
                    launchDestinationSelection(
                        state.getData().getJourneyId(),
                        state.getData().getSeatNumber()
                    );

                    // Dashboard will auto-refresh
                    break;

                case ERROR:
                    loadingOverlay.setVisibility(View.GONE);
                    showBoardingError(state.getError());
                    break;

                case IDLE:
                default:
                    loadingOverlay.setVisibility(View.GONE);
                    break;
            }
        });
    }

    private void setupToolbarMenu() {
        toolbar.inflateMenu(R.menu.menu_dashboard);
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_refresh) {
                viewModel.refreshNow();
                return true;
            } else if (itemId == R.id.action_end_duty) {
                showEndDutyDialog();
                return true;
            } else if (itemId == R.id.action_settings) {
                showSettingsPlaceholder();
                return true;
            } else if (itemId == R.id.action_help) {
                showHelpPlaceholder();
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

    private void onSeatCardClicked(int seatNumber) {
        // Get current seat status from state (Module 2 Part 4, Module 4 Part 2)
        DashboardViewModel.DashboardState state = viewModel.getDashboardState().getValue();
        if (state == null || state.getData() == null || state.getData().getSeats() == null) {
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
            return;
        }

        // Handle based on seat status
        if (seat.isVacant()) {
            // Boarding flow (Module 2)
            confirmBoarding(seatNumber);
        } else if (seat.isActive() || seat.isApproaching()) {
            // Alighting flow (Module 4 Part 2)
            showAlightingConfirmationDialog(seat);
        } else if (seat.isAwaiting()) {
            // Awaiting destination selection
            Toast.makeText(this, R.string.awaiting_destination_selection, Toast.LENGTH_SHORT).show();
        } else {
            // Other states
            Toast.makeText(this, "Seat " + seatNumber + " is " + seat.getStatus(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmBoarding(int seatNumber) {
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
     * Settings placeholder (Module 2 Part 5)
     */
    private void showSettingsPlaceholder() {
        Toast.makeText(this, "Settings - Coming in future update", Toast.LENGTH_SHORT).show();
    }

    /**
     * Help placeholder (Module 2 Part 5)
     */
    private void showHelpPlaceholder() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.help)
            .setMessage("TRMS Driver App\nVersion 1.0.0\n\nFor support:\nEmail: support@trms.mw\nPhone: +265 888 123 456")
            .setPositiveButton(R.string.ok, null)
            .show();
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
        Intent intent = new Intent(this, com.cgana.trmsdriver.ui.destination.DestinationSelectionActivity.class);
        intent.putExtra(com.cgana.trmsdriver.ui.destination.DestinationSelectionActivity.EXTRA_VEHICLE_ID, vehicleId);
        intent.putExtra(com.cgana.trmsdriver.ui.destination.DestinationSelectionActivity.EXTRA_JOURNEY_ID, journeyId);
        intent.putExtra(com.cgana.trmsdriver.ui.destination.DestinationSelectionActivity.EXTRA_SEAT_NUMBER, seatNumber);
        startActivityForResult(intent, REQUEST_DESTINATION_SELECTION);
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

        LiveData<AlightingRepository.Result<AlightingResponse>> result =
            alightingRepository.recordAlighting(vehicleId, journeyId, seatNumber,
                                              latitude, longitude, fareCollected, missedStop);

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

        LiveData<AlightingRepository.Result<MissedStopResponse>> result =
            alightingRepository.reportMissedStop(vehicleId, journeyId, seatNumber,
                                               latitude, longitude, notes);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_journey_history) {
            // Navigate to Journey History (Module 5)
            Intent intent = new Intent(this, com.cgana.trmsdriver.ui.history.JourneyHistoryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_refresh) {
            // Refresh dashboard
            viewModel.refreshNow();
            return true;
        } else if (id == R.id.action_settings) {
            // Navigate to Settings (Module 7)
            Intent intent = new Intent(this, com.cgana.trmsdriver.ui.settings.SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_end_duty) {
            // End duty - navigate to duty status
            Intent intent = new Intent(this, DutyStatusActivity.class);
            intent.putExtra("end_duty", true);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            // Logout confirmation
            showLogoutConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmation() {
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
}

