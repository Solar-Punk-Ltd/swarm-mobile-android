package com.swarm.lib;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import beelite.Beelite;
import beelite.Beelite_;
import beelite.LiteOptions;

public class SwarmNode {
    private NodeInfo nodeInfo;
    private Beelite_ bee;
    private final List<SwarmNodeListener> listeners;
    private final String dataDir;
    private final String password;
    private final String rpcEndpoint;
    private final String natAddress;


    public SwarmNode(String dataDir, String password, String rpcEndpoint, String natAddress) {
        this.listeners = new ArrayList<>();
        this.dataDir = dataDir;
        this.password = password;
        this.rpcEndpoint = rpcEndpoint;
        this.natAddress = natAddress;
        this.nodeInfo = new NodeInfo("", NodeStatus.Started);
    }

    public void addListener(SwarmNodeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SwarmNodeListener listener) {
        listeners.remove(listener);
    }

    public void updateStatus(NodeStatus nodeStatus) {
        this.nodeInfo = new NodeInfo("", nodeStatus);
        notifyNodeInfoChanged();
    }

    public void updateNodeInfo(String walletAddress, NodeStatus nodeStatus) {
        this.nodeInfo = new NodeInfo(walletAddress, nodeStatus);
        notifyNodeInfoChanged();
    }

    public void start() {
        updateNodeInfo(nodeInfo.walletAddress(), NodeStatus.Started);
        try {
            this.bee = connect();
            updateNodeInfo(bee.walletAddress(), NodeStatus.Running);
        } catch (RuntimeException re) {
            updateNodeInfo(nodeInfo.walletAddress(), NodeStatus.Stopped);
            throw re;
        }
    }

    public void stop() {

        notifyNodeInfoChanged();
    }

    public boolean isRunning() {
        return this.nodeInfo.status() == NodeStatus.Running;
    }


    private void notifyNodeInfoChanged() {
        for (SwarmNodeListener listener : listeners) {
            listener.onNodeInfoChanged(this.nodeInfo);
        }
    }

    public long getConnectedPeers() {
        if (isRunning()) {
            if (bee == null) {
                throw new RuntimeException("Bee is not initialized");
            }

            return bee.connectedPeerCount();
        }

        return 0;
    }

    public void download(String hash) {
        if (isRunning()) {
            try {
                var file = bee.download(hash);

                if (file == null) {
                    Logger.getLogger(this.getClass().getName(), "").info("Download failed: file is null for hash " + hash);
                    return;
                }

                for (SwarmNodeListener listener : listeners) {
                    listener.onDownloadFinished(file.getName(), file.getData());
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    @NonNull
    private LiteOptions getLiteOptions() {
        String dataDir = (this.dataDir != null && !this.dataDir.isEmpty()) ? this.dataDir : "/data/data/beelite";

        var options = new LiteOptions();
        options.setFullNodeMode(false);
        options.setBootnodeMode(false);
        options.setBootnodes("/dnsaddr/mainnet.ethswarm.org");
        options.setDataDir(dataDir);
        options.setWelcomeMessage("welcomeMessage");
        options.setBlockchainRpcEndpoint(rpcEndpoint);
        options.setSwapInitialDeposit("0");
        options.setPaymentThreshold("100000000");
        options.setSwapEnable(false);
        options.setChequebookEnable(true);
        options.setUsePostageSnapshot(false);
        options.setMainnet(true);
        options.setNetworkID(1);
        options.setNATAddr(natAddress);
        options.setRetrievalCaching(true);

        return options;
    }

    private Beelite_ connect() {
        var options = getLiteOptions();

        Beelite_ bee = null;
        try {
            bee = Beelite.start(options, password, "3");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (bee == null) {
            throw new RuntimeException("Bee is not defined");
        }

        return bee;
    }
}
