package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private Button loginButton;         // define login button variable
    private Button signupButton;        // define signup button variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);            // link to Login activity XML

        /* initialize UI elements */
        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);    // link to login button in the Login activity XML
        signupButton = findViewById(R.id.login_signup_btn);  // link to signup button in the Login activity XML

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

            if (!username.equals(savedUser) || !password.equals(savedPass)) {
                Toast.makeText(this, "Wrong username or password", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit().putBoolean("LOGGED_IN", true).apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
            finish();
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);  // go to SignupActivity
            }
        });
    }
}