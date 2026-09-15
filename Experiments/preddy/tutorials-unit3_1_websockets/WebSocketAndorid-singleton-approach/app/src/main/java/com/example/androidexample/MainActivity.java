package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button connectBtn, connectBtn2, backBtn, backBtn2;
    private EditText serverEtx, usernameEtx, serverEtx2, usernameEtx2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBtn = findViewById(R.id.connectBtn);
        connectBtn2 = findViewById(R.id.connectBtn2);
        backBtn = findViewById(R.id.backBtn);
        backBtn2 = findViewById(R.id.backBtn2);
        serverEtx = findViewById(R.id.serverEdt);
        usernameEtx = findViewById(R.id.unameEdt);
        serverEtx2 = findViewById(R.id.serverEdt2);
        usernameEtx2 = findViewById(R.id.unameEdt2);

        connectBtn.setOnClickListener(view -> {
            String server = serverEtx.getText().toString().trim();
            String username = usernameEtx.getText().toString().trim();

            if (server.isEmpty()) {
                serverEtx.setError("Enter server URL");
                return;
            }

            if (username.isEmpty()) {
                usernameEtx.setError("Enter username");
                return;
            }

            String serverUrl = server;

            WebSocketManager1.getInstance().connectWebSocket(serverUrl);
            Toast.makeText(MainActivity.this, "Opening Chat 1...", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, ChatActivity1.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        connectBtn2.setOnClickListener(view -> {
            String server = serverEtx2.getText().toString().trim();
            String username = usernameEtx2.getText().toString().trim();

            if (server.isEmpty()) {
                serverEtx2.setError("Enter server URL");
                return;
            }

            if (username.isEmpty()) {
                usernameEtx2.setError("Enter username");
                return;
            }

            String serverUrl = server;

            WebSocketManager2.getInstance().connectWebSocket(serverUrl);
            Toast.makeText(MainActivity.this, "Opening Chat 2...", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, ChatActivity2.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        backBtn.setOnClickListener(view -> finish());
        backBtn2.setOnClickListener(view -> finish());
    }
}