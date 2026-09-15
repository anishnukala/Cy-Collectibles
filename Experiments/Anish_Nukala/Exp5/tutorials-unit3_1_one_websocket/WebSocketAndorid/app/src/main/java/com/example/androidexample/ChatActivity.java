package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.handshake.ServerHandshake;

import java.text.SimpleDateFormat; // for timestamp
import java.util.Date; // for timestamp
import java.util.Locale; // for formatting time

/**
 * ChatActivity handles the chat interface where users can send and receive messages
 * using a WebSocket connection.
 */
public class ChatActivity extends AppCompatActivity implements WebSocketListener {

    private Button sendBtn;
    private EditText msgEtx;
    private TextView msgTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sendBtn = findViewById(R.id.sendBtn);
        msgEtx = findViewById(R.id.msgEdt);
        msgTv = findViewById(R.id.tx1);

        WebSocketManager.getInstance().setWebSocketListener(this);

        appendMessage("System", "Chat started"); // show initial status

        sendBtn.setOnClickListener(v -> {
            try {
                String message = msgEtx.getText().toString().trim(); // remove extra spaces

                if (message.isEmpty()) { // prevent empty messages
                    appendMessage("System", "Empty message not sent");
                    return;
                }

                WebSocketManager.getInstance().sendMessage(message);
                appendMessage("You", message); // show sent message immediately
                msgEtx.setText("");
            } catch (Exception e) {
                Log.d("ExceptionSendMessage", String.valueOf(e.getMessage())); // safer logging
                appendMessage("Error", "Send failed"); // show error in UI
            }
        });
    }

    private String getTime() { // generate current time for each message
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    private void appendMessage(String sender, String message) { // format all messages consistently
        String s = msgTv.getText().toString();
        String line = "[" + getTime() + "] " + sender + ": " + message;
        msgTv.setText(s.isEmpty() ? line : s + "\n" + line);
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            appendMessage("Server", message); // label incoming messages
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            appendMessage("System", "Connection closed by " + closedBy + " | Reason: " + reason); // clearer status message
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(() -> appendMessage("System", "Connected")); // show connection success
    }

    @Override
    public void onWebSocketError(Exception ex) {
        runOnUiThread(() -> appendMessage("Error", "WebSocket error")); // show error state
    }
}