package com.swarm.mobile.fragments;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.swarm.interfaces.StampListener;
import com.swarm.interfaces.UploadListener;
import com.swarm.lib.NodeInfo;
import com.swarm.lib.NodeStatus;
import com.swarm.lib.Stamp;
import com.swarm.mobile.R;
import com.swarm.mobile.StampAdapter;
import com.swarm.mobile.SwarmNodeService;
import com.swarm.mobile.UploadHistoryRecord;
import com.swarm.mobile.UploadHistoryRecordAdapter;
import com.swarm.mobile.interfaces.OnStampClickListener;
import com.swarm.mobile.storage.UploadHistoryStorage;
import com.swarm.mobile.views.TruncatedTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UploadFragment extends Fragment implements StampListener,
        OnStampClickListener,
        UploadListener {

    private MaterialButton uploadButton;
    private MaterialButton createStampButton;
    private MaterialButton selectStampButton;
    private MaterialButton showUploadsButton;

    private MaterialCardView selectedStampCard;
    private TextView selectedStampLabel;
    private TruncatedTextView selectedStampBatchId;
    private TextView selectedStampDetails;
    private TextView uploadCountText;
    private TextView noStampPlaceholder;
    private View selectedStampBatchIdRow;
    private View clearStampButton;
    private View clearFileButton;

    private View selectedFileInfoView;
    private TextView noFilePlaceholder;
    private ImageView selectedFileIcon;
    private TextView selectedFileNameText;

    private NodeInfo latestNodeInfo;
    private Stamp selectedStamp = null;

    private Uri selectedFileUri = null;
    private String selectedFileName = null;
    private String selectedFileMimeType = null;
    private ActivityResultLauncher<String> filePickerLauncher;

    private final List<UploadHistoryRecord> uploadHistory = new ArrayList<>();
    private UploadHistoryStorage uploadHistoryStorage;

    private SwarmNodeService swarmNodeService;
    private boolean isUploading = false;

    public UploadFragment() {
    }

    public void setSwarmNodeService(SwarmNodeService swarmNodeService) {
        this.swarmNodeService = swarmNodeService;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null) {
            uploadHistoryStorage = new UploadHistoryStorage(getContext());

            if (uploadHistory.isEmpty()) {
                List<UploadHistoryRecord> savedHistory = uploadHistoryStorage.loadUploadHistory();
                uploadHistory.addAll(savedHistory);
                Log.i("UploadFragment", "Loaded " + savedHistory.size() + " upload records from storage");
            } else {
                Log.i("UploadFragment", "Upload history already loaded, skipping to avoid duplicates");
            }
        }

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        readFileFromUri(uri);
                    }
                }
        );
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        View selectFileButton = view.findViewById(R.id.selectFileButton);
        selectStampButton = view.findViewById(R.id.selectStampButton);

        uploadButton = view.findViewById(R.id.uploadButton);
        createStampButton = view.findViewById(R.id.createStampButton);
        showUploadsButton = view.findViewById(R.id.showUploadsButton);

        selectedStampCard = view.findViewById(R.id.selectedStampCard);
        selectedStampLabel = view.findViewById(R.id.selectedStampLabel);
        selectedStampBatchId = view.findViewById(R.id.selectedStampBatchId);
        selectedStampDetails = view.findViewById(R.id.selectedStampDetails);
        noStampPlaceholder = view.findViewById(R.id.noStampPlaceholder);
        selectedStampBatchIdRow = view.findViewById(R.id.selectedStampBatchIdRow);
        clearStampButton = view.findViewById(R.id.clearStampButton);
        clearFileButton = view.findViewById(R.id.clearFileButton);

        uploadCountText = view.findViewById(R.id.uploadCountText);
        selectedFileInfoView = view.findViewById(R.id.selectedFileInfo);
        noFilePlaceholder = view.findViewById(R.id.noFilePlaceholder);
        selectedFileIcon = view.findViewById(R.id.selectedFileIcon);
        selectedFileNameText = view.findViewById(R.id.selectedFileNameText);

        clearStampButton.setOnClickListener(v -> {
            selectedStamp = null;
            updateSelectedStampDisplay();
            updateUploadButtonState();
        });

        clearFileButton.setOnClickListener(v -> {
            selectedFileUri = null;
            selectedFileName = null;
            selectedFileMimeType = null;
            updateSelectedFileDisplay();
            updateUploadButtonState();
        });

        selectFileButton.setOnClickListener(v -> {
            // Launch the file picker to select any file
            filePickerLauncher.launch("*/*");
        });




        uploadButton.setOnClickListener(v -> {
            if (swarmNodeService == null) {
                Log.e("UploadFragment", "SwarmNodeService is null, cannot perform upload");
                return;
            }
            var context = getContext();
            if (context == null) {
                Log.e("UploadFragment", "Context is null, cannot perform upload");
                return;
            }
            boolean started = swarmNodeService.upload(selectedFileUri, context.getContentResolver(), selectedFileName, selectedFileMimeType, selectedStamp, this);
            if (started) {
                isUploading = true;
                updateUploadButtonState();
            }
        });

        selectStampButton.setOnClickListener(v -> this.getAllStamps());

        createStampButton.setOnClickListener(v -> showCreateStampDialog());

        showUploadsButton.setOnClickListener(v -> showUploadHistoryDialog());

        updateUploadCount();
        updateSelectedStampDisplay();

        if (latestNodeInfo != null) {
            updateNodeInfo(latestNodeInfo);
        }

        if (selectedStamp != null) {
            updateSelectedStampDisplay();
        }

        return view;
    }

    public void stampsReceived(List<Stamp> stamps) {
        if (getContext() == null || getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_select_stamp, null);
            RecyclerView recyclerView = dialogView.findViewById(R.id.stampListRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .create();

            StampAdapter adapter = new StampAdapter(stamps, stamp -> {
                selectedStamp = stamp;
                updateSelectedStampDisplay();
                updateUploadButtonState();
                dialog.dismiss();
            });
            recyclerView.setAdapter(adapter);

            dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        });
    }

    private void showCreateStampDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_stamp, null);

        TextInputEditText amountInput = dialogView.findViewById(R.id.amountInput);
        TextInputEditText depthInput = dialogView.findViewById(R.id.depthInput);
        TextInputEditText labelInput = dialogView.findViewById(R.id.labelInput);
        MaterialCheckBox immutableCheckbox = dialogView.findViewById(R.id.immutableCheckbox);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.createButton).setOnClickListener(v -> {
            String amountStr = amountInput.getText() != null ? amountInput.getText().toString() : "";
            String depthStr = depthInput.getText() != null ? depthInput.getText().toString() : "";
            String label = labelInput.getText() != null ? labelInput.getText().toString() : "";
            boolean immutable = immutableCheckbox.isChecked();

            if (amountStr.isEmpty() || depthStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in amount and depth", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                if (swarmNodeService == null) return;
                swarmNodeService.buyStamp(amountStr, depthStr, label, immutable, this);
                Toast.makeText(getContext(),
                        "Creating stamp. Please wait...",
                        Toast.LENGTH_LONG).show();

                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    public void updateNodeInfo(NodeInfo nodeInfo) {
        latestNodeInfo = nodeInfo;

        if (getActivity() == null) {
            return;
        }

        updateUploadButtonState();

        var nodeRunning = NodeStatus.Running == nodeInfo.status();
        getActivity().runOnUiThread(() -> {
            selectStampButton.setEnabled(nodeRunning);
            createStampButton.setEnabled(nodeRunning);
        });
    }

    public void updateUploadButtonState() {
        if (getActivity() == null) {
            return;
        }

        var uploadEnabled = latestNodeInfo != null
                && NodeStatus.Running == latestNodeInfo.status()
                && selectedStamp != null
                && selectedFileUri != null
                && !isUploading;

        getActivity().runOnUiThread(() -> uploadButton.setEnabled(uploadEnabled));
    }

    public void setUploading(boolean uploading) {
        isUploading = uploading;
        updateUploadButtonState();
    }

    private void getAllStamps() {
        if (swarmNodeService == null) return;
        this.swarmNodeService.getAllStamps(this);
    }

    public void stampCreated(String hash) {
        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Stamp created with hash: " + hash, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onStampClick(Stamp stamp) {
        selectedStamp = stamp;
        updateSelectedStampDisplay();
        updateUploadButtonState();
    }

    private void updateSelectedFileDisplay() {
        if (selectedFileInfoView == null) return;
        if (selectedFileUri != null && selectedFileName != null) {
            noFilePlaceholder.setVisibility(View.GONE);
            selectedFileIcon.setVisibility(View.VISIBLE);
            selectedFileNameText.setText(selectedFileName);
            selectedFileNameText.setVisibility(View.VISIBLE);
            clearFileButton.setVisibility(View.VISIBLE);
        } else {
            noFilePlaceholder.setVisibility(View.VISIBLE);
            selectedFileIcon.setVisibility(View.GONE);
            selectedFileNameText.setVisibility(View.GONE);
            clearFileButton.setVisibility(View.GONE);
        }
    }

    private void updateSelectedStampDisplay() {
        if (selectedStampCard == null) return;

        if (selectedStamp == null) {
            noStampPlaceholder.setVisibility(View.VISIBLE);
            selectedStampLabel.setVisibility(View.GONE);
            selectedStampBatchIdRow.setVisibility(View.GONE);
            selectedStampDetails.setVisibility(View.GONE);
            clearStampButton.setVisibility(View.GONE);
            return;
        }

        noStampPlaceholder.setVisibility(View.GONE);
        selectedStampLabel.setVisibility(View.VISIBLE);
        selectedStampBatchIdRow.setVisibility(View.VISIBLE);
        selectedStampDetails.setVisibility(View.VISIBLE);
        clearStampButton.setVisibility(View.VISIBLE);

        String displayLabel = (selectedStamp.label() != null && !selectedStamp.label().isEmpty())
                ? selectedStamp.label()
                : "Stamp";
        selectedStampLabel.setText(displayLabel);

        selectedStampBatchId.setText(selectedStamp.batchID());
        selectedStampBatchId.setMaxLength(20);

        String details = String.format(Locale.US, """
                        Capacity (%s): %s
                        Depth: %d""",
                selectedStamp.immutable() ? "immutable" : "mutable",
                selectedStamp.amount(),
                selectedStamp.depth());

        selectedStampDetails.setText(details);
    }

    @Override
    public void onUploadSuccessful(String hash, String uploadRateMBps) {
        Log.i("UploadFragment", "Upload successful with hash: " + hash);
        if (swarmNodeService != null) swarmNodeService.onUploadFinished();
        setUploading(false);

        if (selectedFileName != null && selectedStamp != null) {
            UploadHistoryRecord existingRecord = findRecordByHash(hash);

            if (existingRecord != null) {
                uploadHistory.remove(existingRecord);
                Log.i("UploadFragment", "Found existing record for hash, updating it");
            }

            UploadHistoryRecord record = new UploadHistoryRecord(
                    selectedFileName,
                    hash,
                    System.currentTimeMillis(),
                    selectedStamp.batchID(),
                    selectedStamp.label(),
                    uploadRateMBps
            );
            uploadHistory.add(0, record);

            if (uploadHistoryStorage != null) {
                uploadHistoryStorage.saveUploadHistory(uploadHistory);
                Log.i("UploadFragment", "Saved upload history to storage");
            }
        }

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Upload successful with hash: " + hash, Toast.LENGTH_SHORT).show();
                updateUploadCount();
            });
        }
    }

    @Override
    public void onUploadFailed(String error) {
        if (swarmNodeService != null) swarmNodeService.onUploadFinished();
        setUploading(false);
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Error during upload: " + error, Toast.LENGTH_SHORT).show()
            );
        }
    }

    /**
     * Stores the URI and reads only file metadata (name, MIME type, size).
     * The actual file bytes are NOT loaded into memory here — they will be
     * streamed lazily by SwarmNode when the upload is triggered.
     */
    private void readFileFromUri(Uri uri) {
        if (getContext() == null) {
            return;
        }

        try {
            // Get the MIME type from the URI
            String mimeType = getContext().getContentResolver().getType(uri);
            selectedFileMimeType = (mimeType != null && !mimeType.isEmpty()) ? mimeType : "application/octet-stream";

            // Get the file name and size from the URI
            String fileName = null;
            long fileSize = -1;
            try (var cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                    int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex);
                    }
                }
            }

            // If we couldn't get the name from the query, try to get it from the URI path
            if (fileName == null) {
                String path = uri.getPath();
                if (path != null) {
                    int lastSlash = path.lastIndexOf('/');
                    fileName = lastSlash != -1 ? path.substring(lastSlash + 1) : path;
                }
            }

            // Default to "file" if we still don't have a name
            if (fileName == null || fileName.isEmpty()) {
                fileName = "file";
            }

            selectedFileUri = uri;
            selectedFileName = fileName;
            updateUploadButtonState();
            updateSelectedFileDisplay();

            String sizeStr = fileSize >= 0 ? fileSize + " bytes" : "size unknown";
            String message = String.format(Locale.US, "File selected: %s (%s, %s)",
                    selectedFileName, sizeStr, selectedFileMimeType);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

            Log.i("UploadFragment", "File selected: " + selectedFileName
                    + ", " + sizeStr + ", MIME type: " + selectedFileMimeType);
        } catch (Exception e) {
            Log.e("UploadFragment", "Error reading file metadata", e);
            Toast.makeText(getContext(), "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            selectedFileUri = null;
            selectedFileName = null;
            selectedFileMimeType = null;
        }
    }

    private void showUploadHistoryDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_upload_history, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.uploadRecordsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        UploadHistoryRecordAdapter adapter = getUploadRecordAdapter();
        recyclerView.setAdapter(adapter);

        dialogView.findViewById(R.id.clearHistoryButton).setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (confirmDialog, which) -> {
                    if (uploadHistoryStorage != null) {
                        uploadHistoryStorage.clearUploadHistory();
                    }
                    int size = uploadHistory.size();
                    uploadHistory.clear();
                    adapter.notifyItemRangeRemoved(0, size);
                    updateUploadCount();
                    dialog.dismiss();
                })
                .setNegativeButton("No", null)
                .show());

        dialog.show();
    }

    @NonNull
    private UploadHistoryRecordAdapter getUploadRecordAdapter() {
        UploadHistoryRecordAdapter adapter = new UploadHistoryRecordAdapter(uploadHistory);
        adapter.setOnRemoveListener(position -> {
            if (position >= 0 && position < uploadHistory.size()) {
                uploadHistory.remove(position);
                adapter.notifyItemRemoved(position);
                if (uploadHistoryStorage != null) {
                    uploadHistoryStorage.saveUploadHistory(uploadHistory);
                }
                updateUploadCount();
            }
        });

        return adapter;
    }

    private void updateUploadCount() {
        if (uploadCountText != null) {
            String countText = String.format(Locale.US,
                    getString(R.string.uploads_count),
                    uploadHistory.size());
            uploadCountText.setText(countText);
        }
        if (showUploadsButton != null) {
            showUploadsButton.setVisibility(uploadHistory.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        }
    }

    private UploadHistoryRecord findRecordByHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return null;
        }

        for (UploadHistoryRecord record : uploadHistory) {
            if (hash.equals(record.hash())) {
                return record;
            }
        }

        return null;
    }


}
