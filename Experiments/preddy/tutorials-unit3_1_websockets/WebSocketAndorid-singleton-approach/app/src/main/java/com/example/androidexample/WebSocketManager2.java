package com.example.androidexample;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketManager2 {

    private static WebSocketManager2 instance;
    private MyWebSocketClient webSocketClient;
    private WebSocketListener webSocketListener;

    private WebSocketManager2() {}

    public static synchronized WebSocketManager2 getInstance() {
        if (instance == null) {
            instance = new WebSocketManager2();
        }
        return instance;
    }

    public void setWebSocketListener(WebSocketListener listener) {
        this.webSocketListener = listener;
    }

    public void removeWebSocketListener() {
        this.webSocketListener = null;
    }

    public void connectWebSocket(String serverUrl) {
        try {
            Log.d("WebSocket2", "Trying to connect to: " + serverUrl);
            URI serverUri = URI.create(serverUrl);
            webSocketClient = new MyWebSocketClient(serverUri);
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e("WebSocket2", "Connection failed", e);
            if (webSocketListener != null) {
                webSocketListener.onWebSocketError(e);
            }
        }
    }

    public boolean sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
            Log.d("WebSocket2", "Sent: " + message);
            return true;
        } else {
            Log.d("WebSocket2", "Message not sent. Socket is not open.");
            return false;
        }
    }

    public void disconnectWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    private class MyWebSocketClient extends WebSocketClient {

        private MyWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("WebSocket2", "Connected successfully");
            if (webSocketListener != null) {
                webSocketListener.onWebSocketOpen(handshakedata);
            }
        }

        @Override
        public void onMessage(String message) {
            Log.d("WebSocket2", "Received: " + message);
            if (webSocketListener != null) {
                webSocketListener.onWebSocketMessage(message);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d("WebSocket2", "Closed. code=" + code + " reason=" + reason);
            if (webSocketListener != null) {
                webSocketListener.onWebSocketClose(code, reason, remote);
            }
        }

        @Override
        public void onError(Exception ex) {
            Log.e("WebSocket2", "Error: " + ex.getMessage(), ex);
            if (webSocketListener != null) {
                webSocketListener.onWebSocketError(ex);
            }
        }
    }
}