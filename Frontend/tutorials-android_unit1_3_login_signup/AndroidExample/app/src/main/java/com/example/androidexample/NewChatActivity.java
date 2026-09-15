package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewChatActivity extends AppCompatActivity implements UserSearchAdapter.OnUserClickListener {

    private RecyclerView recyclerUsers;
    private EditText edtSearch;
    private EditText edtGroupName;
    private Button btnCreate;
    private TextView txtTitle;
    private TextView txtSelectedCount;
    private LinearLayout groupNameContainer;
    private View btnBack;

    private final ArrayList<UserSearchItem> allUsers = new ArrayList<>();
    private final ArrayList<UserSearchItem> filteredUsers = new ArrayList<>();
    private final ArrayList<UserSearchItem> selectedUsers = new ArrayList<>();

    private UserSearchAdapter adapter;

    private long userId = -1;
    private String username = "";
    private String userType = "";
    private String mode = "DIRECT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        loadSession();

        recyclerUsers = findViewById(R.id.recycler_users);
        edtSearch = findViewById(R.id.edt_search_user);
        edtGroupName = findViewById(R.id.edt_group_name);
        btnCreate = findViewById(R.id.btn_create_chat);
        txtTitle = findViewById(R.id.txt_new_chat_title);
        txtSelectedCount = findViewById(R.id.txt_selected_count);
        groupNameContainer = findViewById(R.id.group_name_container);
        btnBack = findViewById(R.id.btn_back_new_chat);

        mode = getIntent().getStringExtra("MODE");
        if (mode == null || mode.trim().isEmpty()) {
            mode = "DIRECT";
        }

        txtTitle.setText("GROUP".equalsIgnoreCase(mode) ? "Create Group" : "Start Chat");
        groupNameContainer.setVisibility("GROUP".equalsIgnoreCase(mode) ? View.VISIBLE : View.GONE);
        updateSelectedCount();

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserSearchAdapter(filteredUsers, this, "GROUP".equalsIgnoreCase(mode));
        recyclerUsers.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnCreate.setOnClickListener(v -> {
            if ("GROUP".equalsIgnoreCase(mode)) {
                createGroupChat();
            } else {
                createDirectChat();
            }
        });

        loadUsers();
    }

    private void loadSession() {
        Intent intent = getIntent();

        userId = intent.getLongExtra("USER_ID", -1);
        username = intent.getStringExtra("USERNAME");
        userType = intent.getStringExtra("USERTYPE");

        if (username == null) username = "";
        if (userType == null) userType = "";

        if (userId == -1 || username.isEmpty() || userType.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
            if (userId == -1) userId = prefs.getLong("USER_ID", -1);
            if (username.isEmpty()) username = prefs.getString("USERNAME", "");
            if (userType.isEmpty()) userType = prefs.getString("USERTYPE", "");
        }
    }

    private void loadUsers() {
        String url = ApiConfig.BASE_URL + "/users";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::bindUsers,
                error -> {
                    Log.e("NEW_CHAT", "Failed to load users", error);
                    Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void bindUsers(JSONArray response) {
        allUsers.clear();
        filteredUsers.clear();

        if (response == null) {
            adapter.notifyDataSetChanged();
            return;
        }

        for (int i = 0; i < response.length(); i++) {
            JSONObject obj = response.optJSONObject(i);
            if (obj == null) continue;

            int id = obj.optInt("id", -1);
            String uname = obj.optString("username", "");
            String email = obj.optString("emailId", "");
            String type = obj.optString("userType", "");

            if (id == -1) continue;
            if (id == (int) userId) continue;

            // exclude admins from any chat creation list
            if ("admin".equalsIgnoreCase(type)) {
                continue;
            }

            allUsers.add(new UserSearchItem(id, uname, email, type));
        }

        filteredUsers.addAll(allUsers);
        adapter.notifyDataSetChanged();
    }

    private void filterUsers(String query) {
        filteredUsers.clear();

        String q = query == null ? "" : query.trim().toLowerCase();

        if (q.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            for (UserSearchItem item : allUsers) {
                if ((item.getUsername() != null && item.getUsername().toLowerCase().contains(q)) ||
                        (item.getEmail() != null && item.getEmail().toLowerCase().contains(q)) ||
                        (item.getUserType() != null && item.getUserType().toLowerCase().contains(q))) {
                    filteredUsers.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onUserClicked(UserSearchItem user) {
        if ("GROUP".equalsIgnoreCase(mode)) {
            toggleGroupSelection(user);
        } else {
            selectedUsers.clear();
            selectedUsers.add(user);
            adapter.setSingleSelectedUserId(user.getId());
            adapter.notifyDataSetChanged();
            updateSelectedCount();
        }
    }

    private void toggleGroupSelection(UserSearchItem user) {
        boolean removed = false;

        for (int i = 0; i < selectedUsers.size(); i++) {
            if (selectedUsers.get(i).getId() == user.getId()) {
                selectedUsers.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            selectedUsers.add(user);
        }

        adapter.setSelectedUsers(selectedUsers);
        adapter.notifyDataSetChanged();
        updateSelectedCount();
    }

    private void updateSelectedCount() {
        if ("GROUP".equalsIgnoreCase(mode)) {
            txtSelectedCount.setText("Selected: " + selectedUsers.size());
        } else {
            txtSelectedCount.setText(selectedUsers.isEmpty() ? "No user selected" : "Selected: 1");
        }
    }

    private void createDirectChat() {
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Select a user first", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        JSONArray memberIds = new JSONArray();

        try {
            body.put("type", "DIRECT");
            memberIds.put((int) userId);
            memberIds.put(selectedUsers.get(0).getId());
            body.put("memberIds", memberIds);
        } catch (JSONException e) {
            Toast.makeText(this, "Failed to build request", Toast.LENGTH_SHORT).show();
            return;
        }

        sendCreateChannelRequest(body, false);
    }

    private void createGroupChat() {
        String groupName = edtGroupName.getText().toString().trim();

        if (groupName.isEmpty()) {
            Toast.makeText(this, "Enter a group name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Select at least one user", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        JSONArray memberIds = new JSONArray();

        try {
            body.put("type", "GROUP");
            body.put("name", groupName);
            memberIds.put((int) userId);

            for (UserSearchItem user : selectedUsers) {
                memberIds.put(user.getId());
            }

            body.put("memberIds", memberIds);
        } catch (JSONException e) {
            Toast.makeText(this, "Failed to build request", Toast.LENGTH_SHORT).show();
            return;
        }

        sendCreateChannelRequest(body, true);
    }

    private void sendCreateChannelRequest(JSONObject body, boolean isGroup) {
        btnCreate.setEnabled(false);

        String url = ApiConfig.BASE_URL + "/channel";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    btnCreate.setEnabled(true);
                    Toast.makeText(this, isGroup ? "Group created" : "Chat created", Toast.LENGTH_SHORT).show();
                    goBackToChatHome(isGroup ? "GROUP" : "DIRECT");
                },
                error -> {
                    btnCreate.setEnabled(true);

                    int code = error.networkResponse != null ? error.networkResponse.statusCode : -1;

                    if (code == 201) {
                        goBackToChatHome(isGroup ? "GROUP" : "DIRECT");
                        return;
                    }

                    if (code == 409) {
                        Toast.makeText(this, "Chat may already exist", Toast.LENGTH_SHORT).show();
                    } else if (code == 400) {
                        Toast.makeText(this, "Invalid request", Toast.LENGTH_SHORT).show();
                    } else if (code == 404) {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to create chat", Toast.LENGTH_SHORT).show();
                    }

                    Log.e("NEW_CHAT", "Create channel failed", error);
                }
        ) {
            @Override
            protected com.android.volley.Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                if (response != null && response.statusCode == 201 && (response.data == null || response.data.length == 0)) {
                    return com.android.volley.Response.success(new JSONObject(), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                }
                return super.parseNetworkResponse(response);
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void goBackToChatHome(String openTab) {
        Intent intent = new Intent(NewChatActivity.this, ChatHomeActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USERNAME", username);
        intent.putExtra("USERTYPE", userType);
        intent.putExtra("OPEN_TAB", openTab);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}