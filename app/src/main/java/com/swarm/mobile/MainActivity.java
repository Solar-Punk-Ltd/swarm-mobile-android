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
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.swarm.lib.NodeInfo;
import com.swarm.lib.NodeStatus;
import com.swarm.lib.SwarmNodeListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SwarmNodeListener {

    private SwarmNodeService swarmNodeService;
    private boolean serviceBound = false;
    private TextView walletAddressText;
    private TextView nodeStatusText;
    private TextView peerCountText;
    private MaterialButton startDownloadButton;
    private MaterialButton stopNodeButton;
    private TextInputEditText hashInput;

    private Handler refreshHandler;

    private byte[] pendingDownloadData;
    private String pendingDownloadFilename;

    private ActivityResultLauncher<Intent> createDocumentLauncher;

    private String password;
    private String rpcEndpoint;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SwarmNodeService.SwarmNodeBinder binder = (SwarmNodeService.SwarmNodeBinder) service;
            swarmNodeService = binder.getService();
            serviceBound = true;
            swarmNodeService.addListener(MainActivity.this);
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
        }

        walletAddressText = findViewById(R.id.walletAddressText);
        nodeStatusText = findViewById(R.id.statusText);
        startDownloadButton = findViewById(R.id.downloadByHashButton);
        stopNodeButton = findViewById(R.id.stopNodeButton);
        hashInput = findViewById(R.id.hashInput);

        peerCountText = findViewById(R.id.peersListText);

        // Prepare service intent
        Intent serviceIntent = new Intent(this, SwarmNodeService.class);
        serviceIntent.putExtra(IntentKeys.DATA_DIR, getApplicationContext().getFilesDir().getAbsolutePath());
        serviceIntent.putExtra(IntentKeys.PASSWORD, password);
        serviceIntent.putExtra(IntentKeys.RPC_ENDPOINT, rpcEndpoint);

        if (!isServiceRunning(SwarmNodeService.class)) {
            // Use startForegroundService on Android 8+ (API 26+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }

        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        startDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });

        stopNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopNode();
                navigateToConfig();
            }
        });

        refreshHandler = new Handler(Looper.getMainLooper());
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                var connectedPeers = updatePeerCount();
                var delayMillis = connectedPeers < 100 ? 1000 : 5000;
                refreshHandler.postDelayed(this, delayMillis);
            }
        };
        refreshHandler.post(refreshRunnable);

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        try {
                            var uri = result.getData().getData();
                            var outputStream = getContentResolver().openOutputStream(uri);
                            if (outputStream != null && pendingDownloadData != null) {
                                outputStream.write(pendingDownloadData);
                                outputStream.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            pendingDownloadData = null;
                            pendingDownloadFilename = null;
                        }
                    }
                }
        );
    }

    @SuppressLint("SetTextI18n")
    private long updatePeerCount() {
        if (serviceBound && swarmNodeService != null) {
            var connectedPeers = swarmNodeService.getConnectedPeers();

            peerCountText.setText("" + connectedPeers);

            return connectedPeers;
        }

        return 0;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        android.app.ActivityManager manager = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }


    private void startDownload() {
        var hash = Objects.requireNonNull(hashInput.getText()).toString().trim();

        if (serviceBound && swarmNodeService != null) {
            swarmNodeService.download(hash);
        }
    }

    private void stopNode() {
        if (serviceBound && swarmNodeService != null) {
            swarmNodeService.removeListener(this);
            unbindService(serviceConnection);
            serviceBound = false;
        }

        Intent serviceIntent = new Intent(this, SwarmNodeService.class);
        stopService(serviceIntent);

    }

    private void navigateToConfig() {
        Intent intent = new Intent(this, ConfigActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onNodeInfoChanged(NodeInfo nodeInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                nodeStatusText.setText(nodeInfo.status().name());

                walletAddressText.setText(nodeInfo.walletAddress());

                if (NodeStatus.Running == nodeInfo.status()) {
                    nodeStatusText.setTextColor(getResources().getColor(R.color.status_running));
                    startDownloadButton.setEnabled(true);
                    hashInput.setEnabled(true);
                } else {
                    nodeStatusText.setTextColor(getResources().getColor(R.color.status_stopped));
                }
            }
        });
    }

    @Override
    public void onDownloadFinished(String filename, byte[] data) {
        runOnUiThread(() -> {
            pendingDownloadData = data;
            pendingDownloadFilename = filename;
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE, filename);
            createDocumentLauncher.launch(intent);
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
