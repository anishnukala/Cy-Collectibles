package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BuyerHomeActivity extends AppCompatActivity {

    private LinearLayout listContainer;
    private Button btnCategory;
    private Button btnSort;
    private EditText etSearch;

    private String selectedGenre = null;
    private String sortField = "datePosted";
    private String sortDir = "desc";
    private String searchQuery = "";

    private final List<String> genres = new ArrayList<>();
    private final List<JSONObject> allPostings = new ArrayList<>();

    private long userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_home);

        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        userId = prefs.getLong("USER_ID", -1);

        if (userId == -1) {
            forceLogoutToLogin("Session expired. Please login again.");
            return;
        }

        NotificationSocketManager.getInstance().connect(this, userId);

        ImageButton profileBtn = findViewById(R.id.btn_profile);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        listContainer = findViewById(R.id.list_container);
        btnCategory = findViewById(R.id.btn_category);
        btnSort = findViewById(R.id.btn_sort);
        etSearch = findViewById(R.id.et_search);

        if (profileBtn != null) {
            profileBtn.setOnClickListener(v ->
                    startActivity(new Intent(BuyerHomeActivity.this, UserProfileActivity.class))
            );
        }

        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_buyer_postings);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_buyer_postings) {
                    return true;
                } else if (id == R.id.nav_buyer_cart) {
                    startActivity(new Intent(BuyerHomeActivity.this, CartActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_buyer_transactions) {
                    startActivity(new Intent(BuyerHomeActivity.this, BuyerTransactionsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_buyer_chat) {
                    startActivity(new Intent(BuyerHomeActivity.this, ChatHomeActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            });
        }

        if (btnCategory != null) {
            btnCategory.setOnClickListener(v -> showCategoryMenu());
        }

        if (btnSort != null) {
            btnSort.setOnClickListener(v -> showSortMenu());
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString().trim();
                    filterAndRenderPostings();
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        getWindow().setStatusBarColor(android.graphics.Color.WHITE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        loadGenres();
        loadPostings();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        userId = prefs.getLong("USER_ID", -1);

        if (userId == -1) {
            forceLogoutToLogin("Session expired. Please login again.");
            return;
        }

        NotificationSocketManager.getInstance().connect(this, userId);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_buyer_postings);
        }
    }

    private void forceLogoutToLogin(String message) {
        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(BuyerHomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean handleAuthError(int statusCode) {
        if (statusCode == 403) {
            forceLogoutToLogin("Your account has been banned or is no longer active.");
            return true;
        }
        return false;
    }

    private void loadGenres() {
        String url = ApiConfig.BASE_URL + "/postings/genre";

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                arr -> {
                    genres.clear();
                    genres.add("All");

                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            String g = arr.optString(i, "").trim();
                            if (!g.isEmpty()) {
                                genres.add(g);
                            }
                        }
                    }
                },
                error -> {
                    int status = (error.networkResponse != null)
                            ? error.networkResponse.statusCode : -1;

                    if (handleAuthError(status)) return;

                    Toast.makeText(this,
                            "Failed to load genres (" + status + ")",
                            Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void showCategoryMenu() {
        if (genres.isEmpty()) {
            Toast.makeText(this, "Genres not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        PopupMenu menu = new PopupMenu(this, btnCategory);

        for (String g : genres) {
            menu.getMenu().add(g);
        }

        menu.setOnMenuItemClickListener(item -> {
            String choice = item.getTitle().toString();

            if ("All".equals(choice)) {
                selectedGenre = null;
                btnCategory.setText("Category");
            } else {
                selectedGenre = choice;
                btnCategory.setText(choice);
            }

            loadPostings();
            return true;
        });

        menu.show();
    }

    private void showSortMenu() {
        PopupMenu menu = new PopupMenu(this, btnSort);
        menu.getMenu().add("Newest");
        menu.getMenu().add("Oldest");
        menu.getMenu().add("Price: Low to High");
        menu.getMenu().add("Price: High to Low");
        menu.getMenu().add("Title: A-Z");
        menu.getMenu().add("Title: Z-A");

        menu.setOnMenuItemClickListener(item -> {
            String choice = item.getTitle().toString();

            switch (choice) {
                case "Newest":
                    sortField = "datePosted";
                    sortDir = "desc";
                    break;
                case "Oldest":
                    sortField = "datePosted";
                    sortDir = "asc";
                    break;
                case "Price: Low to High":
                    sortField = "price";
                    sortDir = "asc";
                    break;
                case "Price: High to Low":
                    sortField = "price";
                    sortDir = "desc";
                    break;
                case "Title: A-Z":
                    sortField = "title";
                    sortDir = "asc";
                    break;
                case "Title: Z-A":
                    sortField = "title";
                    sortDir = "desc";
                    break;
            }

            btnSort.setText(choice);
            loadPostings();
            return true;
        });

        menu.show();
    }

    private void loadPostings() {
        Uri.Builder builder = Uri.parse(ApiConfig.BASE_URL + "/postings").buildUpon();

        if (selectedGenre != null && !selectedGenre.trim().isEmpty()) {
            builder.appendQueryParameter("genre", selectedGenre.trim());
        }

        builder.appendQueryParameter("sort", sortField + "," + sortDir);

        String url = builder.build().toString();

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                arr -> {
                    allPostings.clear();

                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.optJSONObject(i);
                            if (obj != null) {
                                allPostings.add(obj);
                            }
                        }
                    }

                    filterAndRenderPostings();
                },
                error -> {
                    int status = (error.networkResponse != null)
                            ? error.networkResponse.statusCode : -1;
                    String msg = (error.getMessage() != null)
                            ? error.getMessage() : "Network error";

                    if (handleAuthError(status)) return;

                    Toast.makeText(this,
                            "Failed to load (" + status + "): " + msg,
                            Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void filterAndRenderPostings() {
        if (listContainer == null) return;

        listContainer.removeAllViews();

        List<JSONObject> filtered = new ArrayList<>();
        String query = searchQuery == null ? "" : searchQuery.toLowerCase(Locale.getDefault());

        for (JSONObject o : allPostings) {
            String title = o.optString("title", "").toLowerCase(Locale.getDefault());
            String genre = o.optString("genre", "").toLowerCase(Locale.getDefault());
            String description = o.optString("description", "").toLowerCase(Locale.getDefault());

            boolean matchesSearch =
                    query.isEmpty()
                            || title.contains(query)
                            || genre.contains(query)
                            || description.contains(query);

            if (matchesSearch) {
                filtered.add(o);
            }
        }

        if (filtered.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No matching listings found.");
            tv.setTextSize(18);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            listContainer.addView(tv);
            return;
        }

        for (JSONObject o : filtered) {
            renderSinglePosting(o);
        }
    }

    private void renderSinglePosting(JSONObject o) {
        if (isFinishing() || isDestroyed()) return;

        String title = o.optString("title", "Untitled");
        String genre = o.optString("genre", "");
        int price = o.optInt("price", 0);
        String imageUrl = o.optString("imageUrl", "");
        int postingId = o.optInt("postingId", -1);

        String fullImageUrl = getFullImageUrl(imageUrl);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardLp.bottomMargin = dp(14);
        card.setLayoutParams(cardLp);

        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(180)
        );
        imgLp.bottomMargin = dp(12);
        img.setLayoutParams(imgLp);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this)
                .load(fullImageUrl)
                .placeholder(android.R.drawable.picture_frame)
                .error(android.R.drawable.ic_dialog_alert)
                .into(img);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(22);
        tvTitle.setTextColor(0xFF444444);
        tvTitle.setTypeface(tvTitle.getTypeface(), Typeface.BOLD);

        TextView tvMeta = new TextView(this);
        tvMeta.setText("$" + price + " • " + genre);
        tvMeta.setTextSize(18);
        tvMeta.setTextColor(0xFF666666);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams buttonRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonRowLp.topMargin = dp(14);
        buttonRow.setLayoutParams(buttonRowLp);

        Button btnDetails = new Button(this);
        btnDetails.setText("Item Detail");
        btnDetails.setTransformationMethod(null);
        btnDetails.setTextColor(0xFFFFFFFF);
        btnDetails.setTextSize(16);
        btnDetails.setBackgroundResource(R.drawable.bg_red_rounded);

        LinearLayout.LayoutParams detailsLp = new LinearLayout.LayoutParams(
                0,
                dp(52),
                1f
        );
        detailsLp.rightMargin = dp(8);
        btnDetails.setLayoutParams(detailsLp);

        btnDetails.setOnClickListener(v -> {
            if (postingId == -1) {
                Toast.makeText(this, "Invalid posting id", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(BuyerHomeActivity.this, ItemDetailActivity.class);
            intent.putExtra("postingId", postingId);
            startActivity(intent);
        });

        Button btnAddToCart = new Button(this);
        btnAddToCart.setText("Add to Cart");
        btnAddToCart.setTransformationMethod(null);
        btnAddToCart.setTextColor(0xFFFFFFFF);
        btnAddToCart.setTextSize(16);
        btnAddToCart.setBackgroundResource(R.drawable.bg_grey_rounded);

        LinearLayout.LayoutParams cartBtnLp = new LinearLayout.LayoutParams(
                0,
                dp(52),
                1f
        );
        cartBtnLp.leftMargin = dp(8);
        btnAddToCart.setLayoutParams(cartBtnLp);

        btnAddToCart.setOnClickListener(v -> addToCart(o));

        buttonRow.addView(btnDetails);
        buttonRow.addView(btnAddToCart);

        card.addView(img);
        card.addView(tvTitle);
        card.addView(tvMeta);
        card.addView(buttonRow);

        listContainer.addView(card);
    }

    private String getFullImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return "";
        }

        if (imageUrl.startsWith("http")) {
            return imageUrl;
        } else if (imageUrl.startsWith("/")) {
            return ApiConfig.BASE_URL + imageUrl;
        } else {
            return ApiConfig.BASE_URL + "/" + imageUrl;
        }
    }

    private void addToCart(JSONObject posting) {
        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        long currentUserId = prefs.getLong("USER_ID", -1);

        if (currentUserId == -1) {
            forceLogoutToLogin("Session expired. Please login again.");
            return;
        }

        int postingId = posting.optInt("postingId", -1);
        String title = posting.optString("title", "Item");

        if (postingId == -1) {
            Toast.makeText(this, "Invalid posting id", Toast.LENGTH_SHORT).show();
            return;
        }

        CartManager.addToCart(this, currentUserId, postingId,
                response -> Toast.makeText(this, "Added to cart: " + title, Toast.LENGTH_SHORT).show(),
                error -> {
                    if (error.networkResponse != null) {
                        int status = error.networkResponse.statusCode;

                        if (status == 403) {
                            forceLogoutToLogin("Your account has been banned or is no longer active.");
                        } else if (status == 409) {
                            Toast.makeText(this, "Item already in cart or unavailable", Toast.LENGTH_SHORT).show();
                        } else if (status == 404) {
                            Toast.makeText(this, "User or posting not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to add to cart (" + status + ")", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Network error while adding to cart", Toast.LENGTH_SHORT).show();
                    }
                }
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