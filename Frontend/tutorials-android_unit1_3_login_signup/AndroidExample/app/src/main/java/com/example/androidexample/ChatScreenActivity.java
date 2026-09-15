package com.example.androidexample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatScreenActivity extends AppCompatActivity {

    private static final long RECONNECT_INTERVAL_MS = 3000;

    private TextView chatTitleText;
    private TextView backButton;
    private ImageButton sendButton;
    private EditText messageInput;
    private RecyclerView messagesRecycler;

    private View replyPreviewContainer;
    private TextView txtReplySender;
    private TextView txtReplyText;
    private TextView btnCancelReply;

    private final List<ChatMessage> messageList = new ArrayList<>();
    private final Set<Integer> seenMessageIds = new HashSet<>();

    private ChatMessageAdapter adapter;
    private ChatSocketManager socketManager;

    private int channelId = -1;
    private long userId = -1;
    private String username = "";
    private String userType = "";
    private String chatName = "";

    private Integer replyingToMessageId = null;
    private boolean socketConnected = false;

    private final Handler reconnectHandler = new Handler(Looper.getMainLooper());
    private boolean reconnectLoopStarted = false;

    private final Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (isFinishing() || isDestroyed()) return;

            if (socketManager != null) {
                socketManager.disconnect();
                socketConnected = false;
                connectSocket();
            }

            reconnectHandler.postDelayed(this, RECONNECT_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        channelId = getIntent().getIntExtra("CHANNEL_ID", -1);
        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        userType = getIntent().getStringExtra("USERTYPE");
        chatName = getIntent().getStringExtra("CHAT_NAME");

        if (username == null) username = "";
        if (userType == null) userType = "";
        if (chatName == null || chatName.trim().isEmpty()) chatName = "Chat";

        chatTitleText = findViewById(R.id.txt_chat_screen_title);
        backButton = findViewById(R.id.btn_chat_back);
        sendButton = findViewById(R.id.btn_send_message);
        messageInput = findViewById(R.id.edt_message_input);
        messagesRecycler = findViewById(R.id.recycler_messages);

        replyPreviewContainer = findViewById(R.id.reply_preview_container);
        txtReplySender = findViewById(R.id.txt_reply_sender);
        txtReplyText = findViewById(R.id.txt_reply_text);
        btnCancelReply = findViewById(R.id.btn_cancel_reply);

        chatTitleText.setText(chatName);

        adapter = new ChatMessageAdapter(messageList, this::showReplyPreview);
        messagesRecycler.setLayoutManager(new LinearLayoutManager(this));
        messagesRecycler.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());
        btnCancelReply.setOnClickListener(v -> clearReplyPreview());

        sendButton.setOnClickListener(v -> {
            String content = messageInput.getText().toString().trim();
            if (TextUtils.isEmpty(content)) return;

            if (socketManager != null) {
                socketManager.sendMessage(content, replyingToMessageId);
                messageInput.setText("");
                clearReplyPreview();
            } else {
                Toast.makeText(this, "Chat reconnecting...", Toast.LENGTH_SHORT).show();
            }
        });

        socketManager = new ChatSocketManager(username);
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectSocket();
        startReconnectLoop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopReconnectLoop();
        if (socketManager != null) {
            socketManager.disconnect();
        }
        socketConnected = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopReconnectLoop();
        if (socketManager != null) {
            socketManager.disconnect();
        }
    }

    private void startReconnectLoop() {
        if (reconnectLoopStarted) return;
        reconnectLoopStarted = true;
        reconnectHandler.postDelayed(reconnectRunnable, RECONNECT_INTERVAL_MS);
    }

    private void stopReconnectLoop() {
        reconnectLoopStarted = false;
        reconnectHandler.removeCallbacks(reconnectRunnable);
    }

    private void showReplyPreview(ChatMessage message) {
        replyingToMessageId = message.getMsgId();
        replyPreviewContainer.setVisibility(View.VISIBLE);
        txtReplySender.setText(message.getSender());
        txtReplySender.setTextColor(android.graphics.Color.parseColor("#B30000"));
        txtReplyText.setText(message.getContent());
    }

    private void clearReplyPreview() {
        replyingToMessageId = null;
        replyPreviewContainer.setVisibility(View.GONE);
        txtReplySender.setText("");
        txtReplyText.setText("");
    }

    private void connectSocket() {
        if (channelId == -1 || userId == -1) {
            Toast.makeText(this, "Missing chat info", Toast.LENGTH_SHORT).show();
            return;
        }

        socketManager.connect(BuildConfig.BASE_URL, channelId, userId, new ChatSocketManager.ChatSocketListener() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> socketConnected = true);
            }

            @Override
            public void onMessageReceived(ChatMessage message) {
                runOnUiThread(() -> addMessageIfNew(message));
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> socketConnected = false);
            }

            @Override
            public void onClosed() {
                runOnUiThread(() -> socketConnected = false);
            }
        });
    }

    private void addMessageIfNew(ChatMessage message) {
        int msgId = message.getMsgId();

        if (msgId != -1 && seenMessageIds.contains(msgId)) {
            return;
        }

        if (msgId != -1) {
            seenMessageIds.add(msgId);
        }

        messageList.add(message);
        adapter.notifyItemInserted(messageList.size() - 1);
        messagesRecycler.smoothScrollToPosition(messageList.size() - 1);
    }
}