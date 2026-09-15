package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChatHomeActivity extends AppCompatActivity {

    private TextView btnClose;
    private EditText edtSearchChats;

    private TextView chipAll;
    private TextView chipDirect;
    private TextView chipGroups;
    private TextView btnNewChatFab;
    private BottomNavigationView bottomNav;

    private long userId = -1;
    private String username = "";
    private String userType = "";

    private String currentFilter = "ALL";
    private String currentSearch = "";

    private PopupWindow createPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_home);

        btnClose = findViewById(R.id.btn_close);
        edtSearchChats = findViewById(R.id.edt_search_chats);
        chipAll = findViewById(R.id.chip_all);
        chipDirect = findViewById(R.id.chip_direct);
        chipGroups = findViewById(R.id.chip_groups);
        btnNewChatFab = findViewById(R.id.btn_new_chat_fab);
        bottomNav = findViewById(R.id.bottom_nav);

        if (btnClose == null || edtSearchChats == null || chipAll == null ||
                chipDirect == null || chipGroups == null || btnNewChatFab == null || bottomNav == null) {
            Toast.makeText(this, "Layout error: missing view IDs", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadUserSession();

        if (userId == -1 || userType.isEmpty()) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnClose.setOnClickListener(v -> goToDashboard());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goToDashboard();
            }
        });

        setupBottomNav();

        chipAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateChips();
            loadChatListFragment();
        });

        chipDirect.setOnClickListener(v -> {
            currentFilter = "DIRECT";
            updateChips();
            loadChatListFragment();
        });

        chipGroups.setOnClickListener(v -> {
            currentFilter = "GROUP";
            updateChips();
            loadChatListFragment();
        });

        btnNewChatFab.setOnClickListener(this::showCreateMenu);

        edtSearchChats.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s == null ? "" : s.toString().trim();
                loadChatListFragment();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) { }
        });

        updateChips();
        loadChatListFragment();
    }

    private void loadUserSession() {
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

    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.getMenu().clear();

        if ("seller".equalsIgnoreCase(userType)) {
            bottomNav.inflateMenu(R.menu.seller_bottom_menu);
            bottomNav.setSelectedItemId(R.id.nav_seller_chat);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_seller_dashboard) {
                    Intent intent = new Intent(ChatHomeActivity.this, SellerHomeActivity.class);
                    intent.putExtra("USER_ID", userId);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("USERTYPE", userType);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;

                } else if (id == R.id.nav_seller_active_listings) {
                    Intent intent = new Intent(ChatHomeActivity.this, SellerActiveListingsActivity.class);
                    intent.putExtra("USER_ID", userId);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("USERTYPE", userType);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;

                } else if (id == R.id.nav_seller_transactions) {
                    Intent intent = new Intent(ChatHomeActivity.this, SellerTransactionsActivity.class);
                    intent.putExtra("USER_ID", userId);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("USERTYPE", userType);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;

                } else if (id == R.id.nav_seller_chat) {
                    return true;
                }

                return false;
            });

        } else {
            bottomNav.inflateMenu(R.menu.buyer_bottom_nav);
            bottomNav.setSelectedItemId(R.id.nav_buyer_chat);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_buyer_postings) {
                    Intent intent = new Intent(ChatHomeActivity.this, BuyerHomeActivity.class);
                    intent.putExtra("USER_ID", userId);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("USERTYPE", userType);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;

                } else if (id == R.id.nav_buyer_cart) {
                    Intent intent = new Intent(ChatHomeActivity.this, CartActivity.class);
                    intent.putExtra("USER_ID", userId);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("USERTYPE", userType);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;

                } else if (id == R.id.nav_buyer_transactions) {
                    Intent intent = new Intent(ChatHomeActivity.this, BuyerTransactionsActivity.class);
                    intent.putExtra("USER_ID", userId);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("USERTYPE", userType);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;

                } else if (id == R.id.nav_buyer_chat) {
                    return true;
                }

                return false;
            });
        }
    }

    private void goToDashboard() {
        Intent intent;

        if ("seller".equalsIgnoreCase(userType)) {
            intent = new Intent(ChatHomeActivity.this, SellerHomeActivity.class);
        } else {
            intent = new Intent(ChatHomeActivity.this, BuyerHomeActivity.class);
        }

        intent.putExtra("USER_ID", userId);
        intent.putExtra("USERNAME", username);
        intent.putExtra("USERTYPE", userType);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void loadChatListFragment() {
        Fragment fragment = ChatListFragment.newInstance(
                userId,
                username,
                userType,
                currentFilter,
                currentSearch
        );

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chat_list_container, fragment)
                .commit();
    }

    private void updateChips() {
        chipAll.setBackgroundResource("ALL".equalsIgnoreCase(currentFilter)
                ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipDirect.setBackgroundResource("DIRECT".equalsIgnoreCase(currentFilter)
                ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipGroups.setBackgroundResource("GROUP".equalsIgnoreCase(currentFilter)
                ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);

        chipAll.setTextColor("ALL".equalsIgnoreCase(currentFilter) ? 0xFFFFFFFF : 0xFF555555);
        chipDirect.setTextColor("DIRECT".equalsIgnoreCase(currentFilter) ? 0xFFFFFFFF : 0xFF555555);
        chipGroups.setTextColor("GROUP".equalsIgnoreCase(currentFilter) ? 0xFFFFFFFF : 0xFF555555);
    }

    private void showCreateMenu(View anchor) {
        if (createPopup != null && createPopup.isShowing()) {
            createPopup.dismiss();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(0), dp(6), dp(0), dp(6));

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(18));
        bg.setStroke(dp(1), Color.parseColor("#E5E5E5"));
        root.setBackground(bg);

        TextView btnNewGroup = new TextView(this);
        btnNewGroup.setText("New Group");
        btnNewGroup.setTextSize(18);
        btnNewGroup.setTextColor(Color.parseColor("#333333"));
        btnNewGroup.setTypeface(null, Typeface.BOLD);
        btnNewGroup.setPadding(dp(18), dp(16), dp(18), dp(16));

        TextView divider = new TextView(this);
        LinearLayout.LayoutParams dParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        dParams.setMargins(dp(18), 0, dp(18), 0);
        divider.setLayoutParams(dParams);
        divider.setBackgroundColor(Color.parseColor("#E8E8E8"));

        TextView btnNewChat = new TextView(this);
        btnNewChat.setText("New Chat");
        btnNewChat.setTextSize(18);
        btnNewChat.setTextColor(Color.parseColor("#333333"));
        btnNewChat.setTypeface(null, Typeface.BOLD);
        btnNewChat.setPadding(dp(18), dp(16), dp(18), dp(16));

        TextView divider2 = new TextView(this);
        LinearLayout.LayoutParams d2Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        d2Params.setMargins(dp(18), 0, dp(18), 0);
        divider2.setLayoutParams(d2Params);
        divider2.setBackgroundColor(Color.parseColor("#E8E8E8"));

        TextView btnCyBot = new TextView(this);
        btnCyBot.setText("CyBot AI");
        btnCyBot.setTextSize(18);
        btnCyBot.setTextColor(Color.parseColor("#BB0202"));
        btnCyBot.setTypeface(null, Typeface.BOLD);
        btnCyBot.setPadding(dp(18), dp(16), dp(18), dp(16));

        root.addView(btnNewGroup);
        root.addView(divider);
        root.addView(btnNewChat);
        root.addView(divider2);
        root.addView(btnCyBot);

        createPopup = new PopupWindow(
                root,
                dp(220),
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        createPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        createPopup.setOutsideTouchable(true);
        createPopup.setElevation(dp(10));

        btnNewGroup.setOnClickListener(v -> {
            createPopup.dismiss();
            Intent intent = new Intent(this, NewChatActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("USERTYPE", userType);
            intent.putExtra("MODE", "GROUP");
            startActivity(intent);
        });

        btnNewChat.setOnClickListener(v -> {
            createPopup.dismiss();
            Intent intent = new Intent(this, NewChatActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("USERTYPE", userType);
            intent.putExtra("MODE", "DIRECT");
            startActivity(intent);
        });
        btnCyBot.setOnClickListener(v -> {
            createPopup.dismiss();

            Intent intent = new Intent(this, AiChatActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("USERTYPE", userType);
            startActivity(intent);
        });

        root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        int popupWidth = root.getMeasuredWidth();
        int popupHeight = root.getMeasuredHeight();

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);

        createPopup.showAtLocation(
                anchor,
                Gravity.NO_GRAVITY,
                location[0] - popupWidth + anchor.getWidth(),
                location[1] - popupHeight - dp(12)
        );
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }


}