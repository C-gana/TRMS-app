package com.cgana.trmsownerapp.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgana.trmsownerapp.R;

import java.util.List;

/**
 * Dialog for selecting a vehicle from the user's vehicle list
 */
public class VehicleSelectionDialog extends DialogFragment {

    private List<String> vehicleIds;
    private String currentVehicleId;
    private OnVehicleSelectedListener listener;

    public interface OnVehicleSelectedListener {
        void onVehicleSelected(String vehicleId);
    }

    public static VehicleSelectionDialog newInstance(List<String> vehicleIds, String currentVehicleId) {
        VehicleSelectionDialog dialog = new VehicleSelectionDialog();
        dialog.vehicleIds = vehicleIds;
        dialog.currentVehicleId = currentVehicleId;
        return dialog;
    }

    public void setOnVehicleSelectedListener(OnVehicleSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        View view = getLayoutInflater().inflate(R.layout.dialog_vehicle_selection, null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        VehicleAdapter adapter = new VehicleAdapter(vehicleIds, currentVehicleId, vehicleId -> {
            if (listener != null) {
                listener.onVehicleSelected(vehicleId);
            }
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        builder.setView(view)
                .setTitle("Select Vehicle")
                .setNegativeButton("Cancel", (dialog, which) -> dismiss());

        return builder.create();
    }

    private static class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {
        private List<String> vehicleIds;
        private String currentVehicleId;
        private OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(String vehicleId);
        }

        VehicleAdapter(List<String> vehicleIds, String currentVehicleId, OnItemClickListener listener) {
            this.vehicleIds = vehicleIds;
            this.currentVehicleId = currentVehicleId;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_vehicle_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String vehicleId = vehicleIds.get(position);
            holder.bind(vehicleId, vehicleId.equals(currentVehicleId), listener);
        }

        @Override
        public int getItemCount() {
            return vehicleIds.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvVehicleId;
            View selectedIndicator;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvVehicleId = itemView.findViewById(R.id.tvVehicleId);
                selectedIndicator = itemView.findViewById(R.id.selectedIndicator);
            }

            void bind(String vehicleId, boolean isSelected, OnItemClickListener listener) {
                tvVehicleId.setText(vehicleId);
                selectedIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(vehicleId);
                    }
                });
            }
        }
    }
}

