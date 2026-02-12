package com.swarm.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swarm.lib.Stamp;

import java.util.List;
import java.util.Locale;

public class StampAdapter extends RecyclerView.Adapter<StampAdapter.StampViewHolder> {

    private final List<Stamp> stamps;
    private final OnStampClickListener listener;

    public StampAdapter(List<Stamp> stamps, OnStampClickListener listener) {
        this.stamps = stamps;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StampViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stamp, parent, false);
        return new StampViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StampViewHolder holder, int position) {
        Stamp stamp = stamps.get(position);
        holder.bind(stamp, listener);
    }

    @Override
    public int getItemCount() {
        return stamps.size();
    }

    static public class StampViewHolder extends RecyclerView.ViewHolder {
        private final TextView stampIdText;
        private final TextView stampDetailsText;

        public StampViewHolder(@NonNull View itemView) {
            super(itemView);
            stampIdText = itemView.findViewById(R.id.stampIdText);
            stampDetailsText = itemView.findViewById(R.id.stampDetailsText);
        }

        public void bind(Stamp stamp, OnStampClickListener listener) {
            String displayLabel = (stamp.label() != null && !stamp.label().isEmpty())
                ? stamp.label()
                : "Stamp";

            // bee sets 'recovered' string when no label is provided on stamp creation
            if (displayLabel.equals("recovered")) {
                displayLabel = "N/A";
            }

            stampIdText.setText(displayLabel);

            String batchIdHex = bytesToHex(stamp.batchID());
            String shortBatchId = batchIdHex.length() > 16
                ? batchIdHex.substring(0, 16) + "..."
                : batchIdHex;

            String details = String.format(Locale.US, "ID: %s\nCapacity (%s): %s \nDepth: %d",
                shortBatchId,
                stamp.immutable() ? "immutable" : "mutable",
                stamp.amount(),
                stamp.depth());

            stampDetailsText.setText(details);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStampClick(stamp);
                }
            });
        }

        private String bytesToHex(byte[] bytes) {
            if (bytes == null || bytes.length == 0) {
                return "";
            }
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }
}
