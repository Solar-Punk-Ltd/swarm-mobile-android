package com.swarm.mobile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.swarm.lib.NodeInfo;
import com.swarm.lib.NodeStatus;
import com.swarm.lib.Stamp;
import com.swarm.lib.StampListener;
import com.swarm.lib.SwarmNode;

import java.util.List;
import java.util.Locale;

public class UploadFragment extends Fragment implements StampListener, OnStampClickListener {

    private MaterialButton uploadButton;
    private MaterialButton selectStampButton;
    private MaterialButton createStampButton;

    private MaterialCardView selectedStampCard;
    private TextView selectedStampLabel;
    private TextView selectedStampDetails;

    private NodeInfo latestNodeInfo;
    private Stamp selectedStamp = null;

    private final SwarmNode swarmNode;

    public UploadFragment(SwarmNode swarmNode) {
        this.swarmNode = swarmNode;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        MaterialButton selectFileButton = view.findViewById(R.id.selectFileButton);
        uploadButton = view.findViewById(R.id.uploadButton);
        selectStampButton = view.findViewById(R.id.selectStampButton);
        createStampButton = view.findViewById(R.id.createStampButton);

        selectedStampCard = view.findViewById(R.id.selectedStampCard);
        selectedStampLabel = view.findViewById(R.id.selectedStampLabel);
        selectedStampDetails = view.findViewById(R.id.selectedStampDetails);

        selectFileButton.setOnClickListener(v -> {
            // TODO: Implement file selection
            Toast.makeText(getContext(), "File selection - Coming soon", Toast.LENGTH_SHORT).show();
        });

        uploadButton.setOnClickListener(v -> {
            // TODO: Implement upload functionality
            Toast.makeText(getContext(), "Upload - Coming soon", Toast.LENGTH_SHORT).show();
        });

        selectStampButton.setOnClickListener(v -> this.getAllStamps());

        createStampButton.setOnClickListener(v -> showCreateStampDialog());

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

        String batchIdHex = bytesToHex(selectedStamp.batchID());
        String shortBatchId = batchIdHex.length() > 16
                ? batchIdHex.substring(0, 16) + "..."
                : batchIdHex;

        String details = String.format(Locale.US, """
                        ID: %s
                        Capacity (%s): %s\
                        Depth: %d""",
                shortBatchId,
                selectedStamp.immutable() ? "immutable" : "mutable",
                selectedStamp.amount(),
                selectedStamp.depth());

        selectedStampDetails.setText(details);
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
