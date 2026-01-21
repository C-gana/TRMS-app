package com.cgana.trmsdriver.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.model.Seat;

/**
 * Dialog for boarding a passenger (Module 2 Part 3)
 * Confirms the action before proceeding with the API call
 */
public class BoardingDialogFragment extends DialogFragment {

    public interface BoardingConfirmListener {
        void onConfirmBoarding(int seatNumber);
    }

    private static final String ARG_SEAT_NUMBER = "seat_number";

    private BoardingConfirmListener listener;
    private int seatNumber;

    public static BoardingDialogFragment newInstance(Seat seat) {
        BoardingDialogFragment fragment = new BoardingDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SEAT_NUMBER, seat.getSeat_number());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BoardingConfirmListener) {
            listener = (BoardingConfirmListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BoardingConfirmListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            seatNumber = getArguments().getInt(ARG_SEAT_NUMBER);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_board_passenger, null);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        tvTitle.setText(getString(R.string.board_passenger_title, seatNumber));
        tvMessage.setText(R.string.board_passenger_message);

        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmBoarding(seatNumber);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}

