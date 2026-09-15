package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView messageText;
    private Button counterButton;

    private int lastCounterValue = 0;

    private final ActivityResultLauncher<Intent> counterLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    lastCounterValue = result.getData().getIntExtra("NUM", 0);
                    messageText.setText("Returned counter: " + lastCounterValue);

                    Toast.makeText(this, "Counter saved: " + lastCounterValue, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No result returned", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageText = findViewById(R.id.main_msg_txt);
        counterButton = findViewById(R.id.main_counter_btn);


        if (savedInstanceState != null) {
            lastCounterValue = savedInstanceState.getInt("LAST", 0);
        }
        messageText.setText("Intent Example (Last: " + lastCounterValue + ")");

        counterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, CounterActivity.class);

                intent.putExtra("START_NUM", lastCounterValue);

                counterLauncher.launch(intent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("LAST", lastCounterValue);
    }
}
