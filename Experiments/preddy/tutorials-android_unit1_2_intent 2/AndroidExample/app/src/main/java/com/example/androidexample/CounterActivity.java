package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CounterActivity extends AppCompatActivity {

    private TextView numberTxt;
    private Button increaseBtn, decreaseBtn, backBtn;

    private Button resetBtn;


    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        numberTxt = findViewById(R.id.number);
        increaseBtn = findViewById(R.id.counter_increase_btn);
        decreaseBtn = findViewById(R.id.counter_decrease_btn);
        backBtn = findViewById(R.id.counter_back_btn);

        // NEW: restore counter after rotation OR initialize from MainActivity
        if (savedInstanceState != null) {
            counter = savedInstanceState.getInt("COUNTER", 0);
        } else {
            counter = getIntent().getIntExtra("START_NUM", 0); // NEW
        }

        updateCounterText();

        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter++;
                updateCounterText();
            }
        });

        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NEW: simple validation so it doesn't go negative
                if (counter > 0) {
                    counter--;
                    updateCounterText();
                } else {
                    Toast.makeText(CounterActivity.this, "Counter can't go below 0", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnResultAndFinish();
            }
        });

        resetBtn = findViewById(R.id.counter_reset_btn);

        resetBtn.setOnClickListener(v -> {
            counter = 0;
            updateCounterText();
            Toast.makeText(this, "Reset to 0", Toast.LENGTH_SHORT).show();
        });

    }

    // NEW: helper method (shows clean design)
    private void updateCounterText() {
        numberTxt.setText(String.valueOf(counter));
    }

    private void returnResultAndFinish() {
        Intent intent = new Intent();
        intent.putExtra("NUM", counter);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("COUNTER", counter);
    }

    @Override
    public void onBackPressed() {
        returnResultAndFinish();
    }
}
