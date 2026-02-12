package com.swarm.mobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.swarm.lib.NodeInfo;
import com.swarm.lib.SwarmNode;
import com.swarm.lib.SwarmNodeListener;

public class MainActivity extends AppCompatActivity implements SwarmNodeListener {

    private SwarmNode swarmNode;
    private Handler refreshHandler;

    private byte[] pendingDownloadData;
    private String pendingDownloadFilename;

    private ActivityResultLauncher<Intent> createDocumentLauncher;

    private String password;
    private String rpcEndpoint;
    private String nodeMode;

    private NodeFragment nodeFragment;
    private DownloadFragment downloadFragment;
    private UploadFragment uploadFragment;

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

        swarmNode = new SwarmNode(getApplicationContext().getFilesDir().getAbsolutePath(), password, rpcEndpoint, NodeMode.LIGHT.name().equals(nodeMode));
        swarmNode.addListener(this);

        nodeFragment = new NodeFragment();
        downloadFragment = new DownloadFragment();
        uploadFragment = new UploadFragment(swarmNode);

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


        new Thread(() -> swarmNode.start()).start();

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

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @SuppressLint("SetTextI18n")
    private long updatePeerCount() {
        var connectedPeersCount = this.swarmNode.getConnectedPeers();
        nodeFragment.updatePeerCount(connectedPeersCount);
        return connectedPeersCount;
    }

    private void startDownload(String hash) {
        this.swarmNode.download(hash);
    }

    @Override
    public void onNodeInfoChanged(NodeInfo nodeInfo) {
        nodeFragment.updateNodeInfo(nodeInfo);
        downloadFragment.updateNodeInfo(nodeInfo);
        uploadFragment.updateNodeInfo(nodeInfo);
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
