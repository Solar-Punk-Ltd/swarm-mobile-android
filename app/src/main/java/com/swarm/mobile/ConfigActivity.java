package com.swarm.mobile;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ConfigActivity extends AppCompatActivity {

    private Spinner nodeModeSpinner;
    private TextInputEditText rpcEndpointInput;
    private TextInputEditText natAddressInput;
    private TextInputEditText welcomeMessageInput;
    private MaterialButton startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Initialize views
        nodeModeSpinner = findViewById(R.id.nodeModeSpinner);
        rpcEndpointInput = findViewById(R.id.rpcEndpointInput);
        natAddressInput = findViewById(R.id.natAddressInput);
        welcomeMessageInput = findViewById(R.id.welcomeMessageInput);
        startButton = findViewById(R.id.startButton);

        // Setup node mode spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.node_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeModeSpinner.setAdapter(adapter);

        // Set default NAT address
        String deviceIp = getDeviceIpAddress();
        natAddressInput.setText(deviceIp + ":1633");

        // Start button click listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNode();
            }
        });
    }

    private String getDeviceIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return Formatter.formatIpAddress(ipAddress);
        }
        return "0.0.0.0";
    }

    private void startNode() {
        String nodeMode = nodeModeSpinner.getSelectedItem().toString();
        String rpcEndpoint = rpcEndpointInput.getText().toString().trim();
        String natAddress = natAddressInput.getText().toString().trim();
        String welcomeMessage = welcomeMessageInput.getText().toString().trim();

        // Navigate to PeersActivity with parameters
        Intent intent = new Intent(ConfigActivity.this, PeersActivity.class);
        intent.putExtra("nodeMode", nodeMode);
        intent.putExtra("rpcEndpoint", rpcEndpoint);
        intent.putExtra("natAddress", natAddress);
        intent.putExtra("welcomeMessage", welcomeMessage);
        startActivity(intent);
    }
}
