package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import com.example.androidexample.BuildConfig;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class EditUserActivity extends AppCompatActivity {

    private EditText usernameEdt, emailEdt, oldPasswordEdt, newPasswordEdt;
    private TextView usernameValueTv, emailValueTv;

    private LinearLayout usernameEditorLayout, emailEditorLayout, passwordEditorLayout;

    private Button saveUsernameBtn, saveEmailBtn, savePasswordBtn;
    private ImageView editUsernameBtn, editEmailBtn, editPasswordBtn;

    private long userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        TextView closeBtn = findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(v -> finish());
        // Connect UI fields to Java variables
        usernameEdt = findViewById(R.id.edit_username);
        emailEdt = findViewById(R.id.edit_email);
        oldPasswordEdt = findViewById(R.id.edit_old_password);
        newPasswordEdt = findViewById(R.id.edit_new_password);

        usernameValueTv = findViewById(R.id.tv_username_value);
        emailValueTv = findViewById(R.id.tv_email_value);

        usernameEditorLayout = findViewById(R.id.layout_username_editor);
        emailEditorLayout = findViewById(R.id.layout_email_editor);
        passwordEditorLayout = findViewById(R.id.layout_password_editor);

        saveUsernameBtn = findViewById(R.id.btn_save_username);
        saveEmailBtn = findViewById(R.id.btn_save_email);
        savePasswordBtn = findViewById(R.id.btn_save_password);

        editUsernameBtn = findViewById(R.id.btn_edit_username);
        editEmailBtn = findViewById(R.id.btn_edit_email);
        editPasswordBtn = findViewById(R.id.btn_edit_password);

        // Get saved login data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        userId = prefs.getLong("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "No user id saved. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        editUsernameBtn.setOnClickListener(v -> toggleEditor(usernameEditorLayout));
        editEmailBtn.setOnClickListener(v -> toggleEditor(emailEditorLayout));
        editPasswordBtn.setOnClickListener(v -> toggleEditor(passwordEditorLayout));

        saveUsernameBtn.setOnClickListener(v -> updateUsername());
        saveEmailBtn.setOnClickListener(v -> updateEmail());
        savePasswordBtn.setOnClickListener(v -> updatePassword());

        loadUserDetails();
    }

    // Shows selected editor layout and hides the others
    private void loadUserDetails() {
        String url = BuildConfig.BASE_URL + "/users/" + userId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    String username = response.optString("username", "");
                    String email = response.optString("emailId", "");

                    usernameValueTv.setText(username.isEmpty() ? "No username" : username);
                    emailValueTv.setText(email.isEmpty() ? "No email" : email);

                    usernameEdt.setText(username);
                    emailEdt.setText(email);

                    SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("USERNAME", username);
                    editor.putString("EMAIL", email);
                    editor.apply();
                },
                error -> {
                    int status = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;
                    Toast.makeText(this, "Failed to load user details (" + status + ")", Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(req);
    }
    // Sends new username to backend

    private void toggleEditor(LinearLayout targetLayout) {
        if (targetLayout == usernameEditorLayout) {
            usernameEditorLayout.setVisibility(
                    usernameEditorLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
            emailEditorLayout.setVisibility(View.GONE);
            passwordEditorLayout.setVisibility(View.GONE);

        } else if (targetLayout == emailEditorLayout) {
            emailEditorLayout.setVisibility(
                    emailEditorLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
            usernameEditorLayout.setVisibility(View.GONE);
            passwordEditorLayout.setVisibility(View.GONE);

        } else if (targetLayout == passwordEditorLayout) {
            passwordEditorLayout.setVisibility(
                    passwordEditorLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
            usernameEditorLayout.setVisibility(View.GONE);
            emailEditorLayout.setVisibility(View.GONE);
        }
    }
    // Sends new username to backend

    private void updateUsername() {
        String newUsername = usernameEdt.getText().toString().trim();

        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("username", newUsername);
        } catch (Exception e) {
            Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show();
            return;
        }

        sendPatchRequest(body, "username");
    }

    private void updateEmail() {
        String newEmail = emailEdt.getText().toString().trim();

        if (newEmail.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("emailId", newEmail);
        } catch (Exception e) {
            Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show();
            return;
        }

        sendPatchRequest(body, "email");
    }

    private void updatePassword() {
        String oldPassword = oldPasswordEdt.getText().toString().trim();
        String newPassword = newPasswordEdt.getText().toString().trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Fill old and new password", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("oldPassword", oldPassword);
            body.put("password", newPassword);
        } catch (Exception e) {
            Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show();
            return;
        }

        sendPatchRequest(body, "password");
    }
    // Sends new username to backend
    private void sendPatchRequest(JSONObject body, String updateType) {
        String url = ApiConfig.BASE_URL + "/users/" + userId;

        setAllButtonsEnabled(false);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                body,
                response -> {
                    setAllButtonsEnabled(true);

                    SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    if (response.has("username")) {
                        String savedUsername = response.optString("username", usernameEdt.getText().toString().trim());
                        usernameValueTv.setText(savedUsername);
                        usernameEdt.setText(savedUsername);
                        editor.putString("USERNAME", savedUsername);
                    }

                    if (response.has("emailId")) {
                        String savedEmail = response.optString("emailId", emailEdt.getText().toString().trim());
                        emailValueTv.setText(savedEmail);
                        emailEdt.setText(savedEmail);
                        editor.putString("EMAIL", savedEmail);
                    }

                    editor.apply();

                    if ("username".equals(updateType)) {
                        usernameEditorLayout.setVisibility(View.GONE);
                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();

                    } else if ("email".equals(updateType)) {
                        emailEditorLayout.setVisibility(View.GONE);
                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();

                    } else if ("password".equals(updateType)) {
                        passwordEditorLayout.setVisibility(View.GONE);
                        oldPasswordEdt.setText("");
                        newPasswordEdt.setText("");
                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    setAllButtonsEnabled(true);

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

                    if (status == 400) {
                        Toast.makeText(this, (serverMsg != null) ? serverMsg : "Invalid input", Toast.LENGTH_LONG).show();
                    } else if (status == 401) {
                        Toast.makeText(this, (serverMsg != null) ? serverMsg : "Old password incorrect", Toast.LENGTH_LONG).show();
                    } else if (status == 409) {
                        Toast.makeText(this, (serverMsg != null) ? serverMsg : "Username/email already exists", Toast.LENGTH_LONG).show();
                    } else if (status == 404) {
                        Toast.makeText(this, "User not found (404)", Toast.LENGTH_LONG).show();
                    } else if (status == -1) {
                        String msg = (error.getMessage() != null) ? error.getMessage() : "Network error";
                        Toast.makeText(this, "Update failed: " + msg, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Update failed (" + status + ")" + (serverMsg != null ? ": " + serverMsg : ""), Toast.LENGTH_LONG).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void setAllButtonsEnabled(boolean enabled) {
        saveUsernameBtn.setEnabled(enabled);
        saveEmailBtn.setEnabled(enabled);
        savePasswordBtn.setEnabled(enabled);

        editUsernameBtn.setEnabled(enabled);
        editEmailBtn.setEnabled(enabled);
        editPasswordBtn.setEnabled(enabled);
    }
}