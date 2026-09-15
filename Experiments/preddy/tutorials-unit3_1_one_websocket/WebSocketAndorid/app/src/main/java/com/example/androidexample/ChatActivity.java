package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.java_websocket.handshake.ServerHandshake;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements WebSocketListener {

    private Button sendBtn;
    private EditText msgEtx;
    private TextView msgTv;
    private ScrollView chatScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sendBtn = findViewById(R.id.sendBtn);
        msgEtx = findViewById(R.id.msgEdt);
        msgTv = findViewById(R.id.tx1);
        chatScroll = findViewById(R.id.chatScroll);

        WebSocketManager.getInstance().setWebSocketListener(this);

        sendBtn.setOnClickListener(v -> sendMessage());

        msgEtx.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        try {
            String message = msgEtx.getText().toString().trim();

            if (!message.isEmpty()) {
                WebSocketManager.getInstance().sendMessage(message);
                appendMessage("You", message);
                msgEtx.setText("");
            }
        } catch (Exception e) {
            Log.d("ExceptionSendMessage", String.valueOf(e.getMessage()));
            appendSystemMessage("Error sending message: " + e.getMessage());
        }
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void appendMessage(String sender, String message) {
        runOnUiThread(() -> {
            String current = msgTv.getText().toString();
            String newMessage = "[" + getCurrentTime() + "] " + sender + ": " + message;

            if (current.isEmpty()) {
                msgTv.setText(newMessage);
            } else {
                msgTv.append("\n\n" + newMessage);
            }

            scrollToBottom();
        });
    }

    private void appendSystemMessage(String message) {
        runOnUiThread(() -> {
            String current = msgTv.getText().toString();
            String newMessage = "[" + getCurrentTime() + "] " + message;

            if (current.isEmpty()) {
                msgTv.setText(newMessage);
            } else {
                msgTv.append("\n\n" + newMessage);
            }

            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        chatScroll.post(() -> chatScroll.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @Override
    public void onWebSocketMessage(String message) {
        appendMessage("Server", message);
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        appendSystemMessage("---\nConnection closed by " + closedBy + "\nReason: " + reason);
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        appendSystemMessage("Connected to server");
    }

    @Override
    public void onWebSocketError(Exception ex) {
        appendSystemMessage("Error: " + ex.getMessage());
    }
}