package com.cgana.trmsownerapp.ui.alerts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cgana.trmsownerapp.R;
import com.cgana.trmsownerapp.data.model.Alert;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private List<Alert> alerts = new ArrayList<>();
    private int expandedPosition = -1;
    private OnAcknowledgeListener listener;

    public interface OnAcknowledgeListener {
        void onAcknowledge(Alert alert, String notes);
    }

    public AlertAdapter(OnAcknowledgeListener listener) {
        this.listener = listener;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts != null ? alerts : new ArrayList<>();
        this.expandedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert_card, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alerts.get(position);
        holder.bind(alert, position == expandedPosition);
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    class AlertViewHolder extends RecyclerView.ViewHolder {
        View severityIndicator;
        TextView tvAlertIcon, tvAlertTitle, tvMessage, tvSeatNumber, tvDestination;
        TextView tvTimestamp, tvAcknowledgedBadge, tvExpandIndicator;
        LinearLayout expandedDetails, acknowledgmentSection, acknowledgedInfo;
        TextInputEditText etNotes;
        MaterialButton btnAcknowledge;
        TextView tvAcknowledgedBy, tvAcknowledgedAt, tvAcknowledgedNotes;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            severityIndicator = itemView.findViewById(R.id.severityIndicator);
            tvAlertIcon = itemView.findViewById(R.id.tvAlertIcon);
            tvAlertTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvSeatNumber = itemView.findViewById(R.id.tvSeatNumber);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvAcknowledgedBadge = itemView.findViewById(R.id.tvAcknowledgedBadge);
            tvExpandIndicator = itemView.findViewById(R.id.tvExpandIndicator);
            expandedDetails = itemView.findViewById(R.id.expandedDetails);
            acknowledgmentSection = itemView.findViewById(R.id.acknowledgmentSection);
            acknowledgedInfo = itemView.findViewById(R.id.acknowledgedInfo);
            etNotes = itemView.findViewById(R.id.etNotes);
            btnAcknowledge = itemView.findViewById(R.id.btnAcknowledge);
            tvAcknowledgedBy = itemView.findViewById(R.id.tvAcknowledgedBy);
            tvAcknowledgedAt = itemView.findViewById(R.id.tvAcknowledgedAt);
            tvAcknowledgedNotes = itemView.findViewById(R.id.tvAcknowledgedNotes);

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

        public void bind(Alert alert, boolean isExpanded) {
            // Severity indicator
            int severityColor = ContextCompat.getColor(itemView.getContext(), alert.getSeverityColor());
            severityIndicator.setBackgroundColor(severityColor);

            // Icon & Title
            tvAlertIcon.setText(alert.getAlertIcon());
            tvAlertTitle.setText(alert.getAlertTitle());

            // Message
            tvMessage.setText(alert.getMessage());

            // Journey details
            tvSeatNumber.setText(String.format(Locale.getDefault(), "Seat %d", alert.getSeatNumber()));
            if (alert.getDestinationName() != null && !alert.getDestinationName().isEmpty()) {
                tvDestination.setText(alert.getDestinationName());
            } else {
                tvDestination.setText("N/A");
            }

            // Timestamp
            tvTimestamp.setText(formatRelativeTime(alert.getCreatedAt()));

            // Acknowledged badge
            if (alert.isAcknowledged()) {
                tvAcknowledgedBadge.setVisibility(View.VISIBLE);
            } else {
                tvAcknowledgedBadge.setVisibility(View.GONE);
            }

            // Expand/Collapse
            if (isExpanded) {
                tvExpandIndicator.setText(R.string.tap_to_collapse);
                expandedDetails.setVisibility(View.VISIBLE);

                if (alert.isAcknowledged()) {
                    // Show acknowledged info
                    acknowledgmentSection.setVisibility(View.GONE);
                    acknowledgedInfo.setVisibility(View.VISIBLE);

                    tvAcknowledgedBy.setText(alert.getAcknowledgedBy() != null ? alert.getAcknowledgedBy() : "Unknown");
                    tvAcknowledgedAt.setText(formatDateTime(alert.getAcknowledgedAt()));

                    if (alert.getNotes() != null && !alert.getNotes().isEmpty()) {
                        tvAcknowledgedNotes.setText(String.format(Locale.getDefault(), "Notes: %s", alert.getNotes()));
                        tvAcknowledgedNotes.setVisibility(View.VISIBLE);
                    } else {
                        tvAcknowledgedNotes.setVisibility(View.GONE);
                    }
                } else {
                    // Show acknowledgment form
                    acknowledgmentSection.setVisibility(View.VISIBLE);
                    acknowledgedInfo.setVisibility(View.GONE);

                    // Clear previous notes
                    etNotes.setText("");

                    // Acknowledge button
                    btnAcknowledge.setOnClickListener(v -> {
                        if (listener != null) {
                            String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
                            listener.onAcknowledge(alert, notes);
                        }
                    });
                }
            } else {
                tvExpandIndicator.setText(R.string.tap_to_expand);
                expandedDetails.setVisibility(View.GONE);
            }
        }

        private String formatRelativeTime(String isoDateTime) {
            if (isoDateTime == null) return "Unknown time";
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(isoDateTime);
                if (date == null) return isoDateTime;

                long diff = System.currentTimeMillis() - date.getTime();
                long minutes = diff / (60 * 1000);
                long hours = diff / (60 * 60 * 1000);
                long days = diff / (24 * 60 * 60 * 1000);

                if (minutes < 1) {
                    return "Just now";
                } else if (minutes < 60) {
                    return minutes + " min ago";
                } else if (hours < 24) {
                    return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
                } else {
                    return days + " day" + (days > 1 ? "s" : "") + " ago";
                }
            } catch (ParseException e) {
                return isoDateTime;
            }
        }

        private String formatDateTime(String isoDateTime) {
            if (isoDateTime == null) return "Unknown";
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                Date date = inputFormat.parse(isoDateTime);
                if (date == null) return isoDateTime;
                return outputFormat.format(date);
            } catch (ParseException e) {
                return isoDateTime;
            }
        }
    }
}

