package com.cgana.trmsdriver.ui.destination;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.Destination;
import com.cgana.trmsdriver.data.repository.DestinationRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Destination Selection Activity (Module 3 Part 3)
 * Allows driver to select destination with 90-second countdown
 */
public class DestinationSelectionActivity extends AppCompatActivity
        implements DestinationAdapter.OnDestinationSelectedListener {

    // UI Components
    private MaterialToolbar toolbar;
    private MaterialCardView timeoutWarningCard;
    private TextView tvTimeoutMessage, tvCountdown;
    private TextInputEditText etSearch;
    private RecyclerView recyclerDestinations;
    private LinearLayout emptyState;
    private FrameLayout loadingOverlay;

    // ViewModel & Data
    private DestinationSelectionViewModel viewModel;
    private DestinationAdapter adapter;
    private TokenManager tokenManager;
    private FusedLocationProviderClient fusedLocationClient;

    // Intent extras
    private String vehicleId;
    private long journeyId;
    private int seatNumber;
    private Destination selectedDestination;

    public static final String EXTRA_VEHICLE_ID = "vehicle_id";
    public static final String EXTRA_JOURNEY_ID = "journey_id";
    public static final String EXTRA_SEAT_NUMBER = "seat_number";

    private static final int LOCATION_PERMISSION_REQUEST = 3001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_selection);

        // Get intent extras
        vehicleId = getIntent().getStringExtra(EXTRA_VEHICLE_ID);
        journeyId = getIntent().getLongExtra(EXTRA_JOURNEY_ID, -1);
        seatNumber = getIntent().getIntExtra(EXTRA_SEAT_NUMBER, -1);

        // Validate intent extras
        if (vehicleId == null || journeyId == -1 || seatNumber == -1) {
            Toast.makeText(this, "Invalid data. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize
        tokenManager = new TokenManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Initialize ViewModel
        DestinationRepository repository = new DestinationRepository(tokenManager);
        DestinationSelectionViewModelFactory factory =
            new DestinationSelectionViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(DestinationSelectionViewModel.class);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search
        setupSearch();

        // Observe ViewModel
        observeViewModel();

        // Update UI with seat info
        updateSeatInfo();

        // Load destinations
        viewModel.loadDestinations(vehicleId);

        // Start countdown timer
        viewModel.startCountdown();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        timeoutWarningCard = findViewById(R.id.timeoutWarningCard);
        tvTimeoutMessage = findViewById(R.id.tvTimeoutMessage);
        tvCountdown = findViewById(R.id.tvCountdown);
        etSearch = findViewById(R.id.etSearch);
        recyclerDestinations = findViewById(R.id.recyclerDestinations);
        emptyState = findViewById(R.id.emptyState);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new DestinationAdapter(this, new ArrayList<>(), this);
        recyclerDestinations.setLayoutManager(new LinearLayoutManager(this));
        recyclerDestinations.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateSeatInfo() {
        tvTimeoutMessage.setText(getString(R.string.seat_x_select_destination, seatNumber));
    }

    private void observeViewModel() {
        // Observe destinations loading
        viewModel.getDestinationsState().observe(this, state -> {
            switch (state.getStatus()) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    List<Destination> destinations = state.getDestinations();
                    if (destinations != null && !destinations.isEmpty()) {
                        adapter.setDestinations(destinations);
                        recyclerDestinations.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    } else {
                        recyclerDestinations.setVisibility(View.GONE);
                        emptyState.setVisibility(View.VISIBLE);
                    }
                    break;

                case ERROR:
                    showLoading(false);
                    showError(state.getError());
                    break;

                case IDLE:
                default:
                    showLoading(false);
                    break;
            }
        });

        // Observe destination setting
        viewModel.getSetDestinationState().observe(this, state -> {
            switch (state.getStatus()) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    showSuccessAndFinish(state.getData().getMessage());
                    break;

                case ERROR:
                    showLoading(false);
                    showError(state.getError());
                    break;

                case IDLE:
                default:
                    showLoading(false);
                    break;
            }
        });

        // Observe countdown timer
        viewModel.getCountdownSeconds().observe(this, seconds -> {
            if (seconds != null) {
                tvCountdown.setText(String.valueOf(seconds));

                // Change color based on remaining time
                if (seconds <= 10) {
                    // Critical - Red
                    timeoutWarningCard.setCardBackgroundColor(
                        ContextCompat.getColor(this, R.color.timeout_critical)
                    );
                } else if (seconds <= 30) {
                    // Warning - Orange
                    timeoutWarningCard.setCardBackgroundColor(
                        ContextCompat.getColor(this, R.color.timeout_warning)
                    );
                } else {
                    // Normal - Orange
                    timeoutWarningCard.setCardBackgroundColor(
                        ContextCompat.getColor(this, R.color.timeout_warning)
                    );
                }
            }
        });

        // Observe timeout expiration
        viewModel.getTimeoutExpired().observe(this, expired -> {
            if (expired != null && expired) {
                showTimeoutDialog();
            }
        });
    }

    @Override
    public void onDestinationSelected(Destination destination) {
        selectedDestination = destination;

        // Show confirmation dialog
        showConfirmationDialog(destination);
    }

    private void showConfirmationDialog(Destination destination) {
        DestinationConfirmationDialog dialog = DestinationConfirmationDialog.newInstance(
            seatNumber,
            destination.getName(),
            destination.getFare(),
            destination.getDistanceKm(),
            destination.getEstimatedTimeMinutes()
        );

        dialog.setOnConfirmListener(() -> {
            // Check location permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndSetDestination();
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
                );
            }
        });

        dialog.show(getSupportFragmentManager(), "DestinationConfirmationDialog");
    }

    private void getCurrentLocationAndSetDestination() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Use default location
            setDestination(-15.7891, 35.0412);
            return;
        }

        showLoading(true);

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
                setDestination(latitude, longitude);
            })
            .addOnFailureListener(this, e -> {
                // Use default location on failure
                setDestination(-15.7891, 35.0412);
            });
    }

    private void setDestination(double latitude, double longitude) {
        if (selectedDestination != null) {
            viewModel.setDestination(
                vehicleId,
                journeyId,
                seatNumber,
                selectedDestination.getDestinationId(),
                latitude,
                longitude
            );
        }
    }

    private void showTimeoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.timeout_title)
            .setMessage(R.string.timeout_message)
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(R.string.ok, (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    private void showSuccessAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Haptic feedback
        findViewById(android.R.id.content).performHapticFeedback(
            android.view.HapticFeedbackConstants.CONFIRM
        );

        // Return to dashboard
        setResult(RESULT_OK);
        finish();
    }

    private void showError(String error) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.error)
            .setMessage(error)
            .setIcon(R.drawable.ic_error)
            .setPositiveButton(R.string.ok, null)
            .show();

        // Haptic feedback
        findViewById(android.R.id.content).performHapticFeedback(
            android.view.HapticFeedbackConstants.REJECT
        );
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            recyclerDestinations.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerDestinations.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndSetDestination();
            } else {
                // Use default location
                setDestination(-15.7891, 35.0412);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Warn user about timeout
        new AlertDialog.Builder(this)
            .setTitle(R.string.cancel_selection_title)
            .setMessage(R.string.cancel_selection_message)
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(R.string.yes_cancel, (dialog, which) -> {
                viewModel.stopCountdown();
                super.onBackPressed();
            })
            .setNegativeButton(R.string.no_continue, null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.stopCountdown();
    }
}

