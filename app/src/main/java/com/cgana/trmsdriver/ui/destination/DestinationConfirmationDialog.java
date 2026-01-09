package com.cgana.trmsdriver.ui.destination;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cgana.trmsdriver.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

/**
 * Destination Confirmation Dialog (Module 3 Part 3)
 * Shows journey details before setting destination
 */
public class DestinationConfirmationDialog extends DialogFragment {

    public interface OnConfirmListener {
        void onConfirm();
    }

    private OnConfirmListener listener;
    private int seatNumber;
    private String destinationName;
    private int fare;
    private double distanceKm;
    private int estimatedMinutes;

    public static DestinationConfirmationDialog newInstance(int seatNumber, String destinationName,
                                                            int fare, double distanceKm,
                                                            int estimatedMinutes) {
        DestinationConfirmationDialog dialog = new DestinationConfirmationDialog();
        Bundle args = new Bundle();
        args.putInt("seat_number", seatNumber);
        args.putString("destination_name", destinationName);
        args.putInt("fare", fare);
        args.putDouble("distance_km", distanceKm);
        args.putInt("estimated_minutes", estimatedMinutes);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnConfirmListener(OnConfirmListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            seatNumber = getArguments().getInt("seat_number");
            destinationName = getArguments().getString("destination_name");
            fare = getArguments().getInt("fare");
            distanceKm = getArguments().getDouble("distance_km");
            estimatedMinutes = getArguments().getInt("estimated_minutes");
        }

        // Inflate layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_destination_confirmation, null);

        // Setup views
        TextView tvSeatNumber = view.findViewById(R.id.tvSeatNumber);
        TextView tvDestinationName = view.findViewById(R.id.tvDestinationName);
        TextView tvFare = view.findViewById(R.id.tvFare);
        TextView tvJourneyDetails = view.findViewById(R.id.tvJourneyDetails);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        // Set data
        tvSeatNumber.setText(getString(R.string.seat_number_format, seatNumber));
        tvDestinationName.setText(destinationName);
        tvFare.setText(String.format(Locale.getDefault(), "%,d MK", fare));
        tvJourneyDetails.setText(String.format(Locale.getDefault(),
            "%.1fkm · Estimated %d minutes", distanceKm, estimatedMinutes));

        // Build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setCancelable(true);

        AlertDialog dialog = builder.create();

        // Button listeners
        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirm();
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }
}

