package com.swarm.mobile;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.swarm.lib.SwarmNode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwarmNode.SwarmNodeListener {

    private SwarmNode swarmNode;
    private TextView nodeIdText;
    private TextView statusText;
    private TextView peersListText;
    private MaterialButton startButton;
    private MaterialButton stopButton;
    private MaterialButton connectPeerButton;
    private TextInputEditText peerIdInput;
    private List<String> connectedPeers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SwarmNode
        swarmNode = new SwarmNode();
        swarmNode.addListener(this);
        connectedPeers = new ArrayList<>();

        // Initialize views
        nodeIdText = findViewById(R.id.nodeIdText);
        statusText = findViewById(R.id.statusText);
        peersListText = findViewById(R.id.peersListText);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        connectPeerButton = findViewById(R.id.connectPeerButton);
        peerIdInput = findViewById(R.id.peerIdInput);

        // Set node ID
        nodeIdText.setText(swarmNode.getNodeId());

        // Set button listeners
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNode();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopNode();
            }
        });

        connectPeerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToPeer();
            }
        });
    }

    private void startNode() {
        swarmNode.start();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        connectPeerButton.setEnabled(true);
        Toast.makeText(this, "Swarm node started", Toast.LENGTH_SHORT).show();
    }

    private void stopNode() {
        swarmNode.stop();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        connectPeerButton.setEnabled(false);
        
        // Clear peers on stop
        connectedPeers.clear();
        updatePeersList();
        
        Toast.makeText(this, "Swarm node stopped", Toast.LENGTH_SHORT).show();
    }

    private void connectToPeer() {
        String peerId = peerIdInput.getText().toString().trim();
        if (!peerId.isEmpty()) {
            swarmNode.connectPeer(peerId);
            peerIdInput.setText("");
            Toast.makeText(this, "Connected to peer: " + peerId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please enter a peer ID", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(status);
                if (status.equals("Running")) {
                    statusText.setTextColor(getResources().getColor(R.color.status_running, null));
                } else {
                    statusText.setTextColor(getResources().getColor(R.color.status_stopped, null));
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
        if (swarmNode != null && swarmNode.isRunning()) {
            swarmNode.stop();
        }
    }
}
