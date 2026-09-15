package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEdt;

    private Button resetBtn;

    private static final String RESET_URL = "http://10.0.2.2:8080/api/forgot-password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password); // load layout

        // link views
        emailEdt = findViewById(R.id.forgot_email_edt);
        resetBtn = findViewById(R.id.reset_password_btn);
        Button backBtn = findViewById(R.id.back_to_login_btn);

        // send reset request
        resetBtn.setOnClickListener(v -> {
            String email = emailEdt.getText().toString().trim();

            // check email not empty
            if (email.isEmpty()) {
                emailEdt.setError("Email required");
                return;
            }

            // check valid email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEdt.setError("Enter valid email");
                return;
            }

            sendResetRequest(email);
        });

        backBtn.setOnClickListener(v -> finish());
    }

    private void sendResetRequest(String email) {
        new Thread(() -> {
            try {
                // open connection
                URL url = new URL(RESET_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // build JSON body
                JSONObject json = new JSONObject();
                json.put("email", email);

                byte[] payload = json.toString().getBytes(StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(payload.length);

                // send request body
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload);
                }

                // get response code
                int code = conn.getResponseCode();
                Log.d("FORGOT_PASSWORD", "Response Code: " + code);

                // update UI on main thread
                runOnUiThread(() -> {
                    if (code >= 200 && code < 300) {
                        Toast.makeText(this, "Verification mail has been sent", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Failed (" + code + ")", Toast.LENGTH_LONG).show();
                    }
                });

                conn.disconnect();

            } catch (Exception e) {
                Log.e("FORGOT_PASSWORD", "Error", e);

                // show error message
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start(); // run in background thread
    }
}