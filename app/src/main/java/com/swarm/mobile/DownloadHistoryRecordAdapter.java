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

public class DownloadHistoryRecordAdapter extends RecyclerView.Adapter<DownloadHistoryRecordAdapter.DownloadHistoryRecordViewHolder> {

    public interface OnRemoveListener {
        void onRemove(int position);
    }

    private final List<DownloadHistoryRecord> downloadHistoryRecords;
    private final String datePrefix;
    private OnRemoveListener onRemoveListener;

    public DownloadHistoryRecordAdapter(List<DownloadHistoryRecord> downloadHistoryRecords, String datePrefix) {
        this.downloadHistoryRecords = downloadHistoryRecords;
        this.datePrefix = datePrefix;
    }

    public void setOnRemoveListener(OnRemoveListener listener) {
        this.onRemoveListener = listener;
    }

    @NonNull
    @Override
    public DownloadHistoryRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download_record, parent, false);
        return new DownloadHistoryRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadHistoryRecordViewHolder holder, int position) {
        DownloadHistoryRecord record = downloadHistoryRecords.get(position);
        holder.bind(record, datePrefix);
        holder.removeButton.setOnClickListener(v -> {
            if (onRemoveListener != null) {
                onRemoveListener.onRemove(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return downloadHistoryRecords.size();
    }

    public static class DownloadHistoryRecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView filenameTextView;
        private final TruncatedTextView hashTextView;
        private final TextView dateTextView;
        private final TextView transferRateTextView;
        final ImageButton removeButton;

        public DownloadHistoryRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            filenameTextView = itemView.findViewById(R.id.recordFilename);
            hashTextView = itemView.findViewById(R.id.recordHash);
            dateTextView = itemView.findViewById(R.id.recordCreationDate);
            transferRateTextView = itemView.findViewById(R.id.transferRate);
            removeButton = itemView.findViewById(R.id.removeRecordButton);
        }

        @SuppressLint("SetTextI18n")
        public void bind(DownloadHistoryRecord record, String datePrefix) {
            filenameTextView.setText(record.filename());

            hashTextView.setText(record.hash());
            hashTextView.setMaxLength(20);

            dateTextView.setText(datePrefix + record.getFormattedDate());

            String rate = record.transferRateMBps();
            if (rate != null && !rate.isEmpty()) {
                transferRateTextView.setText("Transfer rate: " + rate);
                transferRateTextView.setVisibility(View.VISIBLE);
            } else {
                transferRateTextView.setVisibility(View.GONE);
            }
        }
    }
}
