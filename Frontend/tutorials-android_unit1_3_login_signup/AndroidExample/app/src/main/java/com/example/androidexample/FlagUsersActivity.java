package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class FlagUsersActivity extends AppCompatActivity {

    private static String baseUrl() {
        return ApiConfig.BASE_URL;
    }

    private int adminId = -1;
    private String loggedInUserType = "";

    private Button close;
    private Button flagBtn;
    private RadioGroup usersRadioGroup;
    private BottomNavigationView bottomNav;

    private long userId = -1;
    private String username = "";
    private String userType = "";

    private final ArrayList<Integer> userIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_user);

        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        userType = getIntent().getStringExtra("USERTYPE");

        if (username == null) username = "";
        if (userType == null) userType = "";

        adminId = (int) userId;
        loggedInUserType = userType;

        close = findViewById(R.id.btn_close);
        flagBtn = findViewById(R.id.btn_flag_selected);
        usersRadioGroup = findViewById(R.id.users_radio_group);
        bottomNav = findViewById(R.id.bottom_nav);

        close.setOnClickListener(v -> finish());

        setupBottomNav();

        flagBtn.setOnClickListener(v -> {
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

            if (selectedUserId == adminId) {
                Toast.makeText(this, "Admin cannot flag themselves", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadio = usersRadioGroup.findViewById(checkedId);
            String selectedUserLabel = selectedRadio != null
                    ? selectedRadio.getText().toString().split("\n")[0]
                    : "this user";

            showFlagReasonDialog(selectedUserId, selectedUserLabel);
        });

        loadUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_users);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                Intent intent = new Intent(FlagUsersActivity.this, AdminHomeActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_banned) {
                Intent intent = new Intent(FlagUsersActivity.this, BannedUserActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_users) {
                return true;
            }

            return false;
        });
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

                if (listedUserId == -1) continue;
                if ("admin".equalsIgnoreCase(listedUserType)) continue;
                if (listedUserId == adminId) continue;

                addUserRow(listedUserId, listedUsername, flagCount);
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
        if (user.has("id")) return user.optInt("id", -1);
        if (user.has("userId")) return user.optInt("userId", -1);
        return -1;
    }

    private void addUserRow(int targetUserId, String usernameValue, int flagCount) {
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
        userIds.add(targetUserId);
    }

    private void showFlagReasonDialog(int targetUserId, String targetUsername) {
        LayoutInflater inflater = LayoutInflater.from(this);
        android.view.View dialogView = inflater.inflate(R.layout.dialog_flag_user_reason, null);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextView tvDialogSubtitle = dialogView.findViewById(R.id.tv_dialog_subtitle);
        EditText etReason = dialogView.findViewById(R.id.et_flag_reason_dialog);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel_flag);
        AppCompatButton btnFlag = dialogView.findViewById(R.id.btn_confirm_flag);

        tvDialogTitle.setText("Flag user");
        tvDialogSubtitle.setText("Enter a reason for flagging \"" + targetUsername + "\"");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnFlag.setOnClickListener(v -> {
            String reasonText = etReason.getText().toString().trim();

            if (reasonText.isEmpty()) {
                etReason.setError("Reason is required");
                return;
            }

            dialog.dismiss();
            showConfirmDialog(targetUserId, reasonText);
        });

        dialog.show();
    }

    private void showConfirmDialog(int targetUserId, String reasonText) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm flag")
                .setMessage("Flag this user?")
                .setPositiveButton("Yes", (dialog, which) -> flagUser(targetUserId, reasonText))
                .setNegativeButton("No", null)
                .show();
    }

    private void flagUser(int targetUserId, String reasonText) {
        String url = baseUrl() + "/flag/user/" + adminId + "/" + targetUserId;

        JSONObject body = new JSONObject();
        try {
            body.put("message", reasonText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                body,
                response -> {
                    Toast.makeText(this, "User flagged successfully", Toast.LENGTH_SHORT).show();
                    usersRadioGroup.clearCheck();
                    loadUsers();
                },
                error -> {
                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;

                        if (code == 403) {
                            Toast.makeText(this, "Only admin can flag users", Toast.LENGTH_SHORT).show();
                        } else if (code == 404) {
                            Toast.makeText(this, "Admin or user not found", Toast.LENGTH_SHORT).show();
                        } else if (code == 409) {
                            Toast.makeText(this, "User already inactive or cannot be flagged", Toast.LENGTH_SHORT).show();
                        } else if (code == 415) {
                            Toast.makeText(this, "Unsupported media type", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to flag user (" + code + ")", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = response.data == null
                            ? ""
                            : new String(
                            response.data,
                            Charset.forName(
                                    HttpHeaderParser.parseCharset(response.headers, "utf-8")
                            )
                    );

                    if (jsonString.isEmpty()) {
                        return Response.success(
                                new JSONObject(),
                                HttpHeaderParser.parseCacheHeaders(response)
                        );
                    }

                    return Response.success(
                            new JSONObject(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response)
                    );
                } catch (Exception e) {
                    return Response.success(
                            new JSONObject(),
                            HttpHeaderParser.parseCacheHeaders(response)
                    );
                }
            }
        };

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