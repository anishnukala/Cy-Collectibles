package com.example.androidexample;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TestFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = new FrameLayout(this);
        container.setId(R.id.test_container);
        setContentView(container);

        Bundle args = getIntent().getBundleExtra("ARGS");

        ChatListFragment fragment = new ChatListFragment();
        if (args != null) {
            fragment.setArguments(args);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.test_container, fragment)
                .commitNow();
    }
}