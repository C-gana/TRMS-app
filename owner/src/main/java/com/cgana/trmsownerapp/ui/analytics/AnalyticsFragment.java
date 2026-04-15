package com.cgana.trmsownerapp.ui.analytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.cgana.trmsownerapp.data.model.AnalyticsResponse;
import com.cgana.trmsownerapp.data.model.DestinationStats;
import com.cgana.trmsownerapp.data.model.PeakHour;
import com.cgana.trmsownerapp.data.model.User;
import com.cgana.trmsownerapp.data.repository.AnalyticsRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private MaterialButtonToggleGroup toggleGroupPeriod;
    private MaterialButton btnToday, btnWeek, btnMonth;
    private TextView tvTotalJourneys, tvTotalRevenue, tvAvgDuration, tvTopDestination;
    private BarChart chartRevenueByDestination;
    private LineChart chartJourneysByHour;
    private TextView tvCompliancePercent, tvMissedStops, tvAvgSelectionTime;
    private ProgressBar progressCompliance;

    private AnalyticsViewModel viewModel;
    private String vehicleId;
    private String currentPeriod = "today";
    private VehicleManager vehicleManager;

    private BroadcastReceiver vehicleChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadAnalytics();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        // Initialize views
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        toggleGroupPeriod = view.findViewById(R.id.toggleGroupPeriod);
        btnToday = view.findViewById(R.id.btnToday);
        btnWeek = view.findViewById(R.id.btnWeek);
        btnMonth = view.findViewById(R.id.btnMonth);
        tvTotalJourneys = view.findViewById(R.id.tvTotalJourneys);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvAvgDuration = view.findViewById(R.id.tvAvgDuration);
        tvTopDestination = view.findViewById(R.id.tvTopDestination);
        chartRevenueByDestination = view.findViewById(R.id.chartRevenueByDestination);
        chartJourneysByHour = view.findViewById(R.id.chartJourneysByHour);
        tvCompliancePercent = view.findViewById(R.id.tvCompliancePercent);
        tvMissedStops = view.findViewById(R.id.tvMissedStops);
        tvAvgSelectionTime = view.findViewById(R.id.tvAvgSelectionTime);
        progressCompliance = view.findViewById(R.id.progressCompliance);

        // Initialize managers
        TokenManager tokenManager = new TokenManager(requireContext());
        vehicleManager = VehicleManager.getInstance(requireContext());
        vehicleId = vehicleManager.getSelectedVehicleId();

        // Initialize ViewModel
        AnalyticsRepository repository = new AnalyticsRepository(tokenManager);
        AnalyticsViewModelFactory factory = new AnalyticsViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(AnalyticsViewModel.class);

        // Setup charts
        setupCharts();
        setupListeners();
        observeViewModel();

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter("com.cgana.trmsownerapp.VEHICLE_CHANGED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(vehicleChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(vehicleChangeReceiver, filter);
        }

        // Set default period (Today)
        toggleGroupPeriod.check(R.id.btnToday);

        // Load data
        loadAnalytics();

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

    private void loadAnalytics() {
        vehicleId = vehicleManager.getSelectedVehicleId();
        if (vehicleId != null) {
            viewModel.loadAnalytics(vehicleId, currentPeriod);
        } else {
            Toast.makeText(requireContext(), "No vehicle assigned", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCharts() {
        // Configure Revenue by Destination chart (Vertical bars facing upwards)
        chartRevenueByDestination.getDescription().setEnabled(false);
        chartRevenueByDestination.setDrawGridBackground(false);
        chartRevenueByDestination.setTouchEnabled(false);
        chartRevenueByDestination.getLegend().setEnabled(false);
        chartRevenueByDestination.setFitBars(true);

        XAxis xAxis = chartRevenueByDestination.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f); // Rotate labels for better visibility
        xAxis.setTextSize(10f); // Slightly smaller text
        xAxis.setYOffset(5f); // Add offset from axis

        chartRevenueByDestination.getAxisLeft().setDrawGridLines(false);
        chartRevenueByDestination.getAxisLeft().setAxisMinimum(0f); // Start Y-axis from 0
        chartRevenueByDestination.getAxisRight().setEnabled(false);
        chartRevenueByDestination.setExtraBottomOffset(40f); // More space for rotated labels
        chartRevenueByDestination.setExtraLeftOffset(10f); // Extra left margin
        chartRevenueByDestination.setExtraRightOffset(10f); // Extra right margin

        // Configure Journeys by Hour chart
        chartJourneysByHour.getDescription().setEnabled(false);
        chartJourneysByHour.setDrawGridBackground(false);
        chartJourneysByHour.setTouchEnabled(false);
        chartJourneysByHour.getLegend().setEnabled(false);

        XAxis xAxisHour = chartJourneysByHour.getXAxis();
        xAxisHour.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisHour.setDrawGridLines(false);
        xAxisHour.setGranularity(1f);

        chartJourneysByHour.getAxisLeft().setDrawGridLines(true);
        chartJourneysByHour.getAxisRight().setEnabled(false);
    }

    private void setupListeners() {
        // Period selector
        toggleGroupPeriod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnToday) {
                    currentPeriod = "today";
                } else if (checkedId == R.id.btnWeek) {
                    currentPeriod = "week";
                } else if (checkedId == R.id.btnMonth) {
                    currentPeriod = "month";
                }
                viewModel.changePeriod(currentPeriod);
            }
        });

        // Refresh
        swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void observeViewModel() {
        viewModel.getAnalyticsState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case LOADING:
                    swipeRefresh.setRefreshing(true);
                    break;
                case SUCCESS:
                    swipeRefresh.setRefreshing(false);
                    updateUI(state.getData());
                    break;
                case ERROR:
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), "Error: " + state.getError(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void updateUI(AnalyticsResponse data) {
        // Summary cards
        tvTotalJourneys.setText(String.valueOf(data.getTotalJourneys()));
        tvTotalRevenue.setText(String.format(Locale.getDefault(), "%,d MK", data.getTotalRevenue()));
        tvAvgDuration.setText(data.getAvgJourneyDuration() + " min");
        tvTopDestination.setText(data.getTopDestination());

        // Charts
        updateRevenueChart(data.getDestinations());
        updateJourneysByHourChart(data.getPeakHours());

        // Driver performance
        if (data.getDriverPerformance() != null) {
            double compliance = data.getDriverPerformance().getDestinationSelectionCompliance();
            tvCompliancePercent.setText(String.format(Locale.getDefault(), "%.1f%%", compliance));
            progressCompliance.setProgress((int) compliance);

            tvMissedStops.setText(String.valueOf(data.getDriverPerformance().getMissedStops()));
            tvAvgSelectionTime.setText(data.getDriverPerformance().getAvgSelectionTimeSeconds() + "s");
        }
    }

    private void updateRevenueChart(List<DestinationStats> destinations) {
        if (destinations == null || destinations.isEmpty()) {
            chartRevenueByDestination.clear();
            chartRevenueByDestination.setNoDataText("No data available");
            chartRevenueByDestination.invalidate();
            return;
        }

        // Take top 5 destinations
        int limit = Math.min(5, destinations.size());
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            DestinationStats dest = destinations.get(i);
            entries.add(new BarEntry(i, dest.getRevenue()));

            // Truncate long destination names to fit better
            String name = dest.getName();
            if (name.length() > 15) {
                name = name.substring(0, 12) + "...";
            }
            labels.add(name);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Revenue");
        int primaryColor = getResources().getColor(R.color.primary, null);
        dataSet.setColor(primaryColor);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary, null));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        chartRevenueByDestination.setData(barData);

        XAxis xAxis = chartRevenueByDestination.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setTextSize(9f);
        xAxis.setYOffset(10f);
        xAxis.setTextColor(getResources().getColor(R.color.text_primary, null));

        chartRevenueByDestination.animateY(500); // Animate Y-axis for vertical bars
        chartRevenueByDestination.invalidate();
    }

    private void updateJourneysByHourChart(List<PeakHour> peakHours) {
        if (peakHours == null || peakHours.isEmpty()) {
            chartJourneysByHour.clear();
            chartJourneysByHour.setNoDataText("No data available");
            chartJourneysByHour.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < peakHours.size(); i++) {
            PeakHour peak = peakHours.get(i);
            entries.add(new Entry(i, peak.getJourneys()));
            labels.add(String.format(Locale.getDefault(), "%02d:00", peak.getHour()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Journeys");
        int successColor = getResources().getColor(R.color.success, null);
        dataSet.setColor(successColor);
        dataSet.setCircleColor(successColor);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(successColor);
        dataSet.setFillAlpha(50);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        chartJourneysByHour.setData(lineData);

        chartJourneysByHour.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartJourneysByHour.getXAxis().setLabelCount(Math.min(labels.size(), 8));
        chartJourneysByHour.getXAxis().setLabelRotationAngle(-45);

        chartJourneysByHour.animateX(500);
        chartJourneysByHour.invalidate();
    }
}
