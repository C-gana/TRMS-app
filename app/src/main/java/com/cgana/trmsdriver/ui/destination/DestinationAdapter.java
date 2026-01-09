package com.cgana.trmsdriver.ui.destination;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.model.Destination;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Destination Adapter with Search Filter (Module 3 Part 2)
 * RecyclerView adapter for displaying destinations with search functionality
 */
public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder> {

    public interface OnDestinationSelectedListener {
        void onDestinationSelected(Destination destination);
    }

    private final Context context;
    private List<Destination> allDestinations;
    private List<Destination> filteredDestinations;
    private final OnDestinationSelectedListener listener;
    private int selectedPosition = -1;

    public DestinationAdapter(Context context, List<Destination> destinations,
                            OnDestinationSelectedListener listener) {
        this.context = context;
        this.allDestinations = new ArrayList<>(destinations);
        this.filteredDestinations = new ArrayList<>(destinations);
        this.listener = listener;
    }

    public void setDestinations(List<Destination> destinations) {
        this.allDestinations = new ArrayList<>(destinations);
        this.filteredDestinations = new ArrayList<>(destinations);
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    /**
     * Filter destinations by search query (Module 3 Part 2)
     */
    public void filter(String query) {
        filteredDestinations.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredDestinations.addAll(allDestinations);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            for (Destination destination : allDestinations) {
                if (destination.getName().toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                    filteredDestinations.add(destination);
                }
            }
        }

        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public Destination getSelectedDestination() {
        if (selectedPosition >= 0 && selectedPosition < filteredDestinations.size()) {
            return filteredDestinations.get(selectedPosition);
        }
        return null;
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
        holder.bind(filteredDestinations.get(position), position);
    }

    @Override
    public int getItemCount() {
        return filteredDestinations.size();
    }

    class DestinationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDestinationName;
        private final TextView tvFare;
        private final TextView tvDistanceTime;
        private final ImageView ivSelected;
        private final MaterialCardView cardDestination;

        public DestinationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDestinationName = itemView.findViewById(R.id.tvDestinationName);
            tvFare = itemView.findViewById(R.id.tvFare);
            tvDistanceTime = itemView.findViewById(R.id.tvDistanceTime);
            ivSelected = itemView.findViewById(R.id.ivSelected);
            cardDestination = itemView.findViewById(R.id.cardDestination);
        }

        public void bind(Destination destination, int position) {
            // Set destination name
            tvDestinationName.setText(destination.getName());

            // Set fare with formatting
            tvFare.setText(destination.getFormattedFare());

            // Set distance and estimated time
            tvDistanceTime.setText(destination.getFormattedDetails());

            // Highlight selected card
            if (position == selectedPosition) {
                cardDestination.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.destination_selected)
                );
                cardDestination.setStrokeColor(
                    ContextCompat.getColor(context, R.color.destination_selected_border)
                );
                cardDestination.setStrokeWidth(4);
                ivSelected.setVisibility(View.VISIBLE);
            } else {
                cardDestination.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.destination_card_background)
                );
                cardDestination.setStrokeColor(
                    ContextCompat.getColor(context, android.R.color.transparent)
                );
                cardDestination.setStrokeWidth(0);
                ivSelected.setVisibility(View.GONE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                // Haptic feedback
                v.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK);

                // Update selection
                int previousPosition = selectedPosition;
                selectedPosition = getAdapterPosition();

                // Notify changes
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition);
                }
                notifyItemChanged(selectedPosition);

                // Callback
                if (listener != null) {
                    listener.onDestinationSelected(destination);
                }
            });
        }
    }
}

