package com.cgana.trmsownerapp.ui.dashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cgana.trmsownerapp.R;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.local.VehicleManager;
import com.cgana.trmsownerapp.data.model.DashboardResponse;
import com.cgana.trmsownerapp.data.model.Seat;
import com.cgana.trmsownerapp.data.model.User;
import com.cgana.trmsownerapp.data.repository.DashboardRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private View offlineIndicator;
    private TextView tvVehicleId, tvRegistration, tvStatus, tvLastSeen;
    private TextView tvTodayJourneys, tvActiveJourneys, tvLastUpdated;
    private GridLayout seatsGrid;
    private DashboardViewModel viewModel;
    private String vehicleId;
    private VehicleManager vehicleManager;
    private com.google.android.material.button.MaterialButton btnCallDriver, btnViewRoute;
    private String driverPhoneNumber;

    private BroadcastReceiver vehicleChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Reload dashboard when vehicle changes
            loadDashboard();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize views
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        offlineIndicator = view.findViewById(R.id.offlineIndicator);
        tvVehicleId = view.findViewById(R.id.tvVehicleId);
        tvRegistration = view.findViewById(R.id.tvRegistration);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvLastSeen = view.findViewById(R.id.tvLastSeen);
        tvTodayJourneys = view.findViewById(R.id.tvTodayJourneys);
        tvActiveJourneys = view.findViewById(R.id.tvActiveJourneys);
        tvLastUpdated = view.findViewById(R.id.tvLastUpdated);
        seatsGrid = view.findViewById(R.id.seatsGrid);
        btnCallDriver = view.findViewById(R.id.btnCallDriver);
        btnViewRoute = view.findViewById(R.id.btnViewRoute);

        // Initialize managers
        TokenManager tokenManager = new TokenManager(requireContext());
        vehicleManager = VehicleManager.getInstance(requireContext());

        // Get selected vehicle ID
        vehicleId = vehicleManager.getSelectedVehicleId();

        // Initialize ViewModel with context for offline support
        DashboardRepository repository = new DashboardRepository(tokenManager, requireContext());
        DashboardViewModelFactory factory = new DashboardViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        setupListeners();
        observeViewModel();

        // Register broadcast receiver for vehicle changes
        IntentFilter filter = new IntentFilter("com.cgana.trmsownerapp.VEHICLE_CHANGED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(vehicleChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(vehicleChangeReceiver, filter);
        }

        // Load dashboard
        loadDashboard();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister broadcast receiver
        try {
            requireContext().unregisterReceiver(vehicleChangeReceiver);
        } catch (Exception e) {
            // Receiver not registered
        }
    }

    private void loadDashboard() {
        vehicleId = vehicleManager.getSelectedVehicleId();
        if (vehicleId != null) {
            viewModel.loadDashboard(vehicleId);
        } else {
            Toast.makeText(requireContext(), "No vehicle selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        btnCallDriver.setOnClickListener(v -> callDriver());
        btnViewRoute.setOnClickListener(v -> showRouteHistory());
    }

    private void observeViewModel() {
        viewModel.getDashboardState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case LOADING:
                    // Don't show loading for refresh (swipeRefresh handles it)
                    break;
                case SUCCESS:
                    swipeRefresh.setRefreshing(false);

                    // Show/hide offline indicator
                    if (state.isOffline()) {
                        offlineIndicator.setVisibility(View.VISIBLE);
                    } else {
                        offlineIndicator.setVisibility(View.GONE);
                    }

                    updateUI(state.getData());
                    break;
                case ERROR:
                    swipeRefresh.setRefreshing(false);

                    // Show offline indicator on error
                    if (state.isOffline()) {
                        offlineIndicator.setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(requireContext(), "Error: " + state.getError(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void updateUI(DashboardResponse data) {
        // Vehicle info
        tvVehicleId.setText(data.getVehicleId());
        tvRegistration.setText(data.getRegistration());

        // Status
        boolean isOnline = "active".equals(data.getStatus());
        tvStatus.setText(isOnline ? "● Online" : "● Offline");
        tvStatus.setTextColor(getResources().getColor(isOnline ? R.color.online : R.color.offline, null));

        // Last seen (you can implement relative time formatting)
        tvLastSeen.setText("Just now");

        // Stats
        tvActiveJourneys.setText(String.valueOf(data.getActiveJourneys()));
        // TODO: Get today's journeys from API (not in current response)

        // Driver phone number - show/hide call button
        if (data.getDriverPhoneNumber() != null && !data.getDriverPhoneNumber().isEmpty()) {
            driverPhoneNumber = data.getDriverPhoneNumber();
            btnCallDriver.setVisibility(View.VISIBLE);
        } else {
            btnCallDriver.setVisibility(View.GONE);
        }

        // Seats
        updateSeats(data.getSeats());

        // Last updated
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        tvLastUpdated.setText("Last updated: " + sdf.format(new Date()));
    }

    private void updateSeats(java.util.List<Seat> seats) {
        seatsGrid.removeAllViews();

        for (Seat seat : seats) {
            View seatCard = LayoutInflater.from(requireContext()).inflate(R.layout.item_seat_card, seatsGrid, false);

            TextView tvSeatNumber = seatCard.findViewById(R.id.tvSeatNumber);
            TextView tvSeatStatus = seatCard.findViewById(R.id.tvSeatStatus);
            TextView tvDestination = seatCard.findViewById(R.id.tvDestination);
            TextView tvFare = seatCard.findViewById(R.id.tvFare);
            TextView tvDistance = seatCard.findViewById(R.id.tvDistance);
            ProgressBar progressBar = seatCard.findViewById(R.id.progressBar);
            View seatContainer = seatCard.findViewById(R.id.seatContainer);

            // Seat number
            tvSeatNumber.setText("Seat " + seat.getSeatNumber());

            // Status and colors
            String status = seat.getStatus();
            switch (status) {
                case "vacant":
                    tvSeatStatus.setText("Vacant");
                    tvSeatStatus.setTextColor(getResources().getColor(R.color.text_secondary, null));
                    seatContainer.setBackgroundColor(getResources().getColor(R.color.surface, null));
                    break;
                case "awaiting":
                    tvSeatStatus.setText("Awaiting Destination");
                    tvSeatStatus.setTextColor(getResources().getColor(R.color.awaiting, null));
                    seatContainer.setBackgroundColor(getResources().getColor(R.color.background, null));
                    break;
                case "active":
                    tvSeatStatus.setText("Active");
                    tvSeatStatus.setTextColor(getResources().getColor(R.color.active, null));
                    if (seat.getDestination() != null) {
                        tvDestination.setText(seat.getDestination().getName());
                        tvDestination.setVisibility(View.VISIBLE);
                    }
                    if (seat.getFare() != null) {
                        tvFare.setText(String.format(Locale.getDefault(), "%,d MK", seat.getFare()));
                        tvFare.setVisibility(View.VISIBLE);
                    }
                    if (seat.getDistanceRemaining() != null) {
                        tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", seat.getDistanceRemaining()));
                        tvDistance.setVisibility(View.VISIBLE);
                    }
                    break;
                case "approaching":
                    tvSeatStatus.setText("Approaching");
                    tvSeatStatus.setTextColor(getResources().getColor(R.color.approaching, null));
                    seatContainer.setBackgroundColor(getResources().getColor(R.color.warning, null));
                    if (seat.getDestination() != null) {
                        tvDestination.setText(seat.getDestination().getName());
                        tvDestination.setVisibility(View.VISIBLE);
                    }
                    if (seat.getFare() != null) {
                        tvFare.setText(String.format(Locale.getDefault(), "%,d MK", seat.getFare()));
                        tvFare.setVisibility(View.VISIBLE);
                    }
                    if (seat.getDistanceRemaining() != null) {
                        tvDistance.setText(String.format(Locale.getDefault(), "%.1f km • ETA %d min",
                                seat.getDistanceRemaining(), seat.getEtaMinutes()));
                        tvDistance.setVisibility(View.VISIBLE);
                    }
                    // Show progress bar
                    if (seat.getDistanceRemaining() != null) {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setMax(100);
                        // Calculate progress (closer = higher progress)
                        int progress = (int) (100 - (seat.getDistanceRemaining() * 20));
                        progressBar.setProgress(Math.max(0, Math.min(100, progress)));
                    }
                    break;
            }

            seatsGrid.addView(seatCard);
        }
    }

    private void callDriver() {
        if (driverPhoneNumber != null && !driverPhoneNumber.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + driverPhoneNumber));
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "Driver phone number not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRouteHistory() {
        Intent intent = new Intent(requireContext(), com.cgana.trmsownerapp.ui.route.RouteHistoryActivity.class);
        intent.putExtra("vehicle_id", vehicleId);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.stopRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (vehicleId != null) {
            viewModel.loadDashboard(vehicleId);
        }
    }
}

