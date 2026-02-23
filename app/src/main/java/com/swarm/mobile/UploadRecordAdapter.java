package com.swarm.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swarm.mobile.views.TruncatedTextView;

import java.util.List;

public class UploadRecordAdapter extends RecyclerView.Adapter<UploadRecordAdapter.UploadRecordViewHolder> {

    private final List<UploadRecord> uploadRecords;

    public UploadRecordAdapter(List<UploadRecord> uploadRecords) {
        this.uploadRecords = uploadRecords;
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
        UploadRecord record = uploadRecords.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return uploadRecords.size();
    }

    static class UploadRecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView filenameTextView;
        private final TruncatedTextView hashTextView;
        private final TextView dateTextView;
        private final TextView stampLabelTextView;
        private final TruncatedTextView stampIdTextView;

        public UploadRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            filenameTextView = itemView.findViewById(R.id.uploadRecordFilename);
            hashTextView = itemView.findViewById(R.id.uploadRecordHash);
            dateTextView = itemView.findViewById(R.id.uploadRecordDate);
            stampLabelTextView = itemView.findViewById(R.id.uploadRecordStampLabel);
            stampIdTextView = itemView.findViewById(R.id.uploadRecordStampId);
        }

        public void bind(UploadRecord record) {
            filenameTextView.setText(record.filename());

            hashTextView.setText(record.hash());
            hashTextView.setMaxLength(20);

            String dateLabel = "Upload date: " + record.getFormattedDate();
            dateTextView.setText(dateLabel);

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
