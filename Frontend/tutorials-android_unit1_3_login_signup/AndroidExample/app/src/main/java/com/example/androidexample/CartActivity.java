package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

public class CartActivity extends AppCompatActivity {

    private LinearLayout cartListContainer;
    private TextView tvEmpty;
    private Button btnClear;
    private Button btnBuyNow;
    private BottomNavigationView bottomNav;

    private long userId = -1;
    private JSONArray cartItems = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartListContainer = findViewById(R.id.cart_list_container);
        tvEmpty = findViewById(R.id.tv_empty);
        btnClear = findViewById(R.id.btn_clear_cart);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        bottomNav = findViewById(R.id.bottom_nav);

        if (cartListContainer == null || tvEmpty == null || btnClear == null || btnBuyNow == null || bottomNav == null) {
            Toast.makeText(this, "Layout error: missing view IDs", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        userId = prefs.getLong("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupBottomNav();

        btnClear.setOnClickListener(v -> clearCart());

        btnBuyNow.setOnClickListener(v -> {
            if (cartItems == null || cartItems.length() == 0) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("cartItems", cartItems.toString());
                startActivity(intent);
            }
        });

        loadCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_buyer_cart);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_buyer_postings) {
                startActivity(new Intent(CartActivity.this, BuyerHomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_buyer_cart) {
                return true;

            } else if (id == R.id.nav_buyer_transactions) {
                startActivity(new Intent(CartActivity.this, BuyerTransactionsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_buyer_chat) {
                startActivity(new Intent(CartActivity.this, ChatHomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadCart() {
        CartManager.getCart(this, userId,
                response -> {
                    cartItems = response;
                    renderCart();
                },
                error -> {
                    cartItems = new JSONArray();
                    renderCart();
                    Toast.makeText(this, "Failed to load cart", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearCart() {
        CartManager.clearCart(this, userId,
                response -> {
                    Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show();
                    loadCart();
                },
                error -> Toast.makeText(this, "Failed to clear cart", Toast.LENGTH_SHORT).show()
        );
    }

    private void renderCart() {
        cartListContainer.removeAllViews();

        boolean empty = (cartItems == null || cartItems.length() == 0);

        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        btnClear.setEnabled(!empty);
        btnBuyNow.setEnabled(!empty);

        if (empty) return;

        for (int i = 0; i < cartItems.length(); i++) {
            JSONObject o = cartItems.optJSONObject(i);
            if (o == null) continue;

            renderSingleCartItem(o);
        }
    }

    private void renderSingleCartItem(JSONObject o) {
        int postingId = o.optInt("postingId", -1);
        String title = o.optString("title", "Untitled");
        String genre = o.optString("genre", "");
        double price = o.optDouble("price", 0);
        String imageUrl = o.optString("imageUrl", "");
        String description = o.optString("description", "");

        String fullImageUrl = BuildConfig.BASE_URL + imageUrl;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(145)
        );
        cardLp.bottomMargin = dp(14);
        card.setLayoutParams(cardLp);

        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(dp(100), dp(100));
        img.setLayoutParams(imgLp);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.addView(img);

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        );
        rightLp.leftMargin = dp(12);
        right.setLayoutParams(rightLp);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(0xFF444444);
        tvTitle.setTypeface(tvTitle.getTypeface(), Typeface.BOLD);
        tvTitle.setMaxLines(2);
        tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView tvMeta = new TextView(this);
        tvMeta.setText(genre + " • $" + price);
        tvMeta.setTextSize(14);
        tvMeta.setTextColor(0xFF555555);

        LinearLayout.LayoutParams metaLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        metaLp.topMargin = dp(4);
        tvMeta.setLayoutParams(metaLp);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams buttonRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonRowLp.topMargin = dp(10);
        buttonRow.setLayoutParams(buttonRowLp);

        Button btnDetails = new Button(this);
        LinearLayout.LayoutParams detailsLp = new LinearLayout.LayoutParams(
                0,
                dp(36),
                1f
        );
        detailsLp.rightMargin = dp(8);
        btnDetails.setLayoutParams(detailsLp);
        btnDetails.setText("Item Detail");
        btnDetails.setTransformationMethod(null);
        btnDetails.setTextColor(0xFFFFFFFF);
        btnDetails.setTextSize(12);
        btnDetails.setBackgroundResource(R.drawable.bg_red_rounded);

        btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, ItemDetailActivity.class);
            intent.putExtra("postingId", postingId);
            intent.putExtra("title", title);
            intent.putExtra("genre", genre);
            intent.putExtra("price", (int) price);
            intent.putExtra("description", description);
            intent.putExtra("imageUrl", imageUrl);
            startActivity(intent);
        });

        Button btnRemove = new Button(this);
        LinearLayout.LayoutParams removeLp = new LinearLayout.LayoutParams(
                0,
                dp(36),
                1f
        );
        btnRemove.setLayoutParams(removeLp);
        btnRemove.setText("Remove");
        btnRemove.setTransformationMethod(null);
        btnRemove.setTextColor(0xFFFFFFFF);
        btnRemove.setTextSize(12);
        btnRemove.setBackgroundResource(R.drawable.bg_grey_rounded);

        btnRemove.setOnClickListener(v -> removeItem(postingId));

        buttonRow.addView(btnDetails);
        buttonRow.addView(btnRemove);

        right.addView(tvTitle);
        right.addView(tvMeta);
        right.addView(buttonRow);

        card.addView(right);
        cartListContainer.addView(card);

        Glide.with(this)
                .load(fullImageUrl)
                .placeholder(android.R.drawable.picture_frame)
                .error(android.R.drawable.ic_dialog_alert)
                .into(img);
    }

    private void removeItem(int postingId) {
        CartManager.removeFromCart(this, userId, postingId,
                response -> {
                    Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show();
                    loadCart();
                },
                error -> Toast.makeText(this, "Failed to remove item", Toast.LENGTH_SHORT).show()
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