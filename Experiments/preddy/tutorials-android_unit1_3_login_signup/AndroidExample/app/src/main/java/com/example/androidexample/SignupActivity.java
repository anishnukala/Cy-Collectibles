package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmEditText;
    private Button loginButton;
    private Button signupButton;

    public static final String PREFS_NAME = "APP_PREFS";
    public static final String KEY_USER = "SAVED_USERNAME";
    public static final String KEY_PASS = "SAVED_PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.signup_username_edt);
        passwordEditText = findViewById(R.id.signup_password_edt);
        confirmEditText  = findViewById(R.id.signup_confirm_edt);
        loginButton      = findViewById(R.id.signup_login_btn);
        signupButton     = findViewById(R.id.signup_signup_btn);

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        signupButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirm  = confirmEditText.getText().toString();

            // Validation
            if (username.isEmpty()) {
                usernameEditText.setError("Username required");
                usernameEditText.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                passwordEditText.setError("Password required");
                passwordEditText.requestFocus();
                return;
            }
            if (password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                passwordEditText.requestFocus();
                return;
            }
            if (!password.equals(confirm)) {
                confirmEditText.setError("Passwords do not match");
                confirmEditText.requestFocus();
                return;
            }

            // Save locally (simple demo)
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .putString(KEY_USER, username)
                    .putString(KEY_PASS, password)
                    .apply();

            Toast.makeText(this, "Signup successful! Please login.", Toast.LENGTH_SHORT).show();

            // Go to login
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }
}
