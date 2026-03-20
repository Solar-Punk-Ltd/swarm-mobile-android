package com.swarm.mobile.fragments;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.swarm.mobile.NodeInfo;
import com.swarm.mobile.NodeStatus;
import com.swarm.mobile.NodeMode;
import com.swarm.mobile.R;
import com.swarm.mobile.utils.NumberUtils;
import com.swarm.mobile.views.TruncatedTextView;


public class NodeFragment extends Fragment {

    private TruncatedTextView walletAddressData;
    private TruncatedTextView chequebookAddressData;
    private TextView chequebookBalanceData;
    private TextView nodeStatusText;
    private TextView peerCountText;
    private View chequebookSection;

    private NodeInfo latestNodeInfo;

    private long lastPeerCount = 0;

    private final NodeMode nodeMode;

    public NodeFragment(NodeMode nodeMode) {
        this.nodeMode = nodeMode;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_node, container, false);

        walletAddressData = view.findViewById(R.id.walletAddressData);
        chequebookAddressData = view.findViewById(R.id.chequebookAddressData);
        chequebookBalanceData = view.findViewById(R.id.chequebookBalanceData);
        chequebookSection = view.findViewById(R.id.chequebookSection);
        nodeStatusText = view.findViewById(R.id.statusText);
        peerCountText = view.findViewById(R.id.peersListText);

        walletAddressData.setMaxLength(16);
        chequebookAddressData.setMaxLength(16);

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

        getActivity().runOnUiThread(() -> peerCountText.setText(String.valueOf(lastPeerCount)));
    }

    public void updateNodeInfo(NodeInfo nodeInfo) {
        latestNodeInfo = nodeInfo;
        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            walletAddressData.setText(latestNodeInfo.walletAddress());
            chequebookAddressData.setText(latestNodeInfo.chequebookAddress());
            chequebookBalanceData.setText(NumberUtils.formatXBzz(latestNodeInfo.chequebookBalance()));

            chequebookSection.setVisibility(nodeMode == NodeMode.LIGHT ? View.VISIBLE : View.GONE);

            boolean running = NodeStatus.Running == nodeInfo.status();
            int statusColor = ContextCompat.getColor(requireContext(),
                    running ? R.color.status_running : R.color.status_stopped);

            nodeStatusText.setText(nodeInfo.status().name());
            nodeStatusText.setTextColor(statusColor);

            // Tint the pill border/background to match status
            android.graphics.drawable.Drawable pillDrawable = ContextCompat
                    .getDrawable(requireContext(), R.drawable.status_pill_background);
            if (pillDrawable != null) {
                GradientDrawable pill = (GradientDrawable) pillDrawable.mutate();
                int fillAlpha = 0x1A; // ~10%
                int fillColor = (fillAlpha << 24) | (statusColor & 0x00FFFFFF);
                pill.setColor(fillColor);
                pill.setStroke(2, statusColor);
                nodeStatusText.setBackground(pill);
            }
        });
    }



}
