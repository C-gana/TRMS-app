package com.cgana.trmsdriver.ui.alighting;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.model.SeatStatus;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Alighting Confirmation Dialog (Module 4 Part 2)
 * Shows journey details and confirms passenger alighting with fare collection
 */
public class AlightingConfirmationDialog extends DialogFragment {

    public interface AlightingDialogListener {
        void onConfirmAlighting(long journeyId, int seatNumber, boolean fareCollected);
        void onMissedStop(long journeyId, int seatNumber);
    }

    private AlightingDialogListener listener;
    private long journeyId;
    private int seatNumber;
    private String destination;
    private int fare;
    private String boardingTime;
    private double distanceKm;

    public static AlightingConfirmationDialog newInstance(SeatStatus seat) {
        AlightingConfirmationDialog dialog = new AlightingConfirmationDialog();
        Bundle args = new Bundle();
        args.putLong("journey_id", seat.getJourneyId());
        args.putInt("seat_number", seat.getSeatNumber());
        args.putString("destination", seat.getDestination());
        args.putInt("fare", seat.getFare());
        args.putString("boarding_time", seat.getBoardingTime());
        args.putDouble("distance_km", seat.getDistanceToDestination() != null ?
                       seat.getDistanceToDestination() : 0.0);
        dialog.setArguments(args);
        return dialog;
    }

    public void setListener(AlightingDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get arguments
        if (getArguments() != null) {
            journeyId = getArguments().getLong("journey_id");
            seatNumber = getArguments().getInt("seat_number");
            destination = getArguments().getString("destination");
            fare = getArguments().getInt("fare");
            boardingTime = getArguments().getString("boarding_time");
            distanceKm = getArguments().getDouble("distance_km");
        }

        // Inflate layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_alighting_confirmation, null);

        // Setup views
        TextView tvSeatNumber = view.findViewById(R.id.tvSeatNumber);
        TextView tvDestination = view.findViewById(R.id.tvDestination);
        TextView tvFare = view.findViewById(R.id.tvFare);
        TextView tvDuration = view.findViewById(R.id.tvDuration);
        TextView tvDistance = view.findViewById(R.id.tvDistance);
        MaterialButton btnYesAlighted = view.findViewById(R.id.btnYesAlighted);
        MaterialButton btnMissedStop = view.findViewById(R.id.btnMissedStop);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        LinearLayout fareCollectionSection = view.findViewById(R.id.fareCollectionSection);
        MaterialButton btnFareYes = view.findViewById(R.id.btnFareYes);
        MaterialButton btnFareNo = view.findViewById(R.id.btnFareNo);

        // Set data
        tvSeatNumber.setText(getString(R.string.seat_number_format, seatNumber));
        tvDestination.setText(destination);
        tvFare.setText(String.format(Locale.getDefault(), "%,d MK", fare));

        // Calculate duration
        if (boardingTime != null) {
            String duration = calculateDuration(boardingTime);
            tvDuration.setText(duration);
        } else {
            tvDuration.setText("N/A");
        }

        // Set distance
        tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", distanceKm));

        // Build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setCancelable(true);

        AlertDialog dialog = builder.create();

        // Button listeners
        btnYesAlighted.setOnClickListener(v -> {
            // Show fare collection question
            v.setEnabled(false);
            btnMissedStop.setEnabled(false);
            fareCollectionSection.setVisibility(View.VISIBLE);

            // Haptic feedback
            v.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK);
        });

        btnMissedStop.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onMissedStop(journeyId, seatNumber);
            }

            // Haptic feedback
            v.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK);
        });

        btnFareYes.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onConfirmAlighting(journeyId, seatNumber, true);
            }

            // Haptic feedback
            v.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM);
        });

        btnFareNo.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onConfirmAlighting(journeyId, seatNumber, false);
            }

            // Haptic feedback
            v.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    /**
     * Calculate journey duration from boarding time
     */
    private String calculateDuration(String boardingTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date boardingDate = sdf.parse(boardingTime);
            if (boardingDate != null) {
                long durationMillis = System.currentTimeMillis() - boardingDate.getTime();
                long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
                return minutes + " minutes";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }
}

