package com.swarm.lib;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import mobile.Mobile;
import mobile.MobileNode;
import mobile.MobileNodeOptions;

public class SwarmNode {
    private NodeInfo nodeInfo;
    private MobileNode mobileNode;
    private final List<SwarmNodeListener> listeners;
    private final String dataDir;
    private final String password;
    private final String rpcEndpoint;
    private final Boolean lightModeEnabled;


    public SwarmNode(String dataDir, String password, String rpcEndpoint, Boolean lightModeEnabled) {
        this.listeners = new ArrayList<>();
        this.dataDir = dataDir;
        this.password = password;
        this.rpcEndpoint = rpcEndpoint;
        this.lightModeEnabled = lightModeEnabled;
        this.nodeInfo = new NodeInfo("", "", "",NodeStatus.Started);
    }

    public void addListener(SwarmNodeListener listener) {
        listeners.add(listener);
    }
    @SuppressWarnings("unused")
    public void removeListener(SwarmNodeListener listener) {
        listeners.remove(listener);
    }

    public void updateNodeInfo(String walletAddress, String chequebookAddress, String chequebookBalance, NodeStatus nodeStatus) {
        this.nodeInfo = new NodeInfo(walletAddress, chequebookAddress, chequebookBalance, nodeStatus);
        notifyNodeInfoChanged();
    }

    public void start() {
        updateNodeInfo(nodeInfo.walletAddress(), nodeInfo.chequebookAddress(), nodeInfo.chequebookBalance(), NodeStatus.Started);
        try {
            this.mobileNode = connect();
            var blockchainData = mobileNode.blockchainData();
            updateNodeInfo(blockchainData.getWalletAddress(), blockchainData.getChequebookAddress(), blockchainData.getChequebookBalance(), NodeStatus.Running);
        } catch (Exception e) {
            updateNodeInfo(nodeInfo.walletAddress(), nodeInfo.chequebookAddress(), nodeInfo.chequebookBalance(), NodeStatus.Stopped);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        this.mobileNode = null; // TODO implement explicit stop method in Go code
        updateNodeInfo("", "", "",NodeStatus.Stopped);
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
            if (mobileNode == null) {
                throw new RuntimeException("Bee is not initialized");
            }

            return mobileNode.connectedPeerCount();
        }

        return 0;
    }

    public void download(String hash) {
        if (isRunning()) {
            try {
                var file = mobileNode.download(hash);

                if (file == null) {
                    Logger.getLogger(this.getClass().getName()).info("Download failed: file is null for hash " + hash);
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
    private MobileNodeOptions getLiteOptions() {

        var options = new MobileNodeOptions();
        options.setFullNodeMode(false);
        options.setBootnodeMode(false);
        options.setBootnodes("/dnsaddr/mainnet.ethswarm.org");
        options.setDataDir(dataDir + "/swarm-mobile");
        options.setWelcomeMessage("welcomeMessage");
        options.setBlockchainRpcEndpoint(rpcEndpoint);
        options.setSwapInitialDeposit("0");
        options.setPaymentThreshold("100000000");
        options.setSwapEnable(lightModeEnabled);
        options.setChequebookEnable(lightModeEnabled);
        options.setUsePostageSnapshot(false);
        options.setMainnet(true);
        options.setNetworkID(1);
        options.setRetrievalCaching(true);

        return options;
    }

    private MobileNode connect() {
        var options = getLiteOptions();

        MobileNode node;
        try {
            node = Mobile.startNode(options, password, "3");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (node == null) {
            throw new RuntimeException("Bee is not defined");
        }

        return node;
    }
}
