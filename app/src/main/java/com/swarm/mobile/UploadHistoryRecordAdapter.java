package com.swarm.mobile;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swarm.mobile.views.TruncatedTextView;

import java.util.List;

public class UploadHistoryRecordAdapter extends RecyclerView.Adapter<UploadHistoryRecordAdapter.UploadHistoryRecordViewHolder> {

    private static final String BZZ_LINK_BASE = "https://bzz.link/bzz/";

    public interface OnRemoveListener {
        void onRemove(int position);
    }

    private final List<UploadHistoryRecord> historyRecords;
    private OnRemoveListener onRemoveListener;

    public UploadHistoryRecordAdapter(List<UploadHistoryRecord> historyRecords) {
        this.historyRecords = historyRecords;
    }

    public void setOnRemoveListener(OnRemoveListener listener) {
        this.onRemoveListener = listener;
    }

    @NonNull
    @Override
    public UploadHistoryRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upload_record, parent, false);
        return new UploadHistoryRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadHistoryRecordViewHolder holder, int position) {
        UploadHistoryRecord record = historyRecords.get(position);
        holder.bind(record);
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

    public static class UploadHistoryRecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView filenameTextView;
        private final TruncatedTextView hashTextView;
        private final TextView dateTextView;
        private final TextView transferRateTextView;
        private final TextView stampLabelTextView;
        private final TruncatedTextView stampIdTextView;
        final ImageButton removeButton;
        final ImageButton shareButton;

        public UploadHistoryRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            filenameTextView = itemView.findViewById(R.id.recordFilename);
            hashTextView = itemView.findViewById(R.id.recordHash);
            dateTextView = itemView.findViewById(R.id.recordCreationDate);
            transferRateTextView = itemView.findViewById(R.id.transferRate);
            stampLabelTextView = itemView.findViewById(R.id.recordStampLabel);
            stampIdTextView = itemView.findViewById(R.id.recordStampId);
            removeButton = itemView.findViewById(R.id.removeRecordButton);
            shareButton = itemView.findViewById(R.id.shareButton);
        }

        @SuppressLint("SetTextI18n")
        public void bind(UploadHistoryRecord record) {
            filenameTextView.setText(record.filename());

            hashTextView.setText(record.hash());
            hashTextView.setMaxLength(20);

            dateTextView.setText("Upload date: " + record.getFormattedDate());

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

            shareButton.setOnClickListener(v -> shareHash(v, record.hash()));
        }

        private void shareHash(View v, String hash) {
            if (hash == null || hash.isEmpty()) return;
            String link = BZZ_LINK_BASE + hash;

            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Swarm Link", link));
            Toast.makeText(v.getContext(), R.string.hash_link_copied, Toast.LENGTH_SHORT).show();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, link);
            Intent chooser = Intent.createChooser(shareIntent, v.getContext().getString(R.string.share_hash));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(chooser);
        }
    }
}
