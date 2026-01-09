package com.cgana.trmsdriver.ui.alighting;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cgana.trmsdriver.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Missed Stop Dialog (Module 4 Part 2)
 * Allows driver to report when passenger misses their stop
 */
public class MissedStopDialog extends DialogFragment {

    public interface MissedStopDialogListener {
        void onReportMissedStop(long journeyId, int seatNumber, String notes);
    }

    private MissedStopDialogListener listener;
    private long journeyId;
    private int seatNumber;

    public static MissedStopDialog newInstance(long journeyId, int seatNumber) {
        MissedStopDialog dialog = new MissedStopDialog();
        Bundle args = new Bundle();
        args.putLong("journey_id", journeyId);
        args.putInt("seat_number", seatNumber);
        dialog.setArguments(args);
        return dialog;
    }

    public void setListener(MissedStopDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get arguments
        if (getArguments() != null) {
            journeyId = getArguments().getLong("journey_id");
            seatNumber = getArguments().getInt("seat_number");
        }

        // Inflate layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_missed_stop_notes, null);

        // Setup views
        TextInputEditText etNotes = view.findViewById(R.id.etNotes);
        MaterialButton btnReportMissed = view.findViewById(R.id.btnReportMissed);
        MaterialButton btnCancelMissed = view.findViewById(R.id.btnCancelMissed);

        // Build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setCancelable(true);

        AlertDialog dialog = builder.create();

        // Button listeners
        btnReportMissed.setOnClickListener(v -> {
            String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
            dialog.dismiss();

            if (listener != null) {
                listener.onReportMissedStop(journeyId, seatNumber, notes);
            }

            // Haptic feedback
            v.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM);
        });

        btnCancelMissed.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }
}

