package com.example.androidexample;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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

public class AllUsersActivity extends AppCompatActivity {

    private static String baseUrl() {
        return ApiConfig.BASE_URL;
    }

    private TextView btnClose;
    private LinearLayout usersContainer;
    private BottomNavigationView bottomNav;

    private long userId = -1;
    private String username = "";
    private String userType = "";

    /**
     * Initializes the all users screen, reads admin session data from the intent,
     * connects UI components, sets the close button behavior, configures bottom
     * navigation, and loads the list of users from the backend.
     *
     * @param savedInstanceState the saved instance state bundle passed to the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        userType = getIntent().getStringExtra("USERTYPE");

        if (username == null) username = "";
        if (userType == null) userType = "";

        btnClose = findViewById(R.id.btn_close);
        usersContainer = findViewById(R.id.users_container);
        bottomNav = findViewById(R.id.bottom_nav);

        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(AllUsersActivity.this, AdminHomeActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("USERTYPE", userType);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        setupBottomNav();
        loadUsers();
    }

    /**
     * Reloads the user list whenever the activity returns to the foreground.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    /**
     * Configures the bottom navigation bar and handles navigation between the
     * dashboard, banned users page, and all users page.
     */
    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_users);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                Intent intent = new Intent(AllUsersActivity.this, AdminHomeActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_banned) {
                Intent intent = new Intent(AllUsersActivity.this, BannedUserActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_users) {
                return true;

            } else if (id == R.id.nav_listings) {
                Intent intent = new Intent(AllUsersActivity.this, AdminActiveListingsActivity.class);
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

    /**
     * Sends a request to retrieve all users from the backend and either binds the
     * results to the screen or shows an error message if the request fails.
     */
    private void loadUsers() {
        String url = baseUrl() + "/users";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::bindUsers,
                error -> {
                    usersContainer.removeAllViews();
                    addEmptyMessage("Failed to load users");
                    Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * Clears the current list and renders a card for each user returned from the
     * backend response. Shows a fallback message if no users are available or if
     * parsing fails.
     *
     * @param users the JSON array containing the users returned by the backend
     */
    private void bindUsers(JSONArray users) {
        usersContainer.removeAllViews();

        if (users == null || users.length() == 0) {
            addEmptyMessage("No users found");
            return;
        }

        try {
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);

                int listedUserId = getUserId(user);
                String listedUsername = user.optString("username", "Unknown");
                int flagCount = user.optInt("flagCount", 0);
                String listedUserType = user.optString("userType", "user");

                addUserCard(listedUserId, listedUsername, flagCount, listedUserType);
            }

            if (usersContainer.getChildCount() == 0) {
                addEmptyMessage("No users found");
            }

        } catch (Exception e) {
            usersContainer.removeAllViews();
            addEmptyMessage("Failed to parse users");
            Toast.makeText(this, "Failed to parse users", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Extracts the user ID from a user JSON object, checking both possible key names.
     *
     * @param user the JSON object representing a user
     * @return the extracted user ID, or -1 if no valid ID is found
     */
    private int getUserId(JSONObject user) {
        if (user.has("id")) {
            return user.optInt("id", -1);
        }
        if (user.has("userId")) {
            return user.optInt("userId", -1);
        }
        return -1;
    }

    /**
     * Adds a simple text message to the container when no users are found or when
     * an error occurs.
     *
     * @param message the message to display to the user
     */
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

        usersContainer.addView(emptyView);
    }

    /**
     * Creates and adds a styled user card showing the username, user ID, flag count,
     * and user type.
     *
     * @param listedUserId the ID of the user being displayed
     * @param listedUsername the username of the user being displayed
     * @param flagCount the number of flags associated with the user
     * @param listedUserType the type or role of the user
     */
    private void addUserCard(int listedUserId, String listedUsername, int flagCount, String listedUserType) {
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
        tvName.setText(listedUsername);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvName.setTextColor(0xFF444444);
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvDetails = new TextView(this);
        tvDetails.setText(
                "User ID: " + listedUserId +
                        "\nFlag Count: " + flagCount +
                        "\nUser Type: " + listedUserType
        );
        tvDetails.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvDetails.setTextColor(0xFF666666);
        tvDetails.setGravity(Gravity.CENTER_VERTICAL);

        card.addView(tvName);
        card.addView(tvDetails);

        usersContainer.addView(card);
    }

    /**
     * Converts a density-independent pixel value into its pixel equivalent for the
     * current device display metrics
     *
     *
     * @param value the density-independent pixel value to convert
     * @return the equivalent pixel value as an integer
     */
    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}