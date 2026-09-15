package com.example.androidexample;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

public class BannedUserActivity extends AppCompatActivity {

    private static String baseUrl() {
        return ApiConfig.BASE_URL;
    }
    private TextView close;
    private LinearLayout bannedUsersContainer;
    private BottomNavigationView bottomNav;

    private long userId = -1;
    private String username = "";
    private String userType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banned_users);

        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        userType = getIntent().getStringExtra("USERTYPE");

        if (username == null) username = "";
        if (userType == null) userType = "";

        close = findViewById(R.id.btn_close);
        bannedUsersContainer = findViewById(R.id.banned_users_container); // FIXED ID
        bottomNav = findViewById(R.id.bottom_nav);

        close.setOnClickListener(v -> {
            Intent intent = new Intent(BannedUserActivity.this, AdminHomeActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("USERTYPE", userType);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        setupBottomNav();
        loadBannedUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBannedUsers();
    }

    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_banned);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                Intent intent = new Intent(BannedUserActivity.this, AdminHomeActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_banned) {
                return true;

            } else if (id == R.id.nav_users) {
                Intent intent = new Intent(BannedUserActivity.this, AllUsersActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_listings) {
                Intent intent = new Intent(BannedUserActivity.this, AdminActiveListingsActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadBannedUsers() {
        if (bannedUsersContainer == null) return;

        String url = baseUrl() + "/admin/users/banned";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::bindBannedUsers,
                error -> {
                    bannedUsersContainer.removeAllViews();
                    addEmptyMessage("Failed to load banned users");

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String responseBody;

                        try {
                            responseBody = new String(error.networkResponse.data);
                        } catch (Exception e) {
                            responseBody = "Could not read error body";
                        }

                        Log.e("BANNED_USERS_ERROR", "Code: " + statusCode);
                        Log.e("BANNED_USERS_ERROR", "Body: " + responseBody);

                        Toast.makeText(this, "Error " + statusCode, Toast.LENGTH_LONG).show();
                    } else {
                        Log.e("BANNED_USERS_ERROR", "No network response", error);
                        Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void bindBannedUsers(JSONArray usersArray) {
        bannedUsersContainer.removeAllViews();

        if (usersArray == null || usersArray.length() == 0) {
            addEmptyMessage("No banned users found");
            return;
        }

        try {
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject userObj = usersArray.getJSONObject(i);

                int bannedUserId = userObj.optInt("id", -1);
                String bannedUsername = userObj.optString("username", "Unknown");
                String emailId = userObj.optString("emailId", "N/A");
                int flagCount = userObj.optInt("flagCount", 0);
                String bannedUserType = userObj.optString("userType", "Unknown");
                boolean banned = userObj.optBoolean("banned", false);
                int userStatus = userObj.optInt("userStatus", -1);

                addBannedUserCard(
                        bannedUserId,
                        bannedUsername,
                        emailId,
                        flagCount,
                        bannedUserType,
                        banned,
                        userStatus
                );
            }

            if (bannedUsersContainer.getChildCount() == 0) {
                addEmptyMessage("No banned users found");
            }

        } catch (Exception e) {
            Log.e("BANNED_USERS_PARSE", "Failed to parse banned users", e);
            bannedUsersContainer.removeAllViews();
            addEmptyMessage("Failed to parse banned users");
            Toast.makeText(this, "Failed to parse banned users", Toast.LENGTH_SHORT).show();
        }
    }

    private void addEmptyMessage(String message) {
        TextView emptyView = new TextView(this);
        emptyView.setText(message);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        emptyView.setTextColor(0xFF666666);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(10);
        emptyView.setLayoutParams(params);

        bannedUsersContainer.addView(emptyView);
    }

    private void addBannedUserCard(
            int bannedUserId,
            String bannedUsername,
            String emailId,
            int flagCount,
            String bannedUserType,
            boolean banned,
            int userStatus
    ) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_input);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.topMargin = dp(12);
        card.setLayoutParams(cardParams);

        TextView tvName = new TextView(this);
        tvName.setText(bannedUsername);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvName.setTextColor(0xFF444444);
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvDetails = new TextView(this);
        tvDetails.setText(
                "User ID: " + bannedUserId +
                        "\nEmail: " + emailId +
                        "\nFlag Count: " + flagCount +
                        "\nUser Type: " + bannedUserType +
                        "\nBanned: " + (banned ? "Yes" : "No") +
                        "\nStatus: " + getStatusText(userStatus)
        );
        tvDetails.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvDetails.setTextColor(0xFF666666);
        tvDetails.setGravity(Gravity.CENTER_VERTICAL);

        card.addView(tvName);
        card.addView(tvDetails);

        bannedUsersContainer.addView(card);
    }

    private String getStatusText(int userStatus) {
        if (userStatus == 0) return "BANNED";
        if (userStatus == 1) return "ACTIVE";
        if (userStatus == -1) return "DELETED";
        return "UNKNOWN";
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}