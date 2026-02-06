package com.swarm.mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.swarm.lib.NodeInfo;
import com.swarm.lib.NodeStatus;

public class UploadFragment extends Fragment {

    private MaterialButton selectFileButton;
    private MaterialButton uploadButton;

    private NodeInfo latestNodeInfo;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        selectFileButton = view.findViewById(R.id.selectFileButton);
        uploadButton = view.findViewById(R.id.uploadButton);

        selectFileButton.setOnClickListener(v -> {
            // TODO: Implement file selection
            Toast.makeText(getContext(), "File selection - Coming soon", Toast.LENGTH_SHORT).show();
        });

        uploadButton.setOnClickListener(v -> {
            // TODO: Implement upload functionality
            Toast.makeText(getContext(), "Upload - Coming soon", Toast.LENGTH_SHORT).show();
        });

        if (latestNodeInfo != null) {
            updateNodeInfo(latestNodeInfo);
        }

        return view;
    }

    public void updateNodeInfo(NodeInfo nodeInfo) {
        latestNodeInfo = nodeInfo;

        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            uploadButton.setEnabled(NodeStatus.Running == latestNodeInfo.status());
        });
    }
}
