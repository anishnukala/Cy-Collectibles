package com.example.androidexample;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminActiveListingsActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_admin_active_listings);

        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        userType = getIntent().getStringExtra("USERTYPE");

        if (username == null) username = "";
        if (userType == null) userType = "";

        btnClose = findViewById(R.id.btn_close);
        listingsContainer = findViewById(R.id.listings_container);
        bottomNav = findViewById(R.id.bottom_nav);

        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActiveListingsActivity.this, AdminHomeActivity.class);
            addUserExtras(intent);
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

    private void addUserExtras(Intent intent) {
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USERNAME", username);
        intent.putExtra("USERTYPE", userType);
    }

    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_listings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                Intent intent = new Intent(AdminActiveListingsActivity.this, AdminHomeActivity.class);
                addUserExtras(intent);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_listings) {
                return true;

            } else if (id == R.id.nav_banned) {
                Intent intent = new Intent(AdminActiveListingsActivity.this, BannedUserActivity.class);
                addUserExtras(intent);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_users) {
                Intent intent = new Intent(AdminActiveListingsActivity.this, AllUsersActivity.class);
                addUserExtras(intent);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadActiveListings() {
        String url = BASE_URL + "/postings";

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
                JSONObject obj = listings.optJSONObject(i);
                if (obj != null) {
                    listingList.add(obj);
                }
            }

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
                String date = formatDate(rawDate);
                String imageUrl = listing.optString("imageUrl", "");

                addListingCard(postingId, title, description, price, genre, date, imageUrl);
            }

        } catch (Exception e) {
            listingsContainer.removeAllViews();
            addEmptyMessage("Failed to parse active listings");
            Toast.makeText(this, "Failed to parse active listings", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDate(String rawDate) {
        try {
            String clean = rawDate.split("T")[0];
            String[] parts = clean.split("-");

            if (parts.length == 3) {
                return parts[2] + "-" + parts[1] + "-" + parts[0];
            }

            return rawDate;
        } catch (Exception e) {
            return rawDate;
        }
    }

    private String getFullImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return "";
        }

        if (imageUrl.startsWith("http")) {
            return imageUrl;
        } else if (imageUrl.startsWith("/")) {
            return BASE_URL + imageUrl;
        } else {
            return BASE_URL + "/" + imageUrl;
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

        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(180)
        );
        imgParams.bottomMargin = dp(12);
        img.setLayoutParams(imgParams);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this)
                .load(getFullImageUrl(imageUrl))
                .placeholder(android.R.drawable.picture_frame)
                .error(android.R.drawable.ic_dialog_alert)
                .into(img);

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

        Button viewButton = new Button(this);
        viewButton.setText("View Post");
        viewButton.setAllCaps(false);
        viewButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        viewButton.setTextColor(0xFFFFFFFF);
        viewButton.setBackgroundResource(R.drawable.bg_grey_rounded);

        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        viewParams.topMargin = dp(12);
        viewButton.setLayoutParams(viewParams);

        viewButton.setOnClickListener(v -> {
            if (postingId == -1) {
                Toast.makeText(AdminActiveListingsActivity.this, "Invalid posting id", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(AdminActiveListingsActivity.this, ItemDetailActivity.class);
            intent.putExtra("postingId", postingId);
            intent.putExtra("title", title);
            intent.putExtra("genre", genre);
            intent.putExtra("price", price);
            intent.putExtra("description", description);
            intent.putExtra("imageUrl", imageUrl);
            startActivity(intent);
        });

        card.addView(img);
        card.addView(tvTitle);
        card.addView(tvDetails);
        card.addView(viewButton);

        listingsContainer.addView(card);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}