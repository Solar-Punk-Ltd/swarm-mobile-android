package com.swarm.mobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.swarm.lib.SwarmNode;
import com.swarm.lib.SwarmNodeListener;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements SwarmNodeListener {

    private SwarmNode swarmNode;
    private TextView walletAddressText;
    private TextView nodeStatusText;
    private TextView peerCountText;
    private MaterialButton startDownloadButton;
    private TextInputEditText hashInput;

    private Handler refreshHandler;

    private byte[] pendingDownloadData;
    private String pendingDownloadFilename;

    private ActivityResultLauncher<Intent> createDocumentLauncher;

    private String password;
    private String rpcEndpoint;
    private String natAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (intent != null) {
            password = intent.getStringExtra(IntentKeys.PASSWORD);
            rpcEndpoint = intent.getStringExtra(IntentKeys.RPC_ENDPOINT);
            natAddress = intent.getStringExtra(IntentKeys.NAT_ADDRESS);
        }

        walletAddressText = findViewById(R.id.walletAddressText);
        nodeStatusText = findViewById(R.id.statusText);
        startDownloadButton = findViewById(R.id.downloadByHashButton);
        hashInput = findViewById(R.id.hashInput);

        peerCountText = findViewById(R.id.peersListText);

        swarmNode = new SwarmNode(getApplicationContext().getFilesDir().getAbsolutePath(), password, rpcEndpoint, natAddress);
        swarmNode.addListener(this);

        new Thread(() -> swarmNode.start()).start();

        startDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });

        refreshHandler = new Handler(Looper.getMainLooper());
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                updatePeerCount();
                refreshHandler.postDelayed(this, 5000);
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
    private void updatePeerCount() {
        var count = this.swarmNode.getConnectedPeers();

        Logger.getGlobal().log(Level.INFO, "Connected peers: " + count);

        peerCountText.setText("" + count);
    }


    private void startDownload() {
        var hash = Objects.requireNonNull(hashInput.getText()).toString().trim();

        this.swarmNode.download(hash);
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
        if (swarmNode != null && swarmNode.isRunning()) {
            swarmNode.stop();
        }
    }
}
