package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton      = findViewById(R.id.login_login_btn);
        signupButton     = findViewById(R.id.login_signup_btn);

        // UX: disable login until inputs are not empty
        loginButton.setEnabled(false);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String u = usernameEditText.getText().toString().trim();
                String p = passwordEditText.getText().toString();
                loginButton.setEnabled(!u.isEmpty() && !p.isEmpty());
            }
        };

        usernameEditText.addTextChangedListener(watcher);
        passwordEditText.addTextChangedListener(watcher);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty()) {
                usernameEditText.setError("Username required");
                return;
            }
            if (password.isEmpty()) {
                passwordEditText.setError("Password required");
                return;
            }

            SharedPreferences prefs = getSharedPreferences(SignupActivity.PREFS_NAME, MODE_PRIVATE);
            String savedUser = prefs.getString(SignupActivity.KEY_USER, null);
            String savedPass = prefs.getString(SignupActivity.KEY_PASS, null);

            if (savedUser == null || savedPass == null) {
                Toast.makeText(this, "No account found. Please sign up first.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.equals(savedUser) && password.equals(savedPass)) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid username/password", Toast.LENGTH_SHORT).show();
            }
        });

        signupButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }
}
