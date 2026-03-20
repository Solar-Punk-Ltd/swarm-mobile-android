package com.swarm.interfaces;

import com.swarm.mobile.NodeInfo;

public interface SwarmNodeListener {
    void onNodeInfoChanged(NodeInfo nodeInfo);

    void onDownloadSuccess(String filename, byte[] data, String downloadRateMBps);

    void onDownloadFailed(String hash, String errorMessage);

    void onHashNotFound();
}