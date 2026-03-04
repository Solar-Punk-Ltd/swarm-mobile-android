package com.swarm.mobile.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.swarm.lib.NodeInfo;
import com.swarm.lib.NodeStatus;
import com.swarm.mobile.R;
import com.swarm.mobile.HistoryRecord;
import com.swarm.mobile.UploadRecordAdapter;
import com.swarm.mobile.storage.DownloadHistoryStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.text.Regex;


public class DownloadFragment extends Fragment {

    private static final Regex HASH_REGEX = new Regex("^[a-fA-F0-9]{64}$");
    private MaterialButton downloadButton;
    private MaterialButton showDownloadsButton;
    private TextInputEditText hashInput;
    private TextInputLayout hashInputLayout;
    private TextView downloadCountText;
    private DownloadListener downloadListener;

    private NodeInfo latestNodeInfo;
    private String lastRequestedHash = null;

    private final List<HistoryRecord> downloadHistory = new ArrayList<>();
    private DownloadHistoryStorage downloadHistoryStorage;


    public interface DownloadListener {
        void onDownloadRequested(String hash);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null) {
            downloadHistoryStorage = new DownloadHistoryStorage(getContext());

            if (downloadHistory.isEmpty()) {
                List<HistoryRecord> savedHistory = downloadHistoryStorage.loadDownloadHistory();
                downloadHistory.addAll(savedHistory);
                Log.i("DownloadFragment", "Loaded " + savedHistory.size() + " download records from storage");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);

        downloadButton = view.findViewById(R.id.downloadByHashButton);
        hashInput = view.findViewById(R.id.hashInput);
        hashInputLayout = view.findViewById(R.id.hashInputLayout);
        downloadCountText = view.findViewById(R.id.downloadCountText);

        MaterialButton showDownloadsButton = view.findViewById(R.id.showDownloadsButton);
        this.showDownloadsButton = showDownloadsButton;
        showDownloadsButton.setOnClickListener(v -> showDownloadHistoryDialog());

        hashInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateHashInput();
            }
        });

        downloadButton.setOnClickListener(v -> {
            if (downloadListener != null && isHashValid()) {
                @SuppressWarnings("DataFlowIssue") String hash = hashInput.getText().toString().trim();
                lastRequestedHash = hash;
                downloadListener.onDownloadRequested(hash);
            }
        });

        if (latestNodeInfo != null) {
            updateNodeInfo(latestNodeInfo);
        }

        updateDownloadCount();

        return view;
    }

    public void onDownloadSuccess(String filename, String downloadRateMBps) {
        String hash = lastRequestedHash != null ? lastRequestedHash : "";
        HistoryRecord record = new HistoryRecord(filename, hash, System.currentTimeMillis(), "", "", downloadRateMBps);

        if (!hash.isEmpty()) {
            downloadHistory.removeIf(r -> hash.equals(r.hash()));
        }
        downloadHistory.add(0, record);

        if (downloadHistoryStorage != null) {
            downloadHistoryStorage.saveDownloadHistory(downloadHistory);
        }

        if (getActivity() != null) {
            getActivity().runOnUiThread(this::updateDownloadCount);
        }
    }

    public void updateNodeInfo(NodeInfo nodeInfo) {
        latestNodeInfo = nodeInfo;

        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(this::updateDownloadButtonState);
    }

    private void validateHashInput() {
        String input = hashInput.getText() != null ? hashInput.getText().toString().trim() : "";

        if (input.isEmpty()) {
            hashInputLayout.setError(null);
            hashInputLayout.setErrorEnabled(false);
        } else if (!isHashValid()) {
            hashInputLayout.setError("Invalid hash format. Must be 64 hexadecimal characters.");
            hashInputLayout.setErrorEnabled(true);
        } else {
            hashInputLayout.setError(null);
            hashInputLayout.setErrorEnabled(false);
        }

        updateDownloadButtonState();
    }

    private boolean isHashValid() {
        return hashInput.getText() != null && HASH_REGEX.matches(hashInput.getText().toString().trim());
    }

    private void updateDownloadButtonState() {
        boolean isNodeRunning = latestNodeInfo != null && NodeStatus.Running == latestNodeInfo.status();

        downloadButton.setEnabled(isNodeRunning && isHashValid() &&
                                   hashInput.getText() != null &&
                                   !hashInput.getText().toString().trim().isEmpty());
    }

    private void updateDownloadCount() {
        if (downloadCountText != null) {
            String countText = String.format(Locale.US,
                    getString(R.string.downloads_count),
                    downloadHistory.size());
            downloadCountText.setText(countText);
        }
        if (showDownloadsButton != null) {
            showDownloadsButton.setEnabled(!downloadHistory.isEmpty());
        }
    }

    private void showDownloadHistoryDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_download_history, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.downloadRecordsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        UploadRecordAdapter adapter = getUploadRecordAdapter();
        recyclerView.setAdapter(adapter);

        dialogView.findViewById(R.id.clearHistoryButton).setOnClickListener(v -> {
            if (downloadHistoryStorage != null) {
                downloadHistoryStorage.clearDownloadHistory();
            }
            int size = downloadHistory.size();
            downloadHistory.clear();
            adapter.notifyItemRangeRemoved(0, size);
            updateDownloadCount();
        });

        dialogView.findViewById(R.id.closeButton).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @NonNull
    private UploadRecordAdapter getUploadRecordAdapter() {
        UploadRecordAdapter adapter = new UploadRecordAdapter(downloadHistory, "Download date: ");
        adapter.setOnRemoveListener(position -> {
            if (position >= 0 && position < downloadHistory.size()) {
                downloadHistory.remove(position);
                adapter.notifyItemRemoved(position);
                if (downloadHistoryStorage != null) {
                    downloadHistoryStorage.saveDownloadHistory(downloadHistory);
                }
                updateDownloadCount();
            }
        });
        return adapter;
    }

    public void setDownloadListener(DownloadListener listener) {
        this.downloadListener = listener;
    }

}
