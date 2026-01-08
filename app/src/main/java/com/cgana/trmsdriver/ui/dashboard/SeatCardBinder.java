package com.cgana.trmsdriver.ui.dashboard;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.model.SeatStatus;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/**
 * Helper class to bind SeatStatus data to seat card views
 */
public class SeatCardBinder {

    /**
     * Bind seat data to a seat card view
     */
    public static void bindSeatCard(View seatCardView, SeatStatus seat, Context context) {
        if (seatCardView == null || seat == null || context == null) {
            return;
        }

        MaterialCardView cardView = (MaterialCardView) seatCardView;
        TextView tvSeatNumber = seatCardView.findViewById(R.id.tvSeatNumber);
        ImageView ivSeatState = seatCardView.findViewById(R.id.ivSeatState);
        TextView tvSeatState = seatCardView.findViewById(R.id.tvSeatState);
        TextView tvDestination = seatCardView.findViewById(R.id.tvDestination);
        TextView tvJourneyDetail = seatCardView.findViewById(R.id.tvJourneyDetail);
        TextView tvSeatTimer = seatCardView.findViewById(R.id.tvSeatTimer);
        ImageView ivAlert = seatCardView.findViewById(R.id.ivAlert);

        // Set seat number
        tvSeatNumber.setText("SEAT " + seat.getSeatNumber());

        // Reset visibility
        tvDestination.setVisibility(View.GONE);
        tvJourneyDetail.setVisibility(View.GONE);
        tvSeatTimer.setVisibility(View.GONE);
        ivAlert.setVisibility(View.GONE);

        // Configure based on seat status
        if (seat.isVacant()) {
            configureSeatVacant(cardView, tvSeatState, ivSeatState, context);
        } else if (seat.isAwaiting()) {
            configureSeatAwaiting(cardView, tvSeatState, ivSeatState, tvSeatTimer, seat, context);
        } else if (seat.isActive()) {
            configureSeatActive(cardView, tvSeatState, ivSeatState, tvDestination,
                    tvJourneyDetail, seat, context);
        } else if (seat.isApproaching()) {
            configureSeatApproaching(cardView, tvSeatState, ivSeatState, tvDestination,
                    tvJourneyDetail, ivAlert, seat, context);
        }
    }

    private static void configureSeatVacant(MaterialCardView cardView, TextView tvSeatState,
                                           ImageView ivSeatState, Context context) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface));
        cardView.setStrokeColor(ContextCompat.getColor(context, R.color.seat_vacant));
        tvSeatState.setText(R.string.seat_vacant);
        tvSeatState.setTextColor(ContextCompat.getColor(context, R.color.status_vacant));
        ivSeatState.setImageResource(R.drawable.ic_seat_vacant);
        ivSeatState.setColorFilter(ContextCompat.getColor(context, R.color.status_vacant));
    }

    private static void configureSeatAwaiting(MaterialCardView cardView, TextView tvSeatState,
                                             ImageView ivSeatState, TextView tvSeatTimer,
                                             SeatStatus seat, Context context) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface));
        cardView.setStrokeColor(ContextCompat.getColor(context, R.color.seat_awaiting));
        tvSeatState.setText(R.string.seat_awaiting);
        tvSeatState.setTextColor(ContextCompat.getColor(context, R.color.status_awaiting));
        ivSeatState.setImageResource(R.drawable.ic_seat_awaiting);
        ivSeatState.setColorFilter(ContextCompat.getColor(context, R.color.status_awaiting));

        // Show timeout if available
        if (seat.getTimeoutSeconds() != null && seat.getTimeoutSeconds() > 0) {
            tvSeatTimer.setVisibility(View.VISIBLE);
            tvSeatTimer.setText(context.getString(R.string.timeout, seat.getTimeoutSeconds()));
        }
    }

    private static void configureSeatActive(MaterialCardView cardView, TextView tvSeatState,
                                           ImageView ivSeatState, TextView tvDestination,
                                           TextView tvJourneyDetail, SeatStatus seat, Context context) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface));
        cardView.setStrokeColor(ContextCompat.getColor(context, R.color.seat_active));
        tvSeatState.setText(R.string.seat_active);
        tvSeatState.setTextColor(ContextCompat.getColor(context, R.color.status_active));
        ivSeatState.setImageResource(R.drawable.ic_seat_active);
        ivSeatState.setColorFilter(ContextCompat.getColor(context, R.color.status_active));

        // Show destination
        if (seat.getDestination() != null && !seat.getDestination().isEmpty()) {
            tvDestination.setVisibility(View.VISIBLE);
            tvDestination.setText(seat.getDestination());
        }

        // Show journey details (distance + ETA)
        if (seat.getDistanceToDestination() != null && seat.getEtaMinutes() != null) {
            tvJourneyDetail.setVisibility(View.VISIBLE);
            String distanceStr = formatDistance(seat.getDistanceToDestination());
            tvJourneyDetail.setText(context.getString(R.string.distance_eta,
                    distanceStr, seat.getEtaMinutes()));
        }
    }

    private static void configureSeatApproaching(MaterialCardView cardView, TextView tvSeatState,
                                                ImageView ivSeatState, TextView tvDestination,
                                                TextView tvJourneyDetail, ImageView ivAlert,
                                                SeatStatus seat, Context context) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface));
        cardView.setStrokeColor(ContextCompat.getColor(context, R.color.seat_approaching));
        tvSeatState.setText(R.string.seat_approaching);
        tvSeatState.setTextColor(ContextCompat.getColor(context, R.color.status_approaching));
        ivSeatState.setImageResource(R.drawable.ic_seat_approaching);
        ivSeatState.setColorFilter(ContextCompat.getColor(context, R.color.status_approaching));

        // Show destination
        if (seat.getDestination() != null && !seat.getDestination().isEmpty()) {
            tvDestination.setVisibility(View.VISIBLE);
            tvDestination.setText(seat.getDestination());
        }

        // Show journey details (distance + ETA)
        if (seat.getDistanceToDestination() != null && seat.getEtaMinutes() != null) {
            tvJourneyDetail.setVisibility(View.VISIBLE);
            String distanceStr = formatDistance(seat.getDistanceToDestination());
            tvJourneyDetail.setText(context.getString(R.string.distance_eta,
                    distanceStr, seat.getEtaMinutes()));
        }

        // Show alert indicator
        if (seat.hasAlert()) {
            ivAlert.setVisibility(View.VISIBLE);
        }
    }

    private static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            // Show in meters
            int meters = (int) (distanceKm * 1000);
            return meters + "m";
        } else {
            // Show in kilometers
            return String.format(Locale.getDefault(), "%.1fkm", distanceKm);
        }
    }
}

