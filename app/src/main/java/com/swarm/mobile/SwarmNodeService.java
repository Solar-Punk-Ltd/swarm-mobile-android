package com.swarm.mobile;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.swarm.interfaces.StampListener;
import com.swarm.interfaces.SwarmNodeListener;
import com.swarm.interfaces.UploadListener;
import com.swarm.lib.Stamp;
import com.swarm.lib.SwarmNode;

public class SwarmNodeService extends Service {
    private static final String TAG = "SwarmNodeService";
    private static final String CHANNEL_ID = "SwarmNodeServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private SwarmNode swarmNode;
    private final IBinder binder = new SwarmNodeBinder();

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
        if (swarmNode != null) {
            return START_STICKY;
        }

        try {
            startForeground(NOTIFICATION_ID, createNotification("Starting Swarm Node..."));
        } catch (Exception e) {
            Log.e(TAG, "Failed to start foreground service", e);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent != null) {
            String dataDir = intent.getStringExtra(IntentKeys.DATA_DIR);
            String password = intent.getStringExtra(IntentKeys.PASSWORD);
            String rpcEndpoint = intent.getStringExtra(IntentKeys.RPC_ENDPOINT);
            var nodeMode = intent.getStringExtra(IntentKeys.NODE_MODE);

            if (dataDir != null && password != null && rpcEndpoint != null) {
                swarmNode = new SwarmNode(dataDir, password, rpcEndpoint, NodeMode.LIGHT.name().equals(nodeMode));

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
        } else {
            // OS restarted the service after a kill (START_STICKY) but has no intent —
            // we can't restart the node without credentials, so stop cleanly.
            Log.w(TAG, "onStartCommand called with null intent (OS restart) — stopping service");
            stopSelf();
            return START_NOT_STICKY;
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
        if (swarmNode != null) {
            try {
                swarmNode.stopNode();
                Log.i(TAG, "SwarmNode stopped successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to stop SwarmNode", e);
            }
        }
        swarmNode = null;
    }

    public void getAllStamps(StampListener listener) {
        if (swarmNode != null) {
            swarmNode.getAllStamps(listener);
        }
    }

    public void buyStamp(String amount, String depth, String label, boolean immutable, StampListener listener) {
        Log.i(TAG, "buyStamp() called: amount=" + amount + ", depth=" + depth
                + ", label=" + label + ", immutable=" + immutable);
        if (swarmNode != null) {
            swarmNode.buyStamp(amount, depth, label, immutable, listener);
        } else {
            Log.w(TAG, "buyStamp() called but swarmNode is null");
        }
    }

    private volatile boolean uploading = false;

    public boolean upload(Uri fileUri, ContentResolver contentResolver, String filename, String contentType, Stamp stamp,
                       UploadListener uploadListener
    ) {
        Log.i(TAG, "upload() called: filename=" + filename + ", contentType=" + contentType
                + ", uri=" + (fileUri != null ? fileUri.toString() : "null")
                + ", stamp=" + (stamp != null ? stamp.toString() : "null"));
        if (swarmNode != null && !uploading) {
            uploading = true;
            swarmNode.upload(fileUri, contentResolver, filename, contentType, stamp, uploadListener);
            return true;
        } else if (uploading) {
            Log.w(TAG, "upload() called but an upload is already in progress");
        } else {
            Log.w(TAG, "upload() called but swarmNode is null");
        }
        return false;
    }

    public void onUploadFinished() {
        uploading = false;
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

    private volatile boolean downloading = false;

    public boolean download(String hash) {
        if (swarmNode != null && !downloading) {
            downloading = true;
            swarmNode.download(hash);
            return true;
        }
        return false;
    }

    public void onDownloadFinished() {
        downloading = false;
    }


    private void createNotificationChannel() {
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
