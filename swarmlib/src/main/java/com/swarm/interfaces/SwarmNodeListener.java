package com.swarm.interfaces;

import com.swarm.lib.NodeInfo;

public interface SwarmNodeListener {
    void onNodeInfoChanged(NodeInfo nodeInfo);

    void onDownloadSuccess(String filename, byte[] data);

    void onHashNotFound();
}