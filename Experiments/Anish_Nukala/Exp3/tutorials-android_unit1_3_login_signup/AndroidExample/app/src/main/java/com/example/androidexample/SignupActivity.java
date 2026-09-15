package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private EditText confirmEditText;   // define confirm edittext variable
    private Button loginButton;         // define login button variable
    private Button signupButton;        // define signup button variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /* initialize UI elements */
        usernameEditText = findViewById(R.id.signup_username_edt);  // link to username edtext in the Signup activity XML
        passwordEditText = findViewById(R.id.signup_password_edt);  // link to password edtext in the Signup activity XML
        confirmEditText = findViewById(R.id.signup_confirm_edt);    // link to confirm edtext in the Signup activity XML
        loginButton = findViewById(R.id.signup_login_btn);    // link to login button in the Signup activity XML
        signupButton = findViewById(R.id.signup_signup_btn);  // link to signup button in the Signup activity XML

        /* click listener on login button pressed */
        loginButton.setOnClickListener(v -> {

            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
            String savedUser = prefs.getString("USERNAME", null);
            String savedPass = prefs.getString("PASSWORD", null);

            if (savedUser == null || savedPass == null) {
                Toast.makeText(this, "Sign up first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.equals(savedUser) && password.equals(savedPass)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Wrong username or password", Toast.LENGTH_SHORT).show();
            }
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(v -> {

            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirm  = confirmEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            // save signup details
            SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
            prefs.edit()
                    .putString("USERNAME", username)
                    .putString("PASSWORD", password)
                    .putBoolean("LOGGED_IN", false)
                    .apply();

            Toast.makeText(this, "Account created. Please login.", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

    }
}