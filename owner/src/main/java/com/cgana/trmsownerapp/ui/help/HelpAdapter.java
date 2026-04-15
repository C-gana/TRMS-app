package com.cgana.trmsownerapp.ui.help;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cgana.trmsownerapp.R;
import java.util.List;

public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.HelpViewHolder> {

    private List<HelpItem> items;

    public HelpAdapter(List<HelpItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public HelpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_help, parent, false);
        return new HelpViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HelpViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class HelpViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvContent;

        public HelpViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvContent = itemView.findViewById(R.id.tvContent);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    HelpItem item = items.get(position);
                    item.setExpanded(!item.isExpanded());
                    notifyItemChanged(position);
                }
            });
        }

        public void bind(HelpItem item) {
            tvTitle.setText(item.getTitle());
            tvSubtitle.setText(item.getSubtitle());

            if (item.isExpanded()) {
                tvContent.setText(item.getContent());
                tvContent.setVisibility(View.VISIBLE);
            } else {
                tvContent.setVisibility(View.GONE);
            }
        }
    }
}

