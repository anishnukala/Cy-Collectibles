package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import com.android.volley.DefaultRetryPolicy;

public class AiChatActivity extends AppCompatActivity {

    private TextView btnBack;
    private TextView txtTitle;
    private RecyclerView recyclerAiMessages;
    private EditText edtAiMessage;
    private ImageButton btnSendAiMessage;

    private final List<AiChatMessage> messageList = new ArrayList<>();
    private AiChatAdapter adapter;

    private long userId = -1;
    private String username = "";
    private String userType = "";
    private boolean isSending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        btnBack = findViewById(R.id.btn_ai_back);
        txtTitle = findViewById(R.id.txt_ai_chat_title);
        recyclerAiMessages = findViewById(R.id.recycler_ai_messages);
        edtAiMessage = findViewById(R.id.edt_ai_message_input);
        btnSendAiMessage = findViewById(R.id.btn_send_ai_message);

        loadUserSession();

        if (userId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtTitle.setText("CyBot");

        adapter = new AiChatAdapter(messageList);
        recyclerAiMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerAiMessages.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnSendAiMessage.setOnClickListener(v -> {
            String message = edtAiMessage.getText().toString().trim();

            if (TextUtils.isEmpty(message)) {
                return;
            }

            sendAiMessage(message);
        });

        loadChatHistory();
    }

    private void loadUserSession() {
        Intent intent = getIntent();

        userId = intent.getLongExtra("USER_ID", -1);
        username = intent.getStringExtra("USERNAME");
        userType = intent.getStringExtra("USERTYPE");

        if (username == null) username = "";
        if (userType == null) userType = "";

        if (userId == -1 || username.isEmpty() || userType.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);

            if (userId == -1) userId = prefs.getLong("USER_ID", -1);
            if (username.isEmpty()) username = prefs.getString("USERNAME", "");
            if (userType.isEmpty()) userType = prefs.getString("USERTYPE", "");
        }
    }

    private void loadChatHistory() {
        String url = ApiConfig.BASE_URL + "/ai/chat/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    messageList.clear();

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = response.optJSONObject(i);
                        if (obj == null) continue;

                        int aiMessageId = obj.optInt("aiMessageId", -1);
                        String role = obj.optString("role", "");
                        String content = obj.optString("content", "");
                        String createdAt = obj.optString("createdAt", "");

                        if (!content.trim().isEmpty()) {
                            messageList.add(new AiChatMessage(
                                    aiMessageId,
                                    role,
                                    content,
                                    createdAt
                            ));
                        }
                    }

                    adapter.notifyDataSetChanged();
                    scrollToBottom();
                },
                error -> {
                    Toast.makeText(this, "Failed to load AI chat history", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void sendAiMessage(String userMessage) {
        if (isSending) return;

        String url = ApiConfig.BASE_URL + "/ai/chat/" + userId;

        isSending = true;
        edtAiMessage.setText("");
        btnSendAiMessage.setEnabled(false);

        AiChatMessage localUserMessage = new AiChatMessage(
                -1,
                "user",
                userMessage,
                ""
        );

        messageList.add(localUserMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();

        JSONObject body = new JSONObject();

        try {
            body.put("message", userMessage);
        } catch (Exception e) {
            isSending = false;
            btnSendAiMessage.setEnabled(true);
            Toast.makeText(this, "Message error", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    isSending = false;
                    btnSendAiMessage.setEnabled(true);

                    String aiResponse = response.optString("response", "");

                    if (!aiResponse.trim().isEmpty()) {
                        AiChatMessage aiMessage = new AiChatMessage(
                                -1,
                                "model",
                                aiResponse,
                                ""
                        );

                        messageList.add(aiMessage);
                        adapter.notifyItemInserted(messageList.size() - 1);
                        scrollToBottom();
                    }
                },
                error -> {
                    isSending = false;
                    btnSendAiMessage.setEnabled(true);

                    Toast.makeText(
                            this,
                            "AI took too long. Please wait before sending again.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                90000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(request);
    }

    private void scrollToBottom() {
        if (!messageList.isEmpty()) {
            recyclerAiMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }
}