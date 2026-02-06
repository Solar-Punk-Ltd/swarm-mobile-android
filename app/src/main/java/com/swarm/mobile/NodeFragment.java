package com.swarm.mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.swarm.lib.NodeInfo;
import com.swarm.lib.NodeStatus;

public class NodeFragment extends Fragment {

    private TextView walletAddressData;
    private TextView chequebookAddressData;
    private TextView chequebookBalanceData;
    private TextView nodeStatusText;
    private TextView peerCountText;

    private NodeInfo latestNodeInfo;

    private long lastPeerCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_node, container, false);

        walletAddressData = view.findViewById(R.id.walletAddressData);
        chequebookAddressData = view.findViewById(R.id.chequebookAddressData);
        chequebookBalanceData = view.findViewById(R.id.chequebookBalanceData);
        nodeStatusText = view.findViewById(R.id.statusText);
        peerCountText = view.findViewById(R.id.peersListText);

        if (latestNodeInfo != null) {
            updateNodeInfo(latestNodeInfo);
        }

        if (lastPeerCount > 0) {
            updatePeerCount(lastPeerCount);
        }

        return view;
    }

    public void updatePeerCount(long peerCount) {
        lastPeerCount = peerCount;
        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            peerCountText.setText(String.valueOf(lastPeerCount));
        });

    }

    public void updateNodeInfo(NodeInfo nodeInfo) {
        latestNodeInfo = nodeInfo;
        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            walletAddressData.setText(latestNodeInfo.walletAddress());
            chequebookAddressData.setText(latestNodeInfo.chequebookAddress());
            chequebookBalanceData.setText(String.valueOf(latestNodeInfo.chequebookBalance()));
            nodeStatusText.setText(nodeInfo.status().name());

            if (NodeStatus.Running == nodeInfo.status()) {
                nodeStatusText.setTextColor(getResources().getColor(R.color.status_running));
            } else {
                nodeStatusText.setTextColor(getResources().getColor(R.color.status_stopped));
            }
        });
    }

}
