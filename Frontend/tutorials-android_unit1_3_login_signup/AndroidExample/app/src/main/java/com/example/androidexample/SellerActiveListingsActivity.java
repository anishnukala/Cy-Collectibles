package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SellerActiveListingsActivity extends AppCompatActivity {

    private static final String BASE_URL = ApiConfig.BASE_URL;

    private TextView btnClose;
    private LinearLayout listingsContainer;
    private BottomNavigationView bottomNav;

    private long userId = -1;
    private String username = "";
    private String userType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_active_listings);

        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        userType = getIntent().getStringExtra("USERTYPE");

        if (username == null) username = "";
        if (userType == null) userType = "";

        btnClose = findViewById(R.id.btn_close);
        listingsContainer = findViewById(R.id.listings_container);
        bottomNav = findViewById(R.id.bottom_nav);

        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(SellerActiveListingsActivity.this, SellerHomeActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("USERTYPE", userType);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        setupBottomNav();
        loadActiveListings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveListings();
    }

    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_seller_active_listings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_seller_dashboard) {
                Intent intent = new Intent(SellerActiveListingsActivity.this, SellerHomeActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_seller_active_listings) {
                return true;
            } else if (id == R.id.nav_seller_transactions) {
                Intent intent = new Intent(SellerActiveListingsActivity.this, SellerTransactionsActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);
                intent.putExtra("USERTYPE", userType);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_seller_chat) {
                Toast.makeText(this, "Chat screen not wired yet", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });
    }

    private void loadActiveListings() {
        String url = BASE_URL + "/postings/seller/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::bindListings,
                error -> {
                    listingsContainer.removeAllViews();
                    addEmptyMessage("Failed to load active listings");
                    Toast.makeText(this, "Failed to load active listings", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void bindListings(JSONArray listings) {
        listingsContainer.removeAllViews();

        if (listings == null || listings.length() == 0) {
            addEmptyMessage("No active listings found");
            return;
        }

        try {
            List<JSONObject> listingList = new ArrayList<>();

            for (int i = 0; i < listings.length(); i++) {
                listingList.add(listings.getJSONObject(i));
            }

            // Newest to oldest based on postingId
            Collections.sort(listingList, (a, b) ->
                    Integer.compare(b.optInt("postingId", -1), a.optInt("postingId", -1))
            );

            for (JSONObject listing : listingList) {
                int postingId = listing.optInt("postingId", -1);
                String title = listing.optString("title", "Unknown");
                String description = listing.optString("description", "");
                int price = listing.optInt("price", 0);
                String genre = listing.optString("genre", "Unknown");
                String rawDate = listing.optString("date", "Unknown");

                String date = "Unknown";
                try {
                    String clean = rawDate.split("T")[0]; // 2026-05-05
                    String[] parts = clean.split("-");    // [2026, 05, 05]

                    date = parts[2] + "-" + parts[1] + "-" + parts[0]; // 05-05-2026
                } catch (Exception e) {
                    date = rawDate;
                }
                String imageUrl = listing.optString("imageUrl", "");

                addListingCard(postingId, title, description, price, genre, date, imageUrl);
            }
        } catch (Exception e) {
            listingsContainer.removeAllViews();
            addEmptyMessage("Failed to parse active listings");
            Toast.makeText(this, "Failed to parse active listings", Toast.LENGTH_SHORT).show();
        }
    }

    private void addEmptyMessage(String message) {
        TextView emptyView = new TextView(this);
        emptyView.setText(message);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        emptyView.setTextColor(0xFF666666);
        emptyView.setGravity(Gravity.CENTER_HORIZONTAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(10);
        emptyView.setLayoutParams(params);

        listingsContainer.addView(emptyView);
    }

    private void addListingCard(int postingId, String title, String description, int price, String genre, String date, String imageUrl) {
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

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvTitle.setTextColor(0xFF444444);
        tvTitle.setTypeface(null, Typeface.BOLD);

        TextView tvDetails = new TextView(this);
        tvDetails.setText(
                "Price: $" + price +
                        "\nGenre: " + genre +
                        "\nDate Posted: " + date +
                        "\nDescription: " + description
        );
        tvDetails.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvDetails.setTextColor(0xFF666666);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams buttonRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonRowParams.topMargin = dp(12);
        buttonRow.setLayoutParams(buttonRowParams);

        Button viewButton = new Button(this);
        viewButton.setText("View Post");
        viewButton.setAllCaps(false);
        viewButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        viewButton.setTextColor(0xFFFFFFFF);
        viewButton.setBackgroundResource(R.drawable.bg_grey_rounded);

        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                0,
                dp(56),
                1
        );
        viewParams.setMarginEnd(dp(6));
        viewButton.setLayoutParams(viewParams);

        viewButton.setOnClickListener(v -> {
            if (postingId == -1) {
                Toast.makeText(SellerActiveListingsActivity.this, "Invalid posting id", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(SellerActiveListingsActivity.this, ItemDetailActivity.class);
            intent.putExtra("postingId", postingId);
            intent.putExtra("title", title);
            intent.putExtra("genre", genre);
            intent.putExtra("price", price);
            intent.putExtra("description", description);
            intent.putExtra("imageUrl", imageUrl);
            startActivity(intent);
        });

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete Post");
        deleteButton.setAllCaps(false);
        deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        deleteButton.setTextColor(0xFFFFFFFF);
        deleteButton.setBackgroundResource(R.drawable.bg_red_rounded);

        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                0,
                dp(56),
                1
        );
        deleteParams.setMarginStart(dp(6));
        deleteButton.setLayoutParams(deleteParams);

        deleteButton.setOnClickListener(v -> showDeleteConfirmDialog(postingId));

        buttonRow.addView(viewButton);
        buttonRow.addView(deleteButton);

        card.addView(tvTitle);
        card.addView(tvDetails);
        card.addView(buttonRow);

        listingsContainer.addView(card);
    }

    private void showDeleteConfirmDialog(int postingId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete listing")
                .setMessage("Delete this post?")
                .setPositiveButton("Yes", (dialog, which) -> deleteListing(postingId))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteListing(int postingId) {
        String url = BASE_URL + "/postings/" + postingId;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                    loadActiveListings();
                },
                error -> {
                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;
                        Toast.makeText(this, "Delete failed (code: " + code + ")", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Network error (no response from server)", Toast.LENGTH_LONG).show();
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