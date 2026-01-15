package com.swarm.mobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class ConfigActivity extends AppCompatActivity {

    private TextInputEditText passwordInput;
    private TextInputEditText rpcEndpointInput;
    private TextInputEditText natAddressInput;
    private MaterialButton startButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        passwordInput = findViewById(R.id.passwordInput);
        rpcEndpointInput = findViewById(R.id.rpcEndpointInput);
        natAddressInput = findViewById(R.id.natAddressInput);
        startButton = findViewById(R.id.startButton);

        var savedPassword = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("password", "");
        passwordInput.setText(savedPassword);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.node_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        String deviceIp = getDeviceIpAddress();
        natAddressInput.setText(deviceIp + ":1633");

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNode();
            }
        });
    }

    private String getDeviceIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "0.0.0.0";
    }

    private void startNode() {
        String password = passwordInput.getText().toString();
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putString("password", password).apply();
        String rpcEndpoint = rpcEndpointInput.getText().toString().trim();
        String natAddress = natAddressInput.getText().toString().trim();

        Intent intent = new Intent(ConfigActivity.this, MainActivity.class);

        intent.putExtra(IntentKeys.PASSWORD, password);
        intent.putExtra(IntentKeys.RPC_ENDPOINT, rpcEndpoint);
        intent.putExtra(IntentKeys.NAT_ADDRESS, natAddress);
        startActivity(intent);
    }
}
