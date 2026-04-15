package com.cgana.trmsownerapp.ui.journeys;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cgana.trmsownerapp.R;
import com.cgana.trmsownerapp.data.model.Journey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.JourneyViewHolder> {

    private List<Journey> journeys = new ArrayList<>();
    private int expandedPosition = -1;

    public void setJourneys(List<Journey> journeys) {
        this.journeys = journeys;
        notifyDataSetChanged();
    }

    public void addJourneys(List<Journey> newJourneys) {
        int oldSize = this.journeys.size();
        this.journeys.addAll(newJourneys);
        notifyItemRangeInserted(oldSize, newJourneys.size());
    }

    @NonNull
    @Override
    public JourneyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journey_card, parent, false);
        return new JourneyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JourneyViewHolder holder, int position) {
        Journey journey = journeys.get(position);
        holder.bind(journey, position == expandedPosition);
    }

    @Override
    public int getItemCount() {
        return journeys.size();
    }

    class JourneyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateTime, tvSeatBadge, tvDestination, tvFare, tvDuration, tvDistance;
        TextView tvAlerts, tvExpandIndicator;
        TextView tvBoardingTime, tvBoardingLocation, tvAlightingTime, tvAlightingLocation;
        LinearLayout expandedDetails;

        public JourneyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvSeatBadge = itemView.findViewById(R.id.tvSeatBadge);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvFare = itemView.findViewById(R.id.tvFare);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvAlerts = itemView.findViewById(R.id.tvAlerts);
            tvExpandIndicator = itemView.findViewById(R.id.tvExpandIndicator);
            tvBoardingTime = itemView.findViewById(R.id.tvBoardingTime);
            tvBoardingLocation = itemView.findViewById(R.id.tvBoardingLocation);
            tvAlightingTime = itemView.findViewById(R.id.tvAlightingTime);
            tvAlightingLocation = itemView.findViewById(R.id.tvAlightingLocation);
            expandedDetails = itemView.findViewById(R.id.expandedDetails);

            // Click to expand/collapse
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (expandedPosition == position) {
                        expandedPosition = -1; // Collapse
                    } else {
                        int previousExpanded = expandedPosition;
                        expandedPosition = position; // Expand
                        if (previousExpanded != -1) {
                            notifyItemChanged(previousExpanded);
                        }
                    }
                    notifyItemChanged(position);
                }
            });
        }

        public void bind(Journey journey, boolean isExpanded) {
            // Date & Time
            tvDateTime.setText(formatDateTime(journey.getBoardingTime()));

            // Seat Badge
            tvSeatBadge.setText("Seat " + journey.getSeatNumber());

            // Destination
            tvDestination.setText(journey.getDestinationName());

            // Fare
            tvFare.setText(String.format(Locale.getDefault(), "%,d MK", journey.getFareCollected()));

            // Duration
            tvDuration.setText(journey.getDurationMinutes() + " min");

            // Distance
            tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", journey.getActualDistance()));

            // Alerts
            if (journey.getAlerts() != null && !journey.getAlerts().isEmpty()) {
                String alertText = getAlertText(journey.getAlerts());
                tvAlerts.setText(alertText);
                tvAlerts.setVisibility(View.VISIBLE);
            } else {
                tvAlerts.setVisibility(View.GONE);
            }

            // Expand/Collapse
            if (isExpanded) {
                tvExpandIndicator.setText("Tap to collapse ▲");
                expandedDetails.setVisibility(View.VISIBLE);

                // Boarding details
                String boardingTime = journey.getBoardingTime();
                tvBoardingTime.setText(boardingTime != null ? formatTime(boardingTime) : "N/A");
                if (journey.getBoardingLocation() != null) {
                    tvBoardingLocation.setText(String.format(Locale.getDefault(),
                            "Lat: %.4f, Lon: %.4f",
                            journey.getBoardingLocation().getLatitude(),
                            journey.getBoardingLocation().getLongitude()));
                }

                // Alighting details
                String alightingTime = journey.getAlightingTime();
                tvAlightingTime.setText(alightingTime != null ? formatTime(alightingTime) : "N/A");
                if (journey.getAlightingLocation() != null) {
                    tvAlightingLocation.setText(String.format(Locale.getDefault(),
                            "Lat: %.4f, Lon: %.4f",
                            journey.getAlightingLocation().getLatitude(),
                            journey.getAlightingLocation().getLongitude()));
                }
            } else {
                tvExpandIndicator.setText("Tap to expand ▼");
                expandedDetails.setVisibility(View.GONE);
            }
        }

        private String formatDateTime(String isoDateTime) {
            if (isoDateTime == null || isoDateTime.isEmpty()) {
                return "N/A";
            }
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault());
                Date date = inputFormat.parse(isoDateTime);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return isoDateTime;
            }
        }

        private String formatTime(String isoDateTime) {
            if (isoDateTime == null || isoDateTime.isEmpty()) {
                return "N/A";
            }
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date date = inputFormat.parse(isoDateTime);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return isoDateTime;
            }
        }

        private String getAlertText(List<String> alerts) {
            StringBuilder sb = new StringBuilder();
            for (String alert : alerts) {
                if (sb.length() > 0) sb.append(", ");
                switch (alert) {
                    case "proximity":
                        sb.append("📍 Proximity alert");
                        break;
                    case "missed_stop":
                        sb.append("⚠️ Missed stop");
                        break;
                    case "timeout":
                        sb.append("⏱️ Timeout");
                        break;
                    default:
                        sb.append(alert);
                }
            }
            return sb.toString();
        }
    }
}

