package com.cgana.trmsdriver.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.cgana.trmsdriver.R;

/**
 * Enhanced Boarding Dialog (Module 2 Part 4)
 * Professional Material Design dialog with information card
 */
public class BoardingDialog extends DialogFragment {

    public interface BoardingDialogListener {
        void onConfirmBoarding(int seatNumber);
        void onCancelBoarding();
    }

    private BoardingDialogListener listener;
    private int seatNumber;

    public static BoardingDialog newInstance(int seatNumber) {
        BoardingDialog dialog = new BoardingDialog();
        Bundle args = new Bundle();
        args.putInt("seat_number", seatNumber);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (BoardingDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement BoardingDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            seatNumber = getArguments().getInt("seat_number");
        }

        // Inflate custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_boarding, null);

        // Setup views
        TextView tvMessage = view.findViewById(R.id.tvBoardingMessage);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmBoarding);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelBoarding);

        tvMessage.setText(getString(R.string.boarding_confirmation_message, seatNumber));

        // Build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setCancelable(true);

        AlertDialog dialog = builder.create();

        // Button listeners
        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmBoarding(seatNumber);
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelBoarding();
            }
            dialog.dismiss();
        });

        return dialog;
    }
}

