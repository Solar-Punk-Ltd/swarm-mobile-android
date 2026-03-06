package com.swarm.mobile;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.swarm.interfaces.SwarmNodeListener;
import com.swarm.lib.NodeInfo;
import com.swarm.lib.NodeStatus;
import com.swarm.mobile.fragments.DownloadFragment;
import com.swarm.mobile.fragments.NodeFragment;
import com.swarm.mobile.fragments.UploadFragment;

public class MainActivity extends AppCompatActivity implements SwarmNodeListener {

    private SwarmNodeService swarmNodeService;
    private boolean serviceBound = false;

    private Handler refreshHandler;

    private byte[] pendingDownloadData;

    private ActivityResultLauncher<Intent> createDocumentLauncher;

    private String password;
    private String rpcEndpoint;
    private String nodeMode;

    private NodeFragment nodeFragment;
    private DownloadFragment downloadFragment;
    private UploadFragment uploadFragment;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SwarmNodeService.SwarmNodeBinder binder = (SwarmNodeService.SwarmNodeBinder) service;
            swarmNodeService = binder.getService();
            serviceBound = true;
            swarmNodeService.addListener(MainActivity.this);
            uploadFragment.setSwarmNodeService(swarmNodeService);
            onNodeInfoChanged(new NodeInfo("", "", "", NodeStatus.Started));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            swarmNodeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        Intent intent = getIntent();
        if (intent != null) {
            password = intent.getStringExtra(IntentKeys.PASSWORD);
            rpcEndpoint = intent.getStringExtra(IntentKeys.RPC_ENDPOINT);
            nodeMode = intent.getStringExtra(IntentKeys.NODE_MODE);
        }

        nodeFragment = new NodeFragment(NodeMode.valueOf(nodeMode));
        downloadFragment = new DownloadFragment();
        uploadFragment = new UploadFragment();

        Intent serviceIntent = new Intent(this, SwarmNodeService.class);
        serviceIntent.putExtra(IntentKeys.DATA_DIR, getApplicationContext().getFilesDir().getAbsolutePath());
        serviceIntent.putExtra(IntentKeys.PASSWORD, password);
        serviceIntent.putExtra(IntentKeys.RPC_ENDPOINT, rpcEndpoint);
        serviceIntent.putExtra(IntentKeys.NODE_MODE, nodeMode);

        // startForegroundService is safe to call even if the service is already running —
        // onStartCommand returns START_STICKY and guards against double-init with a null check.
        startForegroundService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        downloadFragment.setDownloadListener(this::startDownload);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_node) {
                selectedFragment = nodeFragment;
            } else if (itemId == R.id.navigation_download) {
                selectedFragment = downloadFragment;
            } else if (itemId == R.id.navigation_upload) {
                selectedFragment = uploadFragment;
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        loadFragment(nodeFragment);
        bottomNavigation.setSelectedItemId(R.id.navigation_node);

        if (NodeMode.ULTRA_LIGHT.name().equals(nodeMode)) {
            bottomNavigation.getMenu().findItem(R.id.navigation_upload).setVisible(false);
        }

        refreshHandler = new Handler(Looper.getMainLooper());
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                var connectedPeersCount = updatePeerCount();
                var delayInMillis = connectedPeersCount > 100 ? 5000 : 1000;
                refreshHandler.postDelayed(this, delayInMillis);
            }
        };
        refreshHandler.post(refreshRunnable);

        createDocumentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                try {
                    var uri = result.getData().getData();
                    var outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null && pendingDownloadData != null) {
                        outputStream.write(pendingDownloadData);
                        outputStream.close();
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error saving downloaded file", e);
                } finally {
                    pendingDownloadData = null;
                }
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @SuppressLint("SetTextI18n")
    private long updatePeerCount() {
        if (swarmNodeService == null) {
            return 0;
        }

        var connectedPeersCount = this.swarmNodeService.getConnectedPeers();
        nodeFragment.updatePeerCount(connectedPeersCount);
        return connectedPeersCount;
    }


    private void startDownload(String hash) {
        if (serviceBound && swarmNodeService != null) {
            boolean started = swarmNodeService.download(hash);
            if (!started) {
                downloadFragment.setDownloading(false);
            }
        } else {
            Log.w("MainActivity", "startDownload() called but service is not bound");
            downloadFragment.setDownloading(false);
        }
    }

    @Override
    public void onNodeInfoChanged(NodeInfo nodeInfo) {
        nodeFragment.updateNodeInfo(nodeInfo);
        downloadFragment.updateNodeInfo(nodeInfo);
        uploadFragment.updateNodeInfo(nodeInfo);
    }

    @Override
    public void onDownloadSuccess(String filename, byte[] data, String downloadRateMBps) {
        if (swarmNodeService != null) {
            swarmNodeService.onDownloadFinished();
        }
        downloadFragment.setDownloading(false);
        downloadFragment.onDownloadSuccess(filename, downloadRateMBps);
        runOnUiThread(() -> {
            pendingDownloadData = data;
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE, filename);
            createDocumentLauncher.launch(intent);
        });
    }

    public void onHashNotFound() {
        if (swarmNodeService != null) {
            swarmNodeService.onDownloadFinished();
        }
        downloadFragment.setDownloading(false);
        runOnUiThread(() -> {
            var alertDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Hash Not Found")
                    .setMessage("The requested hash was not found in the Swarm network. Please check the hash and try again.")
                    .setPositiveButton("OK", null)
                    .create();
            alertDialog.show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound && swarmNodeService != null) {
            swarmNodeService.removeListener(this);
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }
}
