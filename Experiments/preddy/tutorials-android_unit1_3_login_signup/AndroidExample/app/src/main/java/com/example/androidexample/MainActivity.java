package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView messageText;
    private TextView usernameText;
    private Button loginButton;
    private Button signupButton;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageText  = findViewById(R.id.main_msg_txt);
        usernameText = findViewById(R.id.main_username_txt);
        loginButton  = findViewById(R.id.main_login_btn);
        signupButton = findViewById(R.id.main_signup_btn);

        // NEW: logout button (we will add it in XML)
        logoutButton = findViewById(R.id.main_logout_btn);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            messageText.setText("Home Page");
            usernameText.setVisibility(View.INVISIBLE);
            logoutButton.setVisibility(View.INVISIBLE);
        } else {
            messageText.setText("Welcome");
            usernameText.setText(extras.getString("USERNAME"));
            usernameText.setVisibility(View.VISIBLE);

            loginButton.setVisibility(View.INVISIBLE);
            signupButton.setVisibility(View.INVISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
        }

        loginButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class)));

        signupButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SignupActivity.class)));

        logoutButton.setOnClickListener(v -> {
            // Optional: clear saved login (or keep account but just logout)
            SharedPreferences prefs = getSharedPreferences(SignupActivity.PREFS_NAME, MODE_PRIVATE);
            prefs.edit().remove(SignupActivity.KEY_PASS).apply(); // keep username, remove pass for demo

            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
