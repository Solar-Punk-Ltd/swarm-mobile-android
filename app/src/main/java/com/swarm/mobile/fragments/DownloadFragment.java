package com.swarm.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.swarm.lib.NodeInfo;
import com.swarm.lib.NodeStatus;
import com.swarm.mobile.R;

public class DownloadFragment extends Fragment {

    private MaterialButton downloadButton;
    private TextInputEditText hashInput;
    private DownloadListener downloadListener;

    private NodeInfo latestNodeInfo;


    public interface DownloadListener {
        void onDownloadRequested(String hash);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);

        downloadButton = view.findViewById(R.id.downloadByHashButton);
        hashInput = view.findViewById(R.id.hashInput);

        downloadButton.setOnClickListener(v -> {
            if (downloadListener != null && hashInput.getText() != null) {
                String hash = hashInput.getText().toString().trim();
                downloadListener.onDownloadRequested(hash);
            }
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
            downloadButton.setEnabled(NodeStatus.Running == latestNodeInfo.status());
        });
    }

    public void setDownloadListener(DownloadListener listener) {
        this.downloadListener = listener;
    }

}
