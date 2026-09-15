package com.example.androidexample;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatSocketManager {

    public interface ChatSocketListener {
        void onConnected();

        void onMessageReceived(ChatMessage message);

        void onError(String errorMessage);

        void onClosed();
    }

    private final OkHttpClient client = new OkHttpClient();
    private WebSocket webSocket;
    private ChatSocketListener listener;
    private final String currentUsername;

    private String baseUrl;
    private int channelId = -1;
    private long userId = -1;
    private boolean manualClose = false;

    public ChatSocketManager(String currentUsername) {
        this.currentUsername = currentUsername == null ? "" : currentUsername;
    }

    public void connect(String baseUrl, int channelId, long userId, ChatSocketListener listener) {
        this.baseUrl = baseUrl;
        this.channelId = channelId;
        this.userId = userId;
        this.listener = listener;
        this.manualClose = false;

        String wsBase = baseUrl
                .replace("https://", "wss://")
                .replace("http://", "ws://");

        String socketUrl = wsBase + "/chat/" + channelId + "/" + userId;

        Request request = new Request.Builder()
                .url(socketUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                if (ChatSocketManager.this.listener != null) {
                    ChatSocketManager.this.listener.onConnected();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject obj = new JSONObject(text);

                    int msgId = obj.optInt("msgId", -1);
                    String sender = obj.optString("sender", "");
                    String content = obj.optString("content", "");

                    String rawDate = obj.optString("dateSent", "");
                    String formattedDate = formatSocketDate(rawDate);

                    Integer parentMessageId = obj.isNull("parentMessageId")
                            ? null
                            : obj.optInt("parentMessageId");

                    boolean sentByMe = sender.equalsIgnoreCase(currentUsername);

                    ChatMessage message = new ChatMessage(
                            msgId,
                            sender,
                            content,
                            formattedDate,
                            parentMessageId,
                            sentByMe
                    );

                    if (ChatSocketManager.this.listener != null) {
                        ChatSocketManager.this.listener.onMessageReceived(message);
                    }

                } catch (Exception e) {
                    if (ChatSocketManager.this.listener != null) {
                        ChatSocketManager.this.listener.onError("Failed to parse socket message");
                    }
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                if (ChatSocketManager.this.listener != null) {
                    ChatSocketManager.this.listener.onError(
                            t.getMessage() == null ? "Socket failure" : t.getMessage()
                    );
                }

                if (!manualClose) {
                    reconnect();
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                if (ChatSocketManager.this.listener != null) {
                    ChatSocketManager.this.listener.onClosed();
                }

                if (!manualClose) {
                    reconnect();
                }
            }
        });
    }

    public void sendMessage(String content, Integer parentMessageId) {
        if (webSocket == null || content == null || content.trim().isEmpty()) return;

        JSONObject obj = new JSONObject();
        try {
            obj.put("content", content.trim());
            if (parentMessageId == null) {
                obj.put("parentMessageId", JSONObject.NULL);
            } else {
                obj.put("parentMessageId", parentMessageId);
            }
            webSocket.send(obj.toString());
        } catch (JSONException ignored) {
        }
    }

    public void sendMessage(String content) {
        sendMessage(content, null);
    }

    public void disconnect() {
        manualClose = true;
        if (webSocket != null) {
            webSocket.close(1000, "Closed by user");
            webSocket = null;
        }
    }

    public void reconnect() {
        if (baseUrl == null || channelId == -1 || userId == -1 || listener == null) return;

        if (webSocket != null) {
            try {
                webSocket.cancel();
            } catch (Exception ignored) {
            }
            webSocket = null;
        }

        connect(baseUrl, channelId, userId, listener);
    }

    private String formatSocketDate(String raw) {
        if (raw == null || raw.trim().isEmpty() || "null".equalsIgnoreCase(raw)) {
            return "";
        }

        String trimmed = raw.trim();

        try {
            long millis = Long.parseLong(trimmed);

            java.text.SimpleDateFormat output =
                    new java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault());

            return output.format(new java.util.Date(millis));

        } catch (Exception ignored) {
        }

        String[] patterns = new String[] {
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "EEE MMM dd HH:mm:ss zzz yyyy"
        };

        for (String pattern : patterns) {
            try {
                java.text.SimpleDateFormat input =
                        new java.text.SimpleDateFormat(pattern, java.util.Locale.US);

                java.util.Date date = input.parse(trimmed);

                if (date != null) {
                    java.text.SimpleDateFormat output =
                            new java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault());

                    return output.format(date);
                }
            } catch (Exception ignored) {
            }
        }

        return "";
    }
}