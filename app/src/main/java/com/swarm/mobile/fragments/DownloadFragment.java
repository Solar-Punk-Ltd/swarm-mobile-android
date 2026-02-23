package com.swarm.mobile.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.swarm.lib.NodeInfo;
import com.swarm.lib.NodeStatus;
import com.swarm.mobile.R;

import kotlin.text.Regex;


public class DownloadFragment extends Fragment {

    private static final Regex HASH_REGEX = new Regex("^[a-fA-F0-9]{64}$");
    private MaterialButton downloadButton;
    private TextInputEditText hashInput;
    private TextInputLayout hashInputLayout;
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
        hashInputLayout = view.findViewById(R.id.hashInputLayout);

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

    public void setDownloadListener(DownloadListener listener) {
        this.downloadListener = listener;
    }

}
