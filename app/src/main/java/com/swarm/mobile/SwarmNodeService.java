package com.swarm.mobile;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.swarm.lib.NodeInfo;
import com.swarm.lib.SwarmNode;
import com.swarm.lib.SwarmNodeListener;

public class SwarmNodeService extends Service {
    private static final String TAG = "SwarmNodeService";
    private static final String CHANNEL_ID = "SwarmNodeServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private SwarmNode swarmNode;
    private final IBinder binder = new SwarmNodeBinder();
    private String password;
    private String rpcEndpoint;

    private final SwarmNodeListener internalListener = new SwarmNodeListener() {
        @Override
        public void onNodeInfoChanged(NodeInfo nodeInfo) {
            String message = "Status: " + nodeInfo.status().name();
            if (nodeInfo.walletAddress() != null && !nodeInfo.walletAddress().isEmpty()) {
                message += " - Wallet: " + nodeInfo.walletAddress().substring(0, Math.min(10, nodeInfo.walletAddress().length())) + "...";
            }
            updateNotification(message);
        }

        @Override
        public void onDownloadFinished(String filename, byte[] data) {
            // Not handled in service
        }
    };

    public class SwarmNodeBinder extends Binder {
        SwarmNodeService getService() {
            return SwarmNodeService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If SwarmNode is already initialized, just return (service is already running)
        if (swarmNode != null) {
            return START_STICKY;
        }

        try {
            // Start foreground service immediately (required for Android 12+)
            startForeground(NOTIFICATION_ID, createNotification("Starting Swarm Node..."));
        } catch (Exception e) {
            Log.e(TAG, "Failed to start foreground service", e);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent != null) {
            String dataDir = intent.getStringExtra(IntentKeys.DATA_DIR);
            password = intent.getStringExtra(IntentKeys.PASSWORD);
            rpcEndpoint = intent.getStringExtra(IntentKeys.RPC_ENDPOINT);

            if (dataDir != null && password != null && rpcEndpoint != null) {

                // Initialize and start SwarmNode
                swarmNode = new SwarmNode(dataDir, password, rpcEndpoint);
                swarmNode.addListener(internalListener);

                new Thread(() -> {
                    try {
                        swarmNode.start();
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to start SwarmNode", e);
                        stopSelf();
                    }
                }).start();
            } else {
                Log.w(TAG, "Missing required parameters to start SwarmNode");
                stopSelf();
            }
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping SwarmNodeService");
        super.onDestroy();
        if (swarmNode != null && swarmNode.isRunning()) {
            try {
                swarmNode.stopNode();
                Log.i(TAG, "SwarmNode stopped successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to stop SwarmNode", e);
            }
        }
        swarmNode = null;
    }

    public void startNode() {
        new Thread(() -> {
            try {
                swarmNode.start();
            } catch (Exception e) {
                Log.e(TAG, "Failed to start SwarmNode", e);
                stopSelf();
            }
        }).start();
    }

    public void stopNode() {
        if (swarmNode != null && swarmNode.isRunning()) {
            try {
                swarmNode.stopNode();
                Log.i(TAG, "SwarmNode stopped successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to stop SwarmNode", e);
            }
        }
        swarmNode = null;
    }

    public void addListener(SwarmNodeListener listener) {
        if (swarmNode != null) {
            swarmNode.addListener(listener);
        }
    }

    public void removeListener(SwarmNodeListener listener) {
        if (swarmNode != null) {
            swarmNode.removeListener(listener);
        }
    }

    public long getConnectedPeers() {
        return swarmNode != null ? swarmNode.getConnectedPeers() : 0;
    }

    public void download(String hash) {
        if (swarmNode != null) {
            swarmNode.download(hash);
        }
    }

    public boolean isRunning() {
        return swarmNode != null && swarmNode.isRunning();
    }

    @SuppressLint("MissingPermission")
    public void updateNotification(String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification(message));
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Swarm Node Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification(String message) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Swarm Node")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
    }
}
