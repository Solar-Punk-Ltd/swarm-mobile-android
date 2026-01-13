package com.swarm.mobile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.swarm.lib.SwarmNode;

import java.util.ArrayList;
import java.util.List;

public class PeersActivity extends AppCompatActivity implements SwarmNode.SwarmNodeListener {

    private SwarmNode swarmNode;
    private TextView nodeIdText;
    private TextView statusText;
    private TextView nodeModeText;
    private TextView rpcEndpointText;
    private TextView natAddressText;
    private TextView welcomeMessageText;
    private TextView peersListText;
    private List<String> connectedPeers;
    private Handler refreshHandler;
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers);

        // Get parameters from intent
        String nodeMode = getIntent().getStringExtra("nodeMode");
        String rpcEndpoint = getIntent().getStringExtra("rpcEndpoint");
        String natAddress = getIntent().getStringExtra("natAddress");
        String welcomeMessage = getIntent().getStringExtra("welcomeMessage");

        // Initialize SwarmNode
        swarmNode = new SwarmNode();
        swarmNode.addListener(this);
        connectedPeers = new ArrayList<>();

        // Initialize views
        nodeIdText = findViewById(R.id.nodeIdText);
        statusText = findViewById(R.id.statusText);
        nodeModeText = findViewById(R.id.nodeModeText);
        rpcEndpointText = findViewById(R.id.rpcEndpointText);
        natAddressText = findViewById(R.id.natAddressText);
        welcomeMessageText = findViewById(R.id.welcomeMessageText);
        peersListText = findViewById(R.id.peersListText);

        // Set configuration values
        nodeIdText.setText(swarmNode.getNodeId());
        nodeModeText.setText(nodeMode != null ? nodeMode : "N/A");
        rpcEndpointText.setText(rpcEndpoint != null ? rpcEndpoint : "N/A");
        natAddressText.setText(natAddress != null ? natAddress : "N/A");
        welcomeMessageText.setText(welcomeMessage != null ? welcomeMessage : "N/A");

        // Start the node
        swarmNode.start();

        // Setup auto-refresh for peers list (every 5 seconds)
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshPeersList();
                refreshHandler.postDelayed(this, 5000); // 5 seconds
            }
        };
        refreshHandler.post(refreshRunnable);
    }

    private void refreshPeersList() {
        // This will be called every 5 seconds to update the peers list
        updatePeersList();
    }

    @Override
    public void onStatusChanged(String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(status);
                if (status.equals("Running")) {
                    statusText.setTextColor(getResources().getColor(R.color.status_running, getTheme()));
                } else {
                    statusText.setTextColor(getResources().getColor(R.color.status_stopped, getTheme()));
                }
            }
        });
    }

    @Override
    public void onPeerConnected(String peerId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!connectedPeers.contains(peerId)) {
                    connectedPeers.add(peerId);
                    updatePeersList();
                }
            }
        });
    }

    @Override
    public void onPeerDisconnected(String peerId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectedPeers.remove(peerId);
                updatePeersList();
            }
        });
    }

    private void updatePeersList() {
        if (connectedPeers.isEmpty()) {
            peersListText.setText(R.string.no_peers);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < connectedPeers.size(); i++) {
                sb.append("• ").append(connectedPeers.get(i));
                if (i < connectedPeers.size() - 1) {
                    sb.append("\n");
                }
            }
            peersListText.setText(sb.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
        if (swarmNode != null && swarmNode.isRunning()) {
            swarmNode.stop();
        }
    }
}
