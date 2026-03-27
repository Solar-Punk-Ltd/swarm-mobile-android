package com.swarm.mobile;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;

import com.swarm.interfaces.StampListener;
import com.swarm.interfaces.SwarmNodeListener;
import com.swarm.interfaces.UploadListener;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import mobile.Mobile;
import mobile.MobileNode;
import mobile.MobileNodeOptions;
import mobile.StampData;


public class SwarmNode {

    private static final Long DEFAULT_VALUE = 32 * 1024 * 1024L;
    private NodeInfo nodeInfo;
    private MobileNode mobileNode;
    private final List<SwarmNodeListener> listeners;
    private final String dataDir;
    private final String password;
    private final String rpcEndpoint;
    private final boolean lightModeEnabled;
    private final boolean cacheEnabled;

    private NetworkPriceProvider networkPriceProvider;


    public SwarmNode(String dataDir, String password, String rpcEndpoint, boolean lightModeEnabled, boolean cacheEnabled) {
        this.listeners = new ArrayList<>();
        this.dataDir = dataDir;
        this.password = password;
        this.rpcEndpoint = rpcEndpoint;
        this.lightModeEnabled = lightModeEnabled;
        this.cacheEnabled = cacheEnabled;
        this.nodeInfo = new NodeInfo("", "", "", NodeStatus.Started);
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
        try {
            this.mobileNode = connect();
            var blockchainData = mobileNode.blockchainData();
            updateNodeInfo(blockchainData.getWalletAddress(), blockchainData.getChequebookAddress(), blockchainData.getChequebookBalance(), NodeStatus.Running);
        } catch (Exception e) {
            updateNodeInfo(nodeInfo.walletAddress(), nodeInfo.chequebookAddress(), nodeInfo.chequebookBalance(), NodeStatus.Stopped);
            throw new RuntimeException(e);
        }
    }

