package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;

public class SellerHomeActivity extends AppCompatActivity {

    private static final String BASE_URL = BuildConfig.BASE_URL;

    private ImageButton profileIcon;
    private Button createListingBtn;
    private BottomNavigationView bottomNav;

    private TextView statChatsValue;
    private TextView statActiveListingsValue;
    private TextView statSoldValue;
    private TextView statTransactionsValue;

    private long userId = -1;
    private String username = "";
    private String userType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_home);

        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);

        userId = prefs.getLong("USER_ID", getIntent().getLongExtra("USER_ID", -1));
        username = prefs.getString("USERNAME", getIntent().getStringExtra("USERNAME"));
        userType = prefs.getString("USERTYPE", getIntent().getStringExtra("USERTYPE"));

        if (username == null) username = "";
        if (userType == null) userType = "";

        if (userId == -1) {
            forceLogoutToLogin("Session expired. Please login again.");
            return;
        }

        NotificationSocketManager.getInstance().connect(this, userId);

        profileIcon = findViewById(R.id.btn_profile);
        createListingBtn = findViewById(R.id.btn_create_listing);
        bottomNav = findViewById(R.id.bottom_nav);

        statChatsValue = findViewById(R.id.stat_chats_value);
        statActiveListingsValue = findViewById(R.id.stat_active_listings_value);
        statSoldValue = findViewById(R.id.stat_sold_value);
        statTransactionsValue = findViewById(R.id.stat_transactions_value);

        setupBottomNav();
        loadSellerStats();

        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(SellerHomeActivity.this, UserProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("USERTYPE", userType);
            startActivity(intent);
        });

        createListingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SellerHomeActivity.this, CreateListingActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("USERTYPE", userType);
            startActivity(intent);
        });

        getWindow().setStatusBarColor(android.graphics.Color.WHITE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        userId = prefs.getLong("USER_ID", -1);

        if (userId == -1) {
            forceLogoutToLogin("Session expired. Please login again.");
            return;
        }

        userId = prefs.getLong("USER_ID", userId);
        NotificationSocketManager.getInstance().connect(this, userId);
        loadSellerStats();
    }

    private void forceLogoutToLogin(String message) {
        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(SellerHomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean handleAuthError(int statusCode) {
        if (statusCode == 403) {
            forceLogoutToLogin("Your account has been banned or is no longer active.");
            return true;
        }
        return false;
    }

    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_seller_dashboard);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_seller_dashboard) {
                return true;
            } else if (id == R.id.nav_seller_active_listings) {
                Intent intent = new Intent(SellerHomeActivity.this, SellerActiveListingsActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_seller_transactions) {
                Intent intent = new Intent(SellerHomeActivity.this, SellerTransactionsActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_seller_chat) {
                Intent intent = new Intent(SellerHomeActivity.this, ChatHomeActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void loadSellerStats() {
        statChatsValue.setText("0");
        loadActiveListingsCount();
        loadSellerTransactions();
    }

    private void loadActiveListingsCount() {
        if (userId == -1) {
            statActiveListingsValue.setText("0");
            return;
        }

        String url = BASE_URL + "/postings/seller/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> statActiveListingsValue.setText(String.valueOf(response.length())),
                error -> {
                    int status = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;

                    if (handleAuthError(status)) return;

                    statActiveListingsValue.setText("0");
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadSellerTransactions() {
        if (userId == -1) {
            statSoldValue.setText("0");
            statTransactionsValue.setText("0");
            return;
        }

        String url = BASE_URL + "/transactions/seller/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::bindTransactionStats,
                error -> {
                    int status = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;

                    if (handleAuthError(status)) return;

                    statSoldValue.setText("0");
                    statTransactionsValue.setText("0");
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void bindTransactionStats(JSONArray transactions) {
        int count = transactions == null ? 0 : transactions.length();
        statSoldValue.setText(String.valueOf(count));
        statTransactionsValue.setText(String.valueOf(count));
    }
}