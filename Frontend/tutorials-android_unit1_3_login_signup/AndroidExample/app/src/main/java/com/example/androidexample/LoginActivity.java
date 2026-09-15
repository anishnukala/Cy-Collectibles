package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signupButton;
    private TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        NotificationSocketManager.getInstance().disconnect();

        if (!BuildConfig.DEBUG) {
            requestNotificationPermissionIfNeeded();
        }

        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);
        signupButton = findViewById(R.id.login_signup_btn);
        forgotPassword = findViewById(R.id.forgot_password_txt);

        loginButton.setOnClickListener(v -> doLogin());

        signupButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            overridePendingTransition(0, 0);
        });

        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            overridePendingTransition(0, 0);
        });

        getWindow().setStatusBarColor(android.graphics.Color.WHITE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }

    private void doLogin() {
        final String username = usernameEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.BASE_URL + "/users/login";

        JSONObject body = new JSONObject();
        try {
            body.put("username", username);
            body.put("password", password);
        } catch (JSONException e) {
            Toast.makeText(this, "App error building request", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    loginButton.setEnabled(true);

                    long userId = response.optLong("id", -1);
                    String userType = response.optString("userType", "").trim();
                    boolean banned = response.optBoolean("banned", false);

                    if (userId == -1) {
                        Toast.makeText(this, "Login failed: missing user id", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (banned) {
                        Toast.makeText(this, "Account is banned.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("LOGGED_IN", true)
                            .putLong("USER_ID", userId)
                            .putString("USERNAME", username)
                            .putString("USERTYPE", userType)
                            .putBoolean("BEEN_EDITED", false)
                            .apply();

                    Intent intent;

                    if ("admin".equalsIgnoreCase(userType)) {
                        intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                    } else if ("seller".equalsIgnoreCase(userType)) {
                        intent = new Intent(LoginActivity.this, SellerHomeActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, BuyerHomeActivity.class);
                    }

                    intent.putExtra("USERNAME", username);
                    intent.putExtra("USER_ID", userId);
                    intent.putExtra("USERTYPE", userType);

                    NotificationSocketManager.getInstance().connect(LoginActivity.this, userId);

                    startActivity(intent);
                    finish();
                },
                error -> {
                    loginButton.setEnabled(true);

                    int status = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;

                    String serverMsg = null;
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String json = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            JSONObject errObj = new JSONObject(json);

                            if (errObj.has("message")) {
                                serverMsg = errObj.optString("message", null);
                            } else {
                                Iterator<String> keys = errObj.keys();
                                if (keys.hasNext()) {
                                    String k = keys.next();
                                    String msg = errObj.optString(k, null);
                                    if (msg != null && !msg.isEmpty()) {
                                        serverMsg = k + ": " + msg;
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    if (status == 401) {
                        Toast.makeText(
                                this,
                                (serverMsg != null) ? serverMsg : "Incorrect username or password",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else if (status == 400) {
                        Toast.makeText(
                                this,
                                (serverMsg != null) ? serverMsg : "Invalid input",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else if (status == -1) {
                        String msg = (error.getMessage() != null) ? error.getMessage() : "Network error";
                        Toast.makeText(this, "Login failed: " + msg, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(
                                this,
                                "Login failed (" + status + ")" + (serverMsg != null ? ": " + serverMsg : ""),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(req);
    }
}