    public void stopNode() throws Exception {
        updateNodeInfo("", "", "", NodeStatus.Stopped);
        this.listeners.clear();
        if (this.mobileNode != null) {
            this.mobileNode.shutdown();
            this.mobileNode = null;
        }
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

    public void getAllStamps(StampListener listener) {

        if (mobileNode == null) {
            throw new RuntimeException("Bee is not initialized");
        }

        if (isRunning()) {
            new Thread(() ->
            {
                mobileNode.fetchStamps();
                var count = mobileNode.getStampCount();
                if (count == 0) {
                    Log.i("SwarmNode", "No stamps found");
                    return;
                }

                Log.i("SwarmNode", "Total stamps: " + count);

                var list = new ArrayList<Stamp>();
                for (int i = 0; i < count; i++) {
                    var stampData = mobileNode.getStamp(i);
                    Log.i("SwarmNode", "Stamp " + i + ": " + stampData.getLabel() + ", batchID: " + stampData.getBatchIdHex());
                    list.add(convertStampDataToStamp(stampData));
                }

                listener.stampsReceived(list);
            }).start();
        }

    }

    private static Stamp convertStampDataToStamp(StampData stampData) {
        // bee assigns 'recovered' to stamps created without a label; treat it as no label
        String label = stampData.getLabel();
        if ("recovered".equals(label)) {
            label = "";
        }
        return new Stamp(
                label,
                stampData.getBatchIdHex(),
                stampData.getBatchAmount(),
                stampData.getBatchDepth(),
                stampData.getBucketDepth(),
                stampData.getImmutableFlag()
        );
    }

    public void buyStamp(String amount, String depth, String label, boolean immutable, StampListener listener) {
        if (isRunning()) {
            new Thread(() -> {
                try {
                    var hash = mobileNode.buyStamp(amount, depth, label, immutable);
                    listener.stampCreated(hash);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }).start();
        }
    }

    public void download(String hash) {
        if (isRunning()) {
            new Thread(() -> {
                try {
                    var result = mobileNode.download(hash);

                    if (result == null || result.getFile() == null) {
                        Logger.getLogger(this.getClass().getName()).info("File not found for hash on Swarm: " + hash);
                        notifyHashNotFound();
                        return;
                    }

                    var file = result.getFile();
                    var rateInMBps = result.getStats() != null ? result.getStats().getRateInMBps() : null;

                    notifyDownloadSuccess(file.getName(), file.getData(), rateInMBps);
                } catch (Exception e) {
                    var message = e.getMessage();
                    notifyDownloadFailed(hash, message);
                    Logger.getLogger(this.getClass().getName()).severe("Unexpected error during download: " + message);
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    private void notifyHashNotFound() {
        for (SwarmNodeListener listener : listeners) {
            listener.onHashNotFound();
        }
    }

    private void notifyDownloadSuccess(String fileName, byte[] data, String downloadRateMBps) {
        for (SwarmNodeListener listener : listeners) {
            listener.onDownloadSuccess(fileName, data, downloadRateMBps);
        }
    }

    private void notifyDownloadFailed(String hash, String errorMessage) {
        for (SwarmNodeListener listener : listeners) {
            listener.onDownloadFailed(hash, errorMessage);
        }
    }

    public void upload(Uri fileUri, ContentResolver contentResolver, String filename, String contentType, Stamp stamp,
                       UploadListener uploadListener
    ) {
        if (isRunning()) {
            new Thread(() -> {
                byte[] content;
                try {
                    // Get exact file size to pre-allocate the buffer and avoid OOM from ByteArrayOutputStream growth
                    long fileSize = -1;
                    try (ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(fileUri, "r")) {
                        if (pfd != null) {
                            fileSize = pfd.getStatSize();
                        }
                    } catch (IOException ignored) {
                        // File size unavailable, fall back to dynamic reading
                    }

                    try (InputStream raw = contentResolver.openInputStream(fileUri)) {
                        if (raw == null) {
                            uploadListener.onUploadFailed("Failed to read file: could not open input stream");
                            return;
                        }
                        if (fileSize > 0) {
                            // Pre-allocate exact size — no re-allocation, no OOM surprise
                            content = new byte[(int) fileSize];
                            int offset = 0;
                            int remaining = content.length;
                            int bytesRead;
                            while (remaining > 0 && (bytesRead = raw.read(content, offset, remaining)) != -1) {
                                offset += bytesRead;
                                remaining -= bytesRead;
                            }
                            if (offset < content.length) {
                                // Fewer bytes than expected — trim
                                content = java.util.Arrays.copyOf(content, offset);
                            }
                        } else {
                            // Unknown size fallback: chunked read into a list, then assemble once
                            java.util.List<byte[]> chunks = new java.util.ArrayList<>();
                            int totalBytes = 0;
                            byte[] chunk = new byte[65536];
                            int bytesRead;
                            while ((bytesRead = raw.read(chunk, 0, chunk.length)) != -1) {
                                chunks.add(java.util.Arrays.copyOf(chunk, bytesRead));
                                totalBytes += bytesRead;
                            }
                            content = new byte[totalBytes];
                            int offset = 0;
                            for (byte[] c : chunks) {
                                System.arraycopy(c, 0, content, offset, c.length);
                                offset += c.length;
                            }
                        }
                    }
                } catch (IOException e) {
                    Logger.getLogger(this.getClass().getName()).severe("Failed to read file for upload: " + e.getMessage());
                    uploadListener.onUploadFailed("Failed to read file: " + e.getMessage());
                    return;
                }

                try {
                    var hash = mobileNode.upload(stamp.batchID(),
                            filename,
                            contentType,
                            false,
                            "",
                            false,
                            Byte.parseByte("0"),
                            content);

                    if (hash == null || hash.getReferenceHex().isEmpty()) {
                        Logger.getLogger(this.getClass().getName()).info("Upload failed: hash is null or empty for file " + filename);
                        uploadListener.onUploadFailed("Upload failed: hash is null or empty");
                        return;
                    }

                    var rateInMBps = hash.getStats() != null ? hash.getStats().getRateOutMBps() : null;

                    uploadListener.onUploadSuccessful(hash.getReferenceHex(), rateInMBps);
                } catch (Exception e) {
                    Logger.getLogger(this.getClass().getName()).info("Unexpected error during upload: " + e.getMessage());
                    uploadListener.onUploadFailed("Unexpected error during upload: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }).start();
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
        options.setDBOpenFilesLimit(50);
        options.setDBWriteBufferSize(DEFAULT_VALUE);
        options.setDBBlockCacheCapacity(DEFAULT_VALUE);
        options.setDBDisableSeeksCompaction(false);
        options.setRetrievalCaching(true);
        options.setCacheCapacity(cacheEnabled ? DEFAULT_VALUE : 0);

        return options;
    }

    private MobileNode connect() {
        var options = getLiteOptions();

        MobileNode node;
        try {
            node = Mobile.startNode(options, password, "3");
        } catch (Exception e) {
            this.listeners.clear();
            this.mobileNode = null;
            throw new RuntimeException(e);
        }

        if (node == null) {
            throw new RuntimeException("Bee is not defined");
        }

        return node;
    }

    public BigInteger getCurrentNetworkPrice() {
        if (networkPriceProvider == null) {
            networkPriceProvider = new NetworkPriceProvider(rpcEndpoint);
        }

        return networkPriceProvider.getNetworkPrice();
    }
}
