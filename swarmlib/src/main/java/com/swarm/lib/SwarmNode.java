package com.swarm.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * SwarmNode - Main class for Swarm functionality
 */
public class SwarmNode {
    private String nodeId;
    private boolean isRunning;
    private List<SwarmNodeListener> listeners;
    
    public interface SwarmNodeListener {
        void onStatusChanged(String status);
        void onPeerConnected(String peerId);
        void onPeerDisconnected(String peerId);
    }
    
    public SwarmNode() {
        this.nodeId = generateNodeId();
        this.isRunning = false;
        this.listeners = new ArrayList<>();
    }
    
    private String generateNodeId() {
        return "node-" + System.currentTimeMillis();
    }
    
    public void addListener(SwarmNodeListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(SwarmNodeListener listener) {
        listeners.remove(listener);
    }
    
    public void start() {
        isRunning = true;
        notifyStatusChanged("Started");
    }
    
    public void stop() {
        isRunning = false;
        notifyStatusChanged("Stopped");
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public String getStatus() {
        return isRunning ? "Running" : "Stopped";
    }
    
    private void notifyStatusChanged(String status) {
        for (SwarmNodeListener listener : listeners) {
            listener.onStatusChanged(status);
        }
    }
    
    public void connectPeer(String peerId) {
        if (isRunning) {
            for (SwarmNodeListener listener : listeners) {
                listener.onPeerConnected(peerId);
            }
        }
    }
    
    public void disconnectPeer(String peerId) {
        if (isRunning) {
            for (SwarmNodeListener listener : listeners) {
                listener.onPeerDisconnected(peerId);
            }
        }
    }
}
