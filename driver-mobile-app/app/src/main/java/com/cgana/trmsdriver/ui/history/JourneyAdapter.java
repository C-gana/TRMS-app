package com.cgana.trmsdriver.ui.history;

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
import com.cgana.trmsdriver.data.model.Journey;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Journey Adapter (Module 5 Part 3)
 */
public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.JourneyViewHolder> {

    public interface OnJourneyClickListener {
        void onJourneyClick(Journey journey);
    }

    private Context context;
    private List<Journey> journeys;
    private OnJourneyClickListener listener;

    public JourneyAdapter(Context context, OnJourneyClickListener listener) {
        this.context = context;
        this.journeys = new ArrayList<>();
        this.listener = listener;
    }

    public void setJourneys(List<Journey> journeys) {
        this.journeys = journeys;
        notifyDataSetChanged();
    }

    public void addJourneys(List<Journey> newJourneys) {
        int startPosition = this.journeys.size();
        this.journeys.addAll(newJourneys);
        notifyItemRangeInserted(startPosition, newJourneys.size());
    }

    public void clearJourneys() {
        this.journeys.clear();
        notifyDataSetChanged();
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
        holder.bind(journeys.get(position));
    }

    @Override
    public int getItemCount() {
        return journeys.size();
    }

    class JourneyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvJourneyId, tvSeatNumber, tvDestination, tvTimeInfo, tvFare, tvDistance;
        private ImageView ivFareStatus;

        public JourneyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJourneyId = itemView.findViewById(R.id.tvJourneyId);
            tvSeatNumber = itemView.findViewById(R.id.tvSeatNumber);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvTimeInfo = itemView.findViewById(R.id.tvTimeInfo);
            tvFare = itemView.findViewById(R.id.tvFare);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            ivFareStatus = itemView.findViewById(R.id.ivFareStatus);
        }

        public void bind(Journey journey) {
            // Journey ID
            tvJourneyId.setText(String.format(Locale.getDefault(), "Journey #%d", journey.getJourneyId()));

            // Seat Number
            tvSeatNumber.setText(String.format(Locale.getDefault(), "Seat %d", journey.getSeatNumber()));

            // Destination
            tvDestination.setText(journey.getDestination());

            // Time Info
            String boardingTime = formatTime(journey.getBoardingTime());
            String alightingTime = formatTime(journey.getAlightingTime());
            String timeInfo = String.format(Locale.getDefault(), "%s → %s (%d min)",
                    boardingTime, alightingTime, journey.getDurationMinutes());
            tvTimeInfo.setText(timeInfo);

            // Fare
            tvFare.setText(String.format(Locale.getDefault(), "%,d MK", journey.getFare()));

            // Distance
            tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", journey.getDistanceKm()));

            // Fare Status Icon
            if (journey.isFareCollected()) {
                ivFareStatus.setImageResource(R.drawable.ic_check_circle);
                ivFareStatus.setColorFilter(ContextCompat.getColor(context, R.color.fare_collected));
            } else {
                ivFareStatus.setImageResource(R.drawable.ic_warning);
                ivFareStatus.setColorFilter(ContextCompat.getColor(context, R.color.fare_not_collected));
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onJourneyClick(journey);
                }
            });
        }

        private String formatTime(String timeString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(timeString);
                return date != null ? outputFormat.format(date) : timeString;
            } catch (Exception e) {
                return timeString;
            }
        }
    }
}

