package com.cgana.trmsownerapp.ui.journeys;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.cgana.trmsownerapp.data.model.User;
import com.cgana.trmsownerapp.data.repository.JourneysRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class JourneysFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerJourneys;
    private MaterialButton btnStartDate, btnEndDate, btnApplyFilter;
    private ChipGroup chipGroupFilters;
    private Chip chipToday, chipYesterday, chipThisWeek, chipThisMonth;
    private FloatingActionButton fabExport;

    private JourneysViewModel viewModel;
    private JourneyAdapter adapter;
    private String vehicleId;
    private String startDate, endDate;
    private VehicleManager vehicleManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private BroadcastReceiver vehicleChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadJourneys();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journeys, container, false);

        // Initialize views
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerJourneys = view.findViewById(R.id.recyclerJourneys);
        btnStartDate = view.findViewById(R.id.btnStartDate);
        btnEndDate = view.findViewById(R.id.btnEndDate);
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        chipToday = view.findViewById(R.id.chipToday);
        chipYesterday = view.findViewById(R.id.chipYesterday);
        chipThisWeek = view.findViewById(R.id.chipThisWeek);
        chipThisMonth = view.findViewById(R.id.chipThisMonth);
        fabExport = view.findViewById(R.id.fabExport);

        // Initialize managers
        TokenManager tokenManager = new TokenManager(requireContext());
        vehicleManager = VehicleManager.getInstance(requireContext());
        vehicleId = vehicleManager.getSelectedVehicleId();

        // Initialize ViewModel
        JourneysRepository repository = new JourneysRepository(tokenManager);
        JourneysViewModelFactory factory = new JourneysViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(JourneysViewModel.class);

        // Setup RecyclerView
        adapter = new JourneyAdapter();
        recyclerJourneys.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerJourneys.setAdapter(adapter);

        // Set default date range (last 7 days)
        setDefaultDateRange();

        setupListeners();
        observeViewModel();

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter("com.cgana.trmsownerapp.VEHICLE_CHANGED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(vehicleChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(vehicleChangeReceiver, filter);
        }

        // Load initial data
        loadJourneys();

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

    private void loadJourneys() {
        vehicleId = vehicleManager.getSelectedVehicleId();
        if (vehicleId != null) {
            viewModel.loadJourneys(vehicleId, startDate, endDate);
        } else {
            Toast.makeText(requireContext(), "No vehicle selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void setDefaultDateRange() {
        Calendar calendar = Calendar.getInstance();
        endDate = dateFormat.format(calendar.getTime());
        btnEndDate.setText(displayDateFormat.format(calendar.getTime()));

        calendar.add(Calendar.DAY_OF_MONTH, -7);
        startDate = dateFormat.format(calendar.getTime());
        btnStartDate.setText(displayDateFormat.format(calendar.getTime()));
    }

    private void setupListeners() {
        // Date pickers
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));

        // Quick filters
        chipToday.setOnClickListener(v -> applyQuickFilter(0));
        chipYesterday.setOnClickListener(v -> applyQuickFilter(-1));
        chipThisWeek.setOnClickListener(v -> applyQuickFilter(-7));
        chipThisMonth.setOnClickListener(v -> applyQuickFilter(-30));

        // Apply filter
        btnApplyFilter.setOnClickListener(v -> {
            if (vehicleId != null) {
                viewModel.loadJourneys(vehicleId, startDate, endDate);
            }
        });

        // Refresh
        swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        // Export
        fabExport.setOnClickListener(v -> viewModel.exportJourneys());

        // Pagination
        recyclerJourneys.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1) {
                    viewModel.loadMore();
                }
            }
        });
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String selectedDate = dateFormat.format(calendar.getTime());
                    String displayDate = displayDateFormat.format(calendar.getTime());

                    if (isStartDate) {
                        startDate = selectedDate;
                        btnStartDate.setText(displayDate);
                    } else {
                        endDate = selectedDate;
                        btnEndDate.setText(displayDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void applyQuickFilter(int daysOffset) {
        Calendar calendar = Calendar.getInstance();
        endDate = dateFormat.format(calendar.getTime());
        btnEndDate.setText(displayDateFormat.format(calendar.getTime()));

        calendar.add(Calendar.DAY_OF_MONTH, daysOffset);
        startDate = dateFormat.format(calendar.getTime());
        btnStartDate.setText(displayDateFormat.format(calendar.getTime()));

        if (vehicleId != null) {
            viewModel.loadJourneys(vehicleId, startDate, endDate);
        }
    }

    private void observeViewModel() {
        viewModel.getJourneysState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case LOADING:
                    swipeRefresh.setRefreshing(false);
                    // Show progress (you can add a ProgressBar in layout)
                    break;
                case LOADING_MORE:
                    // Loading more items (pagination)
                    break;
                case SUCCESS:
                    swipeRefresh.setRefreshing(false);
                    adapter.setJourneys(state.getJourneys());
                    if (state.getJourneys().isEmpty()) {
                        Toast.makeText(requireContext(), "No journeys found", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ERROR:
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), "Error: " + state.getError(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getExportState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case EXPORTING:
                    fabExport.setEnabled(false);
                    Toast.makeText(requireContext(), "Exporting...", Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS:
                    fabExport.setEnabled(true);
                    saveCSVFile(state.getData());
                    break;
                case ERROR:
                    fabExport.setEnabled(true);
                    Toast.makeText(requireContext(), "Export failed: " + state.getError(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void saveCSVFile(ResponseBody body) {
        try {
            String fileName = "TRMS_Journeys_" + System.currentTimeMillis() + ".csv";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);

            InputStream inputStream = body.byteStream();
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Toast.makeText(requireContext(), "CSV saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

