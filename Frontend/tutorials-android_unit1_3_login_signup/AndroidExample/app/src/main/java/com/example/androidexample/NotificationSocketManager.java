package com.example.androidexample;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import android.widget.Toast;
public class NotificationSocketManager {

    private static final String CHANNEL_ID = "cycollectibles_notifications";
    private static final String CHANNEL_NAME = "CyCollectibles Notifications";

    private static NotificationSocketManager instance;
    private WebSocketClient webSocketClient;
    private boolean isConnected = false;
    private long currentUserId = -1;

    private NotificationSocketManager() {
    }

    public static synchronized NotificationSocketManager getInstance() {
        if (instance == null) {
            instance = new NotificationSocketManager();
        }
        return instance;
    }

    public void connect(Context context, long userId) {
        if (userId <= 0) return;

        if (isConnected && currentUserId == userId && webSocketClient != null) {
            return;
        }

        disconnect();
        currentUserId = userId;

        try {
            String baseUrl = BuildConfig.BASE_URL;
            String wsBase = baseUrl.replaceFirst("^http", "ws");
            URI serverUri = new URI(wsBase + "/notifications/" + userId);

            Context appContext = context.getApplicationContext();
            createNotificationChannel(appContext);

            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    isConnected = true;
                    System.out.println("Notification socket connected for user " + currentUserId);
                }

                @Override
                public void onMessage(String message) {
                    try {
                        JSONObject obj = new JSONObject(message);

                        String type = obj.optString("type", "NOTICE");
                        String text = obj.optString("message", "New notification");
                        int notificationId = obj.optInt("notificationId", (int) System.currentTimeMillis());

                        if ("BAN".equalsIgnoreCase(type)) {
                            forceLogoutToLogin(appContext, text);
                            return;
                        }

                        showSystemNotification(appContext, type, text, notificationId);
                        System.out.println("Notification received: " + message);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                private void forceLogoutToLogin(Context context, String message) {
                    String finalMessage = (message == null || message.trim().isEmpty())
                            ? "Your account has been banned."
                            : message;

                    showSystemNotification(context, "BAN", finalMessage, (int) System.currentTimeMillis());

                    disconnect();

                    SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected = false;
                    System.out.println("Notification socket closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    isConnected = false;
                    ex.printStackTrace();
                    System.out.println("Notification socket error: " + ex.getMessage());
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (webSocketClient != null) {
                webSocketClient.close();
                webSocketClient = null;
            }
        } catch (Exception ignored) {
        }

        isConnected = false;
        currentUserId = -1;
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for messages, sales, reports, and updates");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    void showSystemNotification(Context context, String type, String text, int notificationId) {
        Intent intent = getNotificationIntent(context, type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("CyCollectibles")
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        manager.notify(notificationId, builder.build());
    }

    Intent getNotificationIntent(Context context, String type) {
        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        String userType = prefs.getString("USERTYPE", "");

        if ("MESSAGE".equalsIgnoreCase(type)) {
            return new Intent(context, ChatHomeActivity.class);
        }

        if ("admin".equalsIgnoreCase(userType)) {
            return new Intent(context, AdminHomeActivity.class);
        } else if ("seller".equalsIgnoreCase(userType)) {
            return new Intent(context, SellerHomeActivity.class);
        } else {
            return new Intent(context, BuyerHomeActivity.class);
        }
    }
}