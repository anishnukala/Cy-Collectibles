package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminHomeActivity extends AppCompatActivity {

    private TextView tvActiveUsersCount;
    private TextView tvActiveListingsCount;
    private TextView tvActiveBuyersCount;
    private TextView tvActiveSellersCount;

    private Button btnReportedPosts;
    private Button btnFlagUsers;
    private Button btnBanUsers;
    private ImageButton btnProfile;
    private BottomNavigationView bottomNav;

    private static final String BASE_URL = ApiConfig.BASE_URL;

    private long userId = -1;
    private String username = "";
    private String userType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        userType = getIntent().getStringExtra("USERTYPE");

        if (username == null) username = "";
        if (userType == null) userType = "";

        tvActiveUsersCount = findViewById(R.id.tv_active_users_count);
        tvActiveListingsCount = findViewById(R.id.tv_active_listings_count);
        tvActiveBuyersCount = findViewById(R.id.tv_buyers_count);
        tvActiveSellersCount = findViewById(R.id.tv_sellers_count);

        btnReportedPosts = findViewById(R.id.btn_reported_posts);
        btnFlagUsers = findViewById(R.id.btn_flag_users);
        btnBanUsers = findViewById(R.id.btn_ban_users);
        btnProfile = findViewById(R.id.btn_profile);
        bottomNav = findViewById(R.id.bottom_nav);

        loadUserStats();
        loadActiveListingsCount();
        setupBottomNav();

        btnReportedPosts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, ReportedPostsActivity.class);
            addUserExtras(intent);
            startActivity(intent);
        });

        btnFlagUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, FlagUsersActivity.class);
            addUserExtras(intent);
            startActivity(intent);
        });

        btnBanUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, BanUsersActivity.class);
            addUserExtras(intent);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, UserProfileActivity.class);
            addUserExtras(intent);
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
        userId = prefs.getLong("USER_ID", userId);

        NotificationSocketManager.getInstance().connect(this, userId);
    }

    private void addUserExtras(Intent intent) {
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USERNAME", username);
        intent.putExtra("USERTYPE", userType);
    }

    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_dashboard);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                return true;

            } else if (id == R.id.nav_listings) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminActiveListingsActivity.class);
                addUserExtras(intent);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_banned) {
                Intent intent = new Intent(AdminHomeActivity.this, BannedUserActivity.class);
                addUserExtras(intent);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_users) {
                Intent intent = new Intent(AdminHomeActivity.this, AllUsersActivity.class);
                addUserExtras(intent);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadUserStats() {
        String url = BASE_URL + "/users/stats";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    int buyers = response.optInt("buyers", 0);
                    int sellers = response.optInt("sellers", 0);

                    tvActiveUsersCount.setText(String.valueOf(buyers + sellers));
                    tvActiveBuyersCount.setText(String.valueOf(buyers));
                    tvActiveSellersCount.setText(String.valueOf(sellers));
                },
                error -> {
                    tvActiveUsersCount.setText("0");
                    tvActiveBuyersCount.setText("0");
                    tvActiveSellersCount.setText("0");
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadActiveListingsCount() {
        String url = BASE_URL + "/postings/count";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    int count = response.optInt("numpostings",
                            response.optInt("numPostings",
                                    response.optInt("count", 0)));

                    tvActiveListingsCount.setText(String.valueOf(count));
                },
                error -> tvActiveListingsCount.setText("0")
        );

        Volley.newRequestQueue(this).add(request);
    }
}