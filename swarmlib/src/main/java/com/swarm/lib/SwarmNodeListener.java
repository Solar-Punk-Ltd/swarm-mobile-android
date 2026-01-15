package com.swarm.lib;

public interface SwarmNodeListener {
    void onNodeInfoChanged(NodeInfo nodeInfo);
    void onDownloadFinished(String filename, byte[] data);
}