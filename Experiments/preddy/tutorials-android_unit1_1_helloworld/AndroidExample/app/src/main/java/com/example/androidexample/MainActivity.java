package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView messageText;
    private Button btnChangeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // link UI elements
        messageText = findViewById(R.id.main_msg_txt);
        btnChangeText = findViewById(R.id.btn_change_text);

        // default message
        messageText.setText("Hello USA");

        // Button: change message when clicked
        btnChangeText.setOnClickListener(v -> {
            messageText.setText("I understand TextView + Buttons 😄");
        });
    }
}
