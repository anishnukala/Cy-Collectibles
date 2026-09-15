package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.handshake.ServerHandshake;

public class ChatActivity1 extends AppCompatActivity implements WebSocketListener {

    private Button sendBtn, backMainBtn;
    private EditText msgEtx;
    private TextView msgTv;
    private String username;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat1);

        sendBtn = findViewById(R.id.sendBtn);
        backMainBtn = findViewById(R.id.backMainBtn);
        msgEtx = findViewById(R.id.msgEdt);
        msgTv = findViewById(R.id.tx1);

        username = getIntent().getStringExtra("username");

        msgTv.setText("Welcome to Chat 1");

        WebSocketManager1.getInstance().setWebSocketListener(this);

        sendBtn.setOnClickListener(v -> {
            String message = msgEtx.getText().toString().trim();

            if (message.isEmpty()) {
                Toast.makeText(ChatActivity1.this, "Message cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean sent = WebSocketManager1.getInstance().sendMessage(message);

            if (sent) {
                msgTv.append("\nYou: " + message);
                msgEtx.setText("");
            } else {
                Toast.makeText(ChatActivity1.this, "Not connected to server yet", Toast.LENGTH_SHORT).show();
            }
        });

        backMainBtn.setOnClickListener(view -> {
            msgTv.setText("");
            Intent intent = new Intent(ChatActivity1.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        isConnected = true;
        runOnUiThread(() -> {
            Toast.makeText(ChatActivity1.this, "Connected to server", Toast.LENGTH_SHORT).show();
            msgTv.append("\n[Status] Connected");
        });
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> msgTv.append("\nServer: " + message));
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        isConnected = false;
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() ->
                msgTv.append("\n[Status] Connection closed by " + closedBy + " | Reason: " + reason)
        );
    }

    @Override
    public void onWebSocketError(Exception ex) {
        runOnUiThread(() ->
                Toast.makeText(ChatActivity1.this, "WebSocket error: " + ex.getMessage(), Toast.LENGTH_LONG).show()
        );
        Log.e("ChatActivity1", "WebSocket error", ex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketManager1.getInstance().removeWebSocketListener();
    }
}