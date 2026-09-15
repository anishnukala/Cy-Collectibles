package com.example.androidexample;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class BanUsersActivity extends AppCompatActivity {

    private static String baseUrl() {
        return ApiConfig.BASE_URL;
    }

    private int adminId = -1;
    private String loggedInUserType = "";

    private Button close;
    private Button banBtn;
    private RadioGroup usersRadioGroup;

    private long userId = -1;
    private String username = "";
    private String userType = "";

    private final ArrayList<Integer> userIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ban_user);

        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        userType = getIntent().getStringExtra("USERTYPE");

        if (username == null) username = "";
        if (userType == null) userType = "";

        adminId = (int) userId;
        loggedInUserType = userType;

        close = findViewById(R.id.btn_close);
        banBtn = findViewById(R.id.btn_ban_selected);
        usersRadioGroup = findViewById(R.id.users_radio_group);

        close.setOnClickListener(v -> finish());

        banBtn.setOnClickListener(v -> {
            if (adminId == -1) {
                Toast.makeText(this, "Missing admin id", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!"admin".equalsIgnoreCase(loggedInUserType)) {
                Toast.makeText(this, "Logged in user is not admin", Toast.LENGTH_SHORT).show();
                return;
            }

            int checkedId = usersRadioGroup.getCheckedRadioButtonId();

            if (checkedId == -1) {
                Toast.makeText(this, "Select one user", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedIndex = usersRadioGroup.indexOfChild(usersRadioGroup.findViewById(checkedId));

            if (selectedIndex < 0 || selectedIndex >= userIds.size()) {
                Toast.makeText(this, "Invalid selected user", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedUserId = userIds.get(selectedIndex);
            showConfirmDialog(selectedUserId);
        });

        loadUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        String url = baseUrl() + "/users";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::bindUsers,
                error -> Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void bindUsers(JSONArray users) {
        usersRadioGroup.removeAllViews();
        userIds.clear();

        if (users == null || users.length() == 0) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No active users found");
            emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            emptyView.setTextColor(0xFF666666);
            usersRadioGroup.addView(emptyView);
            return;
        }

        try {
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);

                int listedUserId = getUserId(user);
                String listedUsername = user.optString("username", "Unknown");
                int flagCount = user.optInt("flagCount", 0);
                String listedUserType = user.optString("userType", "");

                if ("admin".equalsIgnoreCase(listedUserType)) {
                    continue;
                }

                if (listedUserId != -1) {
                    addUserRow(listedUserId, listedUsername, flagCount);
                }
            }

            if (userIds.isEmpty()) {
                TextView emptyView = new TextView(this);
                emptyView.setText("No non-admin active users found");
                emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                emptyView.setTextColor(0xFF666666);
                usersRadioGroup.addView(emptyView);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Failed to parse users", Toast.LENGTH_SHORT).show();
        }
    }

    private int getUserId(JSONObject user) {
        if (user.has("id")) {
            return user.optInt("id", -1);
        }
        if (user.has("userId")) {
            return user.optInt("userId", -1);
        }
        return -1;
    }

    private void addUserRow(int userIdValue, String usernameValue, int flagCount) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setText(usernameValue + "\nFlag count: " + flagCount);
        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        radioButton.setTextColor(0xFF444444);
        radioButton.setGravity(Gravity.CENTER_VERTICAL);
        radioButton.setPadding(dp(14), dp(14), dp(14), dp(14));
        radioButton.setBackgroundResource(R.drawable.bg_input);

        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(10);
        radioButton.setLayoutParams(params);

        usersRadioGroup.addView(radioButton);
        userIds.add(userIdValue);
    }

    private void showConfirmDialog(int targetUserId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm ban")
                .setMessage("Ban this user?")
                .setPositiveButton("Yes", (dialog, which) -> banUser(targetUserId))
                .setNegativeButton("No", null)
                .show();
    }

    private void banUser(int targetUserId) {
        String url = baseUrl() + "/ban/user/" + adminId + "/" + targetUserId;

        StringRequest request = new StringRequest(
                Request.Method.PATCH,
                url,
                response -> {
                    Toast.makeText(this, "User banned successfully", Toast.LENGTH_SHORT).show();
                    loadUsers();
                },
                error -> {
                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;

                        if (code == 403) {
                            Toast.makeText(this, "Only admin can ban users", Toast.LENGTH_SHORT).show();
                        } else if (code == 404) {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        } else if (code == 409) {
                            Toast.makeText(this, "User already inactive or cannot be banned", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to ban user", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}