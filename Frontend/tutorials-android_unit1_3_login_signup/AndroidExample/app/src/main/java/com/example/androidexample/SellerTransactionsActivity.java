package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SellerTransactionsActivity extends AppCompatActivity {

    private static String baseUrl() {
        return ApiConfig.BASE_URL;
    }

    private Button btnClose;
    private LinearLayout transactionsContainer;
    private BottomNavigationView bottomNav;

    private long userId = -1;
    private String username = "";
    private String userType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_transactions);

        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        userType = getIntent().getStringExtra("USERTYPE");

        if (username == null) username = "";
        if (userType == null) userType = "";

        btnClose = findViewById(R.id.btn_close);
        transactionsContainer = findViewById(R.id.transactions_container);
        bottomNav = findViewById(R.id.bottom_nav);

        if (btnClose == null || transactionsContainer == null || bottomNav == null) {
            Toast.makeText(this, "Layout error: missing view IDs", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(SellerTransactionsActivity.this, SellerHomeActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("USERTYPE", userType);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        setupBottomNav();
        loadTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (transactionsContainer != null) {
            loadTransactions();
        }
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_seller_transactions);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_seller_dashboard) {
                Intent intent = new Intent(SellerTransactionsActivity.this, SellerHomeActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_seller_active_listings) {
                Intent intent = new Intent(SellerTransactionsActivity.this, SellerActiveListingsActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_seller_transactions) {
                return true;

            } else if (id == R.id.nav_seller_chat) {
                Intent intent = new Intent(SellerTransactionsActivity.this, ChatHomeActivity.class);
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

    private void loadTransactions() {
        transactionsContainer.removeAllViews();

        if (userId == -1) {
            addEmptyMessage("Invalid user ID");
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = baseUrl() + "/transactions/seller/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::bindTransactions,
                error -> {
                    transactionsContainer.removeAllViews();
                    addEmptyMessage("Failed to load transactions");
                    Toast.makeText(this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void bindTransactions(JSONArray transactions) {
        transactionsContainer.removeAllViews();

        if (transactions == null || transactions.length() == 0) {
            addEmptyMessage("No transactions found");
            return;
        }

        try {
            List<JSONObject> transactionList = new ArrayList<>();

            for (int i = 0; i < transactions.length(); i++) {
                transactionList.add(transactions.getJSONObject(i));
            }

            // NEWEST → OLDEST
            Collections.sort(transactionList, (a, b) -> {
                long timeA = parseDate(a.optString("dateSold", ""));
                long timeB = parseDate(b.optString("dateSold", ""));
                return Long.compare(timeB, timeA);
            });

            for (JSONObject transaction : transactionList) {

                int transactionId = transaction.optInt("transactionId", -1);
                int buyerId = transaction.optInt("buyerId", -1);

                String rawDate = transaction.optString("dateSold", "Unknown");
                String dateSold = formatDisplayDate(rawDate);

                JSONObject posting = transaction.optJSONObject("posting");
                String title = posting != null ? posting.optString("title", "Unknown") : "Unknown";
                int price = posting != null ? posting.optInt("price", 0) : 0;
                String genre = posting != null ? posting.optString("genre", "Unknown") : "Unknown";

                addTransactionCard(transactionId, buyerId, dateSold, title, price, genre);
            }

        } catch (Exception e) {
            transactionsContainer.removeAllViews();
            addEmptyMessage("Failed to parse transactions");
            Toast.makeText(this, "Failed to parse transactions", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔥 FORMAT: Apr 02 2026
    private String formatDisplayDate(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.equalsIgnoreCase("Unknown")) {
            return "Unknown";
        }

        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
            Date date = input.parse(raw);
            if (date == null) return raw;

            SimpleDateFormat output = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
            return output.format(date);

        } catch (Exception e1) {
            try {
                SimpleDateFormat input2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault());
                Date date = input2.parse(raw);
                if (date == null) return raw;

                SimpleDateFormat output = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
                return output.format(date);

            } catch (Exception e2) {
                return raw;
            }
        }
    }

    private long parseDate(String raw) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
            Date date = input.parse(raw);
            return date != null ? date.getTime() : 0;
        } catch (Exception e1) {
            try {
                SimpleDateFormat input2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault());
                Date date = input2.parse(raw);
                return date != null ? date.getTime() : 0;
            } catch (Exception e2) {
                return 0;
            }
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

        transactionsContainer.addView(emptyView);
    }

    private void addTransactionCard(int transactionId, int buyerId, String dateSold, String title, int price, String genre) {
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
        tvName.setText(title);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvName.setTextColor(0xFF444444);
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvDetails = new TextView(this);
        tvDetails.setText(
                "Transaction ID: " + transactionId +
                        "\nBuyer ID: " + buyerId +
                        "\nPrice: $" + price +
                        "\nGenre: " + genre +
                        "\nDate Sold: " + dateSold
        );
        tvDetails.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvDetails.setTextColor(0xFF666666);

        card.addView(tvName);
        card.addView(tvDetails);
        transactionsContainer.addView(card);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}