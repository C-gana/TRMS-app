package com.cgana.trmsdriver.ui.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.model.Seat;

import java.util.List;
import java.util.Locale;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {

    public interface OnSeatActionListener {
        void onBoardSeat(Seat seat);
    }

    private List<Seat> seats;
    private OnSeatActionListener listener;
    private Context context;

    public SeatAdapter(Context context, List<Seat> seats, OnSeatActionListener listener) {
        this.seats = seats;
        this.listener = listener;
        this.context = context;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View seatCard = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seat_card, parent, false);
        return new SeatViewHolder(seatCard);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        holder.bind(seats.get(position));
    }

    @Override
    public int getItemCount() {
        return seats == null ? 0 : seats.size();
    }

    class SeatViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSeatNumber, tvSeatState, tvSeatTimer, tvDestination, tvJourneyDetail;
        private ImageView ivSeatState, ivAlert;

        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeatNumber = itemView.findViewById(R.id.tvSeatNumber);
            tvSeatState = itemView.findViewById(R.id.tvSeatState);
            tvSeatTimer = itemView.findViewById(R.id.tvSeatTimer);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvJourneyDetail = itemView.findViewById(R.id.tvJourneyDetail);
            ivSeatState = itemView.findViewById(R.id.ivSeatState);
            ivAlert = itemView.findViewById(R.id.ivAlert);
        }

        public void bind(Seat seat) {
            tvSeatNumber.setText("SEAT " + seat.getSeat_number());

            // Reset visibility
            tvSeatTimer.setVisibility(View.GONE);
            tvDestination.setVisibility(View.GONE);
            tvJourneyDetail.setVisibility(View.GONE);
            ivAlert.setVisibility(View.GONE);

            String status = seat.getStatus() != null ? seat.getStatus() : "vacant";

            switch (status) {
                case "vacant":
                    itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.seat_vacant));
                    tvSeatState.setText(R.string.seat_vacant);
                    tvSeatState.setTextColor(ContextCompat.getColor(context, R.color.status_vacant));
                    ivSeatState.setImageResource(R.drawable.ic_seat_vacant);
                    ivSeatState.setColorFilter(ContextCompat.getColor(context, R.color.seat_vacant));
                    itemView.setEnabled(true);
                    itemView.setAlpha(1f);
                    itemView.setOnClickListener(v -> {
                        if (listener != null) listener.onBoardSeat(seat);
                    });
                    break;

                case "awaiting_destination":
                    itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.seat_awaiting));
                    tvSeatState.setText(R.string.seat_awaiting);
                    tvSeatState.setTextColor(ContextCompat.getColor(context, R.color.status_awaiting));
                    ivSeatState.setImageResource(R.drawable.ic_seat_awaiting);
                    ivSeatState.setColorFilter(ContextCompat.getColor(context, R.color.seat_awaiting));
                    if (seat.getTimeout_seconds() != null && seat.getTimeout_seconds() > 0) {
                        tvSeatTimer.setText(String.format(Locale.getDefault(), "⏱️ %ds", seat.getTimeout_seconds()));
                        tvSeatTimer.setTextColor(ContextCompat.getColor(context, R.color.status_awaiting));
                        tvSeatTimer.setVisibility(View.VISIBLE);
                    }
                    itemView.setEnabled(false);
                    itemView.setAlpha(0.7f);
                    itemView.setOnClickListener(null);
                    break;

                case "active_journey":
                    itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.seat_active));
                    tvSeatState.setText(R.string.seat_active);
                    tvSeatState.setTextColor(ContextCompat.getColor(context, R.color.status_active));
                    ivSeatState.setImageResource(R.drawable.ic_seat_active);
                    ivSeatState.setColorFilter(ContextCompat.getColor(context, R.color.seat_active));

                    if (seat.getDestination() != null && !seat.getDestination().isEmpty()) {
                        tvDestination.setText(seat.getDestination());
                        tvDestination.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                        tvDestination.setVisibility(View.VISIBLE);
                    }

                    StringBuilder details = new StringBuilder();
                    if (seat.getDistance_to_destination() != null) {
                        details.append(String.format(Locale.getDefault(), "%.1fkm ", seat.getDistance_to_destination()));
                    }
                    if (seat.getEta_minutes() != null) {
                        details.append(String.format(Locale.getDefault(), "%dmin", seat.getEta_minutes()));
                    }
                    if (details.length() > 0) {
                        tvJourneyDetail.setText(details.toString().trim());
                        tvJourneyDetail.setVisibility(View.VISIBLE);
                    }

                    itemView.setEnabled(false);
                    itemView.setAlpha(0.9f);
                    itemView.setOnClickListener(null);
                    break;

                case "approaching_destination":
                    itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.seat_approaching));
                    tvSeatState.setText(R.string.seat_approaching);
                    tvSeatState.setTextColor(ContextCompat.getColor(context, R.color.status_approaching));
                    ivSeatState.setImageResource(R.drawable.ic_seat_approaching);
                    ivSeatState.setColorFilter(ContextCompat.getColor(context, R.color.seat_approaching));

                    if (seat.getDestination() != null && !seat.getDestination().isEmpty()) {
                        tvDestination.setText(seat.getDestination());
                        tvDestination.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                        tvDestination.setVisibility(View.VISIBLE);
                    }

                    StringBuilder details2 = new StringBuilder();
                    if (seat.getDistance_to_destination() != null) {
                        details2.append(String.format(Locale.getDefault(), "%.1fkm ", seat.getDistance_to_destination()));
                    }
                    if (seat.hasAlert()) {
                        details2.append("⚠️");
                        ivAlert.setVisibility(View.VISIBLE);
                    }
                    if (details2.length() > 0) {
                        tvJourneyDetail.setText(details2.toString().trim());
                        tvJourneyDetail.setVisibility(View.VISIBLE);
                    }

                    itemView.setEnabled(false);
                    itemView.setAlpha(1f);
                    itemView.setOnClickListener(null);
                    break;

                default:
                    itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.seat_vacant));
                    tvSeatState.setText(context.getString(R.string.seat_unknown));
                    tvSeatState.setTextColor(Color.RED);
                    ivSeatState.setImageResource(R.drawable.ic_seat_vacant);
                    itemView.setEnabled(false);
                    itemView.setAlpha(0.5f);
                    itemView.setOnClickListener(null);
                    break;
            }
        }
    }
}

