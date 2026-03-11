package com.swarm.mobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class ConfigActivity extends AppCompatActivity {

    private AutoCompleteTextView nodeModeSpinner;
    private TextInputEditText passwordInput;
    private TextInputEditText rpcEndpointInput;
    private TextInputLayout rpcEndpointLayout;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        nodeModeSpinner = findViewById(R.id.nodeModeSpinner);
        passwordInput = findViewById(R.id.passwordInput);
        rpcEndpointInput = findViewById(R.id.rpcEndpointInput);
        rpcEndpointLayout = findViewById(R.id.rpcEndpointLayout);
        MaterialButton startButton = findViewById(R.id.startButton);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, NodeMode.getDisplayNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeModeSpinner.setAdapter(adapter);

        var savedNodeModeName = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("node_mode", NodeMode.ULTRA_LIGHT.getDisplayName());
        nodeModeSpinner.setText(savedNodeModeName, false);

        var savedPassword = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("password", "");
        passwordInput.setText(savedPassword);


        nodeModeSpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateRpcEndpointVisibility(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        updateRpcEndpointVisibility(savedNodeModeName);

        startButton.setOnClickListener(v -> startNode());
    }

    private void updateRpcEndpointVisibility(String nodeModeName) {
        NodeMode nodeMode = NodeMode.fromDisplayName(nodeModeName);

        if (nodeMode == NodeMode.ULTRA_LIGHT) {
            rpcEndpointLayout.setEnabled(false);
            rpcEndpointInput.setEnabled(false);
            rpcEndpointInput.setText("");
            rpcEndpointLayout.setAlpha(0.5f);
        } else {

            rpcEndpointLayout.setEnabled(true);
            rpcEndpointInput.setEnabled(true);
            rpcEndpointLayout.setAlpha(1.0f);

            var savedRpcEndpoint = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .getString("rpc_endpoint",  getString(R.string.default_rpc_endpoint));
            rpcEndpointInput.setText(savedRpcEndpoint);
        }
    }


    private void startNode() {
        String selectedModeName = nodeModeSpinner.getText().toString();
        NodeMode nodeMode = NodeMode.fromDisplayName(selectedModeName);

        String password = Objects.requireNonNull(passwordInput.getText()).toString();

        String rpcEndpoint = (nodeMode == NodeMode.ULTRA_LIGHT) ? "" : Objects.requireNonNull(rpcEndpointInput.getText()).toString().trim();

        var prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putString("node_mode", nodeMode.getDisplayName())
                .putString("password", password);


        if (!rpcEndpoint.isEmpty()) {
            prefs.putString("rpc_endpoint", rpcEndpoint);
        }

        prefs.apply();

        Intent intent = new Intent(ConfigActivity.this, MainActivity.class);

        intent.putExtra(IntentKeys.NODE_MODE, nodeMode.name());
        intent.putExtra(IntentKeys.PASSWORD, password);
        intent.putExtra(IntentKeys.RPC_ENDPOINT, rpcEndpoint);
        startActivity(intent);
    }
}
