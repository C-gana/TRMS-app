package com.cgana.trmsownerapp.ui.destinations;

import android.app.AlertDialog;
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
import com.cgana.trmsownerapp.data.model.Destination;
import com.cgana.trmsownerapp.data.model.User;
import com.cgana.trmsownerapp.data.repository.DestinationsRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DestinationsFragment extends Fragment implements DestinationAdapter.OnDestinationActionListener {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerDestinations;
    private FloatingActionButton fabAdd;
    private LinearLayout emptyState;

    private DestinationsViewModel viewModel;
    private DestinationAdapter adapter;
    private String vehicleId;
    private VehicleManager vehicleManager;

    public static final int REQUEST_ADD_DESTINATION = 1001;
    public static final int REQUEST_EDIT_DESTINATION = 1002;

    private BroadcastReceiver vehicleChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadDestinations();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_destinations, container, false);

        // Initialize views
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerDestinations = view.findViewById(R.id.recyclerDestinations);
        fabAdd = view.findViewById(R.id.fabAdd);
        emptyState = view.findViewById(R.id.emptyState);

        // Initialize managers
        TokenManager tokenManager = new TokenManager(requireContext());
        vehicleManager = VehicleManager.getInstance(requireContext());
        vehicleId = vehicleManager.getSelectedVehicleId();

        // Initialize ViewModel
        DestinationsRepository repository = new DestinationsRepository(tokenManager);
        DestinationsViewModelFactory factory = new DestinationsViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(DestinationsViewModel.class);

        // Setup RecyclerView
        adapter = new DestinationAdapter(this);
        recyclerDestinations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerDestinations.setAdapter(adapter);

        setupListeners();
        observeViewModel();

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter("com.cgana.trmsownerapp.VEHICLE_CHANGED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(vehicleChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(vehicleChangeReceiver, filter);
        }

        // Load destinations
        loadDestinations();

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

    private void loadDestinations() {
        vehicleId = vehicleManager.getSelectedVehicleId();
        if (vehicleId != null) {
            viewModel.loadDestinations(vehicleId);
        } else {
            Toast.makeText(requireContext(), "No vehicle selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        // Add button
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditDestinationActivity.class);
            intent.putExtra("vehicle_id", vehicleId);
            startActivityForResult(intent, REQUEST_ADD_DESTINATION);
        });

        // Refresh
        swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void observeViewModel() {
        viewModel.getDestinationsState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case LOADING:
                    swipeRefresh.setRefreshing(true);
                    break;
                case SUCCESS:
                    swipeRefresh.setRefreshing(false);
                    adapter.setDestinations(state.getDestinations());
                    if (state.getDestinations() == null || state.getDestinations().isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        recyclerDestinations.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        recyclerDestinations.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), "Error: " + state.getError(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getActionState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case DELETING:
                    // Show progress
                    break;
                case DELETE_SUCCESS:
                    Toast.makeText(requireContext(), "Destination deleted", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), "Delete failed: " + state.getError(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @Override
    public void onEdit(Destination destination) {
        Intent intent = new Intent(requireContext(), AddEditDestinationActivity.class);
        intent.putExtra("vehicle_id", vehicleId);
        intent.putExtra("destination_id", destination.getDestinationId());
        intent.putExtra("destination_name", destination.getName());
        intent.putExtra("latitude", destination.getLatitude());
        intent.putExtra("longitude", destination.getLongitude());
        intent.putExtra("fare_amount", destination.getFareAmount());
        intent.putExtra("alert_radius", destination.getAlertRadius());
        startActivityForResult(intent, REQUEST_EDIT_DESTINATION);
    }

    @Override
    public void onDelete(Destination destination) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_destination)
                .setMessage(getString(R.string.confirm_delete_destination) + "\n\n" + destination.getName())
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteDestination(destination.getDestinationId());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == requireActivity().RESULT_OK) {
            if (requestCode == REQUEST_ADD_DESTINATION) {
                Toast.makeText(requireContext(), "Destination added successfully", Toast.LENGTH_SHORT).show();
                viewModel.refresh();
            } else if (requestCode == REQUEST_EDIT_DESTINATION) {
                Toast.makeText(requireContext(), "Destination updated successfully", Toast.LENGTH_SHORT).show();
                viewModel.refresh();
            }
        }
    }
}
