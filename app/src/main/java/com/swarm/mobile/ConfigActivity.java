package com.swarm.mobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;



public class ConfigActivity extends AppCompatActivity {
    public static final String PASSWORD_KEY = "password";
    public static final String RPC_ENDPOINT_KEY = "rpc_endpoint";

    private TextInputEditText passwordInput;
    private TextInputEditText rpcEndpointInput;
    private MaterialButton startButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        passwordInput = findViewById(R.id.passwordInput);
        rpcEndpointInput = findViewById(R.id.rpcEndpointInput);
        startButton = findViewById(R.id.startButton);

        var savedPassword = getSharedPreferences("app_prefs", MODE_PRIVATE).getString(PASSWORD_KEY, "");
        var savedRpcEndpoint = getSharedPreferences("app_prefs", MODE_PRIVATE).getString(RPC_ENDPOINT_KEY, "https://xdai.fairdatasociety.org");
        passwordInput.setText(savedPassword);
        rpcEndpointInput.setText(savedRpcEndpoint);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.node_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNode();
            }
        });
    }


    private void startNode() {
        String password = passwordInput.getText().toString().trim();
        String rpcEndpoint = rpcEndpointInput.getText().toString().trim();
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putString(PASSWORD_KEY, password).apply();
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putString(RPC_ENDPOINT_KEY, rpcEndpoint).apply();

        Intent intent = new Intent(ConfigActivity.this, MainActivity.class);

        intent.putExtra(IntentKeys.PASSWORD, password);
        intent.putExtra(IntentKeys.RPC_ENDPOINT, rpcEndpoint);
        startActivity(intent);
    }
}
