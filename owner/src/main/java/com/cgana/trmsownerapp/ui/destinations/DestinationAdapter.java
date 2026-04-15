package com.cgana.trmsownerapp.ui.destinations;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cgana.trmsownerapp.R;
import com.cgana.trmsownerapp.data.model.Destination;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder> {

    private List<Destination> destinations = new ArrayList<>();
    private OnDestinationActionListener listener;

    public interface OnDestinationActionListener {
        void onEdit(Destination destination);
        void onDelete(Destination destination);
    }

    public DestinationAdapter(OnDestinationActionListener listener) {
        this.listener = listener;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations != null ? destinations : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DestinationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_destination_card, parent, false);
        return new DestinationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DestinationViewHolder holder, int position) {
        holder.bind(destinations.get(position));
    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }

    class DestinationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvFare, tvAlertRadius, tvCoordinates;
        MaterialButton btnEdit, btnDelete;

        public DestinationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvFare = itemView.findViewById(R.id.tvFare);
            tvAlertRadius = itemView.findViewById(R.id.tvAlertRadius);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Destination destination) {
            tvName.setText(destination.getName());
            tvFare.setText(String.format(Locale.getDefault(), "%,d MK", destination.getFareAmount()));
            tvAlertRadius.setText(destination.getAlertRadius() + "m");
            tvCoordinates.setText(String.format(Locale.getDefault(),
                    "Lat: %.4f, Lon: %.4f",
                    destination.getLatitude(),
                    destination.getLongitude()));

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(destination);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(destination);
                }
            });
        }
    }
}

