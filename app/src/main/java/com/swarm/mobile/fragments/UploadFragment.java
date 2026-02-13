package com.swarm.mobile.fragments;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.swarm.interfaces.StampListener;
import com.swarm.interfaces.UploadListener;
import com.swarm.lib.NodeInfo;
import com.swarm.lib.NodeStatus;
import com.swarm.lib.Stamp;
import com.swarm.lib.SwarmNode;
import com.swarm.mobile.R;
import com.swarm.mobile.StampAdapter;
import com.swarm.mobile.UploadRecord;
import com.swarm.mobile.UploadRecordAdapter;
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
    private MaterialButton showUploadsButton;

    private MaterialCardView selectedStampCard;
    private TextView selectedStampLabel;
    private TruncatedTextView selectedStampBatchId;
    private TextView selectedStampDetails;
    private TextView uploadCountText;

    private NodeInfo latestNodeInfo;
    private Stamp selectedStamp = null;

    private byte[] selectedFileData = null;
    private String selectedFileName = null;
    private String selectedFileMimeType = null;
    private ActivityResultLauncher<String> filePickerLauncher;

    private final List<UploadRecord> uploadHistory = new ArrayList<>();
    private UploadHistoryStorage uploadHistoryStorage;

    private final SwarmNode swarmNode;

    public UploadFragment(SwarmNode swarmNode) {
        this.swarmNode = swarmNode;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null) {
            uploadHistoryStorage = new UploadHistoryStorage(getContext());

            List<UploadRecord> savedHistory = uploadHistoryStorage.loadUploadHistory();
            uploadHistory.addAll(savedHistory);
            Log.i("UploadFragment", "Loaded " + savedHistory.size() + " upload records from storage");
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

        MaterialButton selectFileButton = view.findViewById(R.id.selectFileButton);
        MaterialButton selectStampButton = view.findViewById(R.id.selectStampButton);

        uploadButton = view.findViewById(R.id.uploadButton);
        createStampButton = view.findViewById(R.id.createStampButton);
        showUploadsButton = view.findViewById(R.id.showUploadsButton);

        selectedStampCard = view.findViewById(R.id.selectedStampCard);
        selectedStampLabel = view.findViewById(R.id.selectedStampLabel);
        selectedStampBatchId = view.findViewById(R.id.selectedStampBatchId);
        selectedStampDetails = view.findViewById(R.id.selectedStampDetails);

        uploadCountText = view.findViewById(R.id.uploadCountText);

        selectFileButton.setOnClickListener(v -> {
            // Launch the file picker to select any file
            filePickerLauncher.launch("*/*");
        });

        uploadButton.setOnClickListener(v -> {
            swarmNode.upload(selectedFileData, selectedFileName, selectedFileMimeType, selectedStamp, this);
        });

        selectStampButton.setOnClickListener(v -> this.getAllStamps());

        createStampButton.setOnClickListener(v -> showCreateStampDialog());

        showUploadsButton.setOnClickListener(v -> showUploadHistoryDialog());

        updateUploadCount();

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
                Toast.makeText(getContext(), "Selected stamp: " + stamp.label(), Toast.LENGTH_SHORT).show();
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
                swarmNode.buyStamp(amountStr, depthStr, label, immutable, this);
                Toast.makeText(getContext(),
                        "Creating stamp: amount=" + amountStr + ", depth=" + depthStr +
                                ", label=" + label + ", immutable=" + immutable,
                        Toast.LENGTH_SHORT).show();

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

        getActivity().runOnUiThread(() -> {
            uploadButton.setEnabled(NodeStatus.Running == latestNodeInfo.status());
            createStampButton.setEnabled(NodeStatus.Running == latestNodeInfo.status());
        });
    }

    private void getAllStamps() {
        this.swarmNode.getAllStamps(this);
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
    }

    private void updateSelectedStampDisplay() {
        if (selectedStampCard == null) {
            return;
        }

        if (selectedStamp == null) {
            selectedStampCard.setVisibility(View.GONE);
            return;
        }

        selectedStampCard.setVisibility(View.VISIBLE);

        String displayLabel = (selectedStamp.label() != null && !selectedStamp.label().isEmpty())
                ? selectedStamp.label()
                : "Stamp";
        selectedStampLabel.setText(displayLabel);

        String batchIdHex = selectedStamp.batchID();
        selectedStampBatchId.setText(batchIdHex);
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
    public void onUploadSuccessful(String hash) {
        Log.i("UploadFragment", "Upload successful with hash: " + hash);

        if (selectedFileName != null && selectedStamp != null) {
            UploadRecord record = new UploadRecord(
                    selectedFileName,
                    hash,
                    System.currentTimeMillis(),
                    selectedStamp.batchID(),
                    selectedStamp.label()
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
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Error during upload: " + error, Toast.LENGTH_SHORT).show()
            );
        }
    }

    /**
     * Reads file data from the given URI and stores it in selectedFileData and selectedFileName
     */
    private void readFileFromUri(Uri uri) {
        if (getContext() == null) {
            return;
        }

        try {
            // Get the MIME type from the URI
            String mimeType = getContext().getContentResolver().getType(uri);
            selectedFileMimeType = (mimeType != null && !mimeType.isEmpty()) ? mimeType : "application/octet-stream";

            // Get the file name from the URI
            String fileName = null;
            try (var cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
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

            selectedFileName = fileName;

            // Read the file data
            try (InputStream inputStream = getContext().getContentResolver().openInputStream(uri)) {
                if (inputStream != null) {
                    // Read all bytes from the input stream (compatible with API 21+)
                    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                    byte[] data = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }
                    buffer.flush();
                    selectedFileData = buffer.toByteArray();

                    // Show success message
                    String message = String.format(Locale.US, "File selected: %s (%d bytes, %s)",
                            selectedFileName, selectedFileData.length, selectedFileMimeType);
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                    Log.i("UploadFragment", "File loaded: " + selectedFileName +
                            ", size: " + selectedFileData.length + " bytes" +
                            ", MIME type: " + selectedFileMimeType);
                } else {
                    Toast.makeText(getContext(), "Failed to open file", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("UploadFragment", "Error reading file", e);
            Toast.makeText(getContext(), "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            selectedFileData = null;
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

        UploadRecordAdapter adapter = new UploadRecordAdapter(uploadHistory);
        recyclerView.setAdapter(adapter);

        dialogView.findViewById(R.id.closeButton).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateUploadCount() {
        if (uploadCountText != null) {
            String countText = String.format(Locale.US,
                    getString(R.string.uploads_count),
                    uploadHistory.size());
            uploadCountText.setText(countText);
        }
    }


}
