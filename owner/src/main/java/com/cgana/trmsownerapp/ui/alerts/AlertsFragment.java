package com.cgana.trmsownerapp.ui.alerts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cgana.trmsownerapp.R;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.local.VehicleManager;
import com.cgana.trmsownerapp.data.model.Alert;
import com.cgana.trmsownerapp.data.model.User;
import com.cgana.trmsownerapp.data.repository.AlertsRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class AlertsFragment extends Fragment implements AlertAdapter.OnAcknowledgeListener {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerAlerts;
    private LinearLayout emptyState;
    private ChipGroup chipGroupFilters;
    private Chip chipAll, chipUnread, chipTimeout, chipMissedStop, chipProximity;

    private AlertsViewModel viewModel;
    private AlertAdapter adapter;
    private String vehicleId;
    private VehicleManager vehicleManager;

    private BroadcastReceiver vehicleChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadAlerts();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);

        // Initialize views
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerAlerts = view.findViewById(R.id.recyclerAlerts);
        emptyState = view.findViewById(R.id.emptyState);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        chipAll = view.findViewById(R.id.chipAll);
        chipUnread = view.findViewById(R.id.chipUnread);
        chipTimeout = view.findViewById(R.id.chipTimeout);
        chipMissedStop = view.findViewById(R.id.chipMissedStop);
        chipProximity = view.findViewById(R.id.chipProximity);

        // Initialize managers
        TokenManager tokenManager = new TokenManager(requireContext());
        vehicleManager = VehicleManager.getInstance(requireContext());
        vehicleId = vehicleManager.getSelectedVehicleId();

        // Initialize ViewModel
        AlertsRepository repository = new AlertsRepository(tokenManager);
        AlertsViewModelFactory factory = new AlertsViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(AlertsViewModel.class);

        // Setup RecyclerView
        adapter = new AlertAdapter(this);
        recyclerAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAlerts.setAdapter(adapter);

        setupListeners();
        observeViewModel();

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter("com.cgana.trmsownerapp.VEHICLE_CHANGED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(vehicleChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(vehicleChangeReceiver, filter);
        }

        // Load alerts
        loadAlerts();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireContext().unregisterReceiver(vehicleChangeReceiver);
        } catch (Exception e) {
            // Receiver not registered
        }
    }

    private void loadAlerts() {
        vehicleId = vehicleManager.getSelectedVehicleId();
        if (vehicleId != null) {
            viewModel.loadAlerts(vehicleId);
        } else {
            Toast.makeText(requireContext(), "No vehicle selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        // Refresh
        swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        // Filter chips
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // No chip selected, default to "All"
                chipAll.setChecked(true);
                return;
            }

            int checkedId = checkedIds.get(0);
            String filter = "all";

            if (checkedId == R.id.chipAll) {
                filter = "all";
            } else if (checkedId == R.id.chipUnread) {
                filter = "unread";
            } else if (checkedId == R.id.chipTimeout) {
                filter = "timeout";
            } else if (checkedId == R.id.chipMissedStop) {
                filter = "missed_stop";
            } else if (checkedId == R.id.chipProximity) {
                filter = "proximity";
            }

            viewModel.applyFilter(filter);
        });
    }

    private void observeViewModel() {
        viewModel.getAlertsState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case LOADING:
                    swipeRefresh.setRefreshing(true);
                    break;
                case SUCCESS:
                    swipeRefresh.setRefreshing(false);
                    adapter.setAlerts(state.getAlerts());
                    if (state.getAlerts() == null || state.getAlerts().isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        recyclerAlerts.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        recyclerAlerts.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), "Error: " + state.getError(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getAcknowledgeState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case ACKNOWLEDGING:
                    // Show progress (optional)
                    break;
                case SUCCESS:
                    Toast.makeText(requireContext(), "Alert acknowledged", Toast.LENGTH_SHORT).show();
                    viewModel.resetAcknowledgeState();
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), "Failed to acknowledge: " + state.getError(), Toast.LENGTH_SHORT).show();
                    viewModel.resetAcknowledgeState();
                    break;
            }
        });
    }

    @Override
    public void onAcknowledge(Alert alert, String notes) {
        viewModel.acknowledgeAlert(alert.getAlertId(), notes);
    }
}
