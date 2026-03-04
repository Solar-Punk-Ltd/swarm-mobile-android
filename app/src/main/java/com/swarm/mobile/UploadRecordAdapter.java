package com.swarm.mobile;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swarm.mobile.views.TruncatedTextView;

import java.util.List;

public class UploadRecordAdapter extends RecyclerView.Adapter<UploadRecordAdapter.UploadRecordViewHolder> {

    public interface OnRemoveListener {
        void onRemove(int position);
    }

    private final List<HistoryRecord> historyRecords;
    private final String datePrefix;
    private OnRemoveListener onRemoveListener;

    public UploadRecordAdapter(List<HistoryRecord> historyRecords) {
        this(historyRecords, "Upload date: ");
    }

    public UploadRecordAdapter(List<HistoryRecord> historyRecords, String datePrefix) {
        this.historyRecords = historyRecords;
        this.datePrefix = datePrefix;
    }

    public void setOnRemoveListener(OnRemoveListener listener) {
        this.onRemoveListener = listener;
    }

    @NonNull
    @Override
    public UploadRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upload_record, parent, false);
        return new UploadRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadRecordViewHolder holder, int position) {
        HistoryRecord record = historyRecords.get(position);
        holder.bind(record, datePrefix);
        holder.removeButton.setOnClickListener(v -> {
            if (onRemoveListener != null) {
                onRemoveListener.onRemove(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyRecords.size();
    }

    public static class UploadRecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView filenameTextView;
        private final TruncatedTextView hashTextView;
        private final TextView dateTextView;
        private final TextView transferRateTextView;
        private final TextView stampLabelTextView;
        private final TruncatedTextView stampIdTextView;
        final ImageButton removeButton;

        public UploadRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            filenameTextView = itemView.findViewById(R.id.uploadRecordFilename);
            hashTextView = itemView.findViewById(R.id.uploadRecordHash);
            dateTextView = itemView.findViewById(R.id.uploadRecordDate);
            transferRateTextView = itemView.findViewById(R.id.transferRate);
            stampLabelTextView = itemView.findViewById(R.id.uploadRecordStampLabel);
            stampIdTextView = itemView.findViewById(R.id.uploadRecordStampId);
            removeButton = itemView.findViewById(R.id.removeRecordButton);
        }

        @SuppressLint("SetTextI18n")
        public void bind(HistoryRecord record, String datePrefix) {
            filenameTextView.setText(record.filename());

            hashTextView.setText(record.hash());
            hashTextView.setMaxLength(20);

            String dateLabel = datePrefix + record.getFormattedDate();
            dateTextView.setText(dateLabel);

            String rate = record.transferRateMBps();
            if (rate != null && !rate.isEmpty()) {
                transferRateTextView.setText("Transfer rate: " + rate);
                transferRateTextView.setVisibility(View.VISIBLE);
            } else {
                transferRateTextView.setVisibility(View.GONE);
            }

            String stampName = record.stampLabel();
            if (stampName != null && !stampName.isEmpty() && !stampName.equals("recovered")) {
                stampLabelTextView.setText("Stamp: " + stampName);
            } else {
                stampLabelTextView.setText("Stamp: N/A");
            }

            stampIdTextView.setText(record.stampId());
            stampIdTextView.setMaxLength(20);
        }
    }
}
