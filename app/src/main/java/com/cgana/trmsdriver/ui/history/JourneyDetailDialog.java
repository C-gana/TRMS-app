package com.cgana.trmsdriver.ui.history;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.model.Journey;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Journey Detail Dialog (Module 5 Part 3)
 */
public class JourneyDetailDialog extends DialogFragment {

    private Journey journey;

    public static JourneyDetailDialog newInstance(Journey journey) {
        JourneyDetailDialog dialog = new JourneyDetailDialog();
        Bundle args = new Bundle();
        args.putLong("journey_id", journey.getJourneyId());
        args.putInt("seat_number", journey.getSeatNumber());
        args.putString("destination", journey.getDestination());
        args.putString("boarding_time", journey.getBoardingTime());
        args.putString("alighting_time", journey.getAlightingTime());
        args.putInt("duration_minutes", journey.getDurationMinutes());
        args.putDouble("distance_km", journey.getDistanceKm());
        args.putInt("fare", journey.getFare());
        args.putBoolean("fare_collected", journey.isFareCollected());
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get arguments
        if (getArguments() != null) {
            journey = new Journey();
            journey.setJourneyId(getArguments().getLong("journey_id"));
            journey.setSeatNumber(getArguments().getInt("seat_number"));
            journey.setDestination(getArguments().getString("destination"));
            journey.setBoardingTime(getArguments().getString("boarding_time"));
            journey.setAlightingTime(getArguments().getString("alighting_time"));
            journey.setDurationMinutes(getArguments().getInt("duration_minutes"));
            journey.setDistanceKm(getArguments().getDouble("distance_km"));
            journey.setFare(getArguments().getInt("fare"));
            journey.setFareCollected(getArguments().getBoolean("fare_collected"));
        }

        // Inflate layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_journey_detail, null);

        // Setup views
        TextView tvJourneyId = view.findViewById(R.id.tvJourneyId);
        TextView tvDateTime = view.findViewById(R.id.tvDateTime);
        TextView tvDetailSeat = view.findViewById(R.id.tvDetailSeat);
        TextView tvDetailDestination = view.findViewById(R.id.tvDetailDestination);
        TextView tvDetailBoardingTime = view.findViewById(R.id.tvDetailBoardingTime);
        TextView tvDetailAlightingTime = view.findViewById(R.id.tvDetailAlightingTime);
        TextView tvDetailDuration = view.findViewById(R.id.tvDetailDuration);
        TextView tvDetailDistance = view.findViewById(R.id.tvDetailDistance);
        TextView tvDetailFare = view.findViewById(R.id.tvDetailFare);
        TextView tvDetailFareStatus = view.findViewById(R.id.tvDetailFareStatus);
        MaterialButton btnClose = view.findViewById(R.id.btnClose);

        // Set data
        tvJourneyId.setText(String.format(Locale.getDefault(), "Journey #%d", journey.getJourneyId()));
        tvDateTime.setText(formatDateTime(journey.getBoardingTime()));
        tvDetailSeat.setText(String.format(Locale.getDefault(), "Seat %d", journey.getSeatNumber()));
        tvDetailDestination.setText(journey.getDestination());
        tvDetailBoardingTime.setText(formatTime(journey.getBoardingTime()));
        tvDetailAlightingTime.setText(formatTime(journey.getAlightingTime()));
        tvDetailDuration.setText(String.format(Locale.getDefault(), "%d minutes", journey.getDurationMinutes()));
        tvDetailDistance.setText(String.format(Locale.getDefault(), "%.1f km", journey.getDistanceKm()));
        tvDetailFare.setText(String.format(Locale.getDefault(), "%,d MK", journey.getFare()));

        // Fare status
        if (journey.isFareCollected()) {
            tvDetailFareStatus.setText(R.string.fare_collected);
            tvDetailFareStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.fare_collected));
        } else {
            tvDetailFareStatus.setText(R.string.fare_not_collected_label);
            tvDetailFareStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.fare_not_collected));
        }

        // Build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setCancelable(true);

        AlertDialog dialog = builder.create();

        // Close button
        btnClose.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private String formatDateTime(String timeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy - hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(timeString);
            return date != null ? outputFormat.format(date) : timeString;
        } catch (Exception e) {
            return timeString;
        }
    }

    private String formatTime(String timeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(timeString);
            return date != null ? outputFormat.format(date) : timeString;
        } catch (Exception e) {
            return timeString;
        }
    }
}